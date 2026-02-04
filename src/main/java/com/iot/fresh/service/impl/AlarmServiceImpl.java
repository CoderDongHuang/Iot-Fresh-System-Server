package com.iot.fresh.service.impl;

import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.entity.Alarm;
import com.iot.fresh.entity.AlarmHistory;
import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.AlarmHistoryRepository;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.AlarmPushService;
import com.iot.fresh.service.AlarmService;
import com.iot.fresh.service.impl.EmailNotificationServiceImpl;
import com.iot.fresh.service.impl.DingTalkService;
import com.iot.fresh.entity.DingTalkSettings;
import com.iot.fresh.repository.DingTalkSettingsRepository;
import com.iot.fresh.websocket.WebSocketEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private AlarmHistoryRepository alarmHistoryRepository;
    
    @Autowired
    private AlarmPushService alarmPushService;

    @Autowired
    private EmailNotificationServiceImpl emailNotificationService;

    @Autowired
    private DingTalkService dingTalkService;

    @Autowired
    private DingTalkSettingsRepository dingTalkSettingsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ApiResponse<PaginatedResponse<Map<String, Object>>> getAlarmList(Integer pageNum, Integer pageSize, String level, String status, String keyword, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Alarm> alarmPage;
        
        // 转换API状态值到数据库状态值
        String dbStatus = convertApiStatusToDbStatus(status);
        
        // 处理日期筛选
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            startDateTime = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            endDateTime = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            // 按关键词搜索（设备名称或报警内容）
            if (level != null && !level.isEmpty() && status != null && !status.isEmpty() && startDateTime != null && endDateTime != null) {
                // 查找消息或设备名包含关键词且级别、状态和时间范围匹配的报警
                alarmPage = alarmRepository.findByMessageContainingOrDeviceNameContainingAndAlarmLevelAndStatusAndCreatedAtBetween(keyword, keyword, level, dbStatus, startDateTime, endDateTime, pageable);
            } else if (level != null && !level.isEmpty() && status != null && !status.isEmpty()) {
                // 查找消息或设备名包含关键词且级别和状态匹配的报警
                alarmPage = alarmRepository.findByMessageContainingOrDeviceNameContainingAndAlarmLevelAndStatus(keyword, keyword, level, dbStatus, pageable);
            } else if (level != null && !level.isEmpty()) {
                alarmPage = alarmRepository.findByMessageContainingAndAlarmLevelAndDeviceNameContaining(keyword, level, keyword, pageable);
            } else if (status != null && !status.isEmpty()) {
                // 查找消息或设备名包含关键词且状态匹配的报警
                alarmPage = alarmRepository.findByMessageContainingOrDeviceNameContainingAndStatus(keyword, keyword, dbStatus, pageable);
            } else if (startDateTime != null && endDateTime != null) {
                // 查找消息或设备名包含关键词且时间范围匹配的报警
                alarmPage = alarmRepository.findByMessageContainingOrDeviceNameContainingAndCreatedAtBetween(keyword, keyword, startDateTime, endDateTime, pageable);
            } else {
                alarmPage = alarmRepository.findByMessageContainingOrDeviceNameContaining(keyword, keyword, pageable);
            }
        } else {
            // 不按关键词搜索
            if (level != null && !level.isEmpty() && status != null && !status.isEmpty() && startDateTime != null && endDateTime != null) {
                alarmPage = alarmRepository.findByAlarmLevelAndStatusAndCreatedAtBetween(level, dbStatus, startDateTime, endDateTime, pageable);
            } else if (level != null && !level.isEmpty() && status != null && !status.isEmpty()) {
                alarmPage = alarmRepository.findByAlarmLevelAndStatus(level, dbStatus, pageable);
            } else if (level != null && !level.isEmpty()) {
                alarmPage = alarmRepository.findByAlarmLevel(level, pageable);
            } else if (status != null && !status.isEmpty()) {
                alarmPage = alarmRepository.findByStatus(dbStatus, pageable);
            } else if (startDateTime != null && endDateTime != null) {
                alarmPage = alarmRepository.findByCreatedAtBetween(startDateTime, endDateTime, pageable);
            } else {
                alarmPage = alarmRepository.findAll(pageable);
            }
        }
        
        List<Map<String, Object>> alarmList = alarmPage.getContent().stream().map(this::convertToAlarmMap).toList();
        
        PaginatedResponse<Map<String, Object>> paginatedResponse = new PaginatedResponse<>(
                alarmList,
                alarmPage.getTotalElements(),
                pageNum,
                pageSize
        );
        
        return ApiResponse.success(paginatedResponse);
    }
    
    // 将API状态值转换为数据库状态值
    private String convertApiStatusToDbStatus(String apiStatus) {
        if (apiStatus == null) {
            return null;
        }
        
        switch (apiStatus.toLowerCase()) {
            case "待处理":
                return "active"; // API的pending对应数据库的active
            case "已处理":
                return "resolved";
            default:
                return apiStatus; // 如果不是标准状态，直接返回原值
        }
    }

    @Override
    public ApiResponse<String> resolveAlarm(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        Alarm alarm = alarmOpt.get();
        alarm.setStatus("resolved"); // 设置为已处理
        alarm.setUpdatedAt(LocalDateTime.now());
        alarm.setResolvedAt(LocalDateTime.now()); // 设置解决时间为当前时间
        alarmRepository.save(alarm);
        
        // 推送更新后的报警统计数据
        pushUpdatedStatistics();
        
        return ApiResponse.success("报警已处理");
    }


    
    // 推送更新后的报警统计数据到WebSocket客户端
    private void pushUpdatedStatistics() {
        try {
            // 获取更新后的统计数据
            ApiResponse<Map<String, Object>> statsResponse = getAlarmStatistics();
            if (statsResponse.isSuccess()) {
                // 构建推送消息 - 遵循与HTTP API相同的格式
                Map<String, Object> message = new HashMap<>();
                message.put("code", 200);
                message.put("msg", "success");
                message.put("data", statsResponse.getData());
                message.put("type", "alarm_statistics_update");
                message.put("timestamp", LocalDateTime.now().toString());
                
                // 发送到所有WebSocket客户端
                String jsonMessage = objectMapper.writeValueAsString(message);
                WebSocketEndpoint.sendMessageToAll(jsonMessage);
            }
        } catch (Exception e) {
            System.err.println("推送报警统计数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public ApiResponse<String> clearAllAlarms() {
        alarmRepository.deleteAll(); // 清除所有报警
        return ApiResponse.success("所有报警已清除");
    }

    @Override
    public ApiResponse<Map<String, Object>> getAlarmDetail(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        Alarm alarm = alarmOpt.get();
        Map<String, Object> alarmDetail = convertToAlarmDetailMap(alarm);
        
        return ApiResponse.success(alarmDetail);
    }

    @Override
    public ApiResponse<Map<String, Object>> getAlarmStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总报警数
        long total = alarmRepository.count();
        statistics.put("total", total);
        
        // 按状态统计 (按照API规范使用pending, resolved, ignored)
        long pending = alarmRepository.countByStatus("active");  // active状态对应pending
        long resolved = alarmRepository.countByStatus("resolved");
        long ignored = alarmRepository.countByStatus("ignored");
        
        statistics.put("pending", pending);
        statistics.put("resolved", resolved);
        statistics.put("ignored", ignored);
        
        // 按前端三个级别统计：紧急、重要、一般
        // 紧急级别：包含后端的critical和high
        long urgent = alarmRepository.countByAlarmLevel("critical") + alarmRepository.countByAlarmLevel("high");
        // 重要级别：包含后端的medium
        long important = alarmRepository.countByAlarmLevel("medium");
        // 一般级别：包含后端的low
        long normal = alarmRepository.countByAlarmLevel("low");
        
        statistics.put("urgent", urgent);
        statistics.put("important", important);
        statistics.put("normal", normal);
        
        // 新增：按前端级别和状态统计
        statistics.put("urgentPending", alarmRepository.countByAlarmLevelAndStatus("critical", "active") + 
                                        alarmRepository.countByAlarmLevelAndStatus("high", "active"));
        statistics.put("urgentResolved", alarmRepository.countByAlarmLevelAndStatus("critical", "resolved") + 
                                         alarmRepository.countByAlarmLevelAndStatus("high", "resolved"));
        statistics.put("urgentIgnored", alarmRepository.countByAlarmLevelAndStatus("critical", "ignored") + 
                                        alarmRepository.countByAlarmLevelAndStatus("high", "ignored"));
        
        statistics.put("importantPending", alarmRepository.countByAlarmLevelAndStatus("medium", "active"));
        statistics.put("importantResolved", alarmRepository.countByAlarmLevelAndStatus("medium", "resolved"));
        statistics.put("importantIgnored", alarmRepository.countByAlarmLevelAndStatus("medium", "ignored"));
        
        statistics.put("normalPending", alarmRepository.countByAlarmLevelAndStatus("low", "active"));
        statistics.put("normalResolved", alarmRepository.countByAlarmLevelAndStatus("low", "resolved"));
        statistics.put("normalIgnored", alarmRepository.countByAlarmLevelAndStatus("low", "ignored"));
        
        return ApiResponse.success(statistics);
    }
    
    private Map<String, Object> convertToAlarmMap(Alarm alarm) {
        Map<String, Object> alarmMap = new HashMap<>();
        alarmMap.put("id", alarm.getId());
        alarmMap.put("deviceId", alarm.getVid()); // 使用VID作为deviceId
        alarmMap.put("deviceName", alarm.getDeviceName());
        
        // 映射报警级别：将后端级别映射到前端三个级别
        String frontendLevel = convertToFrontendLevel(alarm.getAlarmLevel());
        alarmMap.put("level", frontendLevel); // 前端期望的字段名是level，不是alarmLevel
        
        alarmMap.put("alarmType", alarm.getAlarmType());
        alarmMap.put("alarmContent", alarm.getMessage()); // 使用message字段作为报警内容
        
        // 映射状态值以匹配API规范
        String status = alarm.getStatus();
        if ("active".equals(status)) {
            status = "待处理";
        } else if ("resolved".equals(status)) {
            status = "已处理";
        }
        alarmMap.put("status", status);
        alarmMap.put("timestamp", alarm.getCreatedAt() != null ? formatDateTime(alarm.getCreatedAt()) : null);
        alarmMap.put("resolvedTime", alarm.getResolvedAt() != null ? formatDateTime(alarm.getResolvedAt()) : null);
        alarmMap.put("resolvedBy", null); // 暂时没有处理人字段
        
        return alarmMap;
    }
    
    /**
     * 格式化日期时间，去除ISO 8601格式中的T字符
     * @param dateTime 日期时间对象
     * @return 格式化的日期时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
    
    /**
     * 将后端报警级别映射到前端三个级别
     * @param backendLevel 后端级别 (high, medium, low)
     * @return 前端级别 (high, medium, low) - 前端会根据这些值显示对应的颜色
     */
    private String convertToFrontendLevel(String backendLevel) {
        if (backendLevel == null) {
            return "low";
        }
        
        switch (backendLevel.toLowerCase()) {
            case "high":
                return "high";
            case "medium":
                return "medium";
            case "low":
                return "low";
            default:
                return "low";
        }
    }
    
    private Map<String, Object> convertToAlarmDetailMap(Alarm alarm) {
        Map<String, Object> alarmDetail = new HashMap<>();
        alarmDetail.put("id", alarm.getId());
        alarmDetail.put("deviceId", alarm.getVid()); // 使用VID作为deviceId
        alarmDetail.put("deviceName", alarm.getDeviceName());
        
        // 映射报警级别：将后端级别映射到前端三个级别
        String frontendLevel = convertToFrontendLevel(alarm.getAlarmLevel());
        alarmDetail.put("level", frontendLevel); // 前端期望的字段名是level，不是alarmLevel
        
        alarmDetail.put("alarmType", alarm.getAlarmType());
        alarmDetail.put("alarmContent", alarm.getMessage()); // 使用message字段作为报警内容
        
        // 映射状态值以匹配API规范
        String status = alarm.getStatus();
        if ("active".equals(status)) {
            status = "待处理";
        } else if ("resolved".equals(status)) {
            status = "已处理";
        }
        alarmDetail.put("status", status);
        alarmDetail.put("timestamp", formatDateTime(alarm.getCreatedAt()));
        alarmDetail.put("resolvedTime", alarm.getResolvedAt() != null ? formatDateTime(alarm.getResolvedAt()) : null);
        alarmDetail.put("resolvedBy", null); // 暂时没有处理人字段
        
        // 添加设备信息
        Map<String, Object> deviceInfo = new HashMap<>();
        Device device = deviceRepository.findByVid(alarm.getVid()).orElse(null);
        if (device != null) {
            deviceInfo.put("vid", device.getVid());
            deviceInfo.put("deviceType", device.getDeviceType());
        }
        alarmDetail.put("deviceInfo", deviceInfo);
        
        return alarmDetail;
    }

    @Override
    public void processAlarm(AlarmDataDto alarmData) {
        // 创建报警记录
        Alarm alarm = new Alarm();
        alarm.setVid(alarmData.getVid());
        
        // 设置设备名称（优先使用新格式的deviceName字段）
        if (alarmData.getDeviceName() != null && !alarmData.getDeviceName().isEmpty()) {
            alarm.setDeviceName(alarmData.getDeviceName());
        } else {
            // 如果没有提供设备名称，使用VID作为默认名称
            alarm.setDeviceName("设备" + alarmData.getVid());
        }
        
        // 设置报警类型（优先使用新格式的alarmType字段）
        if (alarmData.getAlarmType() != null && !alarmData.getAlarmType().isEmpty()) {
            alarm.setAlarmType(alarmData.getAlarmType());
        } else {
            alarm.setAlarmType("temperature"); // 默认类型
        }
        
        // 设置报警级别（优先使用新格式的level字段）
        if (alarmData.getLevel() != null && !alarmData.getLevel().isEmpty()) {
            alarm.setAlarmLevel(alarmData.getLevel());
        } else {
            alarm.setAlarmLevel("medium"); // 默认级别
        }
        
        // 设置报警消息（优先使用新格式的alarmContent字段）
        if (alarmData.getAlarmContent() != null && !alarmData.getAlarmContent().isEmpty()) {
            alarm.setMessage(alarmData.getAlarmContent());
        } else if (alarmData.getMessage() != null && !alarmData.getMessage().isEmpty()) {
            alarm.setMessage(alarmData.getMessage());
        } else {
            alarm.setMessage("设备" + alarmData.getVid() + "发生报警");
        }
        
        // 设置报警状态（优先使用新格式的status字段）
        if (alarmData.getStatus() != null && !alarmData.getStatus().isEmpty()) {
            alarm.setStatus(alarmData.getStatus());
        } else {
            alarm.setStatus("active"); // 默认状态
        }
        
        // 设置时间戳（优先使用新格式的timestamp字段）
        if (alarmData.getTimestamp() != null && !alarmData.getTimestamp().isEmpty()) {
            try {
                // 解析时间戳字符串为LocalDateTime
                LocalDateTime timestamp = LocalDateTime.parse(alarmData.getTimestamp(), 
                    java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                alarm.setCreatedAt(timestamp);
            } catch (Exception e) {
                System.err.println("时间戳解析失败，使用当前时间: " + e.getMessage());
                alarm.setCreatedAt(LocalDateTime.now());
            }
        } else {
            alarm.setCreatedAt(LocalDateTime.now());
        }
        
        // 设置device_id：从VID中提取数字部分
        try {
            String vid = alarmData.getVid();
            // 支持多种VID格式：DV0033, DEV0033, 0033等
            if (vid.startsWith("DV") || vid.startsWith("DEV")) {
                // 去掉前缀，提取数字部分
                String numberPart = vid.replaceAll("\\D+", ""); // 移除非数字字符
                if (!numberPart.isEmpty()) {
                    Long deviceId = Long.parseLong(numberPart);
                    alarm.setDeviceId(deviceId);
                    System.out.println("从VID " + vid + " 提取device_id: " + deviceId);
                } else {
                    System.err.println("VID " + vid + " 中不包含数字部分，device_id设置为null");
                    alarm.setDeviceId(null);
                }
            } else if (vid.matches("\\d+")) {
                // 如果VID本身就是纯数字
                Long deviceId = Long.parseLong(vid);
                alarm.setDeviceId(deviceId);
                System.out.println("VID为纯数字，设置device_id: " + deviceId);
            } else {
                System.err.println("无法从VID " + vid + " 中提取device_id，设置为null");
                alarm.setDeviceId(null);
            }
        } catch (Exception e) {
            System.err.println("从VID中提取device_id失败: " + e.getMessage());
            alarm.setDeviceId(null);
        }
        
        // 如果设备名称为空，使用默认的设备名称
        if (alarm.getDeviceName() == null || alarm.getDeviceName().isEmpty()) {
            alarm.setDeviceName("设备" + alarmData.getVid());
        }
        
        Alarm savedAlarm = alarmRepository.save(alarm);
        System.out.println("报警已处理并保存: " + alarm.getMessage());
        
        // 推送新报警的详细信息到前端
        alarmPushService.sendPriorityAlarm(savedAlarm);
        
        // 发送邮件通知
        try {
            emailNotificationService.sendAlarmEmail(savedAlarm);
            System.out.println("邮件通知已发送");
        } catch (Exception e) {
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
        
        // 发送钉钉通知
        try {
            sendDingTalkNotification(savedAlarm);
            System.out.println("钉钉通知已发送");
        } catch (Exception e) {
            System.err.println("发送钉钉通知失败: " + e.getMessage());
        }
        
        // 推送更新后的报警统计数据
        pushUpdatedStatistics();
    }

    @Override
    public void createAlarm(AlarmDto alarmDto) {
        // 创建报警记录
        Alarm alarm = new Alarm();
        alarm.setDeviceId(alarmDto.getDeviceId());
        alarm.setVid(alarmDto.getVid());
        alarm.setDeviceName(alarmDto.getDeviceName());
        alarm.setAlarmType(alarmDto.getAlarmType());
        alarm.setAlarmLevel(alarmDto.getAlarmLevel() != null ? alarmDto.getAlarmLevel() : "medium");
        alarm.setMessage(alarmDto.getMessage());
        alarm.setStatus(alarmDto.getStatus() != null ? alarmDto.getStatus() : "pending");
        alarm.setCreatedAt(alarmDto.getTimestamp() != null ? alarmDto.getTimestamp() : LocalDateTime.now());
        
        alarmRepository.save(alarm);
        System.out.println("报警已创建: " + alarm.getMessage());
        
        // 推送更新后的报警统计数据
        pushUpdatedStatistics();
    }

    @Override
    public ApiResponse<String> closeAlarm(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        Alarm alarm = alarmOpt.get();
        alarm.setStatus("closed"); // 设置为已关闭
        alarm.setUpdatedAt(LocalDateTime.now());
        alarm.setResolvedAt(LocalDateTime.now());
        alarmRepository.save(alarm);
        
        // 添加处理记录
        addAlarmHistory(alarmId, "close", "system", "报警已关闭");
        
        // 推送更新后的报警统计数据
        pushUpdatedStatistics();
        
        return ApiResponse.success("报警已关闭");
    }

    @Override
    public ApiResponse<Map<String, Object>> getAlarmHistory(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        List<AlarmHistory> historyList = alarmHistoryRepository.findByAlarmIdOrderByTimestampDesc(alarmId);
        
        List<Map<String, Object>> historyData = historyList.stream().map(history -> {
            Map<String, Object> historyMap = new HashMap<>();
            historyMap.put("id", history.getId());
            historyMap.put("alarmId", history.getAlarmId());
            historyMap.put("action", history.getAction());
            historyMap.put("operator", history.getOperator());
            historyMap.put("remark", history.getRemark());
            historyMap.put("timestamp", formatDateTime(history.getTimestamp()));
            return historyMap;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("history", historyData);
        
        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<String> addAlarmHistory(Long alarmId, String action, String operator, String remark) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        AlarmHistory history = new AlarmHistory();
        history.setAlarmId(alarmId);
        history.setAction(action);
        history.setOperator(operator);
        history.setRemark(remark);
        history.setTimestamp(LocalDateTime.now());
        
        alarmHistoryRepository.save(history);
        
        return ApiResponse.success("处理记录添加成功");
    }

    /**
     * 发送钉钉通知
     */
    private void sendDingTalkNotification(Alarm alarm) {
        try {
            // 获取钉钉设置
            DingTalkSettings settings = dingTalkSettingsRepository.findByUserId(1L);
            if (settings == null || !settings.getEnabled()) {
                System.out.println("钉钉通知未启用或设置不存在");
                return;
            }
            
            // 检查报警级别
            if (!isDingTalkNotifyLevelEnabled(settings, alarm.getAlarmLevel())) {
                System.out.println("报警级别 " + alarm.getAlarmLevel() + " 未启用钉钉通知");
                return;
            }
            
            // 检查免打扰时段
            if (isDingTalkInQuietHours(settings)) {
                System.out.println("当前处于免打扰时段，不发送钉钉通知");
                return;
            }
            
            // 发送钉钉消息
            String alarmTime = alarm.getCreatedAt() != null ? 
                alarm.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            boolean success = dingTalkService.sendAlarmMessage(
                settings.getWebhookUrl(), 
                settings.getSecret(), 
                alarm.getDeviceName(), 
                alarm.getAlarmLevel(), 
                alarm.getMessage(), 
                alarmTime
            );
            
            if (success) {
                System.out.println("钉钉通知发送成功");
            } else {
                System.out.println("钉钉通知发送失败");
            }
        } catch (Exception e) {
            System.err.println("发送钉钉通知异常: " + e.getMessage());
        }
    }

    /**
     * 检查钉钉报警级别是否启用
     */
    private boolean isDingTalkNotifyLevelEnabled(DingTalkSettings settings, String alarmLevel) {
        try {
            if (settings.getNotifyLevels() == null) {
                return true; // 默认所有级别都启用
            }
            
            List<String> enabledLevels = objectMapper.readValue(settings.getNotifyLevels(), List.class);
            return enabledLevels.contains(alarmLevel);
        } catch (Exception e) {
            System.err.println("检查钉钉报警级别失败: " + e.getMessage());
            return true; // 出错时默认启用
        }
    }

    /**
     * 检查钉钉是否处于免打扰时段
     */
    private boolean isDingTalkInQuietHours(DingTalkSettings settings) {
        try {
            if (settings.getQuietHours() == null) {
                return false; // 默认不启用免打扰
            }
            
            List<String> quietHours = objectMapper.readValue(settings.getQuietHours(), List.class);
            if (quietHours.size() < 2) {
                return false;
            }
            
            String startTime = quietHours.get(0);
            String endTime = quietHours.get(1);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(now.toLocalDate(), 
                java.time.LocalTime.parse(startTime));
            LocalDateTime end = LocalDateTime.of(now.toLocalDate(), 
                java.time.LocalTime.parse(endTime));
            
            // 处理跨天的情况
            if (end.isBefore(start)) {
                end = end.plusDays(1);
            }
            
            return now.isAfter(start) && now.isBefore(end);
        } catch (Exception e) {
            System.err.println("检查钉钉免打扰时段失败: " + e.getMessage());
            return false; // 出错时默认不启用免打扰
        }
    }
}