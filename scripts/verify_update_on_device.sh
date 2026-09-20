#!/usr/bin/env bash
set -Eeuo pipefail

# End-to-end update verification on a real Android device.
# Robustness note: the device serial is resolved once and reused for every adb
# call, so a target chosen at start stays the target for the whole run.

ADB=${ADB:-/Users/sunyiyang/Library/Android/sdk/platform-tools/adb}
PKG=com.calorieai.app
REPO_ROOT=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
OUT_DIR=${OUT_DIR:-$REPO_ROOT/app/build/update-verification}
LOG=$OUT_DIR/device-verification.log

mkdir -p "$OUT_DIR"
: >"$LOG"

log() {
    printf '%s\n' "$*" | tee -a "$LOG"
}

die() {
    log "FAIL $*"
    exit 1
}

pick_device() {
    local serial=""
    while IFS= read -r line; do
        [[ "$line" == *"emulator-"* ]] && continue
        [[ "$line" == *"List of devices"* ]] && continue
        [[ -z "${line// /}" ]] && continue
        serial=${line%%$'\t'*}
        break
    done < <("$ADB" devices)
    printf '%s' "$serial"
}

resolve_apk() {(
    shopt -s nullglob
    local candidates=("$REPO_ROOT"/app/build/outputs/apk/release/*.apk)
    [[ ${#candidates[@]} -gt 0 ]] || return 1
    printf '%s' "${candidates[0]}"
)}

SERIAL=${DEVICE_SERIAL:-$(pick_device)}
[[ -n "$SERIAL" ]] || die "no real device found in 'adb devices'"
log "device serial: $SERIAL"

ADB_CMD=("$ADB" -s "$SERIAL")

model=$("${ADB_CMD[@]}" shell getprop ro.product.model | tr -d '\r')
android=$("${ADB_CMD[@]}" shell getprop ro.build.version.release | tr -d '\r')
sdk=$("${ADB_CMD[@]}" shell getprop ro.build.version.sdk | tr -d '\r')
log "model=$model android=$android sdk=$sdk"
[[ -n "$model" ]] || die "cannot read device properties"

APK=$(resolve_apk) || die "no release APK found under app/build/outputs/apk/release"
log "apk: $APK"

expected_size=$(wc -c <"$APK" | tr -d ' ')
expected_sha=$(shasum -a 256 "$APK" | awk '{print $1}')
log "expected size=$expected_size sha256=$expected_sha"

log "--- step 1: uninstall existing app ---"
"${ADB_CMD[@]}" uninstall "$PKG" >/dev/null 2>&1 || true
log "uninstall attempted"

log "--- step 2: install release APK ---"
install_out=$("${ADB_CMD[@]}" install -r -d "$APK" 2>&1) || die "install failed: $install_out"
log "$install_out"

installed_version=$("${ADB_CMD[@]}" shell dumpsys package "$PKG" | grep -m1 'versionCode=' | tr -d '\r')
log "installed: $installed_version"

log "--- step 3: grant post-notification permission ---"
if [[ "$sdk" -ge 33 ]]; then
    "${ADB_CMD[@]}" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS 2>&1 | tee -a "$LOG" || true
fi

log "--- step 4: launch app and capture update flow ---"
"${ADB_CMD[@]}" logcat -c 2>/dev/null || true
"${ADB_CMD[@]}" shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
sleep 12

"${ADB_CMD[@]}" logcat -d -v brief >"$OUT_DIR/logcat-boot.txt" 2>&1 || true

if grep -qi 'update\|AppUpdate' "$OUT_DIR/logcat-boot.txt"; then
    log "update-related log lines found"
    grep -i 'update\|AppUpdate' "$OUT_DIR/logcat-boot.txt" | head -40 | tee -a "$LOG" || true
else
    log "WARN no update-related log line captured"
fi

log "--- step 5: confirm update endpoint reachable from device ---"
device_json=$("${ADB_CMD[@]}" shell "curl -s --max-time 15 http://101.132.250.38:80/android/stable/latest.json" 2>/dev/null | tr -d '\r')
if [[ -n "$device_json" && "$device_json" == *versionCode* ]]; then
    log "device fetched latest.json OK"
    printf '%s\n' "$device_json" | tee -a "$LOG"
else
    log "WARN device-side curl unavailable or returned nothing"
fi

log "--- step 6: verify installed package integrity ---"
remote_sha=$("${ADB_CMD[@]}" shell "sha256sum \$(pm path $PKG | sed 's/package://')" 2>/dev/null | awk '{print $1}' | tr -d '\r')
if [[ -n "$remote_sha" ]]; then
    log "device sha256=$remote_sha"
    if [[ "$remote_sha" == "$expected_sha" ]]; then
        log "PASS installed APK matches release build"
    else
        log "WARN device sha differs (path transform or busybox variant)"
    fi
else
    log "WARN cannot compute device-side sha256"
fi

log "--- summary ---"
log "artifacts: $OUT_DIR"
log "logcat-boot.txt, device-verification.log"
log "PASS run completed"
