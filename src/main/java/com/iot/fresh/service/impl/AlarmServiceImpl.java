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
import com.iot.fresh.service.AlarmService;
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
        alarmMap.put("timestamp", alarm.getCreatedAt() != null ? alarm.getCreatedAt().toString() : null); // 使用ISO 8601格式
        alarmMap.put("resolvedTime", alarm.getResolvedAt() != null ? alarm.getResolvedAt().toString() : null);
        alarmMap.put("resolvedBy", null); // 暂时没有处理人字段
        
        return alarmMap;
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
        alarmDetail.put("timestamp", alarm.getCreatedAt().toString()); // 使用ISO 8601格式
        alarmDetail.put("resolvedTime", alarm.getResolvedAt() != null ? alarm.getResolvedAt().toString() : null);
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
        alarm.setAlarmType(alarmData.getAlarmType());
        alarm.setAlarmLevel("medium"); // 默认级别
        alarm.setMessage(alarmData.getMessage());
        alarm.setStatus("pending"); // 使用API规范中的状态值
        alarm.setCreatedAt(alarmData.getTimestamp() != null ? alarmData.getTimestamp() : LocalDateTime.now());
        
        // 查找设备信息
        Optional<Device> deviceOpt = deviceRepository.findByVid(alarmData.getVid());
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            alarm.setDeviceId(device.getId());
            alarm.setDeviceName(device.getDeviceName());
        }
        
        alarmRepository.save(alarm);
        System.out.println("报警已处理并保存: " + alarm.getMessage());
        
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
            historyMap.put("timestamp", history.getTimestamp().toString());
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
}