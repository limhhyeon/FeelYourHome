package com.github.individualproject.repository.sensorData;

import com.github.individualproject.repository.userProduct.UserProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    @Override
    public List<SensorData> findAllByRecordedAtDateAndUserProduct(LocalDate date, UserProduct userProduct) {
        QSensorData sensorData = QSensorData.sensorData;

        return queryFactory
                .selectFrom(sensorData)
                .where(sensorData.userProduct.eq(userProduct) // UserProduct 조건 추가
                        .and(sensorData.recordedAt.year().eq(date.getYear()))
                        .and(sensorData.recordedAt.month().eq(date.getMonthValue()))
                        .and(sensorData.recordedAt.dayOfMonth().eq(date.getDayOfMonth())))
                .orderBy(sensorData.recordedAt.desc())
                .fetch();
    }
}
