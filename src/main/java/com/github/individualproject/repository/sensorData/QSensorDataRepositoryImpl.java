package com.github.individualproject.repository.sensorData;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Repository
public class QSensorDataRepositoryImpl implements QSensorDataRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteOldSensorData(LocalDateTime cutoffDate) {
        QSensorData sensorData = QSensorData.sensorData;

        queryFactory.delete(sensorData)
                .where(sensorData.recordedAt.lt(cutoffDate))
                .execute();
    }
}
