/* ============================================================
   CalorieAI 官网脚本
   · hash 路由视图切换 + 移动端菜单
   · 读取 /android/stable/latest.json，校验后渲染版本信息
   · 下载须知弹窗、SHA-256 复制、FAQ 折叠、操作反馈
   页面不写入任何 API key、签名配置或用户数据。
   ============================================================ */

(function () {
  "use strict";

  /* ---------------- 小工具 ---------------- */

  var $ = function (sel) { return document.querySelector(sel); };

  var toastTimer = null;
  function toast(message) {
    var el = $("#toast");
    if (!el) return;
    el.textContent = message;
    el.classList.add("show");
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { el.classList.remove("show"); }, 2200);
  }

  function formatSize(bytes) {
    var mb = bytes / 1024 / 1024;
    return mb >= 100 ? Math.round(mb) + " MiB" : mb.toFixed(1) + " MiB";
  }

  /* ---------------- 视图路由 ---------------- */

  var VIEWS = ["home", "features", "showcase", "download", "changelog", "help", "privacy"];

  function currentView() {
    var hash = (location.hash || "#home").replace("#", "");
    return VIEWS.indexOf(hash) !== -1 ? hash : "home";
  }

  function render() {
    var view = currentView();

    VIEWS.forEach(function (name) {
      var el = document.getElementById("view-" + name);
      if (el) el.classList.toggle("active", name === view);
    });

    document.querySelectorAll("[data-nav]").forEach(function (a) {
      var target = (a.getAttribute("href") || "").replace("#", "");
      a.classList.toggle("active", target === view && a.closest(".nav-links, .mobile-menu") !== null);
    });

    // 切换视图时回到顶部；若 URL 带锚点参数（如 #faqList）则尊重浏览器默认行为
    if (!location.hash || VIEWS.indexOf(location.hash.replace("#", "")) !== -1) {
      window.scrollTo({ top: 0, behavior: "auto" });
    }

    closeMobileMenu();
  }

  window.addEventListener("hashchange", render);

  document.querySelectorAll("[data-nav]").forEach(function (a) {
    a.addEventListener("click", function () {
      // hash 相同时 hashchange 不触发，手动渲染一次
      if (a.getAttribute("href") === location.hash) render();
    });
  });

  /* ---------------- 移动端菜单 ---------------- */

  var navToggle = $("#navToggle");
  var mobileMenu = $("#mobileMenu");

  function closeMobileMenu() {
    if (mobileMenu) mobileMenu.classList.remove("open");
    if (navToggle) navToggle.setAttribute("aria-expanded", "false");
  }

  if (navToggle && mobileMenu) {
    navToggle.addEventListener("click", function () {
      var open = mobileMenu.classList.toggle("open");
      navToggle.setAttribute("aria-expanded", open ? "true" : "false");
    });
    document.addEventListener("click", function (e) {
      if (!mobileMenu.contains(e.target) && !navToggle.contains(e.target)) closeMobileMenu();
    });
  }

  /* ---------------- 版本清单读取 ----------------
     校验规则与应用内 AppUpdateManager 一致：
     版本号 x.y.z、SHA-256 64 位十六进制、
     APK 路径 /releases/<ver>/CalorieAI-v<ver>.apk
     （apkUrl 兼容完整地址与相对路径两种清单写法）。
     校验不过就保持禁用，绝不给出不可靠的下载链接。 */

  var MANIFEST_URL = "/android/stable/latest.json";
  // ?manifest=<地址> 仅限本机自测使用，正式站点一律读固定清单
  if (location.hostname === "127.0.0.1" || location.hostname === "localhost") {
    try {
      var qs = new URLSearchParams(location.search).get("manifest");
      if (qs) MANIFEST_URL = qs;
    } catch (e) { /* 忽略，使用默认地址 */ }
  }

  var manifest = null;
  var downloadReady = false;

  function showManifestState(state, detail) {
    var loading = $("#manifestLoading");
    var error = $("#manifestError");
    var ready = $("#manifestReady");
    if (loading) loading.hidden = state !== "loading";
    if (error) error.hidden = state !== "error";
    if (ready) ready.hidden = state !== "ready";
    if (state === "error" && detail) {
      var p = $("#manifestErrorDetail");
      if (p) p.textContent = detail;
    }
  }

  var APK_PATH_RE = /^\/releases\/[0-9]+\.[0-9]+\.[0-9]+\/CalorieAI-v[0-9]+\.[0-9]+\.[0-9]+\.apk$/;

  function resolveApkUrl(data) {
    try {
      return new URL(String(data.apkUrl || ""), location.href);
    } catch (e) {
      return null;
    }
  }

  function validateManifest(data) {
    if (!data || typeof data !== "object") return "版本清单格式不正确。";

    var versionName = String(data.versionName || "");
    if (!/^[0-9]+\.[0-9]+\.[0-9]+$/.test(versionName)) return "清单中的版本号格式不符合 x.y.z 约定。";

    var sha = String(data.apkSha256 || "");
    if (!/^[0-9a-fA-F]{64}$/.test(sha)) return "清单缺少有效的 SHA-256 校验值。";

    // apkUrl 允许相对路径或完整地址，路径部分必须符合发布约定
    var apkUrl = resolveApkUrl(data);
    if (!apkUrl || !APK_PATH_RE.test(apkUrl.pathname)) {
      return "清单中的下载路径不符合发布约定。";
    }

    var size = Number(data.apkSize);
    if (!isFinite(size) || size <= 0) return "清单缺少有效的安装包大小。";

    if (typeof data.versionCode !== "number" || typeof data.minSupportedVersionCode !== "number") {
      return "清单缺少版本号字段。";
    }

    return null;
  }

  function renderManifest(data) {
    var sizeText = formatSize(data.apkSize);
    var sizeLong = "约 " + sizeText;

    $("#vName").textContent = "v" + data.versionName;
    $("#vCode").textContent = String(data.versionCode);
    $("#vSize").textContent = sizeText;
    $("#vMin").textContent = "versionCode " + data.minSupportedVersionCode + " 及以上可直接覆盖升级；更低版本需先逐级更新";
    $("#vSha").textContent = String(data.apkSha256).toLowerCase();
    $("#vNotes").textContent = data.releaseNotes && String(data.releaseNotes).trim()
      ? String(data.releaseNotes)
      : "暂无更新说明。";

    // 页面内所有包体大小占位（sysbar、体积提示、自查清单、FAQ、页脚）统一改为清单实际值
    document.querySelectorAll("[data-apk-size]").forEach(function (el) {
      el.textContent = sizeLong;
    });

    // 首页 Hero
    var heroLine = $("#heroVersionLine");
    if (heroLine) {
      heroLine.innerHTML = "<strong>当前版本</strong>v" + data.versionName + " · 安装包" + sizeLong;
    }
    var heroSize = $("#heroApkSize");
    if (heroSize) heroSize.textContent = "Android 8.0+ · " + sizeLong;

    // 弹窗里的体积
    var modalSize = $("#modalApkSize");
    if (modalSize) modalSize.textContent = sizeText;

    // 下载按钮
    var btn = $("#dlButton");
    var note = $("#dlButtonNote");
    var hint = $("#dlButtonHint");
    downloadReady = isDownloadAllowed(data);
    if (downloadReady) {
      btn.disabled = false;
      if (note) note.textContent = "（约 " + sizeText + "）";
      if (hint) hint.textContent = "点击后将检查下载通道状态，并弹出下载须知。";
    } else {
      btn.disabled = true;
      if (note) note.textContent = "";
      if (hint) {
        if (location.protocol === "file:") {
          hint.textContent = "当前通过本地文件打开页面，无法校验下载主机，下载按钮已停用。请通过正式站点访问。";
        } else {
          hint.textContent = "下载链接未通过安全校验，下载按钮已停用。请稍后重试，或到项目主页反馈。";
        }
      }
    }
  }

  function isDownloadAllowed(data) {
    if (!data) return false;
    var url = resolveApkUrl(data);
    if (!url) return false;
    if (!APK_PATH_RE.test(url.pathname)) return false;       // 路径必须符合发布约定
    if (!location.protocol.startsWith("http")) return false; // file:// 等本地文件环境一律不放行
    var isLocalDev = location.hostname === "127.0.0.1" || location.hostname === "localhost";
    var isDirectUpdateIp = url.protocol === "http:" &&
      url.hostname === "101.132.250.38" && (url.port === "" || url.port === "80");
    if (!isLocalDev && url.protocol !== "https:" && !isDirectUpdateIp) return false;
    // 主机：优先同源。清单由本站同源发布，属第一方内容；apkUrl 指向其他
    // 主机（如独立下载域名）时同样放行，仅拒绝回环地址。
    if (url.host !== location.host) {
      var h = url.hostname;
      if (h === "127.0.0.1" || h === "localhost" || h === "0.0.0.0" || h === "::1") return false;
    }
    return true;
  }

  function loadManifest() {
    showManifestState("loading");
    // 10 秒拿不到清单就按失败处理，不让请求长期挂着
    var controller = new AbortController();
    var timer = setTimeout(function () { controller.abort(); }, 10000);
    fetch(MANIFEST_URL, { cache: "no-store", signal: controller.signal })
      .then(function (res) {
        clearTimeout(timer);
        if (!res.ok) throw new Error("HTTP " + res.status);
        return res.json();
      })
      .then(function (data) {
        var problem = validateManifest(data);
        if (problem) {
          showManifestState("error", problem + " 未能获取有效版本信息前，下载暂不开放。");
          return;
        }
        manifest = data;
        renderManifest(data);
        showManifestState("ready");
      })
      .catch(function (err) {
        clearTimeout(timer);
        var reason = err && err.name === "AbortError"
          ? "版本清单请求超时（10 秒无响应）。"
          : "版本清单读取失败（" + (err && err.message ? err.message : "网络异常") + "）。";
        showManifestState(
          "error",
          reason + "可能是网络异常或发布服务正在维护。未能获取有效版本信息前，下载暂不开放。"
        );
      });
  }

  var retryBtn = $("#manifestRetry");
  if (retryBtn) retryBtn.addEventListener("click", loadManifest);

  /* ---------------- 下载须知弹窗与下载流程 ----------------
     点「下载 APK」先用 HEAD 预检下载通道；101 的 HTTP 直连地址因浏览器混合内容规则跳过预检：
     200 → 弹出下载须知；429 → 页面内提示繁忙；其他 → 报状态码。
     确认后用隐藏锚点触发浏览器下载，页面不跳转。 */

  var dlModal = $("#dlModal");
  var dlButton = $("#dlButton");
  var dlConfirm = $("#dlConfirm");
  var dlCancel = $("#dlCancel");
  var modalOpener = null;
  var checking = false;

  function openModal(opener) {
    if (!dlModal) return;
    modalOpener = opener || null;
    dlModal.classList.add("open");
    if (dlModal.querySelector(".modal")) dlModal.querySelector(".modal").focus();
  }

  function closeModal() {
    if (!dlModal || !dlModal.classList.contains("open")) return;
    dlModal.classList.remove("open");
    if (modalOpener && typeof modalOpener.focus === "function") modalOpener.focus();
    modalOpener = null;
  }

  function setButtonState(mode, sizeText) {
    var btn = $("#dlButton");
    var note = $("#dlButtonNote");
    if (!btn) return;
    if (mode === "checking") {
      btn.disabled = true;
      btn.innerHTML = "正在检查下载通道…";
      return;
    }
    if (mode === "started") {
      btn.disabled = true;
      btn.innerHTML = "已开始下载<span class=\"btn-note\">浏览器接管中</span>";
      return;
    }
    btn.disabled = false;
    btn.innerHTML = "下载 APK" + (sizeText ? "<span class=\"btn-note\" id=\"dlButtonNote\">（" + sizeText + "）</span>" : "<span class=\"btn-note\" id=\"dlButtonNote\"></span>");
  }

  function setHint(text) {
    var hint = $("#dlButtonHint");
    if (hint) hint.textContent = text;
  }

  // HTTPS 下载先用 HEAD 预检。HTTP 直连 IP 会被 HTTPS 页面按混合内容拦截，交给浏览器下载器处理。
  function preflightDownload(urlHref) {
    try {
      var direct = new URL(urlHref, location.href);
      if (direct.protocol === "http:" && direct.hostname === "101.132.250.38" &&
          (direct.port === "" || direct.port === "80")) {
        return Promise.resolve(200);
      }
    } catch (e) { /* 继续走正常请求，让错误状态可见 */ }
    var controller = new AbortController();
    var timer = setTimeout(function () { controller.abort(); }, 12000);
    return fetch(urlHref, { method: "HEAD", cache: "no-store", signal: controller.signal })
      .then(function (res) { clearTimeout(timer); return res.status; })
      .catch(function (err) {
        clearTimeout(timer);
        throw err;
      });
  }

  if (dlButton) {
    dlButton.addEventListener("click", function () {
      if (!downloadReady || !manifest || checking) return;
      var url = resolveApkUrl(manifest);
      if (!url) return;

      checking = true;
      setButtonState("checking");
      setHint("正在确认下载通道是否空闲…");

      preflightDownload(url.href).then(function (status) {
        checking = false;
        if (status === 429) {
          setButtonState("ready", formatSize(manifest.apkSize));
          setHint("当前网络繁忙，请稍后重试。发布通道同一时刻仅允许一个下载任务。");
          toast("当前网络繁忙，请稍后重试");
          return;
        }
        if (status >= 400) {
          setButtonState("ready", formatSize(manifest.apkSize));
          setHint("下载服务返回 HTTP " + status + "，请稍后重试。");
          toast("下载服务暂时不可用（HTTP " + status + "）");
          return;
        }
        setButtonState("ready", formatSize(manifest.apkSize));
        setHint("下载地址已就绪，请确认下载须知后开始下载。");
        openModal(dlButton);
      }).catch(function (err) {
        checking = false;
        setButtonState("ready", formatSize(manifest.apkSize));
        if (err && err.name === "AbortError") {
          setHint("检查下载通道超时，请稍后重试。");
        } else {
          setHint("网络连接失败，请检查网络后重试。");
        }
        toast("暂时连不上下载服务");
      });
    });
  }

  if (dlCancel) dlCancel.addEventListener("click", closeModal);
  if (dlModal) {
    dlModal.addEventListener("click", function (e) {
      if (e.target === dlModal) closeModal();
    });
    // 简单焦点圈：Tab 在弹窗内循环，不漏到背景页面
    dlModal.addEventListener("keydown", function (e) {
      if (e.key !== "Tab") return;
      var focusables = Array.prototype.filter.call(
        dlModal.querySelectorAll("button, [href], [tabindex]:not([tabindex=\"-1\"])"),
        function (el) { return !el.disabled; }
      );
      if (!focusables.length) return;
      var first = focusables[0];
      var last = focusables[focusables.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    });
  }
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      closeModal();
      closeMobileMenu();
    }
  });

  if (dlConfirm) {
    dlConfirm.addEventListener("click", function () {
      if (!manifest) return;
      var url = resolveApkUrl(manifest);
      if (!url) return;

      // 用隐藏锚点触发下载：同源 + APK 内容类型，浏览器接管下载且不离开本页
      var a = document.createElement("a");
      a.href = url.href;
      a.download = "";
      a.rel = "noopener";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      closeModal();
      setButtonState("started");
      setHint("浏览器已开始下载。若通道繁忙导致任务中断，稍后重试即可；下载完成后建议核对 SHA-256。");
      toast("已开始下载，请留意浏览器下载提示");

      // 一段时间后恢复按钮，便于重复下载
      setTimeout(function () {
        if (manifest) {
          setButtonState("ready", formatSize(manifest.apkSize));
          setHint("再次点击可重新下载。");
        }
      }, 6000);
    });
  }

  /* ---------------- SHA-256 复制 ---------------- */

  var copyBtn = $("#copySha");
  if (copyBtn) {
    copyBtn.addEventListener("click", function () {
      var text = ($("#vSha") && $("#vSha").textContent) || "";
      if (!text || text === "—") return;

      function done() {
        copyBtn.textContent = "已复制";
        copyBtn.classList.add("done");
        toast("SHA-256 已复制到剪贴板");
        setTimeout(function () {
          copyBtn.textContent = "复制";
          copyBtn.classList.remove("done");
        }, 2000);
      }

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(done, function () { fallbackCopy(text); done(); });
      } else {
        fallbackCopy(text);
        done();
      }
    });
  }

  function fallbackCopy(text) {
    var ta = document.createElement("textarea");
    ta.value = text;
    ta.style.position = "fixed";
    ta.style.opacity = "0";
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand("copy"); } catch (e) { /* 忽略 */ }
    document.body.removeChild(ta);
  }

  /* ---------------- FAQ 折叠 ---------------- */

  document.querySelectorAll(".faq-item").forEach(function (item) {
    var q = item.querySelector(".faq-q");
    var a = item.querySelector(".faq-a");
    if (!q || !a) return;
    q.setAttribute("aria-expanded", "false");
    q.addEventListener("click", function () {
      var isOpen = item.classList.toggle("open");
      q.setAttribute("aria-expanded", isOpen ? "true" : "false");
      a.style.maxHeight = isOpen ? a.scrollHeight + "px" : "0px";
      var arrow = q.querySelector(".faq-arrow");
      if (arrow) arrow.textContent = isOpen ? "－" : "＋";
    });
  });

  /* ---------------- 手机预览交互 ----------------
     这是官网里的可操作演示，不连接真实 App 数据，也不改变外层 hash 路由。 */
  function initPhoneDemo() {
    Array.prototype.forEach.call(document.querySelectorAll(".phone-demo"), function (root) {
      var phone = root.querySelector(".phone");
      var screen = root.querySelector("[data-demo-role=\"screen\"]");
      var panel = root.querySelector("[data-demo-role=\"panel\"]");
      var sheet = root.querySelector("[data-demo-role=\"sheet\"]");
      var fab = root.querySelector("[data-demo-role=\"fab\"]");
      var floatingActions = root.querySelectorAll(".p-fab");
      var homeView = root.querySelector(".phone-demo-home-view");
      var title = root.querySelector("[data-demo-role=\"title\"]");
      var feedback = root.querySelector("[data-demo-role=\"feedback\"]");
      var calendar = root.querySelector("[data-demo-action=\"calendar\"]");
      var weekday = root.querySelector("[data-demo-role=\"weekday\"]");
      var date = root.querySelector("[data-demo-role=\"date\"]");
      var relative = root.querySelector("[data-demo-role=\"relative\"]");
      var demoIndex = root.querySelector("[data-demo-role=\"index\"]");
      var demoRoute = root.querySelector("[data-demo-role=\"route\"]");
      var demoHeading = root.querySelector("[data-demo-role=\"heading\"]");
      var demoDescription = root.querySelector("[data-demo-role=\"description\"]");
      var hotspot = root.querySelector("[data-demo-role=\"hotspot\"]");
      var hotspotNo = root.querySelector("[data-demo-role=\"hotspot-no\"]");
      var hotspotLabel = root.querySelector("[data-demo-role=\"hotspot-label\"]");
      var currentLabel = root.querySelector("[data-demo-role=\"current\"]");
      var relatedLabel = root.querySelector("[data-demo-role=\"related\"]");
      if (!phone || !screen || !panel || !sheet || !fab || !title || !feedback || !calendar) return;

      var tabs = root.querySelectorAll("[data-demo-tab]");
      var railTabs = root.querySelectorAll(".phone-demo-pick[data-demo-tab]");
      var feedbackTimer = null;
      var activeTab = "";
      var panelDetail = false;
      var detailSource = null;
      var tabViews = {};
      var panelDomCache = {};
      var viewMount = document.createElement("div");
      var detailMount = document.createElement("div");
      viewMount.className = "phone-demo-view-mount";
      detailMount.className = "phone-demo-detail-mount";
      detailMount.hidden = true;

      // 稳定详情框架结构，避免频繁重构头部与触发完整布局重排
      var detailHeader = document.createElement("div");
      detailHeader.className = "phone-demo-detail-head";
      detailHeader.innerHTML = "<button type=\"button\" class=\"phone-demo-detail-back\" data-demo-panel-close aria-label=\"返回\">‹</button>" +
        "<div><div class=\"phone-demo-kicker\">CalorieAI</div><b class=\"phone-demo-detail-title\"></b></div>";
      var detailTitleText = detailHeader.querySelector(".phone-demo-detail-title");
      var detailBodyMount = document.createElement("div");
      detailBodyMount.className = "phone-demo-detail-body";
      detailMount.appendChild(detailHeader);
      detailMount.appendChild(detailBodyMount);

      var demoMeta = {
        home: {
          index: "01", route: "首页 · 今日记录", heading: "当日热量收支集中在首屏",
          description: "包含日期切换、热量收支、饮食明细与饮水卡片。点击日期、记录或标记可查看关联详情。",
          current: "首页", related: "详细统计", hotspotAction: "overview", hotspotLabel: "今日概览", hotspotAria: "查看今日概览"
        },
        recipe: {
          index: "02", route: "菜谱 · 收藏与复用", heading: "常用菜谱快速记入今日",
          description: "按餐次整理常吃菜谱，支持查看食材与热量，并可直接记入当日饮食。",
          current: "菜谱中心", related: "加入今日", hotspotAction: "recipe-detail", hotspotLabel: "番茄炒蛋", hotspotAria: "查看番茄炒蛋菜谱"
        },
        overview: {
          index: "03", route: "概览 · 趋势与统计", heading: "多维度汇总与变化趋势",
          description: "包含 12 周打卡热力图、月度汇总与数据速览。点击卡片或标记可打开详细统计。",
          current: "概览", related: "详细统计", hotspotAction: "overview", hotspotLabel: "详细统计", hotspotAria: "打开详细统计"
        },
        profile: {
          index: "04", route: "我的 · 资料与设置", heading: "身体档案与系统设置",
          description: "管理个人身体指标、热量目标与显示模式。点击标记可查看设置项。",
          current: "我的", related: "设置", hotspotAction: "profile-action", hotspotLabel: "设置", hotspotAria: "打开设置"
        }
      };

      function heatmapMarkup() {
        var levels = [0, 1, 2, 1, 3, 2, 0, 1, 2, 3, 1, 0, 2, 2, 3, 1, 0, 1, 3, 2, 1, 0, 2, 3, 1, 2, 0, 1, 3, 2, 1, 0, 1, 2, 3, 1, 0, 2, 3, 2, 1, 0, 1, 3, 2, 1, 0, 2, 3, 1, 2, 0, 1, 3, 2, 1, 0, 2, 1, 3, 2, 0, 1, 2, 3, 1, 0, 2, 1, 3, 2, 1, 0, 2, 3, 1, 0, 2, 1, 3, 2, 0, 1, 2];
        return levels.map(function (level) { return "<i" + (level ? " class=\"l" + level + "\"" : "") + "></i>"; }).join("");
      }

      var tabMarkup = {
        home: { title: "", html: "" },
        recipe: {
          title: "菜谱中心",
          html: "<div class=\"phone-demo-kicker\">菜谱中心</div>" +
            "<div class=\"p-card\"><div class=\"p-label\">收藏概览</div><div class=\"phone-demo-note\">常用菜谱与用餐计划</div>" +
            "<div class=\"p-metric-grid\"><div class=\"p-metric\"><b>12</b><small>收藏菜谱</small></div><div class=\"p-metric\"><b>3</b><small>当前菜单</small></div></div></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">快捷复用</div><div class=\"phone-demo-note\">按餐次筛选常用菜，直接记入今日</div><div class=\"p-chip-row\"><span class=\"p-chip on\">早餐</span><span class=\"p-chip\">午餐</span><span class=\"p-chip\">晚餐</span><span class=\"p-chip\">加餐</span></div>" +
            "<button type=\"button\" class=\"phone-demo-list-row\" data-demo-action=\"recipe-detail\" data-demo-label=\"番茄炒蛋\"><span><b>番茄炒蛋</b><small>318 千卡 · 最近复用 6 次</small></span><span>›</span></button>" +
            "<button type=\"button\" class=\"phone-demo-list-row\" data-demo-action=\"recipe-detail\" data-demo-label=\"鸡胸肉沙拉\"><span><b>鸡胸肉沙拉</b><small>286 千卡 · 最近复用 3 次</small></span><span>›</span></button>" +
            "<button type=\"button\" class=\"phone-demo-list-row\" data-demo-action=\"recipe-detail\" data-demo-label=\"全麦三明治\"><span><b>全麦三明治</b><small>354 千卡 · 最近复用 2 次</small></span><span>›</span></button>" +
            "<div class=\"phone-demo-detail-actions\"><button type=\"button\" data-demo-action=\"recipe-detail\" data-demo-label=\"番茄炒蛋\">管理收藏</button></div></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">用餐计划</div><div class=\"phone-demo-note\">本周菜单 · 09-15 至 09-21</div><div class=\"phone-demo-setting-detail\"><b>高蛋白工作日</b><span>覆盖早餐、午餐和晚餐</span></div></div>"
        },
        overview: {
          title: "概览",
          html: "<div class=\"phone-demo-kicker\">概览 · 趋势与统计</div>" +
            "<div class=\"p-card\"><div class=\"p-card-head\"><div class=\"p-label\">本月活跃度</div><span class=\"p-chevron\">12 周</span></div><div class=\"p-heat\">" + heatmapMarkup() + "</div><div class=\"phone-demo-note\">连续打卡 6 天 · 今日记录 3 条 · 活跃度 78%</div></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">本月总结</div><div class=\"p-summary-grid\"><div class=\"p-summary\"><b>32 480</b><small>总热量</small></div><div class=\"p-summary\"><b>2 860</b><small>运动消耗</small></div><div class=\"p-summary\"><b>−0.8kg</b><small>体重变化</small></div></div></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">数据速览</div><div class=\"p-metric-grid\"><div class=\"p-metric\"><b>1 286</b><small>今日摄入</small></div><div class=\"p-metric\"><b>1 200ml</b><small>今日饮水</small></div><div class=\"p-metric\"><b>65.0kg</b><small>当前体重</small></div><div class=\"p-metric\"><b>320kcal</b><small>今日运动</small></div></div></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">快捷入口</div><div class=\"p-quick-list\"><button type=\"button\" class=\"p-quick\" data-demo-action=\"overview\"><span class=\"ic\">▥</span><b>详细统计</b><span>›</span></button><button type=\"button\" class=\"p-quick\" data-demo-action=\"overview\"><span class=\"ic\">⌁</span><b>体重历史</b><span>›</span></button><button type=\"button\" class=\"p-quick\" data-demo-action=\"overview\"><span class=\"ic\">◒</span><b>健康目标</b><span>›</span></button></div></div>"
        },
        profile: {
          title: "我的",
          html: "<div class=\"phone-demo-kicker\">我的 · 资料与设置</div><div class=\"p-card\"><div class=\"p-avatar\"><svg viewBox=\"0 0 24 24\" fill=\"#FFFFFF\" aria-hidden=\"true\"><path d=\"M12 12c2.2 0 4-1.8 4-4s-1.8-4-4-4-4 1.8-4 4 1.8 4 4 4zm0 2c-2.7 0-8 1.3-8 4v2h16v-2c0-2.7-5.3-4-8-4z\"/></svg></div><div class=\"p-me-name\">健康用户</div><div class=\"p-me-meta\">170cm · 65kg · BMI 22.5</div></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">功能菜单</div>" +
            "<button type=\"button\" class=\"phone-demo-setting\" data-demo-action=\"profile-action\" data-demo-label=\"身体档案\"><span class=\"ic\">📋</span><span><b>身体档案</b><small>查看与编辑身体指标</small></span><span>›</span></button>" +
            "<button type=\"button\" class=\"phone-demo-setting\" data-demo-action=\"profile-action\" data-demo-label=\"设置\"><span class=\"ic\">⚙</span><span><b>设置</b><small>外观、备份与偏好设置</small></span><span>›</span></button></div>" +
            "<div class=\"p-card\"><div class=\"p-label\">应用信息</div><div class=\"phone-demo-setting-detail\"><b>CalorieAI</b><span>v1.0.0 · 本地热量记录应用</span></div></div>"
        }
      };

      Object.keys(tabMarkup).forEach(function (name) {
        var view = document.createElement("div");
        view.className = "phone-demo-tab-view";
        view.dataset.demoTabView = name;
        view.innerHTML = tabMarkup[name].html;
        view.hidden = true;
        tabViews[name] = view;
        viewMount.appendChild(view);
      });
      panel.replaceChildren(viewMount, detailMount);

      function updateDemoMeta(tab) {
        var meta = demoMeta[tab] || demoMeta.home;
        if (demoIndex) demoIndex.textContent = meta.index;
        if (demoRoute) demoRoute.textContent = meta.route;
        if (demoHeading) demoHeading.textContent = meta.heading;
        if (demoDescription) demoDescription.textContent = meta.description;
        if (currentLabel) currentLabel.textContent = meta.current;
        if (relatedLabel) relatedLabel.textContent = meta.related;
        if (hotspot) {
          hotspot.setAttribute("data-demo-action", meta.hotspotAction);
          hotspot.setAttribute("data-demo-label", meta.hotspotLabel);
          hotspot.setAttribute("aria-label", meta.hotspotAria);
        }
        if (hotspotNo) hotspotNo.textContent = meta.index.replace(/^0/, "");
        if (hotspotLabel) hotspotLabel.textContent = meta.hotspotLabel;
      }

      function closeSheet(restoreFocus) {
        var wasOpen = !sheet.hidden;
        sheet.hidden = true;
        fab.setAttribute("aria-expanded", "false");
        if (restoreFocus && wasOpen) fab.focus();
      }

      function syncDemoVisibility() {
        if (homeView) homeView.hidden = activeTab !== "home" || panelDetail;
        floatingActions.forEach(function (action) {
          action.hidden = activeTab !== "home" || panelDetail;
        });
      }

      function openPanel(panelTitle, panelKey, markupOrFactory, source) {
        detailSource = source || document.activeElement;
        panelDetail = true;
        detailTitleText.textContent = panelTitle;
        title.textContent = panelTitle;

        if (!panelDomCache[panelKey]) {
          var wrapper = document.createElement("div");
          wrapper.className = "phone-demo-detail-content-item";
          wrapper.innerHTML = typeof markupOrFactory === "function" ? markupOrFactory() : markupOrFactory;
          panelDomCache[panelKey] = wrapper;
        }

        var targetNode = panelDomCache[panelKey];
        if (detailBodyMount.firstElementChild !== targetNode) {
          detailBodyMount.replaceChildren(targetNode);
        }

        detailMount.hidden = false;
        viewMount.hidden = true;
        panel.hidden = false;
        closeSheet(false);
        syncDemoVisibility();
        requestAnimationFrame(function () {
          var closeButton = detailHeader.querySelector("[data-demo-panel-close]");
          if (closeButton) closeButton.focus();
        });
      }

      function closePanel(restoreFocus) {
        if (!panelDetail) return;
        panelDetail = false;
        detailMount.hidden = true;
        viewMount.hidden = false;
        panel.hidden = activeTab === "home";
        title.textContent = tabMarkup[activeTab].title;
        updateDemoMeta(activeTab);
        syncDemoVisibility();
        var source = detailSource;
        detailSource = null;
        if (restoreFocus && source && document.contains(source)) source.focus();
      }

      function updateSelectedDate(day) {
        if (weekday) weekday.textContent = day.weekday;
        if (date) date.textContent = day.date;
        if (relative) relative.textContent = day.relative ? "（" + day.relative + "）" : "";
      }

      function getCalendarMarkup() {
        var days = [
          { weekday: "周一", date: "09-14", relative: "", kcal: "1 648 千卡" },
          { weekday: "周二", date: "09-15", relative: "", kcal: "1 412 千卡" },
          { weekday: "周三", date: "09-16", relative: "昨天", kcal: "1 906 千卡" },
          { weekday: "周四", date: "09-17", relative: "昨天", kcal: "1 286 千卡" },
          { weekday: "周五", date: "09-18", relative: "今天", kcal: "暂无记录" },
          { weekday: "周六", date: "09-19", relative: "明天", kcal: "暂无记录" }
        ];
        var buttons = days.map(function (day) {
          return "<button type=\"button\" class=\"phone-demo-date-option\" data-demo-calendar-date='" +
            JSON.stringify(day).replace(/'/g, "&#39;") + "'><b>" + day.weekday + "</b><strong>" + day.date +
            "</strong><small>" + day.kcal + "</small></button>";
        }).join("");
        return "<div class=\"phone-demo-detail-copy\">选择任意日期，查看当天的热量收支与记录。</div>" +
          "<div class=\"phone-demo-date-list\" role=\"list\">" + buttons + "</div>";
      }

      function openCalendarPanel(source) {
        openPanel("选择日期", "calendar", getCalendarMarkup, source);
      }

      var foodRecords = {
        "番茄炒蛋": ["午餐 · 12:24", "318 千卡", "蛋白质 16.4g · 脂肪 19.1g · 碳水 20.5g"],
        "全麦面包 2 片": ["早餐 · 08:05", "176 千卡", "蛋白质 7.2g · 脂肪 2.8g · 碳水 31.5g"],
        "美式咖啡": ["早餐 · 08:02", "8 千卡", "蛋白质 0.4g · 脂肪 0g · 碳水 1.2g"]
      };

      function getFoodMarkup(label) {
        var record = foodRecords[label] || ["饮食记录", "—", "包含热量及 13 项营养指标明细"];
        return "<div class=\"phone-demo-detail-card\"><b>" + record[1] + "</b><span>" + record[0] + "</span></div>" +
          "<div class=\"phone-demo-detail-copy\">" + record[2] + "</div>" +
          "<div class=\"phone-demo-detail-actions\"><button type=\"button\" data-demo-panel-close>返回今日记录</button></div>";
      }

      function openFoodPanel(label, source) {
        openPanel(label, "food:" + label, function () { return getFoodMarkup(label); }, source);
      }

      function getRecipeMarkup(label) {
        var recipeText = label === "番茄炒蛋" ? "番茄、鸡蛋和少量食用油，适合午餐快速复用。" :
          label === "鸡胸肉沙拉" ? "鸡胸肉、生菜和玉米，清爽高蛋白。" : "全麦面包、生菜和煎蛋，适合作为早餐。";
        return "<div class=\"phone-demo-detail-card\"><b>常用菜谱</b><span>可直接加入今日饮食记录</span></div>" +
          "<div class=\"phone-demo-detail-copy\">" + recipeText + "</div>" +
          "<div class=\"phone-demo-detail-actions\"><button type=\"button\" data-demo-entry=\"加入今日记录\">加入今日记录</button></div>";
      }

      function openRecipePanel(label, source) {
        openPanel(label, "recipe:" + label, function () { return getRecipeMarkup(label); }, source);
      }

      function getProfileMarkup(label) {
        return label === "设置" ? "<div class=\"phone-demo-setting-detail\"><b>简洁模式</b><span>仅保留首页与设置两页，界面更清爽。</span></div>" +
          "<div class=\"phone-demo-setting-detail\"><b>数据与备份</b><span>支持本地导出与 WebDAV 云端备份。</span></div>" :
          "<div class=\"phone-demo-setting-detail\"><b>身体档案</b><span>用于计算基础代谢（BMR）与每日建议热量。</span></div>";
      }

      function openProfilePanel(label, source) {
        openPanel(label, "profile:" + label, function () { return getProfileMarkup(label); }, source);
      }

      function getEntryMarkup(label) {
        return label === "手动录入" ? "<div class=\"phone-demo-detail-copy\">输入食物名称、分量与热量，直接保存至今日记录。</div><div class=\"phone-demo-form-preview\"><span>食物名称</span><span>分量</span><span>餐次</span></div>" :
          label === "记录体重" ? "<div class=\"phone-demo-detail-copy\">输入当前体重数值与备注，记录将同步至趋势与统计。</div><div class=\"phone-demo-form-preview\"><b>65.0 kg</b><span>今天 · 09:17</span></div>" :
          "<div class=\"phone-demo-detail-copy\">支持文字描述、拍照与语音输入，自动估算热量与营养。</div><div class=\"phone-demo-entry-preview\"><span>✦</span><b>选择输入方式</b></div>";
      }

      function openEntryPanel(label, source) {
        openPanel(label, "entry:" + label, function () { return getEntryMarkup(label); }, source);
      }

      function showFeedback(message) {
        if (feedbackTimer) clearTimeout(feedbackTimer);
        feedback.textContent = message;
        feedback.hidden = false;
        if (!feedback.classList.contains("show")) {
          requestAnimationFrame(function () { feedback.classList.add("show"); });
        }
        feedbackTimer = setTimeout(function () {
          feedback.classList.remove("show");
          feedbackTimer = setTimeout(function () { feedback.hidden = true; }, 160);
        }, 2200);
      }

      function setTab(name) {
        var tab = tabMarkup[name] ? name : "home";
        if (panelDetail) closePanel(false);
        activeTab = tab;
        title.textContent = tabMarkup[tab].title;
        panel.hidden = tab === "home";
        viewMount.hidden = false;
        detailMount.hidden = true;
        Object.keys(tabViews).forEach(function (viewName) {
          tabViews[viewName].hidden = viewName !== tab;
        });
        updateDemoMeta(tab);
        tabs.forEach(function (button) {
          var selected = button.getAttribute("data-demo-tab") === tab;
          button.classList.toggle("on", selected);
          button.setAttribute("aria-selected", selected ? "true" : "false");
          button.setAttribute("aria-pressed", selected ? "true" : "false");
        });
        railTabs.forEach(function (button) {
          var isSelected = button.getAttribute("data-demo-tab") === tab;
          button.setAttribute("tabindex", isSelected ? "0" : "-1");
          if (isSelected) {
            screen.setAttribute("aria-labelledby", button.id);
          }
        });
        closeSheet(false);
        syncDemoVisibility();
      }

      root.addEventListener("click", function (event) {
        var target = event.target;
        if (!target || typeof target.closest !== "function") return;

        var el = target.closest("[data-demo-tab], [data-demo-role=\"fab\"], [data-demo-close], [data-demo-panel-close], [data-demo-calendar-date], [data-demo-entry], [data-demo-action]");
        if (!el) return;

        if (el.hasAttribute("data-demo-tab")) {
          setTab(el.getAttribute("data-demo-tab"));
          return;
        }

        if (el.matches("[data-demo-role=\"fab\"]")) {
          var open = sheet.hidden;
          sheet.hidden = !open;
          fab.setAttribute("aria-expanded", open ? "true" : "false");
          if (open) {
            var firstEntry = sheet.querySelector("[data-demo-entry]");
            if (firstEntry) firstEntry.focus();
          }
          return;
        }

        if (el.hasAttribute("data-demo-close")) {
          closeSheet(true);
          return;
        }

        if (el.hasAttribute("data-demo-panel-close")) {
          closePanel(true);
          return;
        }

        if (el.hasAttribute("data-demo-calendar-date")) {
          var dateData = {};
          try { dateData = JSON.parse(el.getAttribute("data-demo-calendar-date") || "{}"); } catch (e) { /* 忽略 */ }
          updateSelectedDate(dateData);
          closePanel(true);
          showFeedback("已切换到 " + (dateData.date || "所选日期"));
          return;
        }

        if (el.hasAttribute("data-demo-entry")) {
          var entryName = el.getAttribute("data-demo-entry") || "记录入口";
          var entrySource = el;
          if (sheet.contains(el)) {
            entrySource = fab;
            closeSheet(false);
          } else if (panel.contains(el)) {
            entrySource = root.querySelector(".phone-demo-pick.on") || fab;
          }
          openEntryPanel(entryName, entrySource);
          return;
        }

        if (el.hasAttribute("data-demo-action")) {
          var actionName = el.getAttribute("data-demo-action");
          if (actionName === "overview") {
            setTab("overview");
            showFeedback("已打开概览");
          } else if (actionName === "calendar") {
            openCalendarPanel(el);
          } else if (actionName === "food-detail") {
            openFoodPanel(el.getAttribute("data-demo-label") || "饮食记录", el);
          } else if (actionName === "recipe-detail") {
            openRecipePanel(el.getAttribute("data-demo-label") || "菜谱", el);
          } else if (actionName === "profile-action") {
            openProfilePanel(el.getAttribute("data-demo-label") || "我的", el);
          } else if (actionName === "water") {
            showFeedback("已增加 250 毫升饮水记录");
          }
        }
      });

      root.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
          if (!sheet.hidden) {
            closeSheet(true);
            showFeedback("已关闭新增记录");
            return;
          }
          if (panelDetail) {
            closePanel(true);
            showFeedback("已返回上一层");
            return;
          }
        }

        if (event.key === "Tab" && !sheet.hidden) {
          var focusable = Array.prototype.filter.call(
            sheet.querySelectorAll("button, [href], [tabindex]:not([tabindex=\"-1\"])"),
            function (el) { return !el.disabled; }
          );
          if (!focusable.length) return;
          var first = focusable[0];
          var last = focusable[focusable.length - 1];
          if (event.shiftKey && document.activeElement === first) {
            event.preventDefault();
            last.focus();
            return;
          }
          if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
            return;
          }
        }

        var railTab = event.target && event.target.closest
          ? event.target.closest(".phone-demo-pick[data-demo-tab]")
          : null;
        if (railTab && (event.key === "ArrowRight" || event.key === "ArrowLeft" || event.key === "Home" || event.key === "End")) {
          event.preventDefault();
          var index = Array.prototype.indexOf.call(railTabs, railTab);
          var next = event.key === "Home" ? 0
            : event.key === "End" ? railTabs.length - 1
            : (index + (event.key === "ArrowRight" ? 1 : -1) + railTabs.length) % railTabs.length;
          railTabs[next].focus();
          setTab(railTabs[next].getAttribute("data-demo-tab"));
          return;
        }

        if (event.key === "Enter" || event.key === " ") {
          var target = event.target;
          if (!target || typeof target.closest !== "function") return;
          var action = target.closest("[data-demo-action]");
          if (action && action.tagName !== "BUTTON") {
            event.preventDefault();
            action.click();
          }
        }
      });

      updateDemoMeta("home");
      setTab("home");
    });
  }

  /* ---------------- 首屏字符场 ---------------- */
  function initHeroField() {
    var hero = document.querySelector(".hero");
    var field = document.getElementById("heroField");
    var pre = document.getElementById("heroFieldText");
    var vignette = document.getElementById("vignette");
    if (!hero || !field || !pre) return;

    var reduce = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    var isLowPerf = false;
    try {
      var cores = navigator.hardwareConcurrency || 4;
      var mem = navigator.deviceMemory || 4;
      var isCoarse = window.matchMedia && window.matchMedia("(pointer: coarse)").matches;
      isLowPerf = cores <= 4 || mem <= 4 || isCoarse;
    } catch (e) {
      isLowPerf = false;
    }

    var chars = "   .-:~+*#calorieai<>/{}=_";
    var charsLen = chars.length;
    var maxCharIdx = charsLen - 1;

    // 低性能设备使用较低密度网格与更大字间距，大幅减少单元格噪声计算量
    var cellW = isLowPerf ? 14.0 : 8.6;
    var cellH = isLowPerf ? 18.0 : 11.5;
    var MAX_COLS = isLowPerf ? 72 : 140;
    var MAX_ROWS = isLowPerf ? 40 : 70;

    var pointerFrame = 0;
    var pointerX = 0;
    var pointerY = 0;
    var heroRect = { left: 0, top: 0, width: 0, height: 0 };
    var lastWidth = 0;
    var lastHeight = 0;
    var resizeTimer = null;
    var lastPointerTime = 0;
    var pointerThrottle = isLowPerf ? 40 : 16;

    function hash(x, y) {
      var n = Math.imul(x, 0x27d4eb2d) ^ Math.imul(y, 0x165667b1);
      n = Math.imul(n ^ (n >>> 15), 0x2c1b3c6d);
      n = Math.imul(n ^ (n >>> 12), 0x297a2d39);
      return ((n ^ (n >>> 15)) >>> 0) / 4294967295;
    }

    function smooth(t) { return t * t * (3 - 2 * t); }

    function renderField() {
      var rect = hero.getBoundingClientRect();
      heroRect = { left: rect.left, top: rect.top, width: rect.width, height: rect.height };
      if (!heroRect.width || !heroRect.height) return;

      lastWidth = heroRect.width;
      lastHeight = heroRect.height;

      var cols = Math.min(MAX_COLS, Math.max(1, Math.ceil(heroRect.width / cellW) + 1));
      var rows = Math.min(MAX_ROWS, Math.max(1, Math.ceil(heroRect.height / cellH) + 1));

      if (isLowPerf) {
        pre.style.fontSize = "13px";
        pre.style.lineHeight = "1.1";
        pre.style.letterSpacing = "0.06em";
      }

      var out = [];
      for (var y = 0; y < rows; y += 1) {
        var y1 = y * 0.16;
        var iy1 = Math.floor(y1);
        var fy1 = smooth(y1 - iy1);

        var y2 = y * 0.07;
        var iy2 = Math.floor(y2);
        var fy2 = smooth(y2 - iy2);

        var line = "";
        for (var x = 0; x < cols; x += 1) {
          var x1 = x * 0.11;
          var ix1 = Math.floor(x1);
          var fx1 = smooth(x1 - ix1);
          var a1 = hash(ix1, iy1);
          var b1 = hash(ix1 + 1, iy1);
          var c1 = hash(ix1, iy1 + 1);
          var d1 = hash(ix1 + 1, iy1 + 1);
          var n1 = a1 + (b1 - a1) * fx1 + (c1 - a1) * fy1 + (a1 - b1 - c1 + d1) * fx1 * fy1;

          var x2 = x * 0.045;
          var ix2 = Math.floor(x2);
          var fx2 = smooth(x2 - ix2);
          var a2 = hash(ix2, iy2);
          var b2 = hash(ix2 + 1, iy2);
          var c2 = hash(ix2, iy2 + 1);
          var d2 = hash(ix2 + 1, iy2 + 1);
          var n2 = a2 + (b2 - a2) * fx2 + (c2 - a2) * fy2 + (a2 - b2 - c2 + d2) * fx2 * fy2;

          var base = 0.72 * n1 + 0.48 * n2;
          var idx = Math.floor((base - 0.58) * 1.9 * charsLen);
          if (idx < 0) idx = 0;
          else if (idx > maxCharIdx) idx = maxCharIdx;
          line += chars[idx];
        }
        out.push(line);
      }
      pre.textContent = out.join("\n");
    }

    function updateVignette() {
      pointerFrame = 0;
      if (vignette && heroRect.width > 0) {
        var xPct = (pointerX / heroRect.width * 100).toFixed(1) + "%";
        var yPct = (pointerY / heroRect.height * 100).toFixed(1) + "%";
        vignette.style.setProperty("--hero-x", xPct);
        vignette.style.setProperty("--hero-y", yPct);
      }
    }

    function onPointerMove(event) {
      var now = Date.now();
      if (now - lastPointerTime < pointerThrottle) return;
      lastPointerTime = now;
      pointerX = event.clientX - heroRect.left;
      pointerY = event.clientY - heroRect.top;
      if (!pointerFrame) pointerFrame = requestAnimationFrame(updateVignette);
    }

    function scheduleResize() {
      var r = hero.getBoundingClientRect();
      var dw = Math.abs(r.width - lastWidth);
      var dh = Math.abs(r.height - lastHeight);
      if (dw < 40 && dh < 40) return;
      if (resizeTimer) clearTimeout(resizeTimer);
      resizeTimer = setTimeout(function () {
        renderField();
      }, isLowPerf ? 300 : 180);
    }

    // 首次绘制在浏览器空闲时完成，不阻塞首屏主线程渲染与交互
    if ("requestIdleCallback" in window) {
      requestIdleCallback(function () { renderField(); }, { timeout: 300 });
    } else {
      setTimeout(renderField, 60);
    }

    // 监听滚动更新 heroRect 位置缓存，避免 pointermove 触发 getBoundingClientRect 布局读取
    var scrollTimer = null;
    window.addEventListener("scroll", function () {
      if (!scrollTimer) {
        scrollTimer = setTimeout(function () {
          scrollTimer = null;
          var r = hero.getBoundingClientRect();
          heroRect = { left: r.left, top: r.top, width: r.width, height: r.height };
        }, 200);
      }
    }, { passive: true });

    if ("ResizeObserver" in window) {
      var resizeObserver = new ResizeObserver(scheduleResize);
      resizeObserver.observe(hero);
    } else {
      window.addEventListener("resize", scheduleResize, { passive: true });
    }

    // 尊重 prefers-reduced-motion：减少动态效果模式下不挂载高频指针反馈
    if (reduce) return;

    var hasFinePointer = !window.matchMedia || window.matchMedia("(pointer: fine)").matches;
    if (hasFinePointer) {
      hero.addEventListener("pointermove", onPointerMove, { passive: true });
      hero.addEventListener("pointerleave", function () {
        if (pointerFrame) {
          cancelAnimationFrame(pointerFrame);
          pointerFrame = 0;
        }
        if (vignette) {
          vignette.style.removeProperty("--hero-x");
          vignette.style.removeProperty("--hero-y");
        }
      }, { passive: true });
    }
  }

  /* ---------------- 启动 ---------------- */

  render();
  loadManifest();
  initPhoneDemo();
  requestAnimationFrame(function () {
    requestAnimationFrame(initHeroField);
  });
})();
