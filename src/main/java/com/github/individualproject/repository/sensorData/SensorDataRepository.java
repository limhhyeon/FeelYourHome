package com.github.individualproject.repository.sensorData;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData,Long>,CustomSensorDataRepository {
    @Modifying
    @Transactional
    @Query("DELETE FROM SensorData s WHERE s.recordedAt < :cutoffDate")
    void deleteOldSensorData(@Param("cutoffDate") LocalDateTime cutoffDate);
}
