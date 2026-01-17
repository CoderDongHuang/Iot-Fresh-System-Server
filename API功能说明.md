# 物联网生鲜品储运系统 - API功能说明

## 已完成功能

### 1. 用户管理功能 (UserController)

#### 1.1 获取用户信息
- **路径**: `GET /api/user/profile`
- **功能**: 获取当前登录用户的基本信息
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "department": "tech",
    "position": "系统管理员",
    "avatar": "https://example.com/avatar.jpg"
  }
}
```

#### 1.2 更新用户信息
- **路径**: `PUT /api/user/profile`
- **功能**: 更新当前登录用户的信息
- **请求体**:
```json
{
  "username": "admin",
  "realName": "管理员",
  "email": "admin@example.com",
  "phone": "13800138000",
  "department": "tech",
  "position": "系统管理员"
}
```
- **返回格式**:
```json
{
  "code": 200,
  "msg": "用户信息更新成功"
}
```

#### 1.3 上传头像
- **路径**: `POST /api/user/avatar`
- **功能**: 上传用户头像
- **请求**: multipart/form-data 格式上传文件
- **返回格式**:
```json
{
  "code": 200,
  "msg": "头像上传成功",
  "data": {
    "avatarUrl": "https://example.com/new-avatar.jpg"
  }
}
```

#### 1.4 修改密码
- **路径**: `PUT /api/user/password`
- **功能**: 修改当前用户密码
- **请求体**:
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```
- **返回格式**:
```json
{
  "code": 200,
  "msg": "密码修改成功"
}
```

### 2. 系统设置功能 (SystemController)

#### 2.1 系统配置
- **路径**: `GET /api/system/config`
- **功能**: 获取系统基本配置信息
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "systemName": "物联网生鲜品储运系统",
    "description": "用于监控和管理物联网设备的综合平台",
    "defaultLanguage": "zh-CN",
    "timezone": "Asia/Shanghai"
  }
}
```

#### 2.2 保存系统配置
- **路径**: `POST /api/system/config`
- **功能**: 保存系统基本配置信息
- **请求体**:
```json
{
  "systemName": "物联网生鲜品储运系统",
  "description": "用于监控和管理物联网设备的综合平台",
  "defaultLanguage": "zh-CN",
  "timezone": "Asia/Shanghai"
}
```

#### 2.3 安全设置
- **路径**: `GET /api/system/security`
- **功能**: 获取安全设置信息
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "passwordMinLength": 8,
    "passwordComplexity": ["uppercase", "lowercase", "numbers"],
    "loginLockEnabled": true,
    "lockDuration": 30,
    "sessionTimeout": 30
  }
}
```

#### 2.4 通知设置
- **路径**: `GET /api/system/notification`
- **功能**: 获取通知设置信息
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "emailEnabled": true,
    "smsEnabled": false,
    "pushEnabled": true,
    "deviceAlarmChannels": ["email", "push"],
    "maintenanceChannels": ["email"],
    "dataAnomalyChannels": ["email", "sms"]
  }
}
```

#### 2.5 备份设置
- **路径**: `GET /api/system/backup`
- **功能**: 获取备份设置信息
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "autoBackupEnabled": true,
    "backupFrequency": "daily",
    "backupTime": "02:00",
    "retentionCount": 7,
    "backupPath": "/var/backups/iot-system"
  }
}
```

#### 2.6 执行手动备份
- **路径**: `POST /api/system/backup/manual`
- **功能**: 触发手动备份操作
- **返回格式**:
```json
{
  "code": 200,
  "msg": "备份任务已启动"
}
```

### 3. 数据查询功能 (DeviceDataController)

#### 3.1 历史数据查询
- **路径**: `GET /api/device/history-data`
- **功能**: 分页查询设备历史数据
- **参数**:
  - `pageNum`: 页码，默认为1
  - `pageSize`: 每页大小，默认为20
  - `vid`: 设备ID，可选
  - `dataType`: 数据类型，可选
  - `startTime`: 开始时间，格式为 "yyyy-MM-dd HH:mm:ss"
  - `endTime`: 结束时间，格式为 "yyyy-MM-dd HH:mm:ss"
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "vid": "DEV001",
        "tin": 20.5,
        "tout": 22.1,
        "lxin": 300,
        "brightness": 80,
        "vStatus": 1,
        "timestamp": "2023-12-01 10:30:45"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 4. 数据统计功能 (DashboardController)

#### 4.1 数据统计
- **路径**: `GET /api/dashboard/data-statistics`
- **功能**: 获取设备数据统计信息
- **参数**:
  - `vid`: 设备ID，可选，不传则统计所有设备
  - `statType`: 统计类型，默认为"comprehensive"
  - `startTime`: 开始时间，格式为 "yyyy-MM-dd"
  - `endTime`: 结束时间，格式为 "yyyy-MM-dd"
- **返回格式**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalRecords": 150,
    "avgTemp": 20.5,
    "maxTemp": 25.0,
    "minTemp": 15.0,
    "avgHumidity": 60.0,
    "avgLight": 300,
    "detail": [
      {
        "deviceName": "设备A",
        "vid": "DEV001",
        "avgTemp": 20.5,
        "maxTemp": 25.0,
        "minTemp": 15.0,
        "avgHumidity": 60.0,
        "avgLight": 300,
        "recordCount": 50,
        "timeRange": "2023-12-01 ~ 2023-12-31"
      }
    ]
  }
}
```

## 技术实现细节

### 1. 数据模型
- 使用JPA进行数据持久化
- 实现了完整的CRUD操作
- 支持分页查询和数据过滤

### 2. 安全性
- 集成Spring Security
- JWT身份验证
- 接口权限控制

### 3. 代码质量
- 所有Java文件都按照要求更新了作者信息为"donghuang"和年份为"2026"
- 遵循项目编码规范
- 统一的API响应格式

### 4. 性能优化
- 合理的数据库索引设计
- 分页查询避免大数据量加载
- 缓存策略优化

## 项目结构

```
src/main/java/com/iot/fresh/
├── controller/
│   ├── UserController.java
│   ├── SystemController.java
│   ├── DeviceDataController.java
│   └── DashboardController.java
├── service/
│   ├── UserService.java
│   └── impl/
│       └── UserServiceImpl.java
├── dto/
│   ├── UserDto.java
│   └── ApiResponse.java
└── repository/
    └── UserRepository.java
```