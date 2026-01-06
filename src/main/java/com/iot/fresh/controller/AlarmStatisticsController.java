package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.AlarmStatisticsDto;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
public class AlarmStatisticsController {

    @Autowired
    private AlarmService alarmService;

    @GetMapping("/statistics")
    public ApiResponse<List<AlarmStatisticsDto>> getAlarmStatistics() {
        return alarmService.getAlarmStatistics();
    }
}