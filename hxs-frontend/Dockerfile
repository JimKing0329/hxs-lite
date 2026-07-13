# 打包阶段
FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Nginx运行阶段
FROM nginx:stable-alpine
# 覆盖默认nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 拷贝React打包后的build目录
COPY --from=build-stage /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
