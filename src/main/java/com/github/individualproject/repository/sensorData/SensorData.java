package com.github.individualproject.repository.sensorData;

import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.web.dto.sensor.SensorResponse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "sensorDataId")
@Entity
@Table(name = "sensor_data")
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_data_id")
    private Long sensorDataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_product_id",nullable = false)
    private UserProduct userProduct;

    @Column(name = "temperature", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidity", nullable = false, precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "recorded_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime recordedAt;

//    public static SensorData of(UserProduct userProduct, SensorResponse sensorResponse){
//        return SensorData.builder()
//                .userProduct(userProduct)
//                .temperature(BigDecimal.valueOf(sensorResponse.getTemp()))
//                .humidity(BigDecimal.valueOf(sensorResponse.getHumid()))
//                .recordedAt(LocalDateTime.now())
//                .build();
//    }
    public static SensorData of(UserProduct userProduct, BigDecimal temperature, BigDecimal humidity, LocalDateTime recordedAt) {
        return SensorData.builder()
                .userProduct(userProduct)
                .temperature(temperature)
                .humidity(humidity)
                .recordedAt(recordedAt)
                .build();
    }

}
