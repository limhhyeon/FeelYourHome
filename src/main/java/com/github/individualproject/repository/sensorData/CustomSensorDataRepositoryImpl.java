package com.github.individualproject.repository.sensorData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomSensorDataRepositoryImpl implements CustomSensorDataRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAllBatch(List<SensorData> sensorDataList) {
        String sql = "INSERT INTO sensor_data (user_product_id, temperature, humidity, recorded_at) " +
                "VALUES (?, ?, ?, ?)";

        log.info("SensorData 배치 삽입 시작, 삽입할 데이터 크기: {}", sensorDataList.size());

        int[] updateCounts = jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        SensorData sensorData = sensorDataList.get(i);
                        ps.setLong(1, sensorData.getUserProduct().getUserProductId()); // 외래키
                        ps.setBigDecimal(2, sensorData.getTemperature()); // temperature
                        ps.setBigDecimal(3, sensorData.getHumidity()); // humidity
                        ps.setTimestamp(4, Timestamp.valueOf(sensorData.getRecordedAt())); // recorded_at
                    }

                    @Override
                    public int getBatchSize() {
                        return sensorDataList.size();
                    }
                });

        log.info("SensorData 배치 삽입 완료, 처리된 행 수: {}", updateCounts.length);

        // 각 행별 성공 여부 확인 (선택적)
        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == 0) {
                log.warn("행 {} 삽입 실패", i);
            }
        }
    }
}
