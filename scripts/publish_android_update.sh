#!/usr/bin/env bash
set -Eeuo pipefail

# Publish one immutable APK version and atomically replace stable metadata.
# The script never changes Nginx or removes an existing release directory.

usage() {
    cat >&2 <<'USAGE'
Usage:
  publish_android_update.sh --check APK VERSION_CODE VERSION_NAME
  publish_android_update.sh APK VERSION_CODE VERSION_NAME

Environment for a remote publish:
  UPDATE_SERVER_KEY   SSH private-key path
  UPDATE_SERVER_HOST  SSH target, default root@101.132.250.38
  UPDATE_CHECK_URL    manifest URL, default https://update.sxyq27.online/android/stable/latest.json
  UPDATE_DOWNLOAD_BASE_URL
                      APK base URL, default https://update.sxyq27.online
  APKANALYZER         optional path to Android SDK apkanalyzer
  APKSIGNER           optional path to Android SDK apksigner
  MIN_SUPPORTED_VERSION_CODE  default 100
  FORCE_UPDATE        true or false, default false
  RELEASE_NOTES       release notes string
USAGE
    exit 2
}

[[ $# -ge 3 ]] || usage

CHECK_ONLY=false
if [[ "$1" == "--check" ]]; then
    CHECK_ONLY=true
    shift
fi

APK_PATH=$1
VERSION_CODE=$2
VERSION_NAME=$3

command -v jq >/dev/null || { echo "jq is required" >&2; exit 1; }
[[ -f "$APK_PATH" ]] || { echo "APK not found: $APK_PATH" >&2; exit 1; }
[[ "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]] || { echo "invalid versionCode" >&2; exit 1; }
[[ "$VERSION_NAME" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || {
    echo "versionName must use MAJOR.MINOR.PATCH" >&2
    exit 1
}

file_size() {
    stat -f '%z' "$1" 2>/dev/null || stat -c '%s' "$1"
}

file_sha256() {
    if command -v shasum >/dev/null; then
        shasum -a 256 "$1" | awk '{print $1}'
    else
        sha256sum "$1" | awk '{print $1}'
    fi
}

find_sdk_tool() {
    local tool_name=$1
    local configured_name=$2
    local configured_path="${!configured_name:-}"
    if [[ -n "$configured_path" && -x "$configured_path" ]]; then
        printf '%s\n' "$configured_path"
        return 0
    fi
    if command -v "$tool_name" >/dev/null 2>&1; then
        command -v "$tool_name"
        return 0
    fi
    local sdk_root=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
    if [[ -z "$sdk_root" && -f "${REPO_ROOT}/local.properties" ]]; then
        sdk_root=$(sed -n 's/^sdk\.dir=//p' "${REPO_ROOT}/local.properties" | head -n 1)
    fi
    if [[ -n "$sdk_root" ]]; then
        find "$sdk_root" -type f -name "$tool_name" -perm -111 -print -quit 2>/dev/null
    fi
}

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd -- "${SCRIPT_DIR}/.." && pwd)

verify_apk_identity() {
    local analyzer
    analyzer=$(find_sdk_tool apkanalyzer APKANALYZER || true)
    [[ -n "$analyzer" ]] || {
        echo "Android SDK apkanalyzer is required to verify APK identity" >&2
        return 1
    }

    local package_id actual_code actual_name
    package_id=$("$analyzer" manifest application-id "$APK_PATH")
    actual_code=$("$analyzer" manifest version-code "$APK_PATH")
    actual_name=$("$analyzer" manifest version-name "$APK_PATH")
    [[ "$package_id" == "com.calorieai.app" ]] || {
        echo "unexpected applicationId: $package_id" >&2
        return 1
    }
    [[ "$actual_code" == "$VERSION_CODE" ]] || {
        echo "APK versionCode $actual_code does not match requested $VERSION_CODE" >&2
        return 1
    }
    [[ "$actual_name" == "$VERSION_NAME" ]] || {
        echo "APK versionName $actual_name does not match requested $VERSION_NAME" >&2
        return 1
    }

    local signer
    signer=$(find_sdk_tool apksigner APKSIGNER || true)
    [[ -n "$signer" ]] || {
        echo "Android SDK apksigner is required to verify APK signature" >&2
        return 1
    }
    "$signer" verify "$APK_PATH" >/dev/null
}

APK_SIZE=$(file_size "$APK_PATH")
APK_SHA256=$(file_sha256 "$APK_PATH")
APK_SHA256_LOWER=$(printf '%s' "$APK_SHA256" | tr '[:upper:]' '[:lower:]')
MIN_SUPPORTED_VERSION_CODE=${MIN_SUPPORTED_VERSION_CODE:-100}
FORCE_UPDATE=${FORCE_UPDATE:-false}
RELEASE_NOTES=${RELEASE_NOTES:-"Bug fixes and stability improvements."}

[[ "$MIN_SUPPORTED_VERSION_CODE" =~ ^[0-9]+$ ]] || {
    echo "invalid MIN_SUPPORTED_VERSION_CODE" >&2
    exit 1
}
[[ "$FORCE_UPDATE" == "true" || "$FORCE_UPDATE" == "false" ]] || {
    echo "FORCE_UPDATE must be true or false" >&2
    exit 1
}
[[ "$APK_SIZE" =~ ^[1-9][0-9]*$ ]] || { echo "invalid APK size" >&2; exit 1; }
[[ "$APK_SHA256" =~ ^[0-9a-fA-F]{64}$ ]] || { echo "invalid APK SHA-256" >&2; exit 1; }
verify_apk_identity

APK_FILE="CalorieAI-v${VERSION_NAME}.apk"
UPDATE_CHECK_URL=${UPDATE_CHECK_URL:-"https://update.sxyq27.online/android/stable/latest.json"}
UPDATE_DOWNLOAD_BASE_URL=${UPDATE_DOWNLOAD_BASE_URL:-"https://update.sxyq27.online"}
UPDATE_DOWNLOAD_BASE_URL=${UPDATE_DOWNLOAD_BASE_URL%/}

case "$UPDATE_CHECK_URL" in
    "https://update.sxyq27.online/android/stable/latest.json") ;;
    *) echo "unsupported UPDATE_CHECK_URL: $UPDATE_CHECK_URL" >&2; exit 1 ;;
esac
case "$UPDATE_DOWNLOAD_BASE_URL" in
    "https://update.sxyq27.online") ;;
    *) echo "unsupported UPDATE_DOWNLOAD_BASE_URL: $UPDATE_DOWNLOAD_BASE_URL" >&2; exit 1 ;;
esac
if [[ "$UPDATE_CHECK_URL" != "https://update.sxyq27.online/android/stable/latest.json" ||
      "$UPDATE_DOWNLOAD_BASE_URL" != "https://update.sxyq27.online" ]]; then
    echo "UPDATE_CHECK_URL and UPDATE_DOWNLOAD_BASE_URL must use the same endpoint pair" >&2
    exit 1
fi

APK_URL="${UPDATE_DOWNLOAD_BASE_URL}/releases/${VERSION_NAME}/${APK_FILE}"
TMP_DIR=$(mktemp -d "${TMPDIR:-/tmp}/calorieai-publish.XXXXXX")
trap 'rm -rf "$TMP_DIR"' EXIT
METADATA_PATH="$TMP_DIR/latest.json"

jq -n \
    --argjson versionCode "$VERSION_CODE" \
    --arg versionName "$VERSION_NAME" \
    --argjson minSupportedVersionCode "$MIN_SUPPORTED_VERSION_CODE" \
    --argjson forceUpdate "$FORCE_UPDATE" \
    --arg apkUrl "$APK_URL" \
    --argjson apkSize "$APK_SIZE" \
    --arg apkSha256 "$APK_SHA256_LOWER" \
    --arg releaseNotes "$RELEASE_NOTES" \
    '{versionCode:$versionCode,versionName:$versionName,minSupportedVersionCode:$minSupportedVersionCode,forceUpdate:$forceUpdate,apkUrl:$apkUrl,apkSize:$apkSize,apkSha256:$apkSha256,releaseNotes:$releaseNotes}' \
    > "$METADATA_PATH"

jq -e --arg sha "$APK_SHA256_LOWER" --argjson size "$APK_SIZE" \
    --arg expectedApkUrl "$APK_URL" \
    '(.versionCode > 0) and (.versionName|type == "string") and
     (.minSupportedVersionCode >= 0) and (.minSupportedVersionCode <= .versionCode) and
     (.forceUpdate|type == "boolean") and
     (.apkUrl == $expectedApkUrl) and
     (.apkSize == $size) and (.apkSha256 == $sha) and
     (has("voiceModel")|not) and (has("modelUrl")|not) and (has("tokensUrl")|not) and
     ((tostring|test("sk-|Authorization|password|private.key";"i"))|not)' \
    "$METADATA_PATH" >/dev/null

echo "APK: $APK_PATH"
echo "versionCode: $VERSION_CODE"
echo "versionName: $VERSION_NAME"
echo "apkSize: $APK_SIZE"
echo "apkSha256: $APK_SHA256_LOWER"
cat "$METADATA_PATH"

if $CHECK_ONLY; then
    exit 0
fi

SERVER_KEY=${UPDATE_SERVER_KEY:?UPDATE_SERVER_KEY is required for remote publish}
SERVER_HOST=${UPDATE_SERVER_HOST:-root@101.132.250.38}
[[ -f "$SERVER_KEY" ]] || { echo "SSH key not found" >&2; exit 1; }

SSH_OPTS=(
    -i "$SERVER_KEY"
    -o IdentitiesOnly=yes
    -o BatchMode=yes
    -o ConnectTimeout=8
    -o StrictHostKeyChecking=accept-new
)
REMOTE_STAGING="/srv/calorieai-updates/staging"
REMOTE_RELEASE_DIR="/srv/calorieai-updates/public/releases/${VERSION_NAME}"
REMOTE_APK="${REMOTE_STAGING}/${APK_FILE}.part"
REMOTE_JSON="${REMOTE_STAGING}/latest-${VERSION_NAME}.json.part"

scp -q "${SSH_OPTS[@]}" "$APK_PATH" "${SERVER_HOST}:${REMOTE_APK}"
scp -q "${SSH_OPTS[@]}" "$METADATA_PATH" "${SERVER_HOST}:${REMOTE_JSON}"

ssh "${SSH_OPTS[@]}" "$SERVER_HOST" bash -s -- \
    "$REMOTE_APK" "$REMOTE_JSON" "$REMOTE_RELEASE_DIR" "$VERSION_CODE" "$VERSION_NAME" \
    "$APK_SIZE" "$APK_SHA256_LOWER" "$APK_FILE" "$APK_URL" <<'REMOTE'
set -Eeuo pipefail
remote_apk=$1
remote_json=$2
remote_release_dir=$3
version_code=$4
version_name=$5
expected_size=$6
expected_sha=$7
apk_file=$8
expected_url=$9

test -s "$remote_apk"
test -s "$remote_json"
test "$(stat -c '%s' "$remote_apk")" = "$expected_size"
test "$(sha256sum "$remote_apk" | awk '{print $1}')" = "$expected_sha"
jq -e --arg sha "$expected_sha" --argjson size "$expected_size" \
    --argjson versionCode "$version_code" --arg versionName "$version_name" \
    --arg expectedUrl "$expected_url" \
    '(.versionCode == $versionCode) and (.versionName == $versionName) and
     (.apkSize == $size) and (.apkSha256 == $sha) and (.apkUrl == $expectedUrl) and
     (has("voiceModel")|not) and (has("modelUrl")|not) and (has("tokensUrl")|not) and
     ((tostring|test("sk-|Authorization|password|private.key";"i"))|not)' \
    "$remote_json" >/dev/null

test ! -e "$remote_release_dir"
if [ -f /srv/calorieai-updates/public/android/stable/latest.json ]; then
    current_version=$(jq -r '.versionCode // 0' /srv/calorieai-updates/public/android/stable/latest.json)
    test "$version_code" -gt "$current_version"
fi
install -d -o root -g root -m 0755 "$remote_release_dir"
mv "$remote_apk" "$remote_release_dir/$apk_file"
chmod 0644 "$remote_release_dir/$apk_file"
mv "$remote_json" /srv/calorieai-updates/public/android/stable/latest.json
chmod 0644 /srv/calorieai-updates/public/android/stable/latest.json
test "$(sha256sum "$remote_release_dir/$apk_file" | awk '{print $1}')" = "$expected_sha"
REMOTE

echo "published: $APK_URL"
