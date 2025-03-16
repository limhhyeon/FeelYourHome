package com.github.individualproject.web.dto.sensor.response;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class CurrentSensorData {
    private float temp;
    private float humid;
}
