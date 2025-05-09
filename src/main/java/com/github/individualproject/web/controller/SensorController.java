package com.github.individualproject.web.controller;

import com.github.individualproject.repository.user.CurrentUser;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.service.sensor.SensorService;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.product.request.BuyProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensor")
@Slf4j
@RequiredArgsConstructor
public class SensorController {
    private final SensorService sensorService;
    @GetMapping("/{userProductId}")
    public ResponseDto latestSensor(@CurrentUser User user,@PathVariable("userProductId")Long userProductId){
        return sensorService.latestSensorResult(user,userProductId);
    }
    @GetMapping("/{userProductId}/today")
    public ResponseDto todayMySensorList(@CurrentUser User user,@PathVariable("userProductId")Long userProductId){
        return sensorService.todayMySensorListResult(user,userProductId);
    }
    @GetMapping("/{userProductId}/week")
    public ResponseDto weekMySensorList(@CurrentUser User user,@PathVariable("userProductId")Long userProductId){
        return  sensorService.weekMySensorListResult(user,userProductId);
    }
    @GetMapping("/{userProductId}/week-avg")
    public ResponseDto weekAvgMySensorList(@CurrentUser User user,@PathVariable("userProductId")Long userProductId){
        return  sensorService.weekAvgMySensorListResult(user,userProductId);
    }
}
