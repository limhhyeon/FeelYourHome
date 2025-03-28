package com.github.individualproject.repository.sensorData;

import java.util.List;

public interface CustomSensorDataRepository {
    void saveAllBatch(List<SensorData> sensorDataList);
}
