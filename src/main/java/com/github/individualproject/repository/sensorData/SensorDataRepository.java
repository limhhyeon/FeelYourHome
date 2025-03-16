package com.github.individualproject.repository.sensorData;

import com.github.individualproject.repository.userProduct.UserProduct;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData,Long>,CustomSensorDataRepository,QSensorDataRepository {
//    @Modifying
//    @Transactional
//    @Query("DELETE FROM SensorData s WHERE s.recordedAt < :cutoffDate")
//    void deleteOldSensorData(@Param("cutoffDate") LocalDateTime cutoffDate);
    Optional<SensorData> findTopByUserProductOrderByRecordedAtDesc(UserProduct userProduct);
    List<SensorData> findAllByRecordedAtOrderByRecordedAtDesc(LocalDateTime localDateTime);
    List<SensorData> findAllByUserProduct(UserProduct userProduct);

}
