package com.github.individualproject.service.sensor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.individualproject.repository.sensorData.SensorData;
import com.github.individualproject.repository.sensorData.SensorDataRepository;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.service.redis.RedisUtil;
import com.github.individualproject.web.dto.sensor.SensorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorDataRepository sensorDataRepository;
    private final UserProductRepository userProductRepository;
    private final RedisUtil redisUtil;
    private static final String CACHE_PREFIX = "sensor:";
    private static final long TWO_HOURS_IN_MILLIS = 2 * 60 * 60 * 1000;

//    @ServiceActivator(inputChannel = "mqttInputChannel")
//    public void handel(String message) throws JsonProcessingException {
//        log.info("수신된 메시지: {}", message); // 디버깅용
//        SensorResponse sensorResponse = objectMapper.readValue(message, SensorResponse.class);
//        System.out.println("temp : " + sensorResponse.getTemp() + " / humid: " + sensorResponse.getHumid());
//
//    }
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) throws JsonProcessingException {
        String payload = message.getPayload();
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        log.info("수신된 토픽: {}", topic);
        log.info("수신된 메시지: {}", payload);

        SensorResponse sensorResponse = objectMapper.readValue(payload, SensorResponse.class);
        log.info("temp: {}, humid: {}", sensorResponse.getTemp(), sensorResponse.getHumid());

        redisUtil.addSensorResponse(topic,sensorResponse);
    }

    @Scheduled(cron = "0 0 0/2 * * *") // 2시간마다 실행
    public void calculateAndSaveSensorAverages() {
        Set<String> sensorKeys = redisUtil.getAllSensorKeys();
        if (sensorKeys.isEmpty()) {
            log.info("처리할 센서 데이터 없음");
            return;
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS); // 현재 정각
        List<SensorData> sensorDataList = new ArrayList<>(); // 모든 SensorData를 모을 리스트

        for (String sensorKey : sensorKeys) {
            String topic = sensorKey.substring(CACHE_PREFIX.length()); // 토픽 추출

            // Redis에서 토픽별 모든 데이터 가져오기
            List<SensorResponse> responses = redisUtil.getSensorResponsesByTopic(topic);
            if (responses.isEmpty()) {
                continue;
            }
            // 평균 계산 (모든 데이터 사용)
            double avgTempDouble = responses.stream()
                    .mapToDouble(SensorResponse::getTemp)
                    .average()
                    .orElse(0.0);
            double avgHumidDouble = responses.stream()
                    .mapToDouble(SensorResponse::getHumid)
                    .average()
                    .orElse(0.0);

            BigDecimal avgTemp = BigDecimal.valueOf(avgTempDouble)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal avgHumid = BigDecimal.valueOf(avgHumidDouble)
                    .setScale(2, RoundingMode.HALF_UP);

            // UserProduct 조회 및 DB 저장
            UserProduct userProduct = redisUtil.getUserProductByTopic(topic);
            SensorData sensorData = SensorData.of(userProduct, avgTemp, avgHumid, now);

            sensorDataList.add(sensorData);
            redisUtil.deleteSensorResponsesByTopic(topic); // 저장 성공 후 삭제
            log.info("토픽 {}: 평균 temp={}, humid={} 저장 완료", topic, avgTemp, avgHumid);
        }
        sensorDataRepository.saveAllBatch(sensorDataList);
    }
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void deleteOldSensorData() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7); // 7일 전 날짜
        sensorDataRepository.deleteOldSensorData(sevenDaysAgo); // 7일 이상 된 데이터 삭제
        log.info("7일이 지난 SensorData 레코드 삭제 완료");
    }

}
