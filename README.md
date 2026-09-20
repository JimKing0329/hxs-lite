# 河小狮 (hxs-lite)

河北师范大学教务信息助手系统，为学生提供便捷的教务信息查询服务，累计服务**18000+** 同学。
<img width="2000" height="393" alt="image" src="https://github.com/user-attachments/assets/46031676-c908-487c-bc57-dd334f2c87a3" />


> 关注微信服务号 **河小狮lite** 或直接访问[河小狮lite](http://82.156.49.70/dashboard)可直接体验已有功能

## 功能特性

- **课程表查询**：支持周课程、学期课程查看
- **成绩查询**：所有成绩查看、GPA 计算、挂科率排名
- **空教室查询**：实时查询空闲教室
- **教材查询**：课程教材信息查询
- **执行计划**：教学计划查询
- **微信公众号集成**：通过公众号进行和息查询
- **管理后台**：管理员数据维护和系统配置

## 技术栈

### 后端
- Java 17
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3
- MySQL 8.0
- JWT 认证
- HttpClient + Jsoup（教务系统数据抓取）
- XStream + dom4j（微信消息处理）

### 前端
- React 18
- Ant Design 5
- React Router 5
- Axios

### 运维部署
- Docker + Docker Compose
- Jenkins CI/CD
- Nginx（反向代理）

## 项目结构

```
hxs-lite/
├── hxs-backend/              # Spring Boot 后端服务
│   ├── src/main/java/com/hxs/
│   │   ├── client/          # 教务系统爬虫客户端
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器（user/admin/wechat）
│   │   ├── service/         # 业务逻辑层
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── model/           # 数据模型（entity/dto/vo）
│   │   ├── interceptor/     # 拦截器（JWT 认证）
│   │   ├── handler/         # 全局异常处理
│   │   ├── utils/           # 工具类
│   │   └── properties/      # 配置属性类
│   └── pom.xml
├── hxs-frontend/            # React 前端应用
│   ├── src/
│   │   ├── components/      # 公共组件
│   │   ├── pages/           # 页面组件
│   │   ├── utils/           # 工具函数
│   │   └── constants/       # 常量配置
│   └── package.json
└── docker/                  # Docker 相关配置
    ├── Dockerfile.backend
    ├── Dockerfile.frontend
    └── docker-compose.yml
```

## 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+ & npm
- MySQL 8.0+
- Docker 20.10+（容器化部署时需要）
- Docker Compose 2.0+（容器化部署时需要）

## 安装与运行

### 方式一：本地开发环境

#### 1. 后端启动

```bash
cd hxs-backend

# 配置数据库连接（修改 application.yml）
# 配置微信公众号参数（如有需要）

# 编译并启动
mvn clean install
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

#### 2. 前端启动

```bash
cd hxs-frontend

# 安装依赖
npm install

# 启动开发服务器
npm start
```

前端服务默认运行在 `http://localhost:3000`

#### 3. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE hxs_lite DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入数据表结构（执行提供的 SQL 脚本）
```

### 方式二：Docker 容器化部署

#### 1. 构建镜像

```bash
# 构建后端镜像
docker build -t hxs-backend:latest -f docker/Dockerfile.backend .

# 构建前端镜像
docker build -t hxs-frontend:latest -f docker/Dockerfile.frontend .
```

#### 2. 使用 Docker Compose 一键部署

```bash
cd docker
docker-compose up -d
```

`docker-compose.yml` 示例配置：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: hxs-mysql
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: hxs_lite
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - hxs-network

  backend:
    image: hxs-backend:latest
    container_name: hxs-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/hxs_lite?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: your_password
      JWT_SECRET: your_jwt_secret
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    networks:
      - hxs-network

  frontend:
    image: hxs-frontend:latest
    container_name: hxs-frontend
    environment:
      REACT_APP_API_BASE_URL: http://your-domain.com/api
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - hxs-network

  nginx:
    image: nginx:alpine
    container_name: hxs-nginx
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
      - frontend
    networks:
      - hxs-network

volumes:
  mysql_data:

networks:
  hxs-network:
    driver: bridge
```

#### 3. Dockerfile 示例

**Dockerfile.backend**:
```dockerfile
FROM maven:3.8-openjdk-17-slim AS builder
WORKDIR /app
COPY hxs-backend/pom.xml .
COPY hxs-backend/src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Dockerfile.frontend**:
```dockerfile
FROM node:16-alpine AS builder
WORKDIR /app
COPY hxs-frontend/package*.json ./
RUN npm install
COPY hxs-frontend/ .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## CI/CD 部署（Jenkins）

### Jenkins Pipeline 示例

```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        BACKEND_IMAGE = 'hxs-backend'
        FRONTEND_IMAGE = 'hxs-frontend'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('hxs-backend') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('hxs-frontend') {
                    bat 'npm install'
                    bat 'npm run build'
                }
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                script {
                    def backendImage = docker.build("${DOCKER_REGISTRY}/${BACKEND_IMAGE}:${BUILD_NUMBER}", "-f docker/Dockerfile.backend .")
                    def frontendImage = docker.build("${DOCKER_REGISTRY}/${FRONTEND_IMAGE}:${BUILD_NUMBER}", "-f docker/Dockerfile.frontend .")
                    
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials-id') {
                        backendImage.push()
                        backendImage.push('latest')
                        frontendImage.push()
                        frontendImage.push('latest')
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sshagent(['ssh-credentials-id']) {
                    sh '''
                        ssh user@server 'cd /path/to/project && docker-compose pull && docker-compose up -d'
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
```

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hxs_lite?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000  # 24小时

wechat:
  appid: your_wechat_appid
  secret: your_wechat_secret
  token: your_wechat_token

admin:
  username: admin
  password: admin_password

edu:
  base-url: http://jwxt.hebtu.edu.cn
```

### 前端配置

在 `hxs-frontend/src/constants/api.js` 中配置 API 地址：

```javascript
export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
```

## Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        proxy_pass http://frontend:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 微信回调
    location /wechat/ {
        proxy_pass http://backend:8080/wechat/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 测试

### 后端测试

```bash
cd hxs-backend
mvn test
```

### 前端测试

```bash
cd hxs-frontend
npm test
```

## 常见问题

**Q: 教务系统登录失败怎么办？**
A: 可能是教务系统临时维护或账号密码错误，请稍后重试或检查账号密码。

**Q: 数据多久同步一次？**
A: 系统会根据需要实时抓取教务系统数据，部分数据会定时同步。

**Q: 如何在微信公众号中使用？**
A: 关注"河小狮lite"服务号，绑定教务系统账号后即可使用各项功能。




## 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [React](https://reactjs.org/)
- [Ant Design](https://ant.design/)
