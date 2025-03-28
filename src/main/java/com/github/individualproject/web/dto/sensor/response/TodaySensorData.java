package com.github.individualproject.web.dto.sensor.response;

import com.github.individualproject.repository.sensorData.HumidityStatus;
import com.github.individualproject.repository.sensorData.SensorData;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class TodaySensorData {
    private double temp;
    private double humid;
    private HumidityStatus humidityStatus;
    public static TodaySensorData of(SensorData sensorData, HumidityStatus humidityStatus){
        return TodaySensorData.builder()
                .humid(sensorData.getHumidity().floatValue())
                .temp(sensorData.getTemperature().floatValue())
                .humidityStatus(humidityStatus)
                .build();
    }
}
