#!/usr/bin/env bash
set -Eeuo pipefail

# Publish one immutable APK version and atomically replace stable metadata.
# The script never changes Nginx and retains the two newest release directories.

usage() {
    cat >&2 <<'USAGE'
Usage:
  publish_android_update.sh --check APK VERSION_CODE VERSION_NAME
  publish_android_update.sh APK VERSION_CODE VERSION_NAME

Environment for a remote publish:
  UPDATE_SERVER_KEY   SSH private-key path
  UPDATE_SERVER_HOST  SSH target, default ubuntu@124.222.153.108
  UPDATE_CHECK_URL    manifest URL, default https://calorieai.sxyq27.online/android/stable/latest.json
  UPDATE_DOWNLOAD_BASE_URL
                      APK base URL, default https://calorieai.sxyq27.online
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
UPDATE_CHECK_URL=${UPDATE_CHECK_URL:-"https://calorieai.sxyq27.online/android/stable/latest.json"}
UPDATE_DOWNLOAD_BASE_URL=${UPDATE_DOWNLOAD_BASE_URL:-"https://calorieai.sxyq27.online"}
UPDATE_DOWNLOAD_BASE_URL=${UPDATE_DOWNLOAD_BASE_URL%/}

case "$UPDATE_CHECK_URL" in
    "https://calorieai.sxyq27.online/android/stable/latest.json") ;;
    *) echo "unsupported UPDATE_CHECK_URL: $UPDATE_CHECK_URL" >&2; exit 1 ;;
esac
case "$UPDATE_DOWNLOAD_BASE_URL" in
    "https://calorieai.sxyq27.online") ;;
    *) echo "unsupported UPDATE_DOWNLOAD_BASE_URL: $UPDATE_DOWNLOAD_BASE_URL" >&2; exit 1 ;;
esac
if [[ "$UPDATE_CHECK_URL" != "https://calorieai.sxyq27.online/android/stable/latest.json" ||
      "$UPDATE_DOWNLOAD_BASE_URL" != "https://calorieai.sxyq27.online" ]]; then
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
SERVER_HOST=${UPDATE_SERVER_HOST:-ubuntu@124.222.153.108}
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
REMOTE_UPLOAD_TOKEN="calorieai-publish-${VERSION_CODE}-${VERSION_NAME}-$$"
REMOTE_UPLOAD_APK="/tmp/${REMOTE_UPLOAD_TOKEN}-${APK_FILE}.part"
REMOTE_UPLOAD_JSON="/tmp/${REMOTE_UPLOAD_TOKEN}-latest.json.part"
REMOTE_STAGING_APK="${REMOTE_STAGING}/${REMOTE_UPLOAD_TOKEN}-${APK_FILE}.part"
REMOTE_STAGING_JSON="${REMOTE_STAGING}/${REMOTE_UPLOAD_TOKEN}-latest.json.part"

scp -q "${SSH_OPTS[@]}" "$APK_PATH" "${SERVER_HOST}:${REMOTE_UPLOAD_APK}"
scp -q "${SSH_OPTS[@]}" "$METADATA_PATH" "${SERVER_HOST}:${REMOTE_UPLOAD_JSON}"

ssh "${SSH_OPTS[@]}" "$SERVER_HOST" bash -s -- \
    "$REMOTE_UPLOAD_APK" "$REMOTE_UPLOAD_JSON" "$REMOTE_STAGING_APK" "$REMOTE_STAGING_JSON" \
    "$REMOTE_RELEASE_DIR" "$VERSION_CODE" "$VERSION_NAME" "$APK_SIZE" "$APK_SHA256_LOWER" \
    "$APK_FILE" "$APK_URL" <<'REMOTE'
set -Eeuo pipefail
remote_upload_apk=$1
remote_upload_json=$2
remote_staging_apk=$3
remote_staging_json=$4
remote_release_dir=$5
version_code=$6
version_name=$7
expected_size=$8
expected_sha=$9
apk_file=${10}
expected_url=${11}

stable_root=/srv/calorieai-updates/public/android/stable
release_root=/srv/calorieai-updates/public/releases
remote_staging=/srv/calorieai-updates/staging

cleanup_uploads() {
    rm -f -- "$remote_upload_apk" "$remote_upload_json"
}
trap cleanup_uploads EXIT

test -s "$remote_upload_apk"
test -s "$remote_upload_json"
sudo install -d -o root -g root -m 0755 "$stable_root" "$release_root" "$remote_staging"
sudo install -o root -g root -m 0644 "$remote_upload_apk" "$remote_staging_apk"
sudo install -o root -g root -m 0644 "$remote_upload_json" "$remote_staging_json"
test "$(stat -c '%s' "$remote_staging_apk")" = "$expected_size"
test "$(sha256sum "$remote_staging_apk" | awk '{print $1}')" = "$expected_sha"
jq -e --arg sha "$expected_sha" --argjson size "$expected_size" \
    --argjson versionCode "$version_code" --arg versionName "$version_name" \
    --arg expectedUrl "$expected_url" \
    '(.versionCode == $versionCode) and (.versionName == $versionName) and
     (.apkSize == $size) and (.apkSha256 == $sha) and (.apkUrl == $expectedUrl) and
     (has("voiceModel")|not) and (has("modelUrl")|not) and (has("tokensUrl")|not) and
     ((tostring|test("sk-|Authorization|password|private.key";"i"))|not)' \
    "$remote_staging_json" >/dev/null

if sudo test -f "$stable_root/latest.json"; then
    current_version=$(sudo jq -r '.versionCode // 0' "$stable_root/latest.json")
    if [ "$version_code" -lt "$current_version" ]; then
        echo "versionCode must increase beyond current manifest" >&2
        exit 1
    fi
    if [ "$version_code" -eq "$current_version" ]; then
        current_manifest=$(sudo jq -S -c . "$stable_root/latest.json")
        staged_manifest=$(sudo jq -S -c . "$remote_staging_json")
        test "$current_manifest" = "$staged_manifest"
    fi
fi
sudo install -d -o root -g root -m 0755 "$remote_release_dir"
release_apk="$remote_release_dir/$apk_file"
if sudo test -e "$release_apk"; then
    sudo test -f "$release_apk"
    test "$(sudo stat -c '%s' "$release_apk")" = "$expected_size"
    test "$(sudo sha256sum "$release_apk" | awk '{print $1}')" = "$expected_sha"
    sudo rm -f -- "$remote_staging_apk"
else
    sudo mv "$remote_staging_apk" "$release_apk"
    sudo chmod 0644 "$release_apk"
fi
test "$(sudo sha256sum "$release_apk" | awk '{print $1}')" = "$expected_sha"
sudo mv "$remote_staging_json" "$stable_root/latest.json"
sudo chmod 0644 "$stable_root/latest.json"
test "$(sudo jq -r '.apkSha256' "$stable_root/latest.json")" = "$expected_sha"

# Keep only the two newest semantic-version directories. The manifest version
# has already been checked to increase, so the active release cannot be removed.
mapfile -t release_versions < <(
    sudo find "$release_root" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' |
        awk '/^[0-9]+\.[0-9]+\.[0-9]+$/' | sort -V
)
delete_count=$(( ${#release_versions[@]} - 2 ))
if (( delete_count > 0 )); then
    for ((index = 0; index < delete_count; index++)); do
        old_version=${release_versions[$index]}
        test "$old_version" != "$version_name"
        sudo rm -rf -- "$release_root/$old_version"
    done
fi
REMOTE

echo "published: $APK_URL"
