package com.iot.fresh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.Device;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private DeviceDataRepository deviceDataRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ApiResponse<DeviceDataDto> saveDeviceData(DeviceDataDto deviceDataDto) {
        DeviceData deviceData = new DeviceData();
        deviceData.setVid(deviceDataDto.getVid());
        deviceData.setDeviceName(deviceDataDto.getDeviceName());
        deviceData.setDeviceType(deviceDataDto.getDeviceType());
        deviceData.setTin(deviceDataDto.getTin());
        deviceData.setTout(deviceDataDto.getTout());
        deviceData.setHin(deviceDataDto.getHin());
        deviceData.setHout(deviceDataDto.getHout());
        deviceData.setLxin(deviceDataDto.getLxin());
        deviceData.setLight(deviceDataDto.getLight());
        deviceData.setPid(deviceDataDto.getPid());
        deviceData.setVstatus(deviceDataDto.getVstatus());
        deviceData.setBattery(deviceDataDto.getBattery());
        deviceData.setBrightness(deviceDataDto.getBrightness());
        deviceData.setSpeedM1(deviceDataDto.getSpeedM1());
        deviceData.setSpeedM2(deviceDataDto.getSpeedM2());
        deviceData.setTimestamp(deviceDataDto.getTimestamp()); // 使用转换后的时间戳

        DeviceData savedData = deviceDataRepository.save(deviceData);
        DeviceDataDto savedDto = convertToDeviceDataDto(savedData);
        return ApiResponse.success("数据保存成功", savedDto);
    }

    @Override
    public ApiResponse<List<DeviceDataDto>> getDeviceRealTimeData(String vid) {
        List<DeviceData> deviceDataList = deviceDataRepository.findByVid(vid);
        List<DeviceDataDto> deviceDataDtos = deviceDataList.stream()
                .map(this::convertToDeviceDataDto)
                .collect(Collectors.toList());
        return ApiResponse.success(deviceDataDtos);
    }

    @Override
    public ApiResponse<List<DeviceDataDto>> getDeviceHistoryData(String vid, LocalDateTime startTime, LocalDateTime endTime) {
        List<DeviceData> deviceDataList = deviceDataRepository.findByVidAndTimeRange(vid, startTime, endTime);
        List<DeviceDataDto> deviceDataDtos = deviceDataList.stream()
                .map(this::convertToDeviceDataDto)
                .collect(Collectors.toList());
        return ApiResponse.success(deviceDataDtos);
    }

    @Override
    public void processDeviceDataFromMqtt(String vid, String payload) {
        // 解析MQTT消息并根据内容决定处理方式
        try {
            DeviceDataDto deviceDataDto = parseDeviceDataFromJson(vid, payload);
            if (deviceDataDto != null) {
                // 输出vstatus的值以进行调试
                System.out.println("Received device data for VID: " + vid + ", vstatus: " + deviceDataDto.getVstatus());
                
                // 检查是否是设备状态更新（包含vstatus字段）
                if (deviceDataDto.getVstatus() != null) {
                    // 这是设备状态更新，更新设备主状态
                    System.out.println("Received status update, updating device status for VID: " + vid + ", vstatus: " + deviceDataDto.getVstatus());
                    updateDeviceMainStatus(vid, deviceDataDto.getVstatus());
                    
                    // 对于状态更新，不存储到device_data表
                    System.out.println("Status update processed for VID: " + vid + ", skipped saving to device_data table");
                } else {
                    // 这是普通数据上报，包含温度、湿度、速度、亮度等参数，存储到device_data表
                    saveDeviceData(deviceDataDto);
                    System.out.println("Received and saved device data for VID: " + vid + 
                        ", tin: " + deviceDataDto.getTin() + 
                        ", tout: " + deviceDataDto.getTout() + 
                        ", hin: " + deviceDataDto.getHin() + 
                        ", hout: " + deviceDataDto.getHout() + 
                        ", lxin: " + deviceDataDto.getLxin() + 
                        ", light: " + deviceDataDto.getLight() + 
                        ", battery: " + deviceDataDto.getBattery() + 
                        ", brightness: " + deviceDataDto.getBrightness() + 
                        ", speedM1: " + deviceDataDto.getSpeedM1() + 
                        ", speedM2: " + deviceDataDto.getSpeedM2());
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 更新设备的主状态（在线/离线/故障等）
     */
    private void updateDeviceMainStatus(String vid, Integer vstatus) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                
                // 将vstatus转换为设备状态
                // 假设vstatus: 0=离线, 1=在线, 2=故障, 3=维护
                // 这里可以根据实际需要调整映射关系
                device.setStatus(vstatus);
                device.setLastHeartbeat(LocalDateTime.now());
                
                deviceRepository.save(device);
                System.out.println("Updated device status for VID: " + vid + ", status: " + vstatus);
            }
        } catch (Exception e) {
            System.err.println("Error updating device status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DeviceDataDto parseDeviceDataFromJson(String vid, String json) {
        try {
            DeviceDataDto dto = objectMapper.readValue(json, DeviceDataDto.class);
            // 如果JSON中没有VID，使用传入的VID
            if (dto.getVid() == null || dto.getVid().isEmpty()) {
                dto.setVid(vid);
            }
            return dto;
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            // 如果Jackson解析失败，回退到手动解析
            return parseDeviceDataManually(vid, json);
        }
    }
    
    // 手动解析方法作为备用
    private DeviceDataDto parseDeviceDataManually(String vid, String json) {
        try {
            DeviceDataDto dto = new DeviceDataDto();
            dto.setVid(vid); // 使用传入的VID
            
            // 简单解析JSON字符串
            if (json.contains("\"deviceType\"")) {
                int start = json.indexOf("\"deviceType\":\"") + 14;
                int end = json.indexOf("\"", start);
                if (start > 13 && end > start) {
                    dto.setDeviceType(json.substring(start, end));
                }
            }
            
            if (json.contains("\"tin\"")) {
                int start = json.indexOf("\"tin\":") + 6;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 5 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTin(Double.parseDouble(value));
                }
            }
            
            if (json.contains("\"tout\"")) {
                int start = json.indexOf("\"tout\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTout(Double.parseDouble(value));
                }
            }
            
            // 支持小写"lxin"格式
            if (json.contains("\"lxin\"")) {
                int start = json.indexOf("\"lxin\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setLxin(Integer.parseInt(value));
                }
            }
            // 支持大写"LXin"格式（来自硬件）
            else if (json.contains("\"LXin\"")) {
                int start = json.indexOf("\"LXin\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setLxin(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"pid\"")) {
                int start = json.indexOf("\"pid\":\"") + 7;
                int end = json.indexOf("\"", start);
                if (start > 6 && end > start) {
                    dto.setPid(json.substring(start, end));
                }
            }
            
            if (json.contains("\"vstatus\"")) {
                int start = json.indexOf("\"vstatus\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setVstatus(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"battery\"")) {
                int start = json.indexOf("\"battery\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setBattery(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"brightness\"")) {
                int start = json.indexOf("\"brightness\":") + 13;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 12 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setBrightness(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"speedM1\"")) {
                int start = json.indexOf("\"speedM1\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setSpeedM1(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"speedM2\"")) {
                int start = json.indexOf("\"speedM2\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setSpeedM2(Integer.parseInt(value));
                }
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error parsing JSON manually: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private DeviceDataDto convertToDeviceDataDto(DeviceData deviceData) {
        DeviceDataDto dto = new DeviceDataDto();
        dto.setId(deviceData.getId());
        dto.setVid(deviceData.getVid());
        dto.setDeviceName(deviceData.getDeviceName());
        dto.setDeviceType(deviceData.getDeviceType());
        dto.setTin(deviceData.getTin());
        dto.setTout(deviceData.getTout());
        dto.setHin(deviceData.getHin());
        dto.setHout(deviceData.getHout());
        dto.setLxin(deviceData.getLxin());
        dto.setLight(deviceData.getLight());
        dto.setPid(deviceData.getPid());
        dto.setVstatus(deviceData.getVstatus());
        dto.setBattery(deviceData.getBattery());
        dto.setBrightness(deviceData.getBrightness());
        dto.setSpeedM1(deviceData.getSpeedM1());
        dto.setSpeedM2(deviceData.getSpeedM2());
        // 设置转换后的时间戳，使用设备数据的时间戳或创建时间
        if (deviceData.getTimestamp() != null) {
            dto.setTimestamp(deviceData.getTimestamp());
        } else {
            dto.setTimestamp(deviceData.getCreatedAt());
        }
        return dto;
    }
    
    @Override
    public ApiResponse<List<Map<String, Object>>> getLightDataByVid(String vid, LocalDateTime startTime, LocalDateTime endTime) {
        // 设置默认时间范围（1小时前到现在）
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        // 查询设备光照数据
        List<DeviceData> deviceDataList = deviceDataRepository.findByVidAndTimeRange(vid, startTime, endTime);

        // 转换为前端期望的格式
        List<Map<String, Object>> result = deviceDataList.stream().map(data -> {
            Map<String, Object> item = new java.util.HashMap<>();
            // 使用ISO 8601格式的时间字符串
            item.put("timestamp", data.getCreatedAt().toString());
            // 优先使用lxin字段，如果没有则使用light字段
            Integer lightValue = data.getLxin() != null ? data.getLxin() : data.getLight();
            item.put("value", lightValue != null ? lightValue : 0);
            return item;
        }).collect(java.util.stream.Collectors.toList());

        return ApiResponse.success("获取成功", result);
    }
    
    @Override
    public void updateDeviceStatus(String vid, Integer status) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                device.setStatus(status);
                device.setLastHeartbeat(LocalDateTime.now());
                deviceRepository.save(device);
                System.out.println("Updated device status in devices table for VID: " + vid + ", status: " + status);
            }
        } catch (Exception e) {
            System.err.println("Error updating device status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public DeviceData getLatestDeviceData(String vid) {
        return deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
    }
    
    @Override
    public ApiResponse<com.iot.fresh.dto.PaginatedResponse<DeviceDataDto>> getDeviceHistoryDataWithPagination(
            String vid, String dataType, LocalDateTime startTime, LocalDateTime endTime, Integer pageNum, Integer pageSize) {
        // 验证参数
        if (pageNum <= 0 || pageSize <= 0) {
            return ApiResponse.error("页码和页面大小必须大于0");
        }

        // 设置默认值
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30); // 默认查询最近30天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now(); // 默认为当前时间
        }

        // 创建分页请求
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize, 
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        // 调用Repository查询数据
        org.springframework.data.domain.Page<DeviceData> deviceDataPage = 
            deviceDataRepository.findByVidAndTimeRangeWithPagination(vid, startTime, endTime, pageable);

        // 如果指定了dataType，则进一步过滤结果
        List<DeviceData> filteredDataList;
        if (dataType != null && !dataType.trim().isEmpty()) {
            // 根据dataType过滤数据，例如：如果dataType是"temperature"，则只保留有温度数据的记录
            filteredDataList = deviceDataPage.getContent().stream()
                .filter(data -> hasDataForType(data, dataType))
                .collect(Collectors.toList());
        } else {
            filteredDataList = deviceDataPage.getContent();
        }

        // 转换为DTO并返回
        List<DeviceDataDto> dtoList = filteredDataList.stream()
                .map(this::convertToDeviceDataDto)
                .collect(Collectors.toList());

        // 由于我们进行了手动过滤，需要重新计算分页信息
         // 为了保持正确的分页行为，我们需要基于过滤后的数据重新计算分页
         int totalFiltered = dtoList.size();
         int startIndex = (pageNum - 1) * pageSize;
         int endIndex = Math.min(startIndex + pageSize, totalFiltered);
         
         List<DeviceDataDto> pagedDtoList;
         if (startIndex >= totalFiltered) {
             pagedDtoList = new ArrayList<>();
         } else {
             pagedDtoList = dtoList.subList(startIndex, endIndex);
         }

         com.iot.fresh.dto.PaginatedResponse<DeviceDataDto> paginatedResponse = new com.iot.fresh.dto.PaginatedResponse<>(
                 pagedDtoList,
                 (long) totalFiltered, // 总数量
                 pageNum,              // 页码
                 pageSize              // 每页数量
         );

        return ApiResponse.success(paginatedResponse);
    }
    
    /**
     * 检查设备数据是否包含指定类型的数据
     * @param data 设备数据
     * @param dataType 数据类型 (如 "temperature", "humidity", "light", "battery", "status")
     * @return 是否包含该类型的数据
     */
    private boolean hasDataForType(DeviceData data, String dataType) {
        // 如果未指定数据类型或为空字符串，则包含所有数据
        if (dataType == null || dataType.trim().length() == 0) {
            return true;
        }
        
        switch (dataType.toLowerCase().trim()) {
            case "temperature":
                return data.getTin() != null || data.getTout() != null;
            case "humidity":
                return data.getHin() != null || data.getHout() != null;
            case "light":
            case "illumination":
                return data.getLxin() != null || data.getLight() != null;
            case "battery":
                return data.getBattery() != null;
            case "status":
                return data.getVstatus() != null;
            case "speed":
            case "fan_speed":
                return data.getSpeedM1() != null || data.getSpeedM2() != null;
            case "brightness":
                return data.getBrightness() != null;
            default:
                // 如果数据类型未知，返回true以包含数据
                return true;
        }
    }
    
    @Override
    public Map<String, Object> getDeviceDataStatistics(String vid, LocalDateTime startTime, LocalDateTime endTime) {
        List<DeviceData> deviceDataList;
        
        if (vid != null && !vid.trim().isEmpty()) {
            // 查询特定设备的数据
            deviceDataList = deviceDataRepository.findByVidAndTimeRange(vid, startTime, endTime);
        } else {
            // 查询所有设备的数据
            deviceDataList = deviceDataRepository.findByTimeRangeWithNoPagination(startTime, endTime);
        }
        
        // 初始化统计结果
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        if (deviceDataList.isEmpty()) {
            statistics.put("totalRecords", 0);
            statistics.put("avgTemp", 0.0);
            statistics.put("maxTemp", 0.0);
            statistics.put("minTemp", 0.0);
            statistics.put("avgHumidity", 0.0);
            statistics.put("avgLight", 0.0);
            statistics.put("detail", java.util.Collections.emptyList());
            return statistics;
        }
        
        // 计算统计数据
        long totalRecords = deviceDataList.size();
        
        // 计算平均温度
        double avgTemp = deviceDataList.stream()
                .filter(data -> data.getTin() != null || data.getTout() != null)
                .mapToDouble(data -> {
                    Double internalTemp = data.getTin();
                    Double externalTemp = data.getTout();
                    if (internalTemp != null && externalTemp != null) {
                        return (internalTemp + externalTemp) / 2.0;
                    } else if (internalTemp != null) {
                        return internalTemp;
                    } else if (externalTemp != null) {
                        return externalTemp;
                    } else {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
        
        // 计算最高温度
        double maxTemp = deviceDataList.stream()
                .filter(data -> data.getTin() != null || data.getTout() != null)
                .mapToDouble(data -> {
                    Double internalTemp = data.getTin();
                    Double externalTemp = data.getTout();
                    if (internalTemp != null && externalTemp != null) {
                        return Math.max(internalTemp, externalTemp);
                    } else if (internalTemp != null) {
                        return internalTemp;
                    } else if (externalTemp != null) {
                        return externalTemp;
                    } else {
                        return 0.0;
                    }
                })
                .max()
                .orElse(0.0);
        
        // 计算最低温度
        double minTemp = deviceDataList.stream()
                .filter(data -> data.getTin() != null || data.getTout() != null)
                .mapToDouble(data -> {
                    Double internalTemp = data.getTin();
                    Double externalTemp = data.getTout();
                    if (internalTemp != null && externalTemp != null) {
                        return Math.min(internalTemp, externalTemp);
                    } else if (internalTemp != null) {
                        return internalTemp;
                    } else if (externalTemp != null) {
                        return externalTemp;
                    } else {
                        return 0.0;
                    }
                })
                .min()
                .orElse(0.0);
        
        // 计算平均湿度
        double avgHumidity = deviceDataList.stream()
                .filter(data -> data.getHin() != null || data.getHout() != null)
                .mapToDouble(data -> {
                    Double internalHumidity = data.getHin();
                    Double externalHumidity = data.getHout();
                    if (internalHumidity != null && externalHumidity != null) {
                        return (internalHumidity + externalHumidity) / 2.0;
                    } else if (internalHumidity != null) {
                        return internalHumidity;
                    } else if (externalHumidity != null) {
                        return externalHumidity;
                    } else {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
        
        // 计算平均光照
        double avgLight = deviceDataList.stream()
                .filter(data -> data.getLxin() != null)
                .mapToInt(DeviceData::getLxin)
                .average()
                .orElse(0.0);
        
        // 设置统计结果
        statistics.put("totalRecords", totalRecords);
        statistics.put("avgTemp", avgTemp);
        statistics.put("maxTemp", maxTemp);
        statistics.put("minTemp", minTemp);
        statistics.put("avgHumidity", avgHumidity);
        statistics.put("avgLight", avgLight);
        
        // 构建详细统计信息
        List<Map<String, Object>> details = new java.util.ArrayList<>();
        if (vid != null && !vid.trim().isEmpty()) {
            // 如果是特定设备，则添加该设备的详细统计
            Map<String, Object> deviceDetail = new java.util.HashMap<>();
            deviceDetail.put("deviceName", deviceDataList.get(0).getDeviceName() != null ? 
                             deviceDataList.get(0).getDeviceName() : "Unknown Device");
            deviceDetail.put("vid", vid);
            deviceDetail.put("avgTemp", avgTemp);
            deviceDetail.put("maxTemp", maxTemp);
            deviceDetail.put("minTemp", minTemp);
            deviceDetail.put("avgHumidity", avgHumidity);
            deviceDetail.put("avgLight", avgLight);
            deviceDetail.put("recordCount", totalRecords);
            deviceDetail.put("timeRange", startTime.toString() + " ~ " + endTime.toString());
            
            details.add(deviceDetail);
        } else {
            // 如果是所有设备，则按设备分组统计
            java.util.Map<String, List<DeviceData>> groupedByDevice = deviceDataList.stream()
                    .collect(java.util.stream.Collectors.groupingBy(DeviceData::getVid));
            
            for (java.util.Map.Entry<String, List<DeviceData>> entry : groupedByDevice.entrySet()) {
                String deviceVid = entry.getKey();
                List<DeviceData> deviceList = entry.getValue();
                
                // 查找设备名称
                String deviceName = deviceList.get(0).getDeviceName();
                if (deviceName == null || deviceName.isEmpty()) {
                    Optional<Device> deviceOpt = deviceRepository.findByVid(deviceVid);
                    if (deviceOpt.isPresent()) {
                        deviceName = deviceOpt.get().getDeviceName();
                    } else {
                        deviceName = "Unknown Device";
                    }
                }
                
                // 计算该设备的统计信息
                double deviceAvgTemp = deviceList.stream()
                        .filter(data -> data.getTin() != null || data.getTout() != null)
                        .mapToDouble(data -> {
                            Double internalTemp = data.getTin();
                            Double externalTemp = data.getTout();
                            if (internalTemp != null && externalTemp != null) {
                                return (internalTemp + externalTemp) / 2.0;
                            } else if (internalTemp != null) {
                                return internalTemp;
                            } else if (externalTemp != null) {
                                return externalTemp;
                            } else {
                                return 0.0;
                            }
                        })
                        .average()
                        .orElse(0.0);
                
                double deviceMaxTemp = deviceList.stream()
                        .filter(data -> data.getTin() != null || data.getTout() != null)
                        .mapToDouble(data -> {
                            Double internalTemp = data.getTin();
                            Double externalTemp = data.getTout();
                            if (internalTemp != null && externalTemp != null) {
                                return Math.max(internalTemp, externalTemp);
                            } else if (internalTemp != null) {
                                return internalTemp;
                            } else if (externalTemp != null) {
                                return externalTemp;
                            } else {
                                return 0.0;
                            }
                        })
                        .max()
                        .orElse(0.0);
                
                double deviceMinTemp = deviceList.stream()
                        .filter(data -> data.getTin() != null || data.getTout() != null)
                        .mapToDouble(data -> {
                            Double internalTemp = data.getTin();
                            Double externalTemp = data.getTout();
                            if (internalTemp != null && externalTemp != null) {
                                return Math.min(internalTemp, externalTemp);
                            } else if (internalTemp != null) {
                                return internalTemp;
                            } else if (externalTemp != null) {
                                return externalTemp;
                            } else {
                                return 0.0;
                            }
                        })
                        .min()
                        .orElse(0.0);
                
                double deviceAvgHumidity = deviceList.stream()
                        .filter(data -> data.getHin() != null || data.getHout() != null)
                        .mapToDouble(data -> {
                            Double internalHumidity = data.getHin();
                            Double externalHumidity = data.getHout();
                            if (internalHumidity != null && externalHumidity != null) {
                                return (internalHumidity + externalHumidity) / 2.0;
                            } else if (internalHumidity != null) {
                                return internalHumidity;
                            } else if (externalHumidity != null) {
                                return externalHumidity;
                            } else {
                                return 0.0;
                            }
                        })
                        .average()
                        .orElse(0.0);
                
                double deviceAvgLight = deviceList.stream()
                        .filter(data -> data.getLxin() != null)
                        .mapToInt(DeviceData::getLxin)
                        .average()
                        .orElse(0.0);
                
                Map<String, Object> deviceDetail = new java.util.HashMap<>();
                deviceDetail.put("deviceName", deviceName);
                deviceDetail.put("vid", deviceVid);
                deviceDetail.put("avgTemp", deviceAvgTemp);
                deviceDetail.put("maxTemp", deviceMaxTemp);
                deviceDetail.put("minTemp", deviceMinTemp);
                deviceDetail.put("avgHumidity", deviceAvgHumidity);
                deviceDetail.put("avgLight", deviceAvgLight);
                deviceDetail.put("recordCount", (long) deviceList.size());
                deviceDetail.put("timeRange", startTime.toString() + " ~ " + endTime.toString());
                
                details.add(deviceDetail);
            }
        }
        
        statistics.put("detail", details);
        
        return statistics;
    }
}