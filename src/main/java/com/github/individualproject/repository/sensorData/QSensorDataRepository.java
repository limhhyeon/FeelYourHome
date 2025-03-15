package com.github.individualproject.repository.sensorData;

import java.time.LocalDateTime;

public interface QSensorDataRepository {
    void deleteOldSensorData(LocalDateTime cutoffDate);
}
