package com.github.individualproject.web.dto.sensor;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class SensorResponse {
    private float temp;
    private float humid;
    private LocalDateTime localDateTime = LocalDateTime.now();
}
