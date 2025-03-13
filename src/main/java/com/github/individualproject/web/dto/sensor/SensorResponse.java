package com.github.individualproject.web.dto.sensor;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class SensorResponse {
    private float temp;
    private float humid;
}
