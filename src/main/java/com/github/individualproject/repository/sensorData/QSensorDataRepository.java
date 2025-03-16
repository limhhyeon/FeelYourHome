package com.github.individualproject.repository.sensorData;

import com.github.individualproject.repository.userProduct.UserProduct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface QSensorDataRepository {
    void deleteOldSensorData(LocalDateTime cutoffDate);
    List<SensorData> findAllByRecordedAtDateAndUserProduct(LocalDate date, UserProduct userProduct);
}
