package com.github.individualproject.web.dto.sensor.response;

import com.github.individualproject.repository.sensorData.SensorData;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class CurrentSensorData {
    private float temp;
    private float humid;
    private LocalDateTime localDateTime = LocalDateTime.now();
    private String humidStatus;

    public static CurrentSensorData from(SensorData sensorData){
        return CurrentSensorData.builder()
                .humid(sensorData.getHumidity().floatValue())
                .temp(sensorData.getTemperature().floatValue())
                .localDateTime(sensorData.getRecordedAt())
                .humidStatus(sensorData.getHumidityStatus().toString())
                .build();
    }
}
