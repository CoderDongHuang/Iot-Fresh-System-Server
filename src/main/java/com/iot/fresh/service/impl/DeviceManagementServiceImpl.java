package com.iot.fresh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.*;
import com.iot.fresh.entity.Device;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DeviceManagementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceManagementServiceImpl implements DeviceManagementService {

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private DeviceDataRepository deviceDataRepository;

    @Autowired(required = false) // 可选注入，以防MQTT配置未启用
    private MqttPahoMessageHandler mqttOutbound;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ApiResponse<PaginatedResponse<DeviceDto>> getDeviceList(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        
        Page<Device> devicePage;
        if (keyword != null && !keyword.isEmpty()) {
            if (status != null) {
                // 在设备名称或VID中搜索，并按状态过滤
                devicePage = deviceRepository.findByDeviceNameContainingOrVidContainingAndStatus(keyword, keyword, status, pageable);
            } else {
                // 在设备名称或VID中搜索，不按状态过滤
                devicePage = deviceRepository.findByDeviceNameContainingOrVidContaining(keyword, keyword, pageable);
            }
        } else {
            if (status != null) {
                devicePage = deviceRepository.findByStatus(status, pageable);
            } else {
                devicePage = deviceRepository.findAll(pageable);
            }
        }
        
        // 转换为DeviceDto列表
        List<DeviceDto> deviceDtos = devicePage.getContent().stream().map(this::convertToDeviceDto).toList();
        
        // 创建分页响应
        PaginatedResponse<DeviceDto> paginatedResponse = new PaginatedResponse<>(
                deviceDtos,
                devicePage.getTotalElements(),
                pageNum,
                pageSize
        );
        
        return ApiResponse.success(paginatedResponse);
    }

    @Override
    public ApiResponse<DeviceDetailDto> getDeviceDetail(String vid) {
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (!deviceOpt.isPresent()) {
            return ApiResponse.error("设备不存在");
        }
        
        Device device = deviceOpt.get();
        DeviceDetailDto detailDto = new DeviceDetailDto();
        
        // 复制基本属性
        detailDto.setVid(device.getVid());
        detailDto.setDeviceName(device.getDeviceName());
        detailDto.setDeviceType(device.getDeviceType());
        detailDto.setStatus(device.getStatus());
        detailDto.setLocation(device.getLocation());
        detailDto.setContactPhone(device.getContactPhone());
        detailDto.setDescription(device.getDescription());
        
        // 设置多种时间格式以满足API规范
        detailDto.setLastOnlineTime(device.getLastHeartbeat());
        detailDto.setLastOnline_time(device.getLastHeartbeat());
        detailDto.setLast_heartbeat(device.getLastHeartbeat());
        detailDto.setLastHeartbeat(device.getLastHeartbeat());
        detailDto.setCreateTime(device.getCreatedAt());
        detailDto.setCreate_time(device.getCreatedAt());
        
        detailDto.setRemarks(device.getDescription()); // 使用描述字段作为备注
        
        // 获取设备当前数据
        DeviceCurrentDataDto currentData = getCurrentDataForDevice(vid);
        detailDto.setCurrentData(currentData);
        
        return ApiResponse.success(detailDto);
    }

    @Override
    public ApiResponse<DeviceCurrentDataDto> getRealTimeData(String vid) {
        DeviceCurrentDataDto currentData = getCurrentDataForDevice(vid);
        if (currentData != null) {
            return ApiResponse.success(currentData);
        } else {
            return ApiResponse.error("未找到设备实时数据");
        }
    }

    @Override
    public ApiResponse<DeviceStatusStatsDto> getStatusStats() {
        DeviceStatusStatsDto stats = new DeviceStatusStatsDto();
        
        // 总设备数
        long totalDevices = deviceRepository.count();
        stats.setTotalDevices((int) totalDevices);
        
        // 在线设备数 (状态为1)
        long onlineDevices = deviceRepository.countByStatus(1);
        stats.setOnlineDevices((int) onlineDevices);
        
        // 离线设备数 (状态为0)
        long offlineDevices = deviceRepository.countByStatus(0);
        stats.setOfflineDevices((int) offlineDevices);
        
        // 故障设备数 (状态为2)
        long faultDevices = deviceRepository.countByStatus(2);
        stats.setFaultDevices((int) faultDevices);
        
        return ApiResponse.success(stats);
    }

    @Override
    public ApiResponse<String> controlDevice(String vid, Map<String, Object> controlCommand) {
        // 验证设备是否存在
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (!deviceOpt.isPresent()) {
            return ApiResponse.error("设备不存在");
        }
        
        Device device = deviceOpt.get();
        
        // 可以控制各种状态的设备，包括离线、故障、维护等状态
        // 实际能否执行命令取决于设备本身的响应能力
        // 注释掉状态验证，允许控制所有状态的设备
        // if (device.getStatus() != 1) { // 1 表示在线
        //     return ApiResponse.error(400, "设备当前离线，无法执行控制命令");
        // }
        
        // 获取命令类型
        String command = (String) controlCommand.get("command");
        if (command == null || command.trim().isEmpty()) {
            return ApiResponse.error(400, "命令类型不能为空");
        }
        
        // 验证命令是否被支持
        boolean isSupportedCommand = isValidCommand(command);
        if (!isSupportedCommand) {
            return ApiResponse.error(400, "不支持的控制命令: " + command);
        }
        
        // 验证命令参数
        String validationResult = validateCommandParams(command, controlCommand.get("params"));
        if (validationResult != null) {
            return ApiResponse.error(400, validationResult);
        }
        
        // 执行命令（分为数据控制和设备控制）
        try {
            // 判断命令类型并执行相应操作
            if (command.startsWith("set")) {
                // 数据控制命令：修改device_data表的数据
                return handleDataControlCommand(vid, command, controlCommand.get("params"));
            } else {
                // 设备控制命令：修改设备状态或执行设备操作
                return handleDeviceControlCommand(vid, command, controlCommand.get("params"));
            }
        } catch (Exception e) {
            System.err.println("控制命令执行失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(500, "控制命令执行失败: " + e.getMessage());
        }
    }
    
    private String buildCommandMessage(String command, Object params) {
        // 构建发送到设备的命令消息
        Map<String, Object> message = new HashMap<>();
        message.put("command", command);
        
        if (params instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = (Map<String, Object>) params;
            if (!paramMap.isEmpty()) {
                message.put("params", paramMap);
            }
        }
        
        try {
             // 使用类级别的ObjectMapper实例来序列化JSON
             return objectMapper.writeValueAsString(message);
         } catch (Exception e) {
             // 如果JSON序列化失败，回退到手动构建字符串
             System.err.println("JSON序列化失败: " + e.getMessage());
             // 手动构建JSON字符串
             StringBuilder sb = new StringBuilder();
             sb.append("{\"command\":\"").append(command).append("\"");
             
             if (params instanceof Map) {
                 @SuppressWarnings("unchecked")
                 Map<String, Object> paramMap = (Map<String, Object>) params;
                 if (!paramMap.isEmpty()) {
                     sb.append(",\"params\":{");
                     boolean first = true;
                     for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                         if (!first) {
                             sb.append(",");
                         }
                         sb.append("\"").append(entry.getKey()).append("\":");
                         if (entry.getValue() instanceof String) {
                             sb.append("\"").append(entry.getValue()).append("\"");
                         } else {
                             sb.append(entry.getValue());
                         }
                         first = false;
                     }
                     sb.append("}");
                 }
             }
             sb.append("}");
             
             return sb.toString();
         }
    }
    
    private DeviceDto convertToDeviceDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setVid(device.getVid());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setStatus(device.getStatus());
        dto.setLocation(device.getLocation());
        dto.setContactPhone(device.getContactPhone());
        dto.setDescription(device.getDescription());
        
        // 设置制造商、型号和固件版本字段
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setFirmwareVersion(device.getFirmwareVersion());
        
        // 设置多种时间格式以满足API规范
        dto.setLastOnlineTime(device.getLastHeartbeat());
        dto.setLastOnline_time(device.getLastHeartbeat());
        dto.setLast_heartbeat(device.getLastHeartbeat());
        dto.setLastHeartbeat(device.getLastHeartbeat());
        dto.setCreateTime(device.getCreatedAt());
        dto.setCreate_time(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        
        // 获取设备当前数据
        DeviceCurrentDataDto currentData = getCurrentDataForDevice(device.getVid());
        dto.setCurrentData(currentData);
        
        return dto;
    }
    
    private DeviceCurrentDataDto getCurrentDataForDevice(String vid) {
        // 获取设备最新的数据
        List<DeviceData> deviceDataList = deviceDataRepository.findByVidOrderByCreatedAtDesc(vid, PageRequest.of(0, 1));
        
        if (!deviceDataList.isEmpty()) {
            DeviceData latestData = deviceDataList.get(0);
            DeviceCurrentDataDto currentData = new DeviceCurrentDataDto();
            currentData.setTin(latestData.getTin());
            currentData.setTout(latestData.getTout());
            currentData.setHin(latestData.getHin());
            currentData.setHout(latestData.getHout());
            currentData.setLxin(latestData.getLxin());
            currentData.setLxout(latestData.getLxout());
            currentData.setBrightness(latestData.getBrightness());
            currentData.setVStatus(latestData.getVstatus());
            
            return currentData;
        }
        
        // 如果没有数据，返回空对象
        return new DeviceCurrentDataDto();
    }
    
    /**
     * 验证控制命令是否被支持
     */
    private boolean isValidCommand(String command) {
        String[] supportedCommands = {
            // 设备控制命令
            "turnOn", "turnOff", "restart", "reset",
            // 数据控制命令
            "setTin", "setTout", "setHin", "setHout", "setLxin", "setLxout", "setBrightness"
        };
        
        for (String supported : supportedCommands) {
            if (supported.equals(command)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证命令参数
     */
    private String validateCommandParams(String command, Object params) {
        if (params == null) {
            return null; // 无参数的命令
        }
        
        if (!(params instanceof Map)) {
            return "参数格式不正确，应为对象格式";
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) params;
        
        switch (command) {
            // 温度控制命令
            case "setTin":
                if (!paramMap.containsKey("tin")) {
                    return "setTin命令需要tin参数";
                }
                Object tin = paramMap.get("tin");
                if (!(tin instanceof Number)) {
                    return "tin参数应为数字类型";
                }
                double tinValue = ((Number) tin).doubleValue();
                if (tinValue < -50 || tinValue > 100) {
                    return "内部温度参数应在-50到100之间";
                }
                break;
                
            case "setTout":
                if (!paramMap.containsKey("tout")) {
                    return "setTout命令需要tout参数";
                }
                Object tout = paramMap.get("tout");
                if (!(tout instanceof Number)) {
                    return "tout参数应为数字类型";
                }
                double toutValue = ((Number) tout).doubleValue();
                if (toutValue < -50 || toutValue > 100) {
                    return "外部温度参数应在-50到100之间";
                }
                break;
                
            // 湿度控制命令
            case "setHin":
                if (!paramMap.containsKey("hin")) {
                    return "setHin命令需要hin参数";
                }
                Object hin = paramMap.get("hin");
                if (!(hin instanceof Number)) {
                    return "hin参数应为数字类型";
                }
                int hinValue = ((Number) hin).intValue();
                if (hinValue < 0 || hinValue > 100) {
                    return "内部湿度参数应在0到100之间";
                }
                break;
                
            case "setHout":
                if (!paramMap.containsKey("hout")) {
                    return "setHout命令需要hout参数";
                }
                Object hout = paramMap.get("hout");
                if (!(hout instanceof Number)) {
                    return "hout参数应为数字类型";
                }
                int houtValue = ((Number) hout).intValue();
                if (houtValue < 0 || houtValue > 100) {
                    return "外部湿度参数应在0到100之间";
                }
                break;
                
            // 光照控制命令
            case "setLxin":
                if (!paramMap.containsKey("lxin")) {
                    return "setLxin命令需要lxin参数";
                }
                Object lxin = paramMap.get("lxin");
                if (!(lxin instanceof Number)) {
                    return "lxin参数应为数字类型";
                }
                int lxinValue = ((Number) lxin).intValue();
                if (lxinValue < 0 || lxinValue > 2000) {
                    return "内部光照参数应在0到2000之间";
                }
                break;
                
            case "setLxout":
                if (!paramMap.containsKey("lxout")) {
                    return "setLxout命令需要lxout参数";
                }
                Object lxout = paramMap.get("lxout");
                if (!(lxout instanceof Number)) {
                    return "lxout参数应为数字类型";
                }
                int lxoutValue = ((Number) lxout).intValue();
                if (lxoutValue < 0 || lxoutValue > 2000) {
                    return "外部光照参数应在0到2000之间";
                }
                break;
                
            // 亮度控制命令
            case "setBrightness":
                if (!paramMap.containsKey("brightness")) {
                    return "setBrightness命令需要brightness参数";
                }
                Object bright = paramMap.get("brightness");
                if (!(bright instanceof Number)) {
                    return "brightness参数应为数字类型";
                }
                int brightness = ((Number) bright).intValue();
                if (brightness < 0 || brightness > 100) {
                    return "亮度参数应在0到100之间";
                }
                break;
                
            // 固件升级命令
            case "updateFirmware":
                if (!paramMap.containsKey("version")) {
                    return "updateFirmware命令需要version参数";
                }
                Object version = paramMap.get("version");
                if (!(version instanceof String)) {
                    return "version参数应为字符串类型";
                }
                break;
                
            default:
                // 其他命令不需要参数或参数验证
                break;
        }
        
        return null; // 验证通过
    }
    
    /**
     * 处理数据控制命令 - 直接修改device_data表中对应设备的现有数据记录
     */
    @Transactional
    private ApiResponse<String> handleDataControlCommand(String vid, String command, Object params) {
        try {
            System.out.println("=== 开始执行数据控制命令 ===");
            System.out.println("设备VID: " + vid);
            System.out.println("命令: " + command);
            System.out.println("参数: " + params);
            
            // 获取设备最新的数据记录
            List<DeviceData> deviceDataList = deviceDataRepository.findByVidOrderByCreatedAtDesc(vid, PageRequest.of(0, 1));
            
            if (deviceDataList.isEmpty()) {
                System.err.println("未找到设备数据记录 - VID: " + vid);
                return ApiResponse.error("未找到设备数据记录");
            }
            
            DeviceData latestData = deviceDataList.get(0);
            System.out.println("找到最新数据记录 - ID: " + latestData.getId() + ", 创建时间: " + latestData.getCreatedAt());
            
            // 记录修改前的数据值
            System.out.println("修改前的数据值:");
            System.out.println("  Tin: " + latestData.getTin());
            System.out.println("  Tout: " + latestData.getTout());
            System.out.println("  Hin: " + latestData.getHin());
            System.out.println("  Hout: " + latestData.getHout());
            System.out.println("  Lxin: " + latestData.getLxin());
            System.out.println("  Lxout: " + latestData.getLxout());
            System.out.println("  Brightness: " + latestData.getBrightness());
            
            // 根据命令类型修改相应字段 - 直接修改现有记录
            if (params instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> paramMap = (Map<String, Object>) params;
                
                System.out.println("开始处理参数: " + paramMap);
                
                switch (command) {
                    case "setTin":
                        double tinValue = ((Number) paramMap.get("tin")).doubleValue();
                        System.out.println("设置内部温度: " + tinValue);
                        latestData.setTin(tinValue);
                        break;
                    case "setTout":
                        double toutValue = ((Number) paramMap.get("tout")).doubleValue();
                        System.out.println("设置外部温度: " + toutValue);
                        latestData.setTout(toutValue);
                        break;
                    case "setHin":
                        int hinValue = ((Number) paramMap.get("hin")).intValue();
                        System.out.println("设置内部湿度: " + hinValue);
                        latestData.setHin(hinValue);
                        break;
                    case "setHout":
                        int houtValue = ((Number) paramMap.get("hout")).intValue();
                        System.out.println("设置外部湿度: " + houtValue);
                        latestData.setHout(houtValue);
                        break;
                    case "setLxin":
                        int lxinValue = ((Number) paramMap.get("lxin")).intValue();
                        System.out.println("设置内部光照: " + lxinValue);
                        latestData.setLxin(lxinValue);
                        break;
                    case "setLxout":
                        int lxoutValue = ((Number) paramMap.get("lxout")).intValue();
                        System.out.println("设置外部光照: " + lxoutValue);
                        latestData.setLxout(lxoutValue);
                        break;
                    case "setBrightness":
                        int brightnessValue = ((Number) paramMap.get("brightness")).intValue();
                        System.out.println("设置亮度: " + brightnessValue);
                        latestData.setBrightness(brightnessValue);
                        break;
                    default:
                        System.err.println("未知的数据控制命令: " + command);
                        return ApiResponse.error("未知的数据控制命令: " + command);
                }
            } else {
                System.err.println("参数格式不正确，应为Map类型");
                return ApiResponse.error("参数格式不正确");
            }
            
            // 更新更新时间字段
            latestData.setUpdatedAt(LocalDateTime.now());
            
            // 保存修改后的数据记录
            System.out.println("开始更新现有数据记录...");
            DeviceData savedData = deviceDataRepository.save(latestData);
            deviceDataRepository.flush(); // 强制刷新到数据库
            System.out.println("数据更新成功 - 记录ID: " + savedData.getId());
            
            // 验证数据是否真的更新了
            List<DeviceData> verifiedDataList = deviceDataRepository.findByVidOrderByCreatedAtDesc(vid, PageRequest.of(0, 1));
            if (!verifiedDataList.isEmpty()) {
                DeviceData verifiedData = verifiedDataList.get(0);
                System.out.println("数据验证成功 - 记录ID: " + verifiedData.getId());
                System.out.println("数据验证成功 - 更新时间: " + verifiedData.getUpdatedAt());
                
                // 打印修改后的数据值
                System.out.println("修改后的数据值:");
                System.out.println("  Tin: " + verifiedData.getTin());
                System.out.println("  Tout: " + verifiedData.getTout());
                System.out.println("  Hin: " + verifiedData.getHin());
                System.out.println("  Hout: " + verifiedData.getHout());
                System.out.println("  Lxin: " + verifiedData.getLxin());
                System.out.println("  Lxout: " + verifiedData.getLxout());
                System.out.println("  Brightness: " + verifiedData.getBrightness());
            } else {
                System.err.println("数据验证失败 - 未找到更新后的记录");
            }
            
            System.out.println("=== 数据控制命令执行成功 ===");
            return ApiResponse.success("数据控制命令执行成功");
            
        } catch (Exception e) {
            System.err.println("数据控制命令执行失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(500, "数据控制命令执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理设备控制命令 - 修改设备状态
     */
    private ApiResponse<String> handleDeviceControlCommand(String vid, String command, Object params) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            if (!deviceOpt.isPresent()) {
                return ApiResponse.error("设备不存在");
            }
            
            Device device = deviceOpt.get();
            
            switch (command) {
                case "turnOn":
                    device.setStatus(1); // 在线状态
                    device.setLastHeartbeat(LocalDateTime.now());
                    break;
                    
                case "turnOff":
                    device.setStatus(0); // 离线状态
                    break;
                    
                case "restart":
                    // 重启：先设置为离线，再设置为在线
                    device.setStatus(0);
                    deviceRepository.save(device);
                    device.setStatus(1);
                    device.setLastHeartbeat(LocalDateTime.now());
                    break;
                    
                case "reset":
                    // 重置：将设备数据表中的7个数值数据全部设为0
                    resetDeviceData(vid);
                    device.setStatus(1); // 重置后设备在线
                    device.setLastHeartbeat(LocalDateTime.now());
                    break;
            }
            
            // 更新设备最后在线时间
            device.setLastHeartbeat(LocalDateTime.now());
            deviceRepository.save(device);
            
            System.out.println("设备控制命令执行成功 - 设备: " + vid + ", 命令: " + command);
            return ApiResponse.success("设备控制命令执行成功");
            
        } catch (Exception e) {
            System.err.println("设备控制命令执行失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(500, "设备控制命令执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置设备数据 - 将7个数值数据全部设为0
     */
    @Transactional
    private void resetDeviceData(String vid) {
        try {
            System.out.println("=== 开始执行设备数据重置 ===");
            System.out.println("设备VID: " + vid);
            
            // 获取设备最新的数据记录
            List<DeviceData> deviceDataList = deviceDataRepository.findByVidOrderByCreatedAtDesc(vid, PageRequest.of(0, 1));
            
            if (!deviceDataList.isEmpty()) {
                DeviceData latestData = deviceDataList.get(0);
                System.out.println("找到最新数据记录 - ID: " + latestData.getId() + ", 创建时间: " + latestData.getCreatedAt());
                
                // 记录重置前的数据值
                System.out.println("重置前的数据值:");
                System.out.println("  Tin: " + latestData.getTin());
                System.out.println("  Tout: " + latestData.getTout());
                System.out.println("  Hin: " + latestData.getHin());
                System.out.println("  Hout: " + latestData.getHout());
                System.out.println("  Lxin: " + latestData.getLxin());
                System.out.println("  Lxout: " + latestData.getLxout());
                System.out.println("  Brightness: " + latestData.getBrightness());
                
                // 将7个数值数据全部设为0 - 直接修改现有记录
                latestData.setTin(0.0);
                latestData.setTout(0.0);
                latestData.setHin(0);
                latestData.setHout(0);
                latestData.setLxin(0);
                latestData.setLxout(0);
                latestData.setBrightness(0);
                
                // 更新更新时间字段
                latestData.setUpdatedAt(LocalDateTime.now());
                
                // 保存重置后的数据
                System.out.println("开始更新现有数据记录...");
                DeviceData savedData = deviceDataRepository.save(latestData);
                deviceDataRepository.flush(); // 强制刷新到数据库
                System.out.println("数据重置成功 - 记录ID: " + savedData.getId());
                
                // 验证数据是否真的重置了
                List<DeviceData> verifiedDataList = deviceDataRepository.findByVidOrderByCreatedAtDesc(vid, PageRequest.of(0, 1));
                if (!verifiedDataList.isEmpty()) {
                    DeviceData verifiedData = verifiedDataList.get(0);
                    System.out.println("数据验证成功 - 记录ID: " + verifiedData.getId());
                    System.out.println("数据验证成功 - 更新时间: " + verifiedData.getUpdatedAt());
                    
                    // 打印重置后的数据值
                    System.out.println("重置后的数据值:");
                    System.out.println("  Tin: " + verifiedData.getTin());
                    System.out.println("  Tout: " + verifiedData.getTout());
                    System.out.println("  Hin: " + verifiedData.getHin());
                    System.out.println("  Hout: " + verifiedData.getHout());
                    System.out.println("  Lxin: " + verifiedData.getLxin());
                    System.out.println("  Lxout: " + verifiedData.getLxout());
                    System.out.println("  Brightness: " + verifiedData.getBrightness());
                } else {
                    System.err.println("数据验证失败 - 未找到重置后的记录");
                }
                
                System.out.println("=== 设备数据重置成功 ===");
            } else {
                System.err.println("未找到设备数据记录 - VID: " + vid);
            }
        } catch (Exception e) {
            System.err.println("设备数据重置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}