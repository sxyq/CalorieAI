# 124 uses the existing DaoCloud mirror for Docker Hub pulls.
FROM m.daocloud.io/docker.io/library/nginx:1.27-alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY web/ /calorieai/web/

RUN mkdir -p \
        /calorieai/public/android/stable \
        /calorieai/public/releases \
        /calorieai/staging \
        /var/cache/nginx \
        /var/run \
        /tmp \
    && rm -f /etc/nginx/conf.d/default.conf \
    && chown -R nginx:nginx /var/cache/nginx /var/run /tmp

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q -O - http://127.0.0.1:8080/healthz | grep -qx 'ok'
