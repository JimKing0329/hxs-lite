# Nginx运行阶段
FROM nginx:stable-alpine
# 覆盖nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 拷贝本地已构建好的 build 目录
COPY build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
