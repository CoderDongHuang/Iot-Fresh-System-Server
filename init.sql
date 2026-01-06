-- IoT Fresh 2022 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_fresh CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE iot_fresh;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
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
    contact_phone VARCHAR(20),
    description TEXT,
    last_heartbeat TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 报警表
CREATE TABLE IF NOT EXISTS alarms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT,
    device_name VARCHAR(100),
    alarm_type VARCHAR(50), -- 温度异常, 湿度异常, 设备故障
    alarm_level VARCHAR(20), -- high, medium, low
    message TEXT,
    status VARCHAR(20) DEFAULT 'active', -- active, resolved, closed
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (device_id) REFERENCES devices(id)
);

-- 设备数据表
CREATE TABLE IF NOT EXISTS device_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vid VARCHAR(50) NOT NULL,
    device_type VARCHAR(50),
    tin DOUBLE, -- 内部温度
    tout DOUBLE, -- 外部温度
    lxin INT, -- 内部光照
    pid VARCHAR(50), -- 产品ID
    vstatus INT, -- 设备状态
    battery INT, -- 电池电量
    brightness INT, -- 亮度
    speed_m1 INT, -- 风机1速度
    speed_m2 INT, -- 风机2速度
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_devices_vid ON devices(vid);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_alarms_device_id ON alarms(device_id);
CREATE INDEX idx_alarms_status ON alarms(status);
CREATE INDEX idx_device_data_vid ON device_data(vid);
CREATE INDEX idx_device_data_created_at ON device_data(created_at);

-- 插入默认管理员用户 (密码是 '123456')
INSERT INTO users (username, password, email, phone, role, status) VALUES 
('admin', '$2a$LEUNZTVbV.T2TfmbsYvevOUSQWCaVMzyQQanb9G73fjzPtkz64AKm', 'admin@example.com', '13800138000', 'ADMIN', 1),
('operator', '$2a$LEUNZTVbV.T2TfmbsYvevOUSQWCaVMzyQQanb9G73fjzPtkz64AKm', 'operator@example.com', '13800138001', 'OPERATOR', 1);

-- 插入示例设备
INSERT INTO devices (vid, device_name, device_type, status, location, contact_phone, description) VALUES
('DEV001', '鲜品存储设备001', 'storage', 1, '仓库A区', '13800138002', '主存储设备'),
('DEV002', '鲜品存储设备002', 'storage', 0, '仓库B区', '13800138003', '备用存储设备'),
('DEV003', '鲜品存储设备003', 'storage', 2, '仓库C区', '13800138004', '故障设备'),
('DEV004', '鲜品存储设备004', 'storage', 3, '仓库D区', '13800138005', '维护中设备'),
('DEV005', '鲜品存储设备005', 'storage', 1, '仓库E区', '13800138006', '温控设备'),
('DEV006', '鲜品存储设备006', 'storage', 1, '仓库F区', '13800138007', '湿度控制设备'),
('DEV007', '鲜品存储设备007', 'storage', 0, '仓库G区', '13800138008', '备用温控设备'),
('DEV008', '鲜品存储设备008', 'storage', 1, '仓库H区', '13800138009', '冷藏设备');

-- 插入示例设备数据
INSERT INTO device_data (vid, device_type, tin, tout, lxin, battery, brightness, speed_m1, speed_m2, created_at) VALUES
('DEV001', 'storage', 22.5, 18.0, 300, 85, 75, 1200, 1100, NOW()),
('DEV002', 'storage', 21.8, 17.5, 280, 90, 80, 1000, 950, NOW()),
('DEV003', 'storage', 23.0, 19.2, 320, 45, 60, 1300, 1250, NOW()),
('DEV004', 'storage', 20.5, 16.8, 250, 95, 85, 900, 850, NOW()),
('DEV005', 'storage', 22.0, 17.8, 290, 78, 70, 1150, 1100, NOW()),
('DEV006', 'storage', 21.5, 17.0, 270, 88, 78, 1050, 1000, NOW()),
('DEV007', 'storage', 22.8, 18.5, 310, 65, 65, 1250, 1200, NOW()),
('DEV008', 'storage', 19.5, 16.0, 260, 92, 82, 950, 900, NOW());

-- 插入示例报警
INSERT INTO alarms (device_name, alarm_type, alarm_level, message, status, created_at) VALUES
('鲜品存储设备001', 'temperature', 'high', '内部温度过高，当前温度25°C', 'active', NOW()),
('鲜品存储设备002', 'humidity', 'medium', '湿度异常，当前湿度90%', 'active', NOW()),
('鲜品存储设备003', 'temperature', 'high', '内部温度异常，当前温度26°C', 'active', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('鲜品存储设备004', 'humidity', 'medium', '湿度异常，当前湿度92%', 'resolved', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('鲜品存储设备005', 'light', 'low', '光照强度异常，当前强度350', 'active', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('鲜品存储设备006', 'device', 'critical', '设备故障，传感器异常', 'active', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('鲜品存储设备007', 'temperature', 'high', '温度超标，当前温度28°C', 'closed', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('鲜品存储设备008', 'light', 'medium', '光照强度异常，当前强度400', 'active', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
('鲜品存储设备001', 'temperature', 'high', '温度异常，当前温度27°C', 'active', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('鲜品存储设备002', 'light', 'low', '光照异常，当前强度200', 'active', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('鲜品存储设备003', 'device', 'critical', '设备故障，通信中断', 'active', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
('鲜品存储设备004', 'humidity', 'medium', '湿度超标，当前湿度85%', 'resolved', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('鲜品存储设备005', 'temperature', 'high', '温度异常，当前温度29°C', 'active', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('鲜品存储设备006', 'light', 'medium', '光照异常，当前强度380', 'active', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('鲜品存储设备007', 'device', 'critical', '设备故障，电源异常', 'closed', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('鲜品存储设备008', 'humidity', 'low', '湿度异常，当前湿度30%', 'active', DATE_SUB(NOW(), INTERVAL 20 MINUTE));