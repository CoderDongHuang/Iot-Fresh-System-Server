-- IoT Fresh 2022 数据库初始化脚本 - 更新版

-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_fresh CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE iot_fresh;

-- 用户表 - 更新版，包含新增字段
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    department VARCHAR(50),
    position VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 设备表
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vid VARCHAR(50) UNIQUE NOT NULL,
    device_name VARCHAR(100),
    device_type VARCHAR(50),
    status TINYINT DEFAULT 0, -- 0:离线, 1:在线, 2:故障, 3:维护
    location VARCHAR(200),
    description TEXT,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    ip_address VARCHAR(45),
    mac_address VARCHAR(17),
    last_online_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 设备数据表 - 包含更多传感器数据
CREATE TABLE IF NOT EXISTS device_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vid VARCHAR(50) NOT NULL,
    tin DOUBLE, -- 内部温度
    tout DOUBLE, -- 外部温度
    hin INT, -- 内部湿度
    hout INT, -- 外部湿度
    lxin INT, -- 内部光照
    lxout INT, -- 外部光照
    brightness INT, -- 亮度
    vstatus TINYINT, -- 设备状态
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 报警表 (根据新规范更新)
CREATE TABLE IF NOT EXISTS alarms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT,
    vid VARCHAR(50),
    device_name VARCHAR(100),
    alarm_type VARCHAR(50), -- 温度异常, 湿度异常, 设备故障
    alarm_level VARCHAR(20) DEFAULT 'medium', -- 使用VARCHAR替代ENUM
    message TEXT,
    status VARCHAR(20) DEFAULT 'active', -- active, resolved, closed
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    extra_info TEXT, -- 附加信息
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_devices_vid ON devices(vid);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_device_data_vid ON device_data(vid);
CREATE INDEX idx_device_data_timestamp ON device_data(timestamp);
CREATE INDEX idx_device_data_vid_timestamp ON device_data(vid, timestamp);
CREATE INDEX idx_alarms_device_id ON alarms(device_id);
CREATE INDEX idx_alarms_vid ON alarms(vid);
CREATE INDEX idx_alarms_alarm_type ON alarms(alarm_type);
CREATE INDEX idx_alarms_alarm_level ON alarms(alarm_level);
CREATE INDEX idx_alarms_status ON alarms(status);
CREATE INDEX idx_alarms_created_at ON alarms(created_at);

-- 插入默认管理员用户 (密码是 '123456')
INSERT INTO users (username, password, real_name, email, phone, department, position, role, status) VALUES 
('admin', '$2a$10$.Jt1Gokcbuf8wIinPytFjehysMhVV9LUTpaJJmxXRL2Ei/5HbOzVO', '系统管理员', 'admin@example.com', '13800138000', '技术部', '系统管理员', 'ADMIN', 1),
('operator', '$2a$10$.Jt1Gokcbuf8wIinPytFjehysMhVV9LUTpaJJmxXRL2Ei/5HbOzVO', '操作员', 'operator@example.com', '13800138001', '运营部', '设备操作员', 'OPERATOR', 1),
('user', '$2a$10$.Jt1Gokcbuf8wIinPytFjehysMhVV9LUTpaJJmxXRL2Ei/5HbOzVO', '普通用户', 'user@example.com', '13800138002', '业务部', '普通员工', 'USER', 1);

-- 插入示例设备
INSERT INTO devices (vid, device_name, device_type, status, location, description, manufacturer, model, firmware_version, ip_address, mac_address, last_online_time) VALUES
('DEV001', '生鲜存储设备001', 'storage', 1, '仓库A区', '主存储设备', '物联网科技', 'STORAGE-X1', 'v1.2.0', '192.168.1.101', 'AA:BB:CC:DD:EE:01', NOW()),
('DEV002', '生鲜存储设备002', 'storage', 1, '仓库B区', '备用存储设备', '物联网科技', 'STORAGE-X1', 'v1.2.0', '192.168.1.102', 'AA:BB:CC:DD:EE:02', NOW()),
('DEV003', '生鲜存储设备003', 'storage', 2, '仓库C区', '故障设备', '物联网科技', 'STORAGE-X1', 'v1.1.5', '192.168.1.103', 'AA:BB:CC:DD:EE:03', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('DEV004', '生鲜存储设备004', 'storage', 3, '仓库D区', '维护中设备', '物联网科技', 'STORAGE-X2', 'v1.3.0', '192.168.1.104', 'AA:BB:CC:DD:EE:04', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('DEV005', '生鲜存储设备005', 'storage', 1, '仓库E区', '温控设备', '物联网科技', 'STORAGE-X2', 'v1.3.0', '192.168.1.105', 'AA:BB:CC:DD:EE:05', NOW()),
('DEV006', '生鲜存储设备006', 'storage', 1, '仓库F区', '湿度控制设备', '物联网科技', 'STORAGE-X1', 'v1.2.0', '192.168.1.106', 'AA:BB:CC:DD:EE:06', NOW()),
('DEV007', '生鲜存储设备007', 'storage', 0, '仓库G区', '备用温控设备', '物联网科技', 'STORAGE-X1', 'v1.2.0', '192.168.1.107', 'AA:BB:CC:DD:EE:07', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('DEV008', '生鲜存储设备008', 'storage', 1, '仓库H区', '冷藏设备', '物联网科技', 'REFRIGERATOR-X1', 'v1.4.0', '192.168.1.108', 'AA:BB:CC:DD:EE:08', NOW()),
('DEV009', '生鲜运输设备009', 'transport', 1, '运输车辆001', '冷链运输车', '运输科技', 'TRANS-T1', 'v1.0.0', '192.168.1.109', 'AA:BB:CC:DD:EE:09', NOW()),
('DEV010', '生鲜运输设备010', 'transport', 1, '运输车辆002', '冷藏运输车', '运输科技', 'TRANS-T1', 'v1.0.0', '192.168.1.110', 'AA:BB:CC:DD:EE:10', NOW()),
('DEV011', '生鲜运输设备011', 'transport', 0, '运输车辆003', '离线运输车', '运输科技', 'TRANS-T1', 'v1.0.0', '192.168.1.111', 'AA:BB:CC:DD:EE:11', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('DEV012', '生鲜运输设备012', 'transport', 2, '运输车辆004', '故障运输车', '运输科技', 'TRANS-T1', 'v1.0.0', '192.168.1.112', 'AA:BB:CC:DD:EE:12', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('DEV013', '生鲜展示设备013', 'display', 1, '超市A区', '展示冷藏柜', '展示科技', 'DISPLAY-D1', 'v1.5.0', '192.168.1.113', 'AA:BB:CC:DD:EE:13', NOW()),
('DEV014', '生鲜展示设备014', 'display', 1, '超市B区', '展示冷冻柜', '展示科技', 'DISPLAY-D1', 'v1.5.0', '192.168.1.114', 'AA:BB:CC:DD:EE:14', NOW()),
('DEV015', '生鲜展示设备015', 'display', 3, '超市C区', '维护展示柜', '展示科技', 'DISPLAY-D1', 'v1.5.0', '192.168.1.115', 'AA:BB:CC:DD:EE:15', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('DEV016', '生鲜展示设备016', 'display', 0, '超市D区', '离线展示柜', '展示科技', 'DISPLAY-D1', 'v1.5.0', '192.168.1.116', 'AA:BB:CC:DD:EE:16', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('DEV017', '生鲜加工设备017', 'processing', 1, '加工车间A', '切割设备', '加工科技', 'PROCESS-P1', 'v2.0.0', '192.168.1.117', 'AA:BB:CC:DD:EE:17', NOW()),
('DEV018', '生鲜加工设备018', 'processing', 1, '加工车间B', '包装设备', '加工科技', 'PROCESS-P1', 'v2.0.0', '192.168.1.118', 'AA:BB:CC:DD:EE:18', NOW()),
('DEV019', '生鲜加工设备019', 'processing', 2, '加工车间C', '故障清洗设备', '加工科技', 'PROCESS-P1', 'v2.0.0', '192.168.1.119', 'AA:BB:CC:DD:EE:19', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('DEV020', '生鲜加工设备020', 'processing', 0, '加工车间D', '离线分拣设备', '加工科技', 'PROCESS-P1', 'v2.0.0', '192.168.1.120', 'AA:BB:CC:DD:EE:20', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('DEV021', '生鲜质检设备021', 'quality', 1, '质检中心A', '质量检测仪', '质检科技', 'QUALITY-Q1', 'v1.8.0', '192.168.1.121', 'AA:BB:CC:DD:EE:21', NOW()),
('DEV022', '生鲜质检设备022', 'quality', 1, '质检中心B', '成分分析仪', '质检科技', 'QUALITY-Q1', 'v1.8.0', '192.168.1.122', 'AA:BB:CC:DD:EE:22', NOW()),
('DEV023', '生鲜质检设备023', 'quality', 3, '质检中心C', '维护检测设备', '质检科技', 'QUALITY-Q1', 'v1.8.0', '192.168.1.123', 'AA:BB:CC:DD:EE:23', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('DEV024', '生鲜质检设备024', 'quality', 0, '质检中心D', '离线分析设备', '质检科技', 'QUALITY-Q1', 'v1.8.0', '192.168.1.124', 'AA:BB:CC:DD:EE:24', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('DEV025', '生鲜监控设备025', 'monitoring', 1, '监控中心A', '环境监控器', '监控科技', 'MONITOR-M1', 'v3.0.0', '192.168.1.125', 'AA:BB:CC:DD:EE:25', NOW()),
('DEV026', '生鲜监控设备026', 'monitoring', 1, '监控中心B', '视频监控器', '监控科技', 'MONITOR-M1', 'v3.0.0', '192.168.1.126', 'AA:BB:CC:DD:EE:26', NOW()),
('DEV027', '生鲜监控设备027', 'monitoring', 2, '监控中心C', '故障监控设备', '监控科技', 'MONITOR-M1', 'v3.0.0', '192.168.1.127', 'AA:BB:CC:DD:EE:27', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
('DEV028', '生鲜监控设备028', 'monitoring', 0, '监控中心D', '离线监控设备', '监控科技', 'MONITOR-M1', 'v3.0.0', '192.168.1.128', 'AA:BB:CC:DD:EE:28', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('DEV029', '生鲜仓储设备029', 'warehouse', 1, '中央仓库A', '智能仓储设备', '仓储科技', 'WAREHOUSE-W1', 'v2.5.0', '192.168.1.129', 'AA:BB:CC:DD:EE:29', NOW()),
('DEV030', '生鲜仓储设备030', 'warehouse', 1, '中央仓库B', '自动化仓储设备', '仓储科技', 'WAREHOUSE-W1', 'v2.5.0', '192.168.1.130', 'AA:BB:CC:DD:EE:30', NOW());

-- 插入示例设备数据
INSERT INTO device_data (vid, tin, tout, hin, hout, lxin, lxout, brightness, vstatus, timestamp) VALUES
('DEV001', 22.5, 18.0, 65, 60, 300, 280, 75, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV002', 21.8, 17.5, 62, 58, 280, 260, 80, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV003', 23.0, 19.2, 70, 65, 320, 300, 60, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV004', 20.5, 16.8, 58, 55, 250, 230, 85, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV005', 22.0, 17.8, 63, 59, 290, 270, 70, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV006', 21.5, 17.0, 60, 56, 270, 250, 78, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV007', 22.8, 18.5, 68, 63, 310, 290, 65, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV008', 19.5, 16.0, 55, 50, 260, 240, 82, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV009', 18.5, 15.0, 50, 45, 200, 180, 90, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV010', 17.8, 14.3, 48, 43, 190, 170, 92, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV011', 19.2, 15.7, 52, 47, 210, 190, 88, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV012', 20.1, 16.6, 55, 50, 220, 200, 85, 2, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV013', 16.5, 13.0, 45, 40, 150, 130, 95, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV014', 15.8, 12.3, 42, 37, 140, 120, 97, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV015', 17.2, 13.7, 47, 42, 160, 140, 93, 3, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV016', 18.1, 14.6, 50, 45, 170, 150, 90, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV017', 22.0, 18.5, 60, 55, 250, 230, 80, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV018', 21.3, 17.8, 58, 53, 240, 220, 82, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV019', 23.5, 20.0, 65, 60, 270, 250, 75, 2, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV020', 24.2, 20.7, 68, 63, 280, 260, 72, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV021', 19.8, 16.3, 54, 49, 180, 160, 88, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV022', 20.5, 17.0, 56, 51, 190, 170, 85, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV023', 21.2, 17.7, 59, 54, 200, 180, 82, 3, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV024', 22.8, 19.3, 63, 58, 220, 200, 78, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV025', 18.2, 14.7, 49, 44, 160, 140, 89, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV026', 17.5, 14.0, 46, 41, 150, 130, 91, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV027', 19.1, 15.6, 52, 47, 170, 150, 86, 2, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV028', 20.3, 16.8, 55, 50, 180, 160, 83, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV029', 16.8, 13.3, 44, 39, 130, 110, 94, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV030', 15.9, 12.4, 41, 36, 120, 100, 96, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- 创建报警处理记录表
CREATE TABLE IF NOT EXISTS alarm_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alarm_id BIGINT,
    action VARCHAR(50), -- 创建报警/处理/关闭
    operator VARCHAR(50), -- 操作人
    remark TEXT, -- 处理备注
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alarm_id) REFERENCES alarms(id) ON DELETE CASCADE
);

-- 插入报警数据 (根据新规范更新，确保数据合理分布)
INSERT INTO alarms (device_id, vid, device_name, alarm_type, alarm_level, message, status, created_at) VALUES
-- 存储设备报警数据 (每个设备1-2个报警，时间间隔合理)
(1, 'DEV001', '设备001', 'temperature', 'high', '设备DEV001内部温度达到25°C，持续高温', 'active', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 'DEV002', '设备002', 'humidity', 'medium', '设备DEV002内部湿度达到85%', 'active', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(3, 'DEV003', '设备003', 'device', 'high', '设备DEV003压缩机工作异常', 'resolved', DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(4, 'DEV004', '设备004', 'light', 'high', '设备DEV004光照强度突然升高', 'active', DATE_SUB(NOW(), INTERVAL 75 MINUTE)),
(5, 'DEV005', '设备005', 'humidity', 'high', '设备DEV005湿度异常升高至90%', 'resolved', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(6, 'DEV006', '设备006', 'device', 'high', '设备DEV006传感器通信异常', 'active', DATE_SUB(NOW(), INTERVAL 105 MINUTE)),
(7, 'DEV007', '设备007', 'temperature', 'high', '设备DEV007温度达到28°C', 'ignored', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(8, 'DEV008', '设备008', 'light', 'medium', '设备DEV008光照强度异常', 'active', DATE_SUB(NOW(), INTERVAL 135 MINUTE)),
-- 运输设备报警数据 (时间间隔合理)
(9, 'DEV009', '运输设备009', 'temperature', 'high', '运输设备009温度异常升高至18.5°C', 'active', DATE_SUB(NOW(), INTERVAL 150 MINUTE)),
(10, 'DEV010', '运输设备010', 'humidity', 'medium', '运输设备010湿度异常降低至48%', 'active', DATE_SUB(NOW(), INTERVAL 165 MINUTE)),
(11, 'DEV011', '运输设备011', 'device', 'high', '运输设备011通信模块故障', 'active', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(12, 'DEV012', '运输设备012', 'temperature', 'medium', '运输设备012温度传感器异常', 'active', DATE_SUB(NOW(), INTERVAL 195 MINUTE)),
-- 展示设备报警数据
(13, 'DEV013', '展示设备013', 'light', 'low', '展示设备013光照强度异常', 'active', DATE_SUB(NOW(), INTERVAL 210 MINUTE)),
(14, 'DEV014', '展示设备014', 'humidity', 'high', '展示设备014湿度控制失效', 'active', DATE_SUB(NOW(), INTERVAL 225 MINUTE)),
(15, 'DEV015', '展示设备015', 'device', 'medium', '展示设备015门禁系统异常', 'active', DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
(16, 'DEV016', '展示设备016', 'temperature', 'low', '展示设备016温度波动异常', 'active', DATE_SUB(NOW(), INTERVAL 255 MINUTE)),
-- 加工设备报警数据
(17, 'DEV017', '加工设备017', 'device', 'high', '加工设备017切割模块故障', 'active', DATE_SUB(NOW(), INTERVAL 270 MINUTE)),
(18, 'DEV018', '加工设备018', 'humidity', 'medium', '加工设备018湿度控制异常', 'active', DATE_SUB(NOW(), INTERVAL 285 MINUTE)),
(19, 'DEV019', '加工设备019', 'temperature', 'high', '加工设备019温度异常升高', 'active', DATE_SUB(NOW(), INTERVAL 300 MINUTE)),
(20, 'DEV020', '加工设备020', 'light', 'low', '加工设备020照明系统异常', 'active', DATE_SUB(NOW(), INTERVAL 315 MINUTE)),
-- 质检设备报警数据
(21, 'DEV021', '质检设备021', 'device', 'medium', '质检设备021检测模块异常', 'active', DATE_SUB(NOW(), INTERVAL 330 MINUTE)),
(22, 'DEV022', '质检设备022', 'humidity', 'high', '质检设备022湿度传感器故障', 'active', DATE_SUB(NOW(), INTERVAL 345 MINUTE)),
(23, 'DEV023', '质检设备023', 'temperature', 'low', '质检设备023温度校准异常', 'active', DATE_SUB(NOW(), INTERVAL 360 MINUTE)),
(24, 'DEV024', '质检设备024', 'light', 'medium', '质检设备024光照控制异常', 'active', DATE_SUB(NOW(), INTERVAL 375 MINUTE)),
-- 监控设备报警数据
(25, 'DEV025', '监控设备025', 'device', 'high', '监控设备025摄像头故障', 'active', DATE_SUB(NOW(), INTERVAL 390 MINUTE)),
(26, 'DEV026', '监控设备026', 'humidity', 'medium', '监控设备026环境湿度异常', 'active', DATE_SUB(NOW(), INTERVAL 405 MINUTE)),
(27, 'DEV027', '监控设备027', 'temperature', 'low', '监控设备027温度传感器异常', 'active', DATE_SUB(NOW(), INTERVAL 420 MINUTE)),
(28, 'DEV028', '监控设备028', 'light', 'high', '监控设备028红外传感器异常', 'active', DATE_SUB(NOW(), INTERVAL 435 MINUTE)),
-- 仓储设备报警数据
(29, 'DEV029', '仓储设备029', 'device', 'medium', '仓储设备029自动化系统异常', 'active', DATE_SUB(NOW(), INTERVAL 450 MINUTE)),
(30, 'DEV030', '仓储设备030', 'humidity', 'low', '仓储设备030湿度控制异常', 'active', DATE_SUB(NOW(), INTERVAL 465 MINUTE)),
-- 历史报警数据 (已解决的报警，时间更早)
(1, 'DEV001', '设备001', 'humidity', 'medium', '设备DEV001内部湿度异常升高至75%', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'DEV002', '设备002', 'temperature', 'high', '设备DEV002温度波动异常，达到24°C', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'DEV003', '设备003', 'temperature', 'high', '设备DEV003内部温度达到26°C，持续高温', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'DEV004', '设备004', 'humidity', 'medium', '设备DEV004内部湿度达到88%', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'DEV005', '设备005', 'light', 'low', '设备DEV005光照强度降至200lux以下', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 'DEV006', '设备006', 'temperature', 'medium', '设备DEV006温度传感器数据异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 'DEV007', '设备007', 'light', 'medium', '设备DEV007光照强度异常波动', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 'DEV008', '设备008', 'temperature', 'low', '设备DEV008温度控制精度异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 'DEV009', '运输设备009', 'device', 'medium', '运输设备009GPS定位异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 'DEV010', '运输设备010', 'temperature', 'low', '运输设备010温度控制精度下降', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 'DEV011', '运输设备011', 'humidity', 'high', '运输设备011湿度传感器故障', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(12, 'DEV012', '运输设备012', 'light', 'medium', '运输设备012照明系统异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(13, 'DEV013', '展示设备013', 'temperature', 'high', '展示设备013温度异常升高', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(14, 'DEV014', '展示设备014', 'device', 'low', '展示设备014门禁系统故障', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(15, 'DEV015', '展示设备015', 'humidity', 'medium', '展示设备015湿度控制异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(16, 'DEV016', '展示设备016', 'light', 'high', '展示设备016照明强度异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(17, 'DEV017', '加工设备017', 'temperature', 'medium', '加工设备017温度控制异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(18, 'DEV018', '加工设备018', 'device', 'low', '加工设备018包装模块故障', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(19, 'DEV019', '加工设备019', 'humidity', 'high', '加工设备019湿度传感器异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(20, 'DEV020', '加工设备020', 'light', 'medium', '加工设备020照明控制异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(21, 'DEV021', '质检设备021', 'temperature', 'low', '质检设备021温度校准异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(22, 'DEV022', '质检设备022', 'device', 'high', '质检设备022分析模块故障', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(23, 'DEV023', '质检设备023', 'humidity', 'medium', '质检设备023湿度控制异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(24, 'DEV024', '质检设备024', 'light', 'low', '质检设备024光照传感器异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(25, 'DEV025', '监控设备025', 'temperature', 'high', '监控设备025温度异常升高', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(26, 'DEV026', '监控设备026', 'device', 'medium', '监控设备026存储模块异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(27, 'DEV027', '监控设备027', 'humidity', 'low', '监控设备027湿度传感器异常', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(28, 'DEV028', '监控设备028', 'light', 'medium', '监控设备028红外传感器故障', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(29, 'DEV029', '仓储设备029', 'temperature', 'high', '仓储设备029温度控制失效', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(30, 'DEV030', '仓储设备030', 'device', 'low', '仓储设备030自动化系统故障', 'resolved', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入报警处理记录示例数据
INSERT INTO alarm_history (alarm_id, action, operator, remark, timestamp) VALUES
(1, 'create', 'system', '系统自动检测到温度异常', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(1, 'acknowledge', 'operator2', '温度异常已确认，正在检查设备运行状态', DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(1, 'resolve', 'operator2', '设备温度已恢复正常，问题已解决', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(2, 'create', 'system', '系统自动检测到湿度异常', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(2, 'acknowledge', 'admin', '湿度异常问题已确认，正在分析原因', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(2, 'resolve', 'admin', '湿度问题已解决，设备运行正常', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(3, 'create', 'system', '系统自动检测到温度异常', DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(3, 'acknowledge', 'operator1', '高温报警已确认，正在检查散热系统', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(3, 'resolve', 'operator1', '散热系统调整完成，温度恢复正常', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(4, 'create', 'system', '系统自动检测到湿度异常', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(4, 'acknowledge', 'admin', '已确认湿度异常问题', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(4, 'resolve', 'admin', '湿度已恢复正常，设备运行稳定', DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(5, 'create', 'system', '系统自动检测到光照强度异常', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(5, 'acknowledge', 'operator1', '已确认光照异常，正在检查设备', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(5, 'resolve', 'operator1', '光照强度已调整至正常范围', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(6, 'create', 'system', '系统自动检测到设备通信故障', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(6, 'acknowledge', 'admin', '设备通信故障，已安排技术人员处理', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(6, 'resolve', 'admin', '设备通信故障已修复，恢复正常运行', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(7, 'create', 'system', '系统自动检测到温度超标', DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
(7, 'acknowledge', 'operator2', '温度波动在可接受范围内，持续监控', DATE_SUB(NOW(), INTERVAL 210 MINUTE)),
(7, 'ignore', 'admin', '温度波动在可接受范围内，无需处理', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(8, 'create', 'system', '系统自动检测到光照强度异常', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(8, 'acknowledge', 'operator1', '光照强度异常，正在调整设备参数', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(8, 'resolve', 'operator1', '光照强度参数调整完成，设备运行正常', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(9, 'create', 'system', '系统自动检测到湿度异常升高', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(9, 'acknowledge', 'operator2', '湿度异常已确认，正在检查加湿器', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(10, 'create', 'system', '系统自动检测到温度波动异常', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(10, 'acknowledge', 'admin', '温度波动异常已确认，正在分析数据', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(11, 'create', 'system', '系统自动检测到风扇转速异常', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(11, 'acknowledge', 'operator1', '风扇转速异常已确认，正在检查设备', DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
(11, 'resolve', 'operator1', '风扇转速已恢复正常，设备运行稳定', DATE_SUB(NOW(), INTERVAL 100 MINUTE)),
(12, 'create', 'system', '系统自动检测到光照传感器异常', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(12, 'acknowledge', 'operator2', '光照传感器异常已确认，正在校准', DATE_SUB(NOW(), INTERVAL 170 MINUTE)),
(12, 'ignore', 'admin', '光照传感器读数在可接受范围内，无需处理', DATE_SUB(NOW(), INTERVAL 160 MINUTE)),
(13, 'create', 'system', '系统自动检测到压缩机工作异常', DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
(13, 'acknowledge', 'admin', '压缩机异常已确认，正在检查系统', DATE_SUB(NOW(), INTERVAL 230 MINUTE)),
(13, 'resolve', 'admin', '压缩机系统已修复，恢复正常运行', DATE_SUB(NOW(), INTERVAL 220 MINUTE)),
(14, 'create', 'system', '系统自动检测到温度传感器校准异常', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(14, 'acknowledge', 'operator1', '温度传感器校准异常已确认', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(15, 'create', 'system', '系统自动检测到网络连接不稳定', DATE_SUB(NOW(), INTERVAL 55 MINUTE)),
(15, 'acknowledge', 'operator2', '网络连接问题已确认，正在检查', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(16, 'create', 'system', '系统自动检测到温度控制精度下降', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(16, 'acknowledge', 'admin', '温度控制精度问题已确认', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(17, 'create', 'system', '系统自动检测到湿度异常升高', DATE_SUB(NOW(), INTERVAL 300 MINUTE)),
(17, 'acknowledge', 'operator1', '湿度异常问题已确认', DATE_SUB(NOW(), INTERVAL 290 MINUTE)),
(17, 'resolve', 'operator1', '湿度控制问题已解决', DATE_SUB(NOW(), INTERVAL 280 MINUTE)),
(18, 'create', 'system', '系统自动检测到湿度控制模块故障', DATE_SUB(NOW(), INTERVAL 360 MINUTE)),
(18, 'acknowledge', 'operator2', '湿度控制模块故障已确认', DATE_SUB(NOW(), INTERVAL 350 MINUTE)),
(18, 'ignore', 'admin', '湿度控制模块在可接受范围内运行', DATE_SUB(NOW(), INTERVAL 340 MINUTE)),
(19, 'create', 'system', '系统自动检测到光照强度异常波动', DATE_SUB(NOW(), INTERVAL 70 MINUTE)),
(19, 'acknowledge', 'operator1', '光照波动问题已确认', DATE_SUB(NOW(), INTERVAL 65 MINUTE)),
(20, 'create', 'system', '系统自动检测到电源模块异常', DATE_SUB(NOW(), INTERVAL 420 MINUTE)),
(20, 'acknowledge', 'admin', '电源模块异常已确认', DATE_SUB(NOW(), INTERVAL 410 MINUTE)),
(20, 'resolve', 'admin', '电源模块问题已修复', DATE_SUB(NOW(), INTERVAL 400 MINUTE)),
(21, 'create', 'system', '系统自动检测到温度控制精度异常', DATE_SUB(NOW(), INTERVAL 80 MINUTE)),
(21, 'acknowledge', 'operator2', '温度控制精度问题已确认', DATE_SUB(NOW(), INTERVAL 75 MINUTE)),
(22, 'create', 'system', '系统自动检测到湿度传感器校准异常', DATE_SUB(NOW(), INTERVAL 95 MINUTE)),
(22, 'acknowledge', 'operator1', '湿度传感器校准问题已确认', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(23, 'create', 'system', '系统自动检测到制冷系统压力异常', DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
(23, 'acknowledge', 'admin', '制冷系统压力异常已确认', DATE_SUB(NOW(), INTERVAL 105 MINUTE)),
(24, 'create', 'system', '系统自动检测到温度传感器数据漂移', DATE_SUB(NOW(), INTERVAL 125 MINUTE)),
(24, 'acknowledge', 'operator2', '温度传感器数据漂移问题已确认', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(25, 'create', 'system', '系统自动检测到加湿器工作异常', DATE_SUB(NOW(), INTERVAL 140 MINUTE)),
(25, 'acknowledge', 'operator1', '加湿器工作异常已确认', DATE_SUB(NOW(), INTERVAL 135 MINUTE)),
(26, 'create', 'system', '系统自动检测到光照强度突然升高', DATE_SUB(NOW(), INTERVAL 155 MINUTE)),
(26, 'acknowledge', 'admin', '光照强度异常升高问题已确认', DATE_SUB(NOW(), INTERVAL 150 MINUTE)),
(27, 'create', 'system', '系统自动检测到风扇转速控制异常', DATE_SUB(NOW(), INTERVAL 170 MINUTE)),
(27, 'acknowledge', 'operator2', '风扇转速控制异常已确认', DATE_SUB(NOW(), INTERVAL 165 MINUTE)),
(28, 'create', 'system', '系统自动检测到温度持续上升', DATE_SUB(NOW(), INTERVAL 185 MINUTE)),
(28, 'acknowledge', 'operator1', '温度持续上升问题已确认', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(29, 'create', 'system', '系统自动检测到湿度传感器读数异常', DATE_SUB(NOW(), INTERVAL 200 MINUTE)),
(29, 'acknowledge', 'admin', '湿度传感器读数异常已确认', DATE_SUB(NOW(), INTERVAL 195 MINUTE)),
(30, 'create', 'system', '系统自动检测到通信模块异常', DATE_SUB(NOW(), INTERVAL 215 MINUTE)),
(30, 'acknowledge', 'operator2', '通信模块异常已确认', DATE_SUB(NOW(), INTERVAL 210 MINUTE)),
(31, 'create', 'system', '系统自动检测到光照强度异常降低', DATE_SUB(NOW(), INTERVAL 230 MINUTE)),
(31, 'acknowledge', 'operator1', '光照强度异常降低问题已确认', DATE_SUB(NOW(), INTERVAL 225 MINUTE)),
(32, 'create', 'system', '系统自动检测到湿度控制失效', DATE_SUB(NOW(), INTERVAL 245 MINUTE)),
(32, 'acknowledge', 'admin', '湿度控制失效问题已确认', DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
(33, 'create', 'system', '系统自动检测到温度波动超出范围', DATE_SUB(NOW(), INTERVAL 260 MINUTE)),
(33, 'acknowledge', 'operator2', '温度波动超出范围问题已确认', DATE_SUB(NOW(), INTERVAL 255 MINUTE)),
(34, 'create', 'system', '系统自动检测到传感器校准异常', DATE_SUB(NOW(), INTERVAL 275 MINUTE)),
(34, 'acknowledge', 'operator1', '传感器校准异常问题已确认', DATE_SUB(NOW(), INTERVAL 270 MINUTE)),
(35, 'create', 'system', '系统自动检测到光照控制模块故障', DATE_SUB(NOW(), INTERVAL 290 MINUTE)),
(35, 'acknowledge', 'admin', '光照控制模块故障问题已确认', DATE_SUB(NOW(), INTERVAL 285 MINUTE)),
(36, 'create', 'system', '系统自动检测到湿度异常升高', DATE_SUB(NOW(), INTERVAL 305 MINUTE)),
(36, 'acknowledge', 'operator2', '湿度异常升高问题已确认', DATE_SUB(NOW(), INTERVAL 300 MINUTE)),
(37, 'create', 'system', '系统自动检测到温度传感器数据异常', DATE_SUB(NOW(), INTERVAL 320 MINUTE)),
(37, 'acknowledge', 'operator1', '温度传感器数据异常问题已确认', DATE_SUB(NOW(), INTERVAL 315 MINUTE)),
(38, 'create', 'system', '系统自动检测到光照强度超标', DATE_SUB(NOW(), INTERVAL 335 MINUTE)),
(38, 'acknowledge', 'admin', '光照强度超标问题已确认', DATE_SUB(NOW(), INTERVAL 330 MINUTE)),
(39, 'create', 'system', '系统自动检测到运输设备温度异常', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(39, 'acknowledge', 'operator2', '运输设备温度异常已确认', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(40, 'create', 'system', '系统自动检测到展示设备湿度异常', DATE_SUB(NOW(), INTERVAL 65 MINUTE)),
(40, 'acknowledge', 'admin', '展示设备湿度异常已确认', DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(41, 'create', 'system', '系统自动检测到加工设备故障', DATE_SUB(NOW(), INTERVAL 80 MINUTE)),
(41, 'acknowledge', 'operator1', '加工设备故障已确认', DATE_SUB(NOW(), INTERVAL 75 MINUTE)),
(42, 'create', 'system', '系统自动检测到质检设备异常', DATE_SUB(NOW(), INTERVAL 95 MINUTE)),
(42, 'acknowledge', 'operator2', '质检设备异常已确认', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(43, 'create', 'system', '系统自动检测到监控设备离线', DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
(43, 'acknowledge', 'admin', '监控设备离线问题已确认', DATE_SUB(NOW(), INTERVAL 105 MINUTE)),
(44, 'create', 'system', '系统自动检测到仓储设备温度异常', DATE_SUB(NOW(), INTERVAL 125 MINUTE)),
(44, 'acknowledge', 'operator1', '仓储设备温度异常已确认', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(45, 'create', 'system', '系统自动检测到运输设备通信异常', DATE_SUB(NOW(), INTERVAL 140 MINUTE)),
(45, 'acknowledge', 'operator2', '运输设备通信异常已确认', DATE_SUB(NOW(), INTERVAL 135 MINUTE)),
(46, 'create', 'system', '系统自动检测到展示设备光照异常', DATE_SUB(NOW(), INTERVAL 155 MINUTE)),
(46, 'acknowledge', 'admin', '展示设备光照异常已确认', DATE_SUB(NOW(), INTERVAL 150 MINUTE)),
(47, 'create', 'system', '系统自动检测到加工设备压力异常', DATE_SUB(NOW(), INTERVAL 170 MINUTE)),
(47, 'acknowledge', 'operator1', '加工设备压力异常已确认', DATE_SUB(NOW(), INTERVAL 165 MINUTE)),
(48, 'create', 'system', '系统自动检测到质检设备校准异常', DATE_SUB(NOW(), INTERVAL 185 MINUTE)),
(48, 'acknowledge', 'operator2', '质检设备校准异常已确认', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(49, 'create', 'system', '系统自动检测到监控设备图像异常', DATE_SUB(NOW(), INTERVAL 200 MINUTE)),
(49, 'acknowledge', 'admin', '监控设备图像异常已确认', DATE_SUB(NOW(), INTERVAL 195 MINUTE)),
(50, 'create', 'system', '系统自动检测到仓储设备湿度异常', DATE_SUB(NOW(), INTERVAL 215 MINUTE)),
(50, 'acknowledge', 'operator1', '仓储设备湿度异常已确认', DATE_SUB(NOW(), INTERVAL 210 MINUTE)),
(51, 'create', 'system', '系统自动检测到运输设备振动异常', DATE_SUB(NOW(), INTERVAL 230 MINUTE)),
(51, 'acknowledge', 'operator2', '运输设备振动异常已确认', DATE_SUB(NOW(), INTERVAL 225 MINUTE)),
(52, 'create', 'system', '系统自动检测到展示设备门禁异常', DATE_SUB(NOW(), INTERVAL 245 MINUTE)),
(52, 'acknowledge', 'admin', '展示设备门禁异常已确认', DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
(53, 'create', 'system', '系统自动检测到加工设备转速异常', DATE_SUB(NOW(), INTERVAL 260 MINUTE)),
(53, 'acknowledge', 'operator1', '加工设备转速异常已确认', DATE_SUB(NOW(), INTERVAL 255 MINUTE)),
(54, 'create', 'system', '系统自动检测到质检设备精度异常', DATE_SUB(NOW(), INTERVAL 275 MINUTE)),
(54, 'acknowledge', 'operator2', '质检设备精度异常已确认', DATE_SUB(NOW(), INTERVAL 270 MINUTE)),
(55, 'create', 'system', '系统自动检测到监控设备存储异常', DATE_SUB(NOW(), INTERVAL 290 MINUTE)),
(55, 'acknowledge', 'admin', '监控设备存储异常已确认', DATE_SUB(NOW(), INTERVAL 285 MINUTE)),
(56, 'create', 'system', '系统自动检测到仓储设备通风异常', DATE_SUB(NOW(), INTERVAL 305 MINUTE)),
(56, 'acknowledge', 'operator1', '仓储设备通风异常已确认', DATE_SUB(NOW(), INTERVAL 300 MINUTE)),
(57, 'create', 'system', '系统自动检测到运输设备定位异常', DATE_SUB(NOW(), INTERVAL 320 MINUTE)),
(57, 'acknowledge', 'operator2', '运输设备定位异常已确认', DATE_SUB(NOW(), INTERVAL 315 MINUTE)),
(58, 'create', 'system', '系统自动检测到展示设备照明异常', DATE_SUB(NOW(), INTERVAL 335 MINUTE)),
(58, 'acknowledge', 'admin', '展示设备照明异常已确认', DATE_SUB(NOW(), INTERVAL 330 MINUTE)),
(59, 'create', 'system', '系统自动检测到加工设备润滑异常', DATE_SUB(NOW(), INTERVAL 350 MINUTE)),
(59, 'acknowledge', 'operator1', '加工设备润滑异常已确认', DATE_SUB(NOW(), INTERVAL 345 MINUTE)),
(60, 'create', 'system', '系统自动检测到质检设备采样异常', DATE_SUB(NOW(), INTERVAL 365 MINUTE)),
(60, 'acknowledge', 'operator2', '质检设备采样异常已确认', DATE_SUB(NOW(), INTERVAL 360 MINUTE));