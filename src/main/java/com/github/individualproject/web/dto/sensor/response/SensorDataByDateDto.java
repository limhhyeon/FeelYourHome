package com.github.individualproject.web.dto.sensor.response;

import java.time.LocalDate;
import java.util.List;

public record SensorDataByDateDto(
        LocalDate date,
        List<CurrentSensorData> sensorResponses) {
}
