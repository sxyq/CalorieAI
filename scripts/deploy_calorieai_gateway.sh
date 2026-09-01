#!/usr/bin/env bash
set -Eeuo pipefail

# Deploy the single update/future-site container to the 124 edge. This script
# never touches existing containers and only reloads Nginx after nginx -t.

usage() {
    cat >&2 <<'USAGE'
Usage:
  deploy_calorieai_gateway.sh prepare
  deploy_calorieai_gateway.sh activate

Environment:
  UPDATE_SERVER_KEY   SSH private-key path (required)
  UPDATE_SERVER_HOST  SSH target, default ubuntu@124.222.153.108

prepare creates the service directories, installs the Docker context, and
starts only calorieai-gateway on 127.0.0.1:18090.
activate requires a valid Let's Encrypt certificate for
calorieai.sxyq27.online, installs the host Nginx proxy files, runs nginx -t,
and reloads Nginx. It does not stop or recreate any other service.
USAGE
    exit 2
}

[[ $# -eq 1 && ( "$1" == prepare || "$1" == activate ) ]] || usage
MODE=$1
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd -- "${SCRIPT_DIR}/.." && pwd)
SERVER_KEY=${UPDATE_SERVER_KEY:?UPDATE_SERVER_KEY is required}
SERVER_HOST=${UPDATE_SERVER_HOST:-ubuntu@124.222.153.108}
[[ -f "$SERVER_KEY" ]] || { echo "SSH key not found" >&2; exit 1; }

SSH_OPTS=(
    -i "$SERVER_KEY"
    -o IdentitiesOnly=yes
    -o BatchMode=yes
    -o ConnectTimeout=8
    -o StrictHostKeyChecking=accept-new
)

LOCAL_CONTEXT="${REPO_ROOT}/deploy/calorieai-gateway"
REMOTE_CONTEXT="/tmp/calorieai-gateway-deploy-$$"
REMOTE_NGINX_HOST="/tmp/calorieai-gateway-nginx-$$.conf"
REMOTE_NGINX_LIMIT="/tmp/calorieai-gateway-limit-$$.conf"

cleanup() {
    ssh "${SSH_OPTS[@]}" "$SERVER_HOST" "rm -rf -- '$REMOTE_CONTEXT' '$REMOTE_NGINX_HOST' '$REMOTE_NGINX_LIMIT'" >/dev/null 2>&1 || true
}
trap cleanup EXIT

ssh "${SSH_OPTS[@]}" "$SERVER_HOST" "mkdir -p -- '$REMOTE_CONTEXT'"
scp -q -r "${SSH_OPTS[@]}" "$LOCAL_CONTEXT/." "${SERVER_HOST}:${REMOTE_CONTEXT}/"
scp -q "${SSH_OPTS[@]}" "${REPO_ROOT}/scripts/nginx/calorieai-updates.conf" "${SERVER_HOST}:${REMOTE_NGINX_HOST}"
scp -q "${SSH_OPTS[@]}" "${REPO_ROOT}/scripts/nginx/calorieai-updates-limit.conf" "${SERVER_HOST}:${REMOTE_NGINX_LIMIT}"

ssh "${SSH_OPTS[@]}" "$SERVER_HOST" bash -s -- \
    "$MODE" "$REMOTE_CONTEXT" "$REMOTE_NGINX_HOST" "$REMOTE_NGINX_LIMIT" <<'REMOTE'
set -Eeuo pipefail
mode=$1
remote_context=$2
remote_nginx_host=$3
remote_nginx_limit=$4

app_root=/opt/calorieai-gateway
public_root=/srv/calorieai-updates/public
stable_root=$public_root/android/stable
release_root=$public_root/releases
staging_root=/srv/calorieai-updates/staging
web_root=/srv/calorieai-web

sudo install -d -o root -g root -m 0755 \
    "$app_root" "$public_root" "$stable_root" "$release_root" "$staging_root" "$web_root"
sudo install -d -o root -g root -m 0755 "$web_root"
sudo install -d -o root -g root -m 0755 "$app_root/web"
sudo find "$remote_context" -maxdepth 1 -type f -exec install -o root -g root -m 0644 {} "$app_root/" \;
sudo find "$remote_context/web" -maxdepth 1 -type f -exec install -o root -g root -m 0644 {} "$app_root/web/" \;

sudo docker compose -f "$app_root/docker-compose.yml" up -d --build calorieai-gateway
sudo docker inspect --format '{{.State.Status}} {{if .State.Health}}{{.State.Health.Status}}{{end}}' calorieai-gateway
curl --fail --silent --show-error --max-time 10 http://127.0.0.1:18090/healthz

if [[ "$mode" == activate ]]; then
    cert=/etc/letsencrypt/live/calorieai.sxyq27.online/fullchain.pem
    key=/etc/letsencrypt/live/calorieai.sxyq27.online/privkey.pem
    sudo test -s "$cert"
    sudo test -s "$key"
    sudo install -o root -g root -m 0644 "$remote_nginx_host" /etc/nginx/conf.d/calorieai-updates.conf
    sudo install -o root -g root -m 0644 "$remote_nginx_limit" /etc/nginx/conf.d/calorieai-updates-limit.conf
    sudo nginx -t
    sudo systemctl reload nginx
fi
REMOTE
