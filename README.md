# IoT Fresh 2022 - 智能鲜品物联网系统

## 项目概述

这是一个基于Spring Boot的物联网系统，用于监控和管理鲜品存储设备。系统支持实时数据监控、历史数据查询、设备控制、报警管理等功能。

## 技术栈

- **核心框架**: Spring Boot 3.x
- **安全认证**: Spring Security + JWT
- **数据访问**: Spring Data JPA
- **数据库**: MySQL 8.0+
- **缓存**: Spring Cache + Redis
- **消息队列**: MQTT (Paho Client)
- **实时通信**: WebSocket
- **API文档**: Swagger/OpenAPI
- **构建工具**: Maven

## 项目结构

```
iot-backend/
├── src/main/java/com/iot/fresh/
│   ├── IotFreshApplication.java          # 主启动类
│   ├── config/                          # 配置类
│   │   ├── SecurityConfig.java          # 安全配置
│   │   ├── WebSocketConfig.java         # WebSocket配置
│   │   ├── JpaConfig.java               # JPA审计配置
│   │   └── MqttConfig.java              # MQTT配置
│   ├── controller/                      # 控制器层
│   │   ├── AuthController.java          # 认证控制器
│   │   ├── DeviceController.java        # 设备控制器
│   │   ├── DataController.java          # 数据控制器
│   │   ├── AlarmController.java         # 报警控制器
│   │   └── DashboardController.java     # 仪表盘控制器
│   ├── service/                         # 服务层
│   │   ├── AuthService.java             # 认证服务接口
│   │   ├── DeviceService.java           # 设备服务接口
│   │   ├── DataService.java             # 数据服务接口
│   │   ├── AlarmService.java            # 报警服务接口
│   │   ├── DashboardService.java        # 仪表盘服务接口
│   │   ├── DeviceDataProcessor.java     # 设备数据处理器
│   │   └── impl/                        # 服务实现类
│   ├── repository/                      # 数据访问层
│   │   ├── UserRepository.java          # 用户数据访问
│   │   ├── DeviceRepository.java        # 设备数据访问
│   │   ├── DeviceDataRepository.java    # 设备数据访问
│   │   └── AlarmRepository.java         # 报警数据访问
│   ├── entity/                          # 实体类
│   │   ├── User.java                    # 用户实体
│   │   ├── Device.java                  # 设备实体
│   │   ├── DeviceData.java              # 设备数据实体
│   │   └── Alarm.java                   # 报警实体
│   ├── dto/                             # 数据传输对象
│   │   ├── LoginRequest.java            # 登录请求
│   │   ├── DeviceDataDto.java           # 设备数据传输对象
│   │   ├── ApiResponse.java             # API响应封装
│   │   └── ...                          # 其他DTO
│   ├── websocket/                       # WebSocket相关
│   │   └── DeviceWebSocket.java         # 设备WebSocket处理器
│   └── util/                            # 工具类
│       └── JwtUtil.java                 # JWT工具类
├── src/main/resources/
│   ├── application.yml                  # 应用配置
└── pom.xml                              # 项目依赖配置
```

## 核心功能

### 1. 设备管理
- 设备注册、查询、更新
- 设备状态监控
- 设备心跳检测

### 2. 数据处理
- 实时数据接收和存储
- 历史数据查询
- 数据可视化支持

### 3. 报警管理
- 实时报警检测
- 报警状态管理
- 报警处理跟踪

### 4. 用户认证
- JWT无状态认证
- 用户权限管理
- 安全访问控制

### 5. 实时通信
- WebSocket实时数据推送
- MQTT设备通信
- 消息广播功能

## API接口

### 认证API
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取用户信息

### 设备API
- `GET /api/devices` - 获取设备列表
- `GET /api/devices/{vid}` - 获取设备详情
- `GET /api/devices/{vid}/real-time` - 获取设备实时数据
- `POST /api/devices/{vid}/control` - 控制设备

### 数据API
- `GET /api/data/history` - 获取历史数据
- `GET /api/data/temperature/{vid}` - 获取温度历史数据

### 报警API
- `GET /api/alarms` - 获取报警列表
- `POST /api/alarms/{id}/handle` - 处理报警

### 仪表盘API
- `GET /api/dashboard/statistics` - 获取统计信息
- `GET /api/dashboard/devices/status` - 获取设备状态分布

## MQTT主题设计

### 数据上报主题（硬件→后端）
- `device/{vid}/data` - 实时数据上报
- `device/{vid}/alarm` - 报警上报
- `device/{vid}/rfid` - RFID读取上报
- `device/{vid}/status` - 设备状态上报

### 控制指令主题（后端→硬件）
- `device/{vid}/control/temperature` - 温度控制
- `device/{vid}/control/light` - 光照控制
- `device/{vid}/control/fan` - 风机控制

## 配置要求

### 配置文件说明
项目使用Spring Profiles进行环境配置管理：

- `application.yml` - 主配置文件，包含公共配置
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置

### 开发环境配置 (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/iot_fresh?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 
    driver-class-name: com.mysql.cj.jdbc.Driver

# MQTT配置
mqtt:
  broker: tcp://localhost:1883
  username: 
  password: 
  client-id: iot-fresh-backend
  qos: 1
```

### 生产环境配置 (application-prod.yml)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:mysql://prod-db:3306/iot_fresh?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai}
    username: ${DATABASE_USERNAME:iot_user}
    password: ${DATABASE_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver

# MQTT配置
mqtt:
  broker: ${MQTT_BROKER:tcp://prod-mqtt:1883}
  username: ${MQTT_USERNAME:}
  password: ${MQTT_PASSWORD:}
  client-id: iot-fresh-backend-prod
  qos: 1

# JWT配置
jwt:
  secret: ${JWT_SECRET:prodSecretKeyForIotFresh2022Project}
  expiration: ${JWT_EXPIRATION:604800} # 7天
```

## 启动说明

1. 确保MySQL数据库已启动并创建`iot_fresh`数据库
2. 修改`application.yml`中的数据库连接信息
3. 运行主类`IotFreshApplication`启动应用

## 安全考虑

- 使用JWT实现无状态认证
- 实现RBAC权限控制
- API访问频率限制
- 敏感数据加密存储
- HTTPS传输（生产环境）

## 缓存功能

系统使用Spring Cache + Redis实现缓存功能：

- 设备信息缓存：缓存设备信息，提高查询性能
- 设备数据缓存：缓存设备实时数据
- 缓存策略：更新数据时自动清除相关缓存

## API文档

系统集成了Swagger/OpenAPI进行API文档管理：

- 访问地址：`http://localhost:8080/swagger-ui.html`
- API文档：`http://localhost:8080/v3/api-docs`

## 扩展功能

- 集成InfluxDB进行时序数据存储
- 集成消息队列进行异步处理
- 添加监控和日志分析功能