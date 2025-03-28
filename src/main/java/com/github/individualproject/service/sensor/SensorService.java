package com.github.individualproject.service.sensor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.individualproject.repository.sensorData.HumidityStatus;
import com.github.individualproject.repository.sensorData.SensorData;
import com.github.individualproject.repository.sensorData.SensorDataRepository;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.service.auth.EmailService;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.service.redis.RedisUtil;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.sensor.response.CurrentSensorData;
import com.github.individualproject.web.dto.sensor.response.SensorDataByDateDto;
import com.github.individualproject.web.dto.sensor.response.SensorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final EmailService emailService;

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
        // 이전 데이터 가져오기
        SensorResponse previousResponse = redisUtil.getPreviousSensorResponse(topic);

        // UserProduct과 User 정보 조회
        UserProduct userProduct = redisUtil.getUserProductByTopic(topic);


        // 이전 데이터가 있으면 비교
        if (previousResponse != null && userProduct.getIsReceiveNotification()) {
            BigDecimal desiredThreshold = userProduct.getTemperatureDiffThreshold();
            BigDecimal previousTemp = BigDecimal.valueOf(previousResponse.getTemp());
            BigDecimal currentTemp = BigDecimal.valueOf(sensorResponse.getTemp());
            BigDecimal difference = currentTemp.subtract(previousTemp).abs();


            if (difference.compareTo(desiredThreshold) > 0) {
                log.warn("온도 변화 발생 - topic: {}, previous: {}, current: {}, threshold: {}, diff: {}",
                        topic, previousTemp, currentTemp, desiredThreshold, difference);
                emailService.sendTempResult(userProduct.getUser().getEmail(), previousTemp,currentTemp, desiredThreshold);
            }
        } else {
            log.info("첫 데이터 수신, 비교 생략: {}", topic);
        }

        redisUtil.addSensorResponse(topic,sensorResponse);
        // 최신 데이터로 덮어씌우기
        redisUtil.addSensorResponseLatest(topic, sensorResponse);
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
            HumidityStatus humidityStatus = determineHumidityStatus(userProduct,avgHumid);
            SensorData sensorData = SensorData.of(userProduct, avgTemp, avgHumid, now,humidityStatus);

            sensorDataList.add(sensorData);
            redisUtil.deleteSensorResponsesByTopic(topic); // 저장 성공 후 삭제
            log.info("토픽 {}: 평균 temp={}, humid={} 저장 완료", topic, avgTemp, avgHumid);
        }
        sensorDataRepository.saveAllBatch(sensorDataList);
    }
    private HumidityStatus determineHumidityStatus(UserProduct userProduct,BigDecimal humidity) {
        double humidValue = humidity.doubleValue();
        if (humidValue < 20 || humidValue > 80) {
            //만약 유저가 알림 수신을 허용했다면 메일 보내기
            if (userProduct.getIsReceiveNotification()){
                emailService.sendHumidResult(userProduct.getUser().getEmail(),humidity);
            }
            return HumidityStatus.DANGER;
        } else if ((humidValue >= 20 && humidValue < 30) || (humidValue > 60 && humidValue <= 80)) {
            return HumidityStatus.WARNING;
        } else {
            return HumidityStatus.NORMAL;
        }
    }
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void deleteOldSensorData() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7); // 7일 전 날짜
        sensorDataRepository.deleteOldSensorData(sevenDaysAgo); // 7일 이상 된 데이터 삭제
        log.info("7일이 지난 SensorData 레코드 삭제 완료");
    }

    public ResponseDto latestSensorResult(User user,Long userProductId) {
        UserProduct userProduct = userProductRepository.findById(userProductId)
                .orElseThrow(()-> new NotFoundException("유저의 상품이 아닙니다."));
        //redis에서 저장된 현재 온도와 습도 가져오기
        SensorResponse previousResponse = redisUtil.getPreviousSensorResponse(userProduct.getMqttTopic());
        //만약 redis에 없다면 db에 가장 최근 온도 습도 가져오기
        if (previousResponse == null){
            SensorData sensorData =sensorDataRepository.findTopByUserProductOrderByRecordedAtDesc(userProduct)
                    .orElseThrow(()-> new NotFoundException("현재 측정된 온도가 존재하지 않습니다."));
            previousResponse=SensorResponse.from(sensorData);
        }
        return new ResponseDto(HttpStatus.OK.value(),"가장 최근 온도 습도 조회 성공", previousResponse);

    }

    public ResponseDto todayMySensorListResult(User user, Long userProductId) {
        UserProduct userProduct = userProductRepository.findById(userProductId)
                .orElseThrow(()-> new NotFoundException("유저의 상품이 아닙니다."));
        List<SensorData> todaySensorData = sensorDataRepository.findAllByRecordedAtDateAndUserProduct(LocalDate.now(),userProduct);
        List<CurrentSensorData> todaySensorResponses = todaySensorData.stream().map(CurrentSensorData::from).toList();
        return new ResponseDto(HttpStatus.OK.value(),"오늘 2시간마다의 평균 온도 습도 조회 성공",todaySensorResponses);
    }

    public ResponseDto weekMySensorListResult(User user, Long userProductId) {
        UserProduct userProduct = userProductRepository.findById(userProductId)
                .orElseThrow(()-> new NotFoundException("유저의 상품이 아닙니다."));
        // 일주일치 데이터 전체 조회
        List<SensorData> weekSensorData = sensorDataRepository.findAllByUserProduct(userProduct);

        // 날짜별로 그룹화하고 DTO로 변환
        List<SensorDataByDateDto> weekSensorResponses = weekSensorData.stream()
                .collect(Collectors.groupingBy(
                        sensorData -> sensorData.getRecordedAt().toLocalDate(), // LocalDateTime -> LocalDate
                        Collectors.mapping(CurrentSensorData::from, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> new SensorDataByDateDto(entry.getKey(), entry.getValue()))
                .toList();

        return new ResponseDto(HttpStatus.OK.value(), "일주일치 날짜별 평균 온도 습도 조회 성공", weekSensorResponses);


    }
}
