# 物联网生鲜品储运系统

## 项目简介

物联网生鲜品储运系统是一个基于Spring Boot的现代化物联网管理系统，专门为生鲜品储运场景设计。系统提供完整的设备监控、数据采集、报警管理和统计分析功能，确保生鲜品在储运过程中的环境参数得到实时监控和有效管理。

## 主要特性

### 🚀 核心功能
- **设备管理**: 完整的设备生命周期管理，支持在线、离线、故障、维护四种状态
- **实时监控**: 实时采集和展示温度、湿度、光照等环境参数
- **报警管理**: 多级别报警系统，支持报警确认、处理和统计分析
- **仪表盘**: 直观的数据统计和可视化展示
- **用户管理**: 完整的用户认证和权限控制

### 🔧 技术特点
- **现代化架构**: 基于Spring Boot 3.2.0，采用分层架构设计
- **高性能**: 优化的数据库设计和索引策略
- **安全性**: JWT身份验证和Spring Security权限控制
- **可扩展性**: 模块化设计，便于功能扩展和系统集成
- **实时性**: 支持MQTT协议实时数据采集

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **消息队列**: MQTT (设备通信)
- **缓存**: Redis
- **构建工具**: Maven
- **Java版本**: 17

### 前端技术
- **框架**: Vue.js 3
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **构建工具**: Vite

## 项目结构

```
Iot-fresh-2022/
├── src/main/java/com/iot/fresh/
│   ├── controller/           # 控制器层
│   ├── dto/                 # 数据传输对象
│   ├── entity/              # 实体类
│   ├── repository/          # 数据访问层
│   ├── service/             # 服务层接口
│   ├── service/impl/        # 服务层实现
│   ├── config/              # 配置类
│   ├── security/            # 安全相关
│   └── util/                # 工具类
├── src/main/resources/      # 资源文件
│   ├── application.yml      # 应用配置
│   └── static/              # 静态资源
├── docs/                    # 文档目录
│   ├── API功能说明.md       # API接口文档
│   ├── 系统架构说明.md      # 系统架构文档
│   ├── 数据库设计说明.md    # 数据库设计文档
│   └── 后端与硬件.md        # 硬件集成文档
└── README.md               # 项目说明文档
```

## 快速开始

### 环境要求
- Java 17+
- MySQL 8.0+
- Maven 3.6+
- Redis 6.0+

### 数据库配置
1. 创建数据库：`CREATE DATABASE iot_fresh;`
2. 导入表结构（参考数据库设计说明.md）

### 应用配置
修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/iot_fresh?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password

mqtt:
  broker-url: tcp://localhost:1883
  username: mqtt_user
  password: mqtt_password

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000
```

### 启动应用
```bash
# 克隆项目
git clone <repository-url>
cd Iot-fresh-2022

# 构建项目
mvn clean package

# 运行应用
java -jar target/iot-fresh-2022.jar
```

应用启动后访问：http://localhost:8080

## API接口

### 设备管理接口
- `GET /api/device/list` - 获取设备列表
- `POST /api/device/add` - 添加设备
- `PUT /api/device/update` - 更新设备信息
- `DELETE /api/device/delete/{id}` - 删除设备
- `GET /api/device/{vid}` - 获取设备详情

### 实时数据接口
- `GET /api/device/data/recent/{vid}` - 获取设备最新数据
- `GET /api/device/data/history` - 获取设备历史数据
- `GET /api/device/real-time-status` - 获取设备实时状态

### 报警管理接口
- `GET /api/alarm/list` - 获取报警列表
- `POST /api/alarm/acknowledge` - 确认报警
- `POST /api/alarm/resolve` - 解决报警
- `GET /api/alarm/statistics` - 报警统计

### 仪表盘接口
- `GET /api/dashboard/statistics` - 获取统计数据
- `GET /api/dashboard/device-status` - 获取设备状态分布
- `GET /api/dashboard/data-trend` - 获取数据趋势

### 用户管理接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/user/profile` - 获取用户信息
- `PUT /api/user/update` - 更新用户信息

详细API文档请参考 [API功能说明.md](docs/API功能说明.md)

## 核心功能说明

### 设备状态管理
系统支持四种设备状态：
- **在线 (1)**: 设备正常运行，能够接收和发送数据
- **离线 (0)**: 设备无法连接，超过心跳时间阈值
- **故障 (2)**: 设备出现硬件或软件故障
- **维护 (3)**: 设备处于维护状态，暂停服务

### 实时数据监控
系统实时监控以下环境参数：
- **温度**: 内部温度和外部温度（摄氏度）
- **湿度**: 内部湿度和外部湿度（百分比）
- **光照强度**: 内部和外部光照强度（勒克斯）
- **亮度**: 相对亮度值
- **设备运行状态**: 运行或停止

### 报警系统
- **报警级别**: 低、中、高、危急四个级别
- **报警状态**: 待处理、已确认、已解决、已忽略
- **报警类型**: 温度异常、湿度异常、光照异常等
- **处理流程**: 报警触发 → 确认 → 处理 → 关闭

## 数据库设计

系统包含以下核心数据表：
- **devices**: 设备信息表
- **device_data**: 设备数据表
- **alarms**: 报警记录表
- **users**: 用户信息表

详细数据库设计请参考 [数据库设计说明.md](docs/数据库设计说明.md)

## 系统架构

系统采用分层架构设计：
- **控制器层**: 处理HTTP请求和响应
- **服务层**: 业务逻辑处理
- **数据访问层**: 数据库操作
- **实体层**: 数据模型定义

详细架构说明请参考 [系统架构说明.md](docs/系统架构说明.md)

## 部署说明

### 开发环境部署
1. 配置开发环境数据库
2. 修改应用配置文件
3. 启动Redis服务
4. 启动MQTT Broker
5. 运行应用

### 生产环境部署
1. 配置生产环境数据库
2. 设置环境变量
3. 配置反向代理（Nginx）
4. 设置SSL证书
5. 配置监控和日志

## 开发指南

### 代码规范
- 遵循Java编码规范
- 使用有意义的命名
- 添加必要的注释
- 保持代码简洁清晰

### 测试
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

### 代码检查
```bash
# 代码格式检查
mvn checkstyle:check

# 静态代码分析
mvn spotbugs:check
```

## 故障排除

### 常见问题
1. **数据库连接失败**: 检查数据库配置和网络连接
2. **Redis连接失败**: 检查Redis服务状态和配置
3. **MQTT连接失败**: 检查MQTT Broker状态和配置
4. **JWT验证失败**: 检查JWT密钥配置

### 日志查看
应用日志文件位于：`logs/application.log`

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 版本历史

- **v1.0.0** (2024-01-15)
  - 初始版本发布
  - 基础设备管理功能
  - 实时数据监控
  - 报警管理
  - 仪表盘统计

## 许可证

本项目采用 MIT 许可证。详情请查看 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目维护者: [Your Name]
- 邮箱: your.email@example.com
- 项目地址: [GitHub Repository URL]

## 致谢

感谢所有为项目做出贡献的开发者和测试人员。