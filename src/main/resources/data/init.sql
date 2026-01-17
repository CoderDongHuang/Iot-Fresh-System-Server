-- IoT Fresh 2022 数据库初始化脚本 - 更新版

-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_fresh CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE iot_fresh;

-- 删除现有表（如果有）
DROP TABLE IF EXISTS device_data;
DROP TABLE IF EXISTS alarms;
DROP TABLE IF EXISTS devices;
DROP TABLE IF EXISTS users;

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

-- 报警表
CREATE TABLE IF NOT EXISTS alarms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT,
    vid VARCHAR(50),
    alarm_type VARCHAR(50), -- 温度异常, 湿度异常, 设备故障
    alarm_level ENUM('low', 'medium', 'high', 'critical') DEFAULT 'medium',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('pending', 'acknowledged', 'resolved', 'ignored') DEFAULT 'pending',
    trigger_value VARCHAR(100),
    threshold_value VARCHAR(100),
    resolved_by BIGINT,
    resolved_at TIMESTAMP NULL,
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (acknowledged_by) REFERENCES users(id) ON DELETE SET NULL
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
('DEV008', '生鲜存储设备008', 'storage', 1, '仓库H区', '冷藏设备', '物联网科技', 'REFRIGERATOR-X1', 'v1.4.0', '192.168.1.108', 'AA:BB:CC:DD:EE:08', NOW());

-- 插入示例设备数据
INSERT INTO device_data (vid, tin, tout, hin, hout, lxin, lxout, brightness, vstatus, timestamp) VALUES
('DEV001', 22.5, 18.0, 65, 60, 300, 280, 75, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV001', 22.3, 17.8, 64, 59, 305, 285, 76, 1, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
('DEV001', 22.7, 18.2, 66, 61, 295, 275, 74, 1, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
('DEV002', 21.8, 17.5, 62, 58, 280, 260, 80, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV002', 21.6, 17.3, 61, 57, 285, 265, 81, 1, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
('DEV003', 23.0, 19.2, 70, 65, 320, 300, 60, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV004', 20.5, 16.8, 58, 55, 250, 230, 85, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV005', 22.0, 17.8, 63, 59, 290, 270, 70, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV006', 21.5, 17.0, 60, 56, 270, 250, 78, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV007', 22.8, 18.5, 68, 63, 310, 290, 65, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('DEV008', 19.5, 16.0, 55, 50, 260, 240, 82, 1, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- 插入示例报警
INSERT INTO alarms (device_id, vid, alarm_type, alarm_level, title, description, status, trigger_value, threshold_value, created_at) VALUES
(1, 'DEV001', 'temperature', 'high', '温度过高警告', '设备DEV001内部温度达到25°C，超过安全阈值', 'pending', '25.0', '24.0', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(2, 'DEV002', 'humidity', 'medium', '湿度异常', '设备DEV002内部湿度达到85%，超出正常范围', 'pending', '85', '80', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(3, 'DEV003', 'temperature', 'high', '温度异常', '设备DEV003内部温度达到26°C，持续高温', 'pending', '26.0', '24.0', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(4, 'DEV004', 'humidity', 'medium', '湿度异常', '设备DEV004内部湿度达到88%', 'resolved', '88', '80', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 'DEV005', 'light', 'low', '光照强度异常', '设备DEV005光照强度降至200lux以下', 'pending', '180', '250', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(6, 'DEV006', 'device', 'critical', '设备故障', '设备DEV006传感器通信异常', 'pending', 'communication_error', 'normal', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(7, 'DEV007', 'temperature', 'high', '温度超标', '设备DEV007温度达到28°C', 'ignored', '28.0', '25.0', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(8, 'DEV008', 'light', 'medium', '光照异常', '设备DEV008光照强度异常', 'pending', '400', '300', DATE_SUB(NOW(), INTERVAL 15 MINUTE));