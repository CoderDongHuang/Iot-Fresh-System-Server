# 物联网生鲜品储运系统 - API功能说明

## 已完成功能

### 1. 设备管理功能 (DeviceController)

#### 1.1 新增设备
- **路径**: `POST /api/device/add`
- **功能**: 新增设备到系统
- **请求体**:
```json
{
  "vid": "device001",
  "deviceName": "设备名称",
  "deviceType": "storage",
  "location": "A区",
  "description": "设备描述",
  "manufacturer": "海康威视",
  "model": "DS-2CD3T45D-I5",
  "firmwareVersion": "V1.0.0",
  "contactPhone": "13800138000",
  "ipAddress": "192.168.1.1",
  "macAddress": "00:00:00:00:00:00",
  "status": "online"
}
```
- **返回格式**:
```json
{
  "code": 200,
  "msg": "设备添加成功",
  "data": {},
  "success": true
}
```

#### 1.2 查询设备详情
- **路径**: `GET /api/device/{vid}`
- **功能**: 根据设备VID查询设备详情
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "id": 1,
    "vid": "device001",
    "deviceName": "设备名称",
    "deviceType": "storage",
    "status": 1,
    "location": "A区",
    "description": "设备描述",
    "manufacturer": "海康威视",
    "model": "DS-2CD3T45D-I5",
    "firmwareVersion": "V1.0.0",
    "ipAddress": "192.168.1.1",
    "macAddress": "00:00:00:00:00:00",
    "lastOnlineTime": "2024-01-15T10:30:25",
    "createTime": "2024-01-01T00:00:00"
  },
  "success": true
}
```

#### 1.3 获取设备详情（扩展）
- **路径**: `GET /api/device/detail/{vid}`
- **功能**: 获取设备的详细信息和实时数据
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "vid": "device001",
    "deviceName": "设备名称",
    "deviceType": "storage",
    "status": 1,
    "location": "A区",
    "description": "设备描述",
    "manufacturer": "海康威视",
    "model": "DS-2CD3T45D-I5",
    "firmwareVersion": "V1.0.0",
    "ipAddress": "192.168.1.1",
    "macAddress": "00:00:00:00:00:00",
    "lastOnlineTime": "2024-01-15T10:30:25",
    "createTime": "2024-01-01T00:00:00"
  },
  "success": true
}
```

#### 1.4 查询设备历史数据
- **路径**: `GET /api/device/{vid}/history-data`
- **功能**: 分页查询设备历史数据
- **参数**:
  - `pageNum`: 页码，默认为1
  - `pageSize`: 每页大小，默认为20
  - `startTime`: 开始时间，格式为 "yyyy-MM-dd HH:mm:ss"
  - `endTime`: 结束时间，格式为 "yyyy-MM-dd HH:mm:ss"
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "vid": "device001",
        "tin": 20.5,
        "tout": 22.1,
        "hin": 60,
        "hout": 55,
        "lxin": 300,
        "lxout": 250,
        "brightness": 80,
        "vStatus": 1,
        "timestamp": "2024-01-15 10:30:45"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  },
  "success": true
}
```

#### 1.5 获取设备列表
- **路径**: `GET /api/device/list`
- **功能**: 分页获取设备列表
- **参数**:
  - `pageNum`: 页码，默认为1
  - `pageSize`: 每页大小，默认为10
  - `keyword`: 搜索关键词，可选
  - `status`: 设备状态，可选（online/offline/error/maintenance）
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "list": [
      {
        "vid": "device001",
        "deviceName": "设备名称",
        "deviceType": "storage",
        "status": 1,
        "location": "A区",
        "manufacturer": "海康威视",
        "model": "DS-2CD3T45D-I5",
        "firmwareVersion": "V1.0.0",
        "lastOnlineTime": "2024-01-15T10:30:25",
        "description": "设备描述",
        "contactPhone": "13800138000"
      }
    ],
    "total": 31
  },
  "success": true
}
```

#### 1.6 更新设备信息
- **路径**: `PUT /api/device/{vid}`
- **功能**: 更新设备信息
- **请求体**: DeviceDto对象
- **返回格式**:
```json
{
  "code": 200,
  "msg": "设备更新成功",
  "data": {},
  "success": true
}
```

#### 1.7 删除设备
- **路径**: `DELETE /api/device/{vid}`
- **功能**: 删除设备
- **返回格式**:
```json
{
  "code": 200,
  "msg": "设备删除成功",
  "data": {},
  "success": true
}
```

### 2. 实时数据功能 (DeviceController)

#### 2.1 获取所有设备实时数据
- **路径**: `GET /api/device/real-time-data`
- **功能**: 获取所有设备的实时数据（用于仪表盘设备状态表）
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取实时数据成功",
  "data": [
    {
      "vid": "device001",
      "deviceName": "设备名称",
      "deviceType": "storage",
      "status": 1,
      "location": "A区",
      "contactPhone": "13800138000",
      "description": "设备描述",
      "lastHeartbeat": "2024-01-15T10:30:25",
      "tin": 20.5,
      "tout": 22.1,
      "hin": 60,
      "hout": 55,
      "lxin": 300,
      "lxout": 250,
      "brightness": 80,
      "vStatus": 1,
      "timestamp": "2024-01-15T10:30:25"
    }
  ],
  "success": true
}
```

#### 2.2 获取单个设备实时数据
- **路径**: `GET /api/device/real-time-data/{vid}`
- **功能**: 获取单个设备的实时数据（用于设备详情页）
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取实时数据成功",
  "data": {
    "vid": "device001",
    "deviceName": "设备名称",
    "deviceType": "storage",
    "status": 1,
    "location": "A区",
    "contactPhone": "13800138000",
    "description": "设备描述",
    "lastHeartbeat": "2024-01-15T10:30:25",
    "manufacturer": "海康威视",
    "model": "DS-2CD3T45D-I5",
    "firmwareVersion": "V1.0.0",
    "lastOnlineTime": "2024-01-15T10:30:25",
    "createTime": "2024-01-01T00:00:00",
    "tin": 20.5,
    "tout": 22.1,
    "hin": 60,
    "hout": 55,
    "lxin": 300,
    "lxout": 250,
    "brightness": 80,
    "vStatus": 1,
    "timestamp": "2024-01-15T10:30:25"
  },
  "success": true
}
```

### 3. 报警管理功能 (AlarmController)

#### 3.1 获取报警列表
- **路径**: `GET /api/alarm/list`
- **功能**: 分页获取报警列表
- **参数**:
  - `pageNum`: 页码，默认为1
  - `pageSize`: 每页大小，默认为10
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "deviceId": 1,
        "vid": "device001",
        "alarmType": "temperature",
        "alarmLevel": "high",
        "title": "温度过高",
        "description": "设备内部温度超过阈值",
        "status": "pending",
        "triggerValue": "25.5",
        "thresholdValue": "25.0",
        "createdAt": "2024-01-15T10:30:25"
      }
    ],
    "total": 8,
    "pageNum": 1,
    "pageSize": 10
  },
  "success": true
}
```

#### 3.2 处理报警
- **路径**: `POST /api/alarm/resolve/{alarmId}`
- **功能**: 处理报警
- **返回格式**:
```json
{
  "code": 200,
  "msg": "报警已处理",
  "data": {},
  "success": true
}
```

#### 3.3 关闭报警
- **路径**: `POST /api/alarm/close/{alarmId}`
- **功能**: 关闭报警
- **返回格式**:
```json
{
  "code": 200,
  "msg": "报警已关闭",
  "data": {},
  "success": true
}
```

#### 3.4 清除全部报警
- **路径**: `DELETE /api/alarm/clear-all`
- **功能**: 清除全部报警
- **返回格式**:
```json
{
  "code": 200,
  "msg": "全部报警已清除",
  "data": {},
  "success": true
}
```

#### 3.5 获取报警详情
- **路径**: `GET /api/alarm/detail/{alarmId}`
- **功能**: 获取报警详情
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "id": 1,
    "deviceId": 1,
    "vid": "device001",
    "alarmType": "temperature",
    "alarmLevel": "high",
    "title": "温度过高",
    "description": "设备内部温度超过阈值",
    "status": "pending",
    "triggerValue": "25.5",
    "thresholdValue": "25.0",
    "resolvedBy": null,
    "resolvedAt": null,
    "acknowledgedBy": null,
    "acknowledgedAt": null,
    "createdAt": "2024-01-15T10:30:25"
  },
  "success": true
}
```

### 4. 仪表盘功能 (DashboardController)

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
        "timeRange": "2024-01-01 ~ 2024-01-31"
      }
    ]
  },
  "success": true
}
```

#### 4.2 顶部统计数据
- **路径**: `GET /api/dashboard/statistics`
- **功能**: 获取顶部统计数据（直接返回统计对象，非包装格式）
- **返回格式**:
```json
{
  "onlineDevices": 25,
  "totalDevices": 31,
  "todayData": 156,
  "dataGrowth": 12,
  "unresolvedAlarms": 8,
  "todayAlarms": 3,
  "systemStatus": "正常",
  "cpuUsage": 45,
  "deviceStatusDistribution": {
    "online": 25,
    "offline": 4,
    "fault": 2,
    "maintenance": 0
  }
}
```

#### 4.3 设备状态分布
- **路径**: `GET /api/dashboard/device-status-distribution`
- **功能**: 获取设备状态分布数据
- **返回格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "deviceStatusDistribution": {
      "online": 25,
      "offline": 4,
      "fault": 2,
      "maintenance": 0
    }
  },
  "success": true
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
│   ├── DeviceController.java
│   ├── AlarmController.java
│   ├── DashboardController.java
│   └── ...
├── service/
│   ├── DeviceService.java
│   ├── AlarmService.java
│   ├── DashboardService.java
│   └── impl/
├── dto/
│   ├── DeviceDto.java
│   ├── ApiResponse.java
│   └── ...
└── repository/
    ├── DeviceRepository.java
    ├── AlarmRepository.java
    └── ...
```