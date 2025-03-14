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
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorDataRepository sensorDataRepository;
    private final UserProductRepository userProductRepository;
    private final RedisUtil redisUtil;

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
//        UserProduct userProduct = userProductRepository.findByMqttTopic(topic)
//                .orElseThrow(() -> new NotFoundException("구독된 토픽에 해당하는 UserProduct 없음: " + topic));

//        SensorData sensorData = SensorData.of(userProduct,sensorResponse);
//        sensorDataRepository.save(sensorData);
        redisUtil.addSensorResponse(topic,sensorResponse);
    }

//    @Scheduled(fixedRate = TWO_HOURS_IN_MILLIS) // 2시간마다 실행
//    public void calculateAndSaveSensorAverages() {
//        String pattern = CACHE_PREFIX + "*";
//        Set<String> sensorKeys = sensorResponseRedisTemplate.keys(pattern);
//        if (sensorKeys == null || sensorKeys.isEmpty()) {
//            log.info("처리할 센서 데이터 없음");
//            return;
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime twoHoursAgo = now.minusHours(2); // 시간 범위는 저장용으로만 사용
//
//        for (String sensorKey : sensorKeys) {
//            String topic = sensorKey.substring(CACHE_PREFIX.length()); // 토픽 추출
//
//            // Redis에서 토픽별 모든 데이터 가져오기
//            List<SensorResponse> responses = sensorResponseRedisTemplate.opsForList().range(sensorKey, 0, -1);
//            if (responses == null || responses.isEmpty()) {
//                continue;
//            }
//
//            // 평균 계산 (모든 데이터 사용)
//            double avgTemp = responses.stream()
//                    .mapToDouble(SensorResponse::getTemp)
//                    .average()
//                    .orElse(0.0);
//            double avgHumid = responses.stream()
//                    .mapToDouble(SensorResponse::getHumid)
//                    .average()
//                    .orElse(0.0);
//
//            // UserProduct 조회 및 DB 저장
//            UserProduct userProduct = userProductRepository.findByMqttTopic(topic)
//                    .orElseThrow(() -> new NotFoundException("토픽에 해당하는 UserProduct 없음: " + topic));
//            SensorData sensorData = SensorData.of(userProduct, avgTemp, avgHumid, twoHoursAgo, now);
//            sensorDataRepository.save(sensorData);
//
//            log.info("토픽 {}: 평균 temp={}, humid={} 저장 완료", topic, avgTemp, avgHumid);
//
//            // 처리된 데이터 삭제
//            sensorResponseRedisTemplate.delete(sensorKey);
//        }
//    }




}
