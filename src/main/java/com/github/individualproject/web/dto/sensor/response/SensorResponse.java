package com.github.individualproject.web.dto.sensor.response;

import com.github.individualproject.repository.sensorData.SensorData;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class SensorResponse {
    private float temp;
    private float humid;//주석
    private LocalDateTime localDateTime = LocalDateTime.now();

    public static SensorResponse from(SensorData sensorData){
        return SensorResponse.builder()
                .humid(sensorData.getHumidity().floatValue())
                .temp(sensorData.getTemperature().floatValue())
                .localDateTime(sensorData.getRecordedAt())
                .build();
    }
}
