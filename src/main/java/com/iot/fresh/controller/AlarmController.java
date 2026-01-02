package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @GetMapping
    public ApiResponse<List<AlarmDto>> getAlarms(@RequestParam(required = false) String status,
                                                 @RequestParam(required = false) Long deviceId) {
        if (deviceId != null) {
            return alarmService.getAlarmsByDeviceId(deviceId);
        } else if (status != null) {
            return alarmService.getAlarmsByStatus(status);
        } else {
            return alarmService.getAllAlarms();
        }
    }

    @PostMapping("/{id}/handle")
    public ApiResponse<AlarmDto> handleAlarm(@PathVariable Long id) {
        return alarmService.resolveAlarm(id);
    }

    @GetMapping("/settings")
    public ApiResponse<?> getAlarmSettings() {
        return ApiResponse.success("报警设置获取接口");
    }

    @PostMapping("/settings")
    public ApiResponse<String> updateAlarmSettings(@RequestBody Object settings) {
        return ApiResponse.success("报警设置更新成功", "OK");
    }
}