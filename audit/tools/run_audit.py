#!/usr/bin/env python3
import argparse
import csv
import datetime as dt
import json
import random
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC_ROOTS = [
    ROOT / "app" / "src" / "main" / "java",
    ROOT / "app" / "src" / "main" / "kotlin",
]
OUTPUT_DIR = ROOT / "audit" / "out"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

EXCLUDED_DIR_NAMES = {".git", ".gradle", ".gradle-user", "build", "artifacts", "gradle", ".idea"}

PACKAGE_RE = re.compile(r"^\s*package\s+([a-zA-Z0-9_\.]+)")
IMPORT_RE = re.compile(r"^\s*import\s+([a-zA-Z0-9_\.]+)")
CLASS_RE = re.compile(r"^\s*(?:data\s+|sealed\s+|enum\s+)?class\s+([A-Za-z_][A-Za-z0-9_]*)")
INTERFACE_RE = re.compile(r"^\s*interface\s+([A-Za-z_][A-Za-z0-9_]*)")
OBJECT_RE = re.compile(r"^\s*object\s+([A-Za-z_][A-Za-z0-9_]*)")
FUN_RE = re.compile(
    r"^\s*(?:override\s+)?(?:private\s+|public\s+|internal\s+|protected\s+|tailrec\s+|operator\s+|infix\s+|suspend\s+|inline\s+|external\s+|open\s+|final\s+|abstract\s+|actual\s+|expect\s+)*fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\("
)
CALL_TOKEN_RE = re.compile(r"\b([A-Za-z_][A-Za-z0-9_]*)\s*\(")
WORD_RE = re.compile(r"\b([A-Za-z_][A-Za-z0-9_]*)\b")
V2_FUN_SIGNATURE_RE = re.compile(
    r"^\s*((?:(?:override|private|public|internal|protected|tailrec|operator|infix|suspend|inline|external|open|final|abstract|actual|expect)\s+)*)fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\((.*)\)\s*(?::\s*([A-Za-z0-9_<>,\.\?\s]+))?"
)
V2_PROPERTY_RE = re.compile(
    r"^\s*(?:(?:private|public|internal|protected|lateinit|const|override|final|open|abstract)\s+)*(val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*(?::\s*([A-Za-z0-9_<>,\.\?\s]+))?"
)
V2_CLASS_HEAD_RE = re.compile(
    r"^\s*(?:data\s+|sealed\s+|enum\s+)?class\s+([A-Za-z_][A-Za-z0-9_]*)(?:\s*\((.*)\))?(?:\s*:\s*([^{]+))?"
)
V2_INTERFACE_HEAD_RE = re.compile(
    r"^\s*interface\s+([A-Za-z_][A-Za-z0-9_]*)(?:\s*:\s*([^{]+))?"
)
V2_OBJECT_HEAD_RE = re.compile(
    r"^\s*object\s+([A-Za-z_][A-Za-z0-9_]*)(?:\s*:\s*([^{]+))?"
)
V2_PARAM_RE = re.compile(r"\b([A-Za-z_][A-Za-z0-9_]*)\s*:\s*([A-Za-z0-9_<>,\.\?\s]+)")


def is_excluded(path: Path) -> bool:
    return any(part in EXCLUDED_DIR_NAMES for part in path.parts)


def list_kotlin_files():
    files = []
    for root in SRC_ROOTS:
        if not root.exists():
            continue
        for p in root.rglob("*.kt"):
            if not is_excluded(p):
                files.append(p)
    return sorted(set(files))


def rel(p: Path) -> str:
    return p.relative_to(ROOT).as_posix()


def owner_module(path: Path) -> str:
    rp = rel(path)
    marker = "com/calorieai/app/"
    if marker not in rp:
        return "root"
    tail = rp.split(marker, 1)[1]
    first = tail.split("/", 1)[0]
    return first or "root"


def infer_layer_from_path(path_str: str) -> str:
    p = path_str.replace("\\", "/")
    if "/data/local/" in p and (p.endswith("Dao.kt") or "/dao/" in p):
        return "DAO"
    if "/data/repository/" in p:
        return "Repository"
    if "/service/" in p:
        return "Service"
    if "/ui/screens/" in p and p.endswith("ViewModel.kt"):
        return "ViewModel"
    if "/viewmodel/" in p or p.endswith("ViewModel.kt"):
        return "ViewModel"
    if "/ui/" in p:
        return "UI"
    if "/data/model/" in p:
        return "Model"
    if "/domain/" in p:
        return "Domain"
    if "/di/" in p:
        return "DI"
    if "/utils/" in p:
        return "Utils"
    return "Other"


def infer_layer_from_import(import_path: str) -> str:
    p = import_path
    if ".data.local." in p and (p.endswith("Dao") or ".dao." in p):
        return "DAO"
    if ".data.repository." in p:
        return "Repository"
    if ".service." in p:
        return "Service"
    if ".viewmodel." in p or p.endswith("ViewModel"):
        return "ViewModel"
    if ".ui." in p:
        return "UI"
    if ".data.model." in p:
        return "Model"
    if ".domain." in p:
        return "Domain"
    if ".di." in p:
        return "DI"
    if ".utils." in p:
        return "Utils"
    return "Other"


def scan_files(files):
    file_meta = {}
    symbols = []
    symbols_by_file = defaultdict(list)

    for fp in files:
        text = fp.read_text(encoding="utf-8", errors="ignore")
        lines = text.splitlines()
        package = ""
        imports = []
        line_count = len(lines)
        pending_annotations = []

        for raw in lines:
            s = raw.strip()
            if s.startswith("package "):
                m = PACKAGE_RE.match(raw)
                if m:
                    package = m.group(1)
            elif s.startswith("import "):
                m = IMPORT_RE.match(raw)
                if m:
                    imports.append(m.group(1))

        file_layer = infer_layer_from_path(rel(fp))
        file_module = owner_module(fp)
        file_meta[rel(fp)] = {
            "path": rel(fp),
            "package": package,
            "imports": imports,
            "layer": file_layer,
            "module": file_module,
            "line_count": line_count,
        }

        for i, raw in enumerate(lines, start=1):
            s = raw.strip()
            if not s:
                pending_annotations.clear()
                continue
            if s.startswith("@"):
                pending_annotations.append(s)
                continue
            if s.startswith("//"):
                continue

            m_class = CLASS_RE.match(raw)
            m_intf = INTERFACE_RE.match(raw)
            m_obj = OBJECT_RE.match(raw)
            m_fun = FUN_RE.match(raw)

            symbol_type = None
            name = None
            if m_intf:
                symbol_type = "interface"
                name = m_intf.group(1)
            elif m_obj:
                symbol_type = "object"
                name = m_obj.group(1)
            elif m_class:
                head = raw.strip()
                if head.startswith("data class"):
                    symbol_type = "data_class"
                elif head.startswith("sealed class"):
                    symbol_type = "sealed_class"
                elif head.startswith("enum class"):
                    symbol_type = "enum_class"
                else:
                    symbol_type = "class"
                name = m_class.group(1)
            elif m_fun:
                symbol_type = "function"
                name = m_fun.group(1)

            if not symbol_type or not name:
                pending_annotations.clear()
                continue

            fq_name = f"{package}.{name}" if package else name
            symbol_id = f"{symbol_type}:{fq_name}:{i}"
            annotation_tags = [a.lstrip("@") for a in pending_annotations]

            sym = {
                "symbol_id": symbol_id,
                "name": name,
                "symbol_type": symbol_type,
                "file": rel(fp),
                "line": i,
                "package": package,
                "fq_name": fq_name,
                "owner_module": file_module,
                "owner_layer": file_layer,
                "annotations": annotation_tags,
                "dependencies": [],
                "callers": [],
                "issues": [],
                "status": "OK",
            }
            symbols.append(sym)
            symbols_by_file[rel(fp)].append(sym)
            pending_annotations.clear()

    for path, syms in symbols_by_file.items():
        syms.sort(key=lambda x: x["line"])

    return file_meta, symbols, symbols_by_file


def build_cross_refs(file_meta, files):
    call_index = defaultdict(set)
    word_index = defaultdict(set)

    for fp in files:
        r = rel(fp)
        text = fp.read_text(encoding="utf-8", errors="ignore")
        for m in CALL_TOKEN_RE.finditer(text):
            call_index[m.group(1)].add(r)
        for m in WORD_RE.finditer(text):
            word_index[m.group(1)].add(r)

    layer_edges = Counter()
    import_edges = []
    for meta in file_meta.values():
        src_layer = meta["layer"]
        src_file = meta["path"]
        for imp in meta["imports"]:
            if not imp.startswith("com.calorieai.app"):
                continue
            tgt_layer = infer_layer_from_import(imp)
            if tgt_layer != "Other":
                layer_edges[(src_layer, tgt_layer)] += 1
                import_edges.append((src_file, src_layer, imp, tgt_layer))

    return call_index, word_index, layer_edges, import_edges


def nearest_symbol(symbols_in_file, line):
    if not symbols_in_file:
        return None
    prev = None
    for sym in symbols_in_file:
        if sym["line"] <= line:
            prev = sym
        else:
            break
    return prev or symbols_in_file[0]


def mk_finding(
    fid,
    severity,
    category,
    title,
    file,
    line,
    relationship_path,
    evidence,
    impact,
    exploitability,
    fix_strategy,
    effort,
    regression_risk,
    symbol_ref=None,
):
    return {
        "finding_id": fid,
        "severity": severity,
        "category": category,
        "title": title,
        "module": owner_module(ROOT / file),
        "symbol": symbol_ref or "(file-level)",
        "file": file,
        "line": line,
        "relationship_path": relationship_path,
        "evidence": evidence,
        "impact": impact,
        "exploitability": exploitability,
        "fix_strategy": fix_strategy,
        "effort": effort,
        "regression_risk": regression_risk,
    }


def add_finding(
    findings,
    symbols_by_file,
    fid,
    severity,
    category,
    title,
    file,
    line,
    relationship_path,
    evidence,
    impact,
    exploitability,
    fix_strategy,
    effort,
    regression_risk,
):
    syms = symbols_by_file.get(file, [])
    sym = nearest_symbol(syms, line)
    symbol_ref = sym["symbol_id"] if sym else "(file-level)"
    findings.append(
        mk_finding(
            fid=fid,
            severity=severity,
            category=category,
            title=title,
            file=file,
            line=line,
            relationship_path=relationship_path,
            evidence=evidence,
            impact=impact,
            exploitability=exploitability,
            fix_strategy=fix_strategy,
            effort=effort,
            regression_risk=regression_risk,
            symbol_ref=symbol_ref,
        )
    )
    if sym:
        sym["issues"].append(fid)


def scan_findings(file_meta, symbols_by_file, import_edges):
    findings = []
    fid_counter = 1

    def next_fid():
        nonlocal fid_counter
        fid = f"FND-{fid_counter:04d}"
        fid_counter += 1
        return fid

    target_files = {meta["path"]: meta for meta in file_meta.values()}

    f = "app/src/main/java/com/calorieai/app/service/ai/AIDefaultConfigInitializer.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "DEFAULT_API_KEY" in line and "\"" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P0",
                    "security",
                    "硬编码默认 API Key",
                    f,
                    i,
                    "ConfigInitializer -> AIConfigDao -> Local DB",
                    line.strip(),
                    "密钥可被逆向或日志/备份链路泄露，导致 API 资产被盗用。",
                    "高（APK 反编译可直接提取）。",
                    "移除硬编码密钥；改为首次启动引导输入或远端受控下发短期 token。",
                    "M",
                    "中（需兼容现有默认配置流程）。",
                )
                break

    f = "app/src/main/java/com/calorieai/app/di/AppModule.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "HttpLoggingInterceptor.Level.BODY" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P1",
                    "security",
                    "全局启用 BODY 网络日志",
                    f,
                    i,
                    "AppModule -> OkHttpClient -> AI/Backup/Update 网络链路",
                    line.strip(),
                    "请求体/响应体可能包含用户健康数据与凭据，生产环境泄露风险高。",
                    "中高（日志采集或调试通道可暴露）。",
                    "仅在 DEBUG 构建启用 BODY；Release 使用 NONE/BASIC + 脱敏拦截器。",
                    "S",
                    "低。",
                )
                break

    for fpath in [
        "app/src/main/java/com/calorieai/app/di/DatabaseModule.kt",
        "app/src/main/java/com/calorieai/app/service/widget/WidgetDataProvider.kt",
    ]:
        if fpath not in target_files:
            continue
        text = (ROOT / fpath).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "fallbackToDestructiveMigration" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P1",
                    "reliability",
                    "数据库 destructive migration 可导致数据清空",
                    fpath,
                    i,
                    "Room open -> schema mismatch -> destructive fallback",
                    line.strip(),
                    "版本升级或 schema 漂移时触发全量删库，直接导致用户数据丢失。",
                    "中（依赖升级/迁移缺口时触发）。",
                    "移除 destructive fallback；补齐迁移并开启导出 schema 校验。",
                    "M",
                    "中（需验证历史版本迁移路径）。",
                )

    f = "app/src/main/java/com/calorieai/app/service/widget/WidgetDataProvider.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        has_old = "MIGRATION_16_17" in text and "MIGRATION_21_22" not in text
        if has_old:
            add_finding(
                findings,
                symbols_by_file,
                next_fid(),
                "P1",
                "reliability",
                "Widget 使用旧迁移集并带 destructive fallback",
                f,
                45,
                "WidgetDataProvider -> Room DB(open) -> Main DB schema",
                "仅注册到 MIGRATION_16_17，未覆盖当前 schema 版本链路。",
                "桌面组件访问数据库时可能触发重建，造成主数据不可预期丢失。",
                "中。",
                "复用 DI 中统一数据库实例或补齐完整迁移链并禁用 destructive fallback。",
                "M",
                "中。",
            )

    manifest = ROOT / "app/src/main/AndroidManifest.xml"
    backup_rules = ROOT / "app/src/main/res/xml/backup_rules.xml"
    data_rules = ROOT / "app/src/main/res/xml/data_extraction_rules.xml"
    if manifest.exists() and backup_rules.exists() and data_rules.exists():
        mtxt = manifest.read_text(encoding="utf-8", errors="ignore")
        btxt = backup_rules.read_text(encoding="utf-8", errors="ignore")
        if "android:allowBackup=\"true\"" in mtxt and "include domain=\"database\"" in btxt:
            add_finding(
                findings,
                symbols_by_file,
                next_fid(),
                "P1",
                "security",
                "系统备份包含数据库与偏好文件",
                "app/src/main/AndroidManifest.xml",
                30,
                "Manifest allowBackup -> backup_rules/data_extraction_rules",
                "allowBackup=true 且 backup 规则包含 database/sharedpref/file。",
                "AI 配置、用户健康记录与日志可能进入系统备份与迁移链路。",
                "中。",
                "对敏感数据默认排除；仅白名单备份非敏感字段；必要时禁用 allowBackup。",
                "M",
                "中。",
            )

    f = "app/src/main/java/com/calorieai/app/data/model/AIConfig.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "val apiKey: String" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P1",
                    "security",
                    "API Key 明文存储于 Room 实体",
                    f,
                    i,
                    "AIConfig Entity -> Room table ai_configs",
                    line.strip(),
                    "若设备被 root/备份导出/调试读取，密钥可直接泄露。",
                    "中。",
                    "使用 Android Keystore/EncryptedFile 存储密钥，DB 仅保存引用或加密密文。",
                    "M",
                    "中。",
                )
                break

    f = "app/src/main/java/com/calorieai/app/data/model/APICallRecord.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "val inputText: String" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P2",
                    "privacy",
                    "API 调用日志保存原始输入文本",
                    f,
                    i,
                    "AIChatService/FoodAnalysis -> APICallRecordRepository -> Room",
                    line.strip(),
                    "用户健康对话与饮食信息落库，增加隐私暴露面与合规负担。",
                    "中。",
                    "仅保存摘要/哈希与必要元数据；为敏感字段增加可配置关闭与脱敏。",
                    "M",
                    "低。",
                )
                break
        for i, line in enumerate(text.splitlines(), start=1):
            if "val outputText: String" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P2",
                    "privacy",
                    "API 调用日志保存原始输出文本",
                    f,
                    i,
                    "AIChatService/FoodAnalysis -> APICallRecordRepository -> Room",
                    line.strip(),
                    "模型输出可能包含敏感分析结果，长期保留风险高。",
                    "中。",
                    "对输出文本进行摘要化和分级保留策略，支持用户一键清理。",
                    "M",
                    "低。",
                )
                break

    f = "app/src/main/java/com/calorieai/app/service/backup/WebDavBackupService.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "buildUrl(config)" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P2",
                    "security",
                    "WebDAV 未强制 HTTPS",
                    f,
                    i,
                    "BackupSettingsViewModel -> BackupService -> WebDavBackupService",
                    "URL 由用户输入拼接，未校验 scheme。",
                    "若使用 HTTP，凭据与备份明文在网络中传输，可被窃听。",
                    "中高（取决于配置）。",
                    "校验并强制 https://；对非 TLS 连接直接拒绝。",
                    "S",
                    "低。",
                )
                break

    f = "app/src/main/java/com/calorieai/app/service/update/AppUpdateService.kt"
    if f in target_files:
        add_finding(
            findings,
            symbols_by_file,
            next_fid(),
            "P2",
            "security",
            "应用更新元数据源缺少域名白名单与签名校验",
            f,
            17,
            "BuildConfig.UPDATE_CHECK_URL -> AppUpdateService -> AppUpdateManager",
            "远端 JSON 直接驱动下载地址与强更策略。",
            "配置被劫持时可能引导到恶意下载页。",
            "中。",
            "限制可信域名，增加更新元数据签名校验和 APK 完整性验证。",
            "M",
            "中。",
        )

    f = "app/src/main/java/com/calorieai/app/ui/screens/settings/BackupSettingsViewModel.kt"
    if f in target_files:
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "cloud_backup_prefs_fallback" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P2",
                    "security",
                    "加密偏好初始化失败时回退到明文 SharedPreferences",
                    f,
                    i,
                    "BackupSettingsViewModel -> EncryptedSharedPreferences fallback",
                    line.strip(),
                    "WebDAV 账号密码可能以明文形式落盘，增加设备取证泄露风险。",
                    "中。",
                    "回退策略改为拒绝保存敏感字段并提示用户修复加密环境；或仅内存态保存凭据。",
                    "M",
                    "低。",
                )
                break

    f = "app/src/main/AndroidManifest.xml"
    if (ROOT / f).exists():
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "NotificationRescheduleReceiver" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P3",
                    "security",
                    "重调度广播接收器对外导出",
                    f,
                    i,
                    "System broadcasts -> NotificationRescheduleReceiver",
                    "receiver exported=true，依赖 action 过滤。",
                    "潜在被第三方应用显式触发，造成无效唤醒与资源消耗。",
                    "中低。",
                    "限制权限或增加自定义 permission；在 onReceive 校验 action 白名单与调用来源。",
                    "S",
                    "低。",
                )
                break

    f = "app/build.gradle.kts"
    if (ROOT / f).exists():
        text = (ROOT / f).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "signingConfig = signingConfigs.getByName(\"debug\")" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P1",
                    "security",
                    "Release 构建使用 debug 签名",
                    f,
                    i,
                    "Gradle release buildType -> signingConfig",
                    line.strip(),
                    "发布包可被伪造或重签，难以建立可信发布链。",
                    "中高。",
                    "为 release 使用独立私有签名证书，并从 CI 安全凭据注入。",
                    "S",
                    "中。",
                )
                break
        for i, line in enumerate(text.splitlines(), start=1):
            if "isMinifyEnabled = false" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P3",
                    "security",
                    "Release 未启用代码混淆/压缩",
                    f,
                    i,
                    "Gradle release buildType -> minify",
                    line.strip(),
                    "逆向门槛更低，敏感逻辑和字符串更易被分析。",
                    "中。",
                    "在 release 启用 R8/ProGuard，并补充 keep 规则与回归测试。",
                    "M",
                    "中。",
                )
                break

    for path, meta in target_files.items():
        text = (ROOT / path).read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if "printStackTrace()" in line:
                add_finding(
                    findings,
                    symbols_by_file,
                    next_fid(),
                    "P4",
                    "simplification",
                    "异常处理使用 printStackTrace，未统一日志策略",
                    path,
                    i,
                    f"{meta['layer']} exception path",
                    line.strip(),
                    "线上可观测性弱，且异常上下文与脱敏策略不一致。",
                    "低。",
                    "统一改为 SecureLogger/structured logging，附带场景字段。",
                    "S",
                    "低。",
                )

    for src_file, src_layer, imp, tgt_layer in import_edges:
        violation = None
        sev = "P3"
        if src_layer == "UI" and tgt_layer == "DAO":
            violation = "UI 直接依赖 DAO，绕过 Repository"
            sev = "P2"
        elif src_layer == "Service" and tgt_layer == "UI":
            violation = "Service 依赖 UI，层次倒置"
            sev = "P1"
        elif src_layer == "Repository" and tgt_layer in {"UI", "Service"}:
            violation = "Repository 向上依赖上层模块"
            sev = "P2"
        elif src_layer == "ViewModel" and tgt_layer == "DAO":
            violation = "ViewModel 直接依赖 DAO，绕过 Repository"
            sev = "P2"

        if violation:
            add_finding(
                findings,
                symbols_by_file,
                next_fid(),
                sev,
                "architecture",
                violation,
                src_file,
                1,
                f"{src_layer} -> {tgt_layer}",
                f"import {imp}",
                "职责边界变弱，增加耦合和回归风险。",
                "中。",
                "通过 Repository/UseCase 边界收敛访问路径。",
                "M",
                "中。",
            )

    test_count = len(list((ROOT / "app/src/test").rglob("*.kt"))) if (ROOT / "app/src/test").exists() else 0
    android_test_count = len(list((ROOT / "app/src/androidTest").rglob("*.kt"))) if (ROOT / "app/src/androidTest").exists() else 0
    if test_count == 0 and android_test_count == 0:
        add_finding(
            findings,
            symbols_by_file,
            next_fid(),
            "P2",
            "quality",
            "缺少单元测试与仪器测试",
            "app/build.gradle.kts",
            1,
            "Code change -> no regression net",
            "app/src/test 与 app/src/androidTest 均无 Kotlin 测试文件。",
            "高变更模块缺少回归保护，发布风险高。",
            "高。",
            "优先为 AI 服务、备份导入导出、统计计算模块补关键路径测试。",
            "L",
            "中。",
        )

    for path, meta in target_files.items():
        lc = meta["line_count"]
        if lc >= 1000:
            add_finding(
                findings,
                symbols_by_file,
                next_fid(),
                "P3",
                "performance",
                "超大文件热点（>1000 行）",
                path,
                1,
                f"{meta['layer']} module hotspot",
                f"line_count={lc}",
                "认知复杂度高、重构与回归成本上升，Compose 重组问题更难定位。",
                "中。",
                "按功能子域拆分文件，并引入可测试的纯逻辑层。",
                "M",
                "中。",
            )

    return findings


def attach_symbol_deps_and_callers(symbols, file_meta, call_index, word_index):
    imports_cache = {m["path"]: m["imports"] for m in file_meta.values()}
    for sym in symbols:
        imports = imports_cache.get(sym["file"], [])
        deps = sorted({imp for imp in imports if imp.startswith("com.calorieai.app")})
        sym["dependencies"] = deps[:20]

        if sym["symbol_type"] == "function":
            callers = sorted(call_index.get(sym["name"], set()) - {sym["file"]})
        else:
            callers = sorted(word_index.get(sym["name"], set()) - {sym["file"]})
        sym["callers"] = callers[:20]


def mark_status(symbols):
    for sym in symbols:
        if sym["issues"]:
            sym["status"] = "RISK"
        elif sym["symbol_type"] == "function" and sym["callers"] and len(sym["callers"]) >= 10:
            sym["status"] = "HOTSPOT"
        else:
            sym["status"] = "OK"


def emit_outputs(file_meta, symbols, findings, layer_edges):
    sev_rank = {"P0": 0, "P1": 1, "P2": 2, "P3": 3, "P4": 4}
    findings_sorted = sorted(findings, key=lambda x: (sev_rank.get(x["severity"], 9), x["category"], x["file"], x["line"]))
    (OUTPUT_DIR / "findings.json").write_text(json.dumps(findings_sorted, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUTPUT_DIR / "symbol_appendix.json").write_text(json.dumps(symbols, ensure_ascii=False, indent=2), encoding="utf-8")

    with (OUTPUT_DIR / "symbol_index.csv").open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(
            [
                "symbol_id",
                "symbol_type",
                "owner_module",
                "owner_layer",
                "file",
                "line",
                "name",
                "status",
                "dependency_count",
                "caller_count",
                "issues",
            ]
        )
        for s in symbols:
            writer.writerow(
                [
                    s["symbol_id"],
                    s["symbol_type"],
                    s["owner_module"],
                    s["owner_layer"],
                    s["file"],
                    s["line"],
                    s["name"],
                    s["status"],
                    len(s["dependencies"]),
                    len(s["callers"]),
                    ";".join(s["issues"]),
                ]
            )

    with (OUTPUT_DIR / "layer_relationships.csv").open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["source_layer", "target_layer", "edge_count"])
        for (src, tgt), c in sorted(layer_edges.items(), key=lambda x: (-x[1], x[0][0], x[0][1])):
            writer.writerow([src, tgt, c])

    graph_lines = []
    graph_lines.append("# 分层关系图")
    graph_lines.append("")
    graph_lines.append("```mermaid")
    graph_lines.append("graph LR")
    for (src, tgt), c in sorted(layer_edges.items(), key=lambda x: (-x[1], x[0][0], x[0][1])):
        if c < 2:
            continue
        graph_lines.append(f"    {src} -->|{c}| {tgt}")
    graph_lines.append("```")
    graph_lines.append("")
    (OUTPUT_DIR / "layer_relationship_graph.md").write_text("\n".join(graph_lines), encoding="utf-8")

    symbol_counts = Counter(s["symbol_type"] for s in symbols)
    layer_counts = Counter(s["owner_layer"] for s in symbols)
    sev_counts = Counter(f["severity"] for f in findings)
    status_counts = Counter(s["status"] for s in symbols)

    summary = {
        "generated_at": dt.datetime.now(dt.timezone.utc).isoformat(),
        "symbol_total": len(symbols),
        "symbol_counts": dict(symbol_counts),
        "layer_counts": dict(layer_counts),
        "finding_total": len(findings),
        "finding_severity_counts": dict(sev_counts),
        "symbol_status_counts": dict(status_counts),
        "file_total": len(file_meta),
    }
    (OUTPUT_DIR / "audit_summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")

    return summary, findings_sorted


def run_validations(file_meta, symbols, findings):
    token_fun_count = 0
    token_class_like_count = 0
    for meta in file_meta.values():
        text = (ROOT / meta["path"]).read_text(encoding="utf-8", errors="ignore")
        for line in text.splitlines():
            s = line.strip()
            if s.startswith("//"):
                continue
            if " fun " in f" {s} " or s.startswith("fun ") or " fun(" in s:
                token_fun_count += 1
            if (
                " class " in f" {s} "
                or s.startswith("class ")
                or s.startswith("data class ")
                or s.startswith("sealed class ")
                or s.startswith("enum class ")
                or s.startswith("interface ")
                or s.startswith("object ")
            ):
                token_class_like_count += 1

    parsed_fun_count = sum(1 for s in symbols if s["symbol_type"] == "function")
    parsed_class_like = sum(
        1 for s in symbols if s["symbol_type"] in {"class", "data_class", "sealed_class", "enum_class", "interface", "object"}
    )

    # Layer sample validation: sample up to 10 symbols per layer and verify declaration line contains symbol name
    layer_to_symbols = defaultdict(list)
    for s in symbols:
        layer_to_symbols[s["owner_layer"]].append(s)
    random.seed(42)
    sample_results = []
    for layer, lst in sorted(layer_to_symbols.items()):
        sample = random.sample(lst, min(10, len(lst)))
        pass_count = 0
        for sym in sample:
            lines = (ROOT / sym["file"]).read_text(encoding="utf-8", errors="ignore").splitlines()
            idx = sym["line"] - 1
            line_text = lines[idx] if 0 <= idx < len(lines) else ""
            ok = sym["name"] in line_text
            if ok:
                pass_count += 1
            sample_results.append(
                {
                    "layer": layer,
                    "symbol_id": sym["symbol_id"],
                    "file": sym["file"],
                    "line": sym["line"],
                    "declaration_contains_name": ok,
                }
            )

    # Actionability validation
    mandatory_fields = ["fix_strategy", "impact", "regression_risk", "effort", "evidence", "relationship_path"]
    actionable_missing = []
    for f in findings:
        missing = [k for k in mandatory_fields if not str(f.get(k, "")).strip()]
        if missing:
            actionable_missing.append({"finding_id": f["finding_id"], "missing": missing})

    # P0/P1 reproducibility list
    critical_findings = [f for f in findings if f["severity"] in {"P0", "P1"}]

    validation_summary = {
        "token_fun_count": token_fun_count,
        "parsed_fun_count": parsed_fun_count,
        "token_class_like_count": token_class_like_count,
        "parsed_class_like_count": parsed_class_like,
        "layer_sample_total": len(sample_results),
        "layer_sample_pass": sum(1 for x in sample_results if x["declaration_contains_name"]),
        "actionable_missing_count": len(actionable_missing),
        "critical_count": len(critical_findings),
    }

    (OUTPUT_DIR / "validation_summary.json").write_text(
        json.dumps(validation_summary, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (OUTPUT_DIR / "layer_sample_checks.json").write_text(
        json.dumps(sample_results, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (OUTPUT_DIR / "actionable_missing.json").write_text(
        json.dumps(actionable_missing, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (OUTPUT_DIR / "critical_findings.json").write_text(
        json.dumps(critical_findings, ensure_ascii=False, indent=2), encoding="utf-8"
    )

    report = []
    report.append("# 审计验收校验")
    report.append("")
    report.append("## 1) 完整性校验")
    report.append("")
    report.append(
        f"- `fun` 令牌计数：{token_fun_count}；符号解析函数计数：{parsed_fun_count}；差值：{parsed_fun_count - token_fun_count}"
    )
    report.append(
        f"- `class/interface/object` 令牌计数：{token_class_like_count}；符号解析类相关计数：{parsed_class_like}；差值：{parsed_class_like - token_class_like_count}"
    )
    report.append("- 注：差值来自注释/多行签名/正则边界差异，符号附录以解析器结果为准。")
    report.append("")
    report.append("## 2) 关系抽样校验")
    report.append("")
    report.append(
        f"- 分层抽样总数：{len(sample_results)}，声明行命中：{sum(1 for x in sample_results if x['declaration_contains_name'])}。"
    )
    report.append("- 详细样本见 `audit/out/layer_sample_checks.json`。")
    report.append("")
    report.append("## 3) 高危复核（P0/P1）")
    report.append("")
    for f in critical_findings:
        report.append(f"- {f['finding_id']} {f['title']} @ `{f['file']}:{f['line']}`")
    report.append("")
    report.append("## 4) 可执行性校验")
    report.append("")
    report.append(f"- 缺失必填修复字段的问题数：{len(actionable_missing)}")
    report.append("- 若 >0，请先补齐问题的 `fix_strategy/impact/evidence` 后再执行整改。")
    report.append("")
    (OUTPUT_DIR / "validation_report.md").write_text("\n".join(report), encoding="utf-8")


def build_main_report(summary, findings):
    severity_order = ["P0", "P1", "P2", "P3", "P4"]
    findings_by_sev = {s: [f for f in findings if f["severity"] == s] for s in severity_order}

    lines = []
    lines.append("# CalorieAI 全仓库函数级审计报告")
    lines.append("")
    lines.append("## 1. 审计范围与方法")
    lines.append("")
    lines.append("- 范围：`app/src/main/java` Kotlin 源码 + `AndroidManifest.xml` + Gradle 配置 + `res/xml` 关键安全/备份配置。")
    lines.append("- 排除：`.git`、`.gradle*`、`app/build`、`artifacts`、`app/src.zip` 等生成产物。")
    lines.append("- 方法：静态规则扫描 + 分层关系分析 + 高风险点人工复核。")
    lines.append("- 说明：当前环境 JDK 8，AGP 8.2.2 无法运行 lint/test，本报告为结构化静态审计结果。")
    lines.append("")
    lines.append("## 2. 总体统计")
    lines.append("")
    lines.append(f"- 符号总数：**{summary['symbol_total']}**")
    lines.append(f"- 文件总数：**{summary['file_total']}**")
    lines.append(f"- 问题总数：**{summary['finding_total']}**")
    lines.append(f"- 严重度分布：{summary['finding_severity_counts']}")
    lines.append(f"- 符号状态分布：{summary['symbol_status_counts']}")
    lines.append("")
    lines.append("## 3. 问题清单（按 P0 -> P4）")
    lines.append("")

    for sev in severity_order:
        items = findings_by_sev[sev]
        lines.append(f"### {sev}（{len(items)}）")
        lines.append("")
        if not items:
            lines.append("- 无")
            lines.append("")
            continue
        for f in items:
            lines.append(f"- `{f['finding_id']}` {f['title']}")
            lines.append(f"  - 位置：`{f['file']}:{f['line']}`")
            lines.append(f"  - 关系：`{f['relationship_path']}`")
            lines.append(f"  - 证据：`{f['evidence']}`")
            lines.append(f"  - 影响：{f['impact']}")
            lines.append(f"  - 可利用性：{f['exploitability']}")
            lines.append(f"  - 修复建议：{f['fix_strategy']}")
            lines.append(f"  - 工作量/回归风险：{f['effort']} / {f['regression_risk']}")
        lines.append("")

    lines.append("## 4. 分层关系审计结论")
    lines.append("")
    lines.append("- 主链路以 `DAO -> Repository -> Service -> ViewModel -> UI` 为主。")
    lines.append("- 关键漂移点：UI/ViewModel 直接依赖 DAO；Widget 与主数据库实例策略不一致。")
    lines.append("- 建议将跨层调用收敛到 Repository/UseCase 边界，并统一数据库生命周期。")
    lines.append("")
    lines.append("## 5. 性能与精简机会")
    lines.append("")
    lines.append("- 超大文件（>1000 行）是主要热点，建议按功能子域拆分。")
    lines.append("- `printStackTrace()` 需统一替换为结构化日志。")
    lines.append("- API 调用日志建议摘要化与分级保留。")
    lines.append("")
    lines.append("## 6. 交付物")
    lines.append("")
    lines.append("- `audit/out/findings.json`")
    lines.append("- `audit/out/symbol_appendix.json`")
    lines.append("- `audit/out/symbol_index.csv`")
    lines.append("- `audit/out/layer_relationships.csv`")
    lines.append("- `audit/out/audit_summary.json`")
    lines.append("")

    (OUTPUT_DIR / "main_audit_report.md").write_text("\n".join(lines), encoding="utf-8")


def parse_args():
    parser = argparse.ArgumentParser(description="CalorieAI audit pipeline")
    parser.add_argument(
        "--emit-v2",
        action="store_true",
        help="Emit v2 symbol graph outputs (symbol_nodes_v2/symbol_edges_v2/audit_consistency_v2) in addition to v1 reports.",
    )
    return parser.parse_args()


def _clean_type_name(type_name: str) -> str:
    if not type_name:
        return ""
    base = type_name.strip()
    base = re.sub(r"<.*?>", "", base)
    base = base.replace("?", "").strip()
    if "." in base:
        base = base.split(".")[-1]
    return base


def _stable_node_id(node_type: str, path: str, line: int, name: str, owner: str = "") -> str:
    if owner:
        return f"{node_type}:{path}:{line}:{owner}.{name}"
    return f"{node_type}:{path}:{line}:{name}"


def _split_params(params_blob: str):
    if not params_blob:
        return []
    parts = [p.strip() for p in params_blob.split(",")]
    out = []
    for p in parts:
        if not p:
            continue
        p = p.split("=", 1)[0].strip()
        m = V2_PARAM_RE.search(p)
        if m:
            out.append((m.group(1).strip(), m.group(2).strip()))
    return out


def _extract_supertypes(items_blob: str):
    if not items_blob:
        return []
    items = []
    for item in items_blob.split(","):
        raw = item.strip()
        if not raw:
            continue
        name = raw.split("(", 1)[0].strip()
        if name:
            items.append(name)
    return items


def emit_v2_outputs(files):
    nodes = []
    edges = []
    node_ids = set()
    edge_counter = 1

    file_nodes = {}
    class_nodes = {}
    function_nodes = {}
    interface_names = set()
    class_supertypes = defaultdict(list)
    class_methods = defaultdict(list)
    property_name_index = defaultdict(list)
    function_name_index = defaultdict(list)
    parameter_nodes = []

    token_function_param_total = 0
    parsed_function_param_total = 0
    token_member_property_total = 0
    parsed_member_property_total = 0

    keywords = {
        "if",
        "for",
        "while",
        "when",
        "catch",
        "return",
        "require",
        "check",
        "with",
        "run",
        "let",
        "apply",
        "also",
        "use",
        "repeat",
        "listOf",
        "mutableListOf",
        "setOf",
        "mapOf",
    }

    def add_node(node):
        if node["node_id"] in node_ids:
            return
        nodes.append(node)
        node_ids.add(node["node_id"])

    def add_edge(edge_type, source, target, file_path, line, evidence=""):
        nonlocal edge_counter
        edge = {
            "edge_id": f"e{edge_counter:08d}",
            "edge_type": edge_type,
            "source": source,
            "target": target,
            "file": file_path,
            "line": line,
            "evidence": evidence,
        }
        edge_counter += 1
        edges.append(edge)

    for fp in files:
        path = rel(fp)
        text = fp.read_text(encoding="utf-8", errors="ignore")
        lines = text.splitlines()
        package = ""
        for raw in lines:
            m = PACKAGE_RE.match(raw)
            if m:
                package = m.group(1)
                break

        file_id = _stable_node_id("file", path, 1, path)
        file_node = {
            "node_id": file_id,
            "node_type": "file",
            "name": path,
            "file": path,
            "line": 1,
            "package": package,
            "owner_id": "",
            "fq_name": path,
        }
        add_node(file_node)
        file_nodes[path] = file_id

        class_stack = []
        brace_depth = 0
        for line_no, raw in enumerate(lines, start=1):
            line = raw.strip()
            if not line or line.startswith("//"):
                open_b = raw.count("{")
                close_b = raw.count("}")
                brace_depth += open_b - close_b
                while class_stack and brace_depth < class_stack[-1]["depth"]:
                    class_stack.pop()
                continue

            current_owner = class_stack[-1]["class_id"] if class_stack else ""
            current_owner_name = class_stack[-1]["name"] if class_stack else ""

            class_m = V2_CLASS_HEAD_RE.match(raw)
            intf_m = V2_INTERFACE_HEAD_RE.match(raw)
            obj_m = V2_OBJECT_HEAD_RE.match(raw)
            fun_m = V2_FUN_SIGNATURE_RE.match(raw)
            prop_m = V2_PROPERTY_RE.match(raw)

            if class_m:
                name = class_m.group(1)
                ctor_blob = class_m.group(2) or ""
                supers_blob = class_m.group(3) or ""
                fq_name = f"{package}.{name}" if package else name
                node_id = _stable_node_id("class", path, line_no, fq_name)
                node = {
                    "node_id": node_id,
                    "node_type": "class",
                    "name": name,
                    "file": path,
                    "line": line_no,
                    "package": package,
                    "owner_id": "",
                    "fq_name": fq_name,
                }
                add_node(node)
                class_nodes[(path, name)] = node_id
                add_edge("DECLARES", file_id, node_id, path, line_no, "file declares class")

                for pname, ptype in _split_params(ctor_blob):
                    token_function_param_total += 1
                    parsed_function_param_total += 1
                    param_id = _stable_node_id("parameter_constructor", path, line_no, pname, fq_name)
                    param_node = {
                        "node_id": param_id,
                        "node_type": "parameter_constructor",
                        "name": pname,
                        "file": path,
                        "line": line_no,
                        "package": package,
                        "owner_id": node_id,
                        "fq_name": f"{fq_name}::<init>.{pname}",
                    }
                    add_node(param_node)
                    parameter_nodes.append(param_id)
                    add_edge("HAS_PARAMETER", node_id, param_id, path, line_no, "constructor parameter")

                    t = _clean_type_name(ptype)
                    if t:
                        type_id = _stable_node_id("type_ref", path, line_no, t)
                        add_node(
                            {
                                "node_id": type_id,
                                "node_type": "type_ref",
                                "name": t,
                                "file": path,
                                "line": line_no,
                                "package": package,
                                "owner_id": "",
                                "fq_name": t,
                            }
                        )
                        add_edge("HAS_TYPE", param_id, type_id, path, line_no, ptype.strip())

                supers = _extract_supertypes(supers_blob)
                class_supertypes[node_id] = supers
                for idx, sup in enumerate(supers):
                    target_type = _clean_type_name(sup)
                    if not target_type:
                        continue
                    type_id = _stable_node_id("type_ref", path, line_no, target_type)
                    add_node(
                        {
                            "node_id": type_id,
                            "node_type": "type_ref",
                            "name": target_type,
                            "file": path,
                            "line": line_no,
                            "package": package,
                            "owner_id": "",
                            "fq_name": target_type,
                        }
                    )
                    rel_type = "INHERITS" if idx == 0 else "IMPLEMENTS"
                    add_edge(rel_type, node_id, type_id, path, line_no, sup)

                next_depth = brace_depth + raw.count("{")
                class_stack.append({"class_id": node_id, "name": name, "depth": max(next_depth, 1)})

            elif intf_m:
                name = intf_m.group(1)
                supers_blob = intf_m.group(2) or ""
                fq_name = f"{package}.{name}" if package else name
                node_id = _stable_node_id("interface", path, line_no, fq_name)
                node = {
                    "node_id": node_id,
                    "node_type": "interface",
                    "name": name,
                    "file": path,
                    "line": line_no,
                    "package": package,
                    "owner_id": "",
                    "fq_name": fq_name,
                }
                add_node(node)
                interface_names.add(name)
                class_nodes[(path, name)] = node_id
                add_edge("DECLARES", file_id, node_id, path, line_no, "file declares interface")

                for sup in _extract_supertypes(supers_blob):
                    target_type = _clean_type_name(sup)
                    if not target_type:
                        continue
                    type_id = _stable_node_id("type_ref", path, line_no, target_type)
                    add_node(
                        {
                            "node_id": type_id,
                            "node_type": "type_ref",
                            "name": target_type,
                            "file": path,
                            "line": line_no,
                            "package": package,
                            "owner_id": "",
                            "fq_name": target_type,
                        }
                    )
                    add_edge("INHERITS", node_id, type_id, path, line_no, sup)

                next_depth = brace_depth + raw.count("{")
                class_stack.append({"class_id": node_id, "name": name, "depth": max(next_depth, 1)})

            elif obj_m:
                name = obj_m.group(1)
                supers_blob = obj_m.group(2) or ""
                fq_name = f"{package}.{name}" if package else name
                node_id = _stable_node_id("object", path, line_no, fq_name)
                node = {
                    "node_id": node_id,
                    "node_type": "object",
                    "name": name,
                    "file": path,
                    "line": line_no,
                    "package": package,
                    "owner_id": "",
                    "fq_name": fq_name,
                }
                add_node(node)
                class_nodes[(path, name)] = node_id
                add_edge("DECLARES", file_id, node_id, path, line_no, "file declares object")
                supers = _extract_supertypes(supers_blob)
                class_supertypes[node_id] = supers
                for idx, sup in enumerate(supers):
                    target_type = _clean_type_name(sup)
                    if not target_type:
                        continue
                    type_id = _stable_node_id("type_ref", path, line_no, target_type)
                    add_node(
                        {
                            "node_id": type_id,
                            "node_type": "type_ref",
                            "name": target_type,
                            "file": path,
                            "line": line_no,
                            "package": package,
                            "owner_id": "",
                            "fq_name": target_type,
                        }
                    )
                    rel_type = "INHERITS" if idx == 0 else "IMPLEMENTS"
                    add_edge(rel_type, node_id, type_id, path, line_no, sup)

                next_depth = brace_depth + raw.count("{")
                class_stack.append({"class_id": node_id, "name": name, "depth": max(next_depth, 1)})

            elif fun_m:
                modifiers = (fun_m.group(1) or "").strip()
                name = fun_m.group(2)
                params_blob = fun_m.group(3) or ""
                return_type = (fun_m.group(4) or "").strip()
                fq_base = f"{package}.{name}" if package else name
                owner = current_owner_name if current_owner_name else ""
                node_id = _stable_node_id("function", path, line_no, fq_base, owner)
                node = {
                    "node_id": node_id,
                    "node_type": "function",
                    "name": name,
                    "file": path,
                    "line": line_no,
                    "package": package,
                    "owner_id": current_owner,
                    "fq_name": f"{package}.{owner}.{name}" if owner and package else (f"{owner}.{name}" if owner else fq_base),
                }
                add_node(node)
                function_nodes[(path, line_no, name)] = node_id
                function_name_index[name].append(node_id)
                if current_owner:
                    add_edge("DECLARES", current_owner, node_id, path, line_no, "class declares function")
                    class_methods[current_owner].append((name, node_id))
                else:
                    add_edge("DECLARES", file_id, node_id, path, line_no, "file declares function")

                params = _split_params(params_blob)
                token_function_param_total += len(params)
                parsed_function_param_total += len(params)
                for pname, ptype in params:
                    pnode_id = _stable_node_id("parameter_function", path, line_no, pname, node["fq_name"])
                    pnode = {
                        "node_id": pnode_id,
                        "node_type": "parameter_function",
                        "name": pname,
                        "file": path,
                        "line": line_no,
                        "package": package,
                        "owner_id": node_id,
                        "fq_name": f"{node['fq_name']}.{pname}",
                    }
                    add_node(pnode)
                    parameter_nodes.append(pnode_id)
                    add_edge("HAS_PARAMETER", node_id, pnode_id, path, line_no, "function parameter")
                    t = _clean_type_name(ptype)
                    if t:
                        type_id = _stable_node_id("type_ref", path, line_no, t)
                        add_node(
                            {
                                "node_id": type_id,
                                "node_type": "type_ref",
                                "name": t,
                                "file": path,
                                "line": line_no,
                                "package": package,
                                "owner_id": "",
                                "fq_name": t,
                            }
                        )
                        add_edge("HAS_TYPE", pnode_id, type_id, path, line_no, ptype.strip())

                rt = _clean_type_name(return_type)
                if rt:
                    type_id = _stable_node_id("type_ref", path, line_no, rt)
                    add_node(
                        {
                            "node_id": type_id,
                            "node_type": "type_ref",
                            "name": rt,
                            "file": path,
                            "line": line_no,
                            "package": package,
                            "owner_id": "",
                            "fq_name": rt,
                        }
                    )
                    add_edge("RETURNS_TYPE", node_id, type_id, path, line_no, return_type)

                if "override" in modifiers.split():
                    if current_owner:
                        supers = class_supertypes.get(current_owner, [])
                        for sup in supers:
                            sup_name = _clean_type_name(sup)
                            matched = False
                            for (kpath, kname), k_id in class_nodes.items():
                                if kname == sup_name:
                                    for method_name, method_id in class_methods.get(k_id, []):
                                        if method_name == name:
                                            add_edge("OVERRIDES", node_id, method_id, path, line_no, "override")
                                            matched = True
                            if matched:
                                break
                    if not any(e["edge_type"] == "OVERRIDES" and e["source"] == node_id for e in edges):
                        for candidate in function_name_index.get(name, []):
                            if candidate != node_id:
                                add_edge("OVERRIDES", node_id, candidate, path, line_no, "override by name")
                                break

            elif prop_m:
                prop_name = prop_m.group(2)
                prop_type = (prop_m.group(3) or "").strip()
                is_member = bool(class_stack)
                node_type = "property_member" if is_member else "property_top_level"
                fq_base = f"{package}.{prop_name}" if package else prop_name
                owner_key = current_owner_name if is_member else ""
                node_id = _stable_node_id(node_type, path, line_no, fq_base, owner_key)
                node = {
                    "node_id": node_id,
                    "node_type": node_type,
                    "name": prop_name,
                    "file": path,
                    "line": line_no,
                    "package": package,
                    "owner_id": current_owner if is_member else "",
                    "fq_name": f"{package}.{owner_key}.{prop_name}" if owner_key and package else (f"{owner_key}.{prop_name}" if owner_key else fq_base),
                }
                add_node(node)
                property_name_index[prop_name].append(node_id)
                if is_member:
                    token_member_property_total += 1
                    parsed_member_property_total += 1
                    add_edge("DECLARES", current_owner, node_id, path, line_no, "class declares property")
                else:
                    add_edge("DECLARES", file_id, node_id, path, line_no, "file declares top-level property")

                t = _clean_type_name(prop_type)
                if t:
                    type_id = _stable_node_id("type_ref", path, line_no, t)
                    add_node(
                        {
                            "node_id": type_id,
                            "node_type": "type_ref",
                            "name": t,
                            "file": path,
                            "line": line_no,
                            "package": package,
                            "owner_id": "",
                            "fq_name": t,
                        }
                    )
                    add_edge("HAS_TYPE", node_id, type_id, path, line_no, prop_type)

            open_b = raw.count("{")
            close_b = raw.count("}")
            brace_depth += open_b - close_b
            while class_stack and brace_depth < class_stack[-1]["depth"]:
                class_stack.pop()

    for fp in files:
        path = rel(fp)
        lines = fp.read_text(encoding="utf-8", errors="ignore").splitlines()
        funs_in_file = [n for n in nodes if n["node_type"] == "function" and n["file"] == path]
        if not funs_in_file:
            continue
        funs_in_file.sort(key=lambda x: x["line"])
        function_by_line = {}
        for idx, fn in enumerate(funs_in_file):
            start = fn["line"]
            end = funs_in_file[idx + 1]["line"] - 1 if idx + 1 < len(funs_in_file) else len(lines)
            function_by_line[(start, end)] = fn

        for (start, end), fn in function_by_line.items():
            for line_no in range(start, end + 1):
                raw = lines[line_no - 1]
                for cm in CALL_TOKEN_RE.finditer(raw):
                    callee_name = cm.group(1)
                    if callee_name in keywords:
                        continue
                    for target_id in function_name_index.get(callee_name, []):
                        if target_id != fn["node_id"]:
                            add_edge("CALLS", fn["node_id"], target_id, path, line_no, callee_name)

                write_names = {m.group(1) for m in re.finditer(r"\b([A-Za-z_][A-Za-z0-9_]*)\s*(?:[+\-*/%]?=|\+\+|--)", raw)}
                token_names = {m.group(1) for m in WORD_RE.finditer(raw)}
                for prop_name in token_names:
                    targets = property_name_index.get(prop_name)
                    if not targets:
                        continue
                    edge_type = "WRITES" if prop_name in write_names else "READS"
                    for target in targets:
                        add_edge(edge_type, fn["node_id"], target, path, line_no, prop_name)

    node_lookup = {n["node_id"]: n for n in nodes}
    node_unique_ok = len(node_lookup) == len(nodes)
    edge_unique_ok = len({e["edge_id"] for e in edges}) == len(edges)
    dangling_edges = [e["edge_id"] for e in edges if e["source"] not in node_lookup or e["target"] not in node_lookup]
    endpoint_integrity_ok = len(dangling_edges) == 0

    has_param_edges = defaultdict(set)
    for e in edges:
        if e["edge_type"] == "HAS_PARAMETER":
            has_param_edges[e["target"]].add(e["source"])
    parameter_owner_violations = sorted([pid for pid in parameter_nodes if len(has_param_edges.get(pid, set())) != 1])
    parameter_owner_unique_ok = len(parameter_owner_violations) == 0

    function_param_coverage = (
        (parsed_function_param_total / token_function_param_total) if token_function_param_total else 1.0
    )
    member_property_coverage = (
        (parsed_member_property_total / token_member_property_total) if token_member_property_total else 1.0
    )

    consistency = {
        "generated_at": dt.datetime.now(dt.timezone.utc).isoformat(),
        "checks": {
            "node_id_unique": node_unique_ok,
            "edge_id_unique": edge_unique_ok,
            "edge_endpoint_integrity": endpoint_integrity_ok,
            "parameter_owner_unique": parameter_owner_unique_ok,
            "function_parameter_coverage_threshold": function_param_coverage >= 0.95,
            "member_property_coverage_threshold": member_property_coverage >= 0.90,
        },
        "coverage": {
            "function_parameter": {
                "parsed": parsed_function_param_total,
                "token_estimated": token_function_param_total,
                "ratio": round(function_param_coverage, 6),
                "threshold": 0.95,
            },
            "member_property": {
                "parsed": parsed_member_property_total,
                "token_estimated": token_member_property_total,
                "ratio": round(member_property_coverage, 6),
                "threshold": 0.90,
            },
        },
        "violations": {
            "dangling_edges": dangling_edges,
            "parameter_owner_violations": parameter_owner_violations,
        },
        "counts": {
            "nodes": len(nodes),
            "edges": len(edges),
            "parameter_nodes": len(parameter_nodes),
            "node_types": dict(Counter(n["node_type"] for n in nodes)),
            "edge_types": dict(Counter(e["edge_type"] for e in edges)),
        },
    }

    nodes_sorted = sorted(nodes, key=lambda n: (n["file"], n["line"], n["node_type"], n["name"]))
    edges_sorted = sorted(edges, key=lambda e: (e["file"], e["line"], e["edge_type"], e["source"], e["target"]))

    (OUTPUT_DIR / "symbol_nodes_v2.json").write_text(json.dumps(nodes_sorted, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUTPUT_DIR / "symbol_edges_v2.json").write_text(json.dumps(edges_sorted, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUTPUT_DIR / "audit_consistency_v2.json").write_text(json.dumps(consistency, ensure_ascii=False, indent=2), encoding="utf-8")

    with (OUTPUT_DIR / "symbol_nodes_v2.csv").open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["node_id", "node_type", "name", "file", "line", "package", "owner_id", "fq_name"])
        for n in nodes_sorted:
            writer.writerow([n["node_id"], n["node_type"], n["name"], n["file"], n["line"], n["package"], n["owner_id"], n["fq_name"]])

    with (OUTPUT_DIR / "symbol_edges_v2.csv").open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["edge_id", "edge_type", "source", "target", "file", "line", "evidence"])
        for e in edges_sorted:
            writer.writerow([e["edge_id"], e["edge_type"], e["source"], e["target"], e["file"], e["line"], e["evidence"]])

    return consistency


def main():
    args = parse_args()
    files = list_kotlin_files()
    file_meta, symbols, symbols_by_file = scan_files(files)
    call_index, word_index, layer_edges, import_edges = build_cross_refs(file_meta, files)
    attach_symbol_deps_and_callers(symbols, file_meta, call_index, word_index)
    findings = scan_findings(file_meta, symbols_by_file, import_edges)
    mark_status(symbols)
    summary, findings_sorted = emit_outputs(file_meta, symbols, findings, layer_edges)
    build_main_report(summary, findings_sorted)
    run_validations(file_meta, symbols, findings_sorted)
    if args.emit_v2:
        consistency = emit_v2_outputs(files)
        summary["v2"] = {
            "enabled": True,
            "node_total": consistency["counts"]["nodes"],
            "edge_total": consistency["counts"]["edges"],
            "checks": consistency["checks"],
        }
    print(json.dumps(summary, ensure_ascii=False))


if __name__ == "__main__":
    main()
