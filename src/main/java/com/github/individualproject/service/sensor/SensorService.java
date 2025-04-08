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
import com.github.individualproject.web.dto.sensor.response.*;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.github.individualproject.repository.userProduct.QUserProduct.userProduct;

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
    // 메시지 페이로드와 토픽 추출
    String payload = message.getPayload();
    String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);

    // 수신된 메시지와 토픽을 로그로 기록
    logReceivedMessage(topic, payload);

    // 페이로드를 SensorResponse 객체로 파싱
    SensorResponse sensorResponse = parseSensorResponse(payload);

    // 센서 데이터 로그 출력
    logSensorData(sensorResponse);

    // 온도 변화 감지 및 처리
    processTemperatureChange(topic, sensorResponse);

    // Redis에 데이터 업데이트
    updateRedisData(topic, sensorResponse);
}

    /**
     * 수신된 메시지와 토픽을 로그로 기록합니다.
     * @param topic MQTT 토픽 이름
     * @param payload 수신된 메시지 내용
     */
    private void logReceivedMessage(String topic, String payload) {
        log.info("수신된 토픽: {}", topic);
        log.info("수신된 메시지: {}", payload);
    }

    /**
     * JSON 페이로드를 SensorResponse 객체로 변환합니다.
     * @param payload MQTT 메시지의 페이로드
     * @return 변환된 SensorResponse 객체
     * @throws JsonProcessingException JSON 파싱 오류 발생 시
     */
    private SensorResponse parseSensorResponse(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, SensorResponse.class);
    }

    /**
     * 센서 데이터(온도, 습도)를 로그로 출력합니다.
     * @param sensorResponse 파싱된 센서 데이터 객체
     */
    private void logSensorData(SensorResponse sensorResponse) {
        log.info("temp: {}, humid: {}", sensorResponse.getTemp(), sensorResponse.getHumid());
    }

    /**
     * 이전 데이터와 비교하여 온도 변화를 감지하고, 필요 시 알림을 보냅니다.
     * @param topic MQTT 토픽 이름
     * @param sensorResponse 현재 센서 데이터
     */
    private void processTemperatureChange(String topic, SensorResponse sensorResponse) {
        // Redis에서 이전 센서 데이터와 사용자 제품 정보 조회
        SensorResponse previousResponse = redisUtil.getPreviousSensorResponse(topic);
        UserProduct userProduct = redisUtil.getUserProductByTopic(topic);

        // 이전 데이터가 있고, 알림 수신 설정이 활성화된 경우
        if (previousResponse != null && userProduct.getIsReceiveNotification()) {
            // 온도 변화 임계값 및 현재/이전 온도 계산
            BigDecimal desiredThreshold = userProduct.getTemperatureDiffThreshold();
            BigDecimal previousTemp = BigDecimal.valueOf(previousResponse.getTemp());
            BigDecimal currentTemp = BigDecimal.valueOf(sensorResponse.getTemp());
            BigDecimal difference = currentTemp.subtract(previousTemp).abs();

            // 온도 차이가 임계값을 초과하면 경고 로그와 이메일 전송
            if (difference.compareTo(desiredThreshold) > 0) {
                logTemperatureChange(topic, previousTemp, currentTemp, desiredThreshold, difference);
                emailService.sendTempResult(userProduct.getUser().getEmail(), previousTemp, currentTemp, desiredThreshold);
            }
        } else {
            // 이전 데이터가 없으면 첫 데이터로 간주하고 비교 생략
            log.info("첫 데이터 수신, 비교 생략: {}", topic);
        }
    }

    /**
     * 온도 변화가 감지되었을 때 경고 로그를 출력합니다.
     * @param topic MQTT 토픽 이름
     * @param previousTemp 이전 온도
     * @param currentTemp 현재 온도
     * @param desiredThreshold 온도 변화 임계값
     * @param difference 온도 차이
     */
    private void logTemperatureChange(String topic, BigDecimal previousTemp, BigDecimal currentTemp,
                                      BigDecimal desiredThreshold, BigDecimal difference) {
        log.warn("온도 변화 발생 - topic: {}, previous: {}, current: {}, threshold: {}, diff: {}",
                topic, previousTemp, currentTemp, desiredThreshold, difference);
    }

    /**
     * Redis에 센서 데이터를 저장하고 최신 데이터로 업데이트합니다.
     * @param topic MQTT 토픽 이름
     * @param sensorResponse 현재 센서 데이터
     */
    private void updateRedisData(String topic, SensorResponse sensorResponse) {
        // 센서 데이터 추가
        redisUtil.addSensorResponse(topic, sensorResponse);
        // 최신 데이터로 덮어씌우기
        redisUtil.addSensorResponseLatest(topic, sensorResponse);
    }
    //--------------------------------------------------------------------------------------


    /**
     * 2시간마다 센서 데이터의 평균을 계산하고 저장합니다.
     */
    @Scheduled(cron = "0 0 0/2 * * *") // 2시간마다 실행
    public void calculateAndSaveSensorAverages() {
        Set<String> sensorKeys = redisUtil.getAllSensorKeys();
        if (sensorKeys.isEmpty()) {
            log.info("처리할 센서 데이터 없음");
            return;
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS); // 현재 정각
        List<SensorData> sensorDataList = processSensorKeys(sensorKeys, now);

        // 모든 센서 데이터를 일괄 저장
        sensorDataRepository.saveAllBatch(sensorDataList);
    }

    /**
     * 모든 센서 키를 순회하며 평균 데이터를 계산하고 리스트로 반환합니다.
     * @param sensorKeys Redis에서 가져온 센서 키 집합
     * @param now 현재 시간 (정각)
     * @return 계산된 SensorData 리스트
     */
    private List<SensorData> processSensorKeys(Set<String> sensorKeys, LocalDateTime now) {
        List<SensorData> sensorDataList = new ArrayList<>(); // 모든 SensorData를 모을 리스트

        for (String sensorKey : sensorKeys) {
            String topic = extractTopicFromKey(sensorKey);
            SensorData sensorData = calculateSensorDataForTopic(topic, now);

            if (sensorData != null) {
                sensorDataList.add(sensorData);
            }
        }
        return sensorDataList;
    }

    /**
     * 센서 키에서 토픽을 추출합니다.
     * @param sensorKey Redis 센서 키
     * @return 추출된 토픽 문자열
     */
    private String extractTopicFromKey(String sensorKey) {
        return sensorKey.substring(CACHE_PREFIX.length());
    }

    /**
     * 특정 토픽의 센서 데이터를 계산하고 SensorData 객체를 반환합니다.
     * @param topic MQTT 토픽 이름
     * @param now 현재 시간 (정각)
     * @return 계산된 SensorData 객체, 데이터가 없으면 null
     */
    private SensorData calculateSensorDataForTopic(String topic, LocalDateTime now) {
        // Redis에서 토픽별 모든 데이터 가져오기
        List<SensorResponse> responses = redisUtil.getSensorResponsesByTopic(topic);
        if (responses.isEmpty()) {
            return null;
        }

        // 온도와 습도의 평균 계산
        BigDecimal[] averages = calculateAverages(responses);
        BigDecimal avgTemp = averages[0];
        BigDecimal avgHumid = averages[1];

        // UserProduct 조회 및 상태 판단
        UserProduct userProduct = redisUtil.getUserProductByTopic(topic);
        HumidityStatus humidityStatus = determineHumidityStatus(userProduct, avgHumid);

        // SensorData 생성 및 Redis 데이터 삭제
        SensorData sensorData = SensorData.of(userProduct, avgTemp, avgHumid, now, humidityStatus);
        redisUtil.deleteSensorResponsesByTopic(topic);
        log.info("토픽 {}: 평균 temp={}, humid={} 저장 완료", topic, avgTemp, avgHumid);

        return sensorData;
    }

    /**
     * 센서 응답 리스트에서 온도와 습도의 평균을 계산합니다.
     * @param responses 센서 응답 리스트
     * @return [평균 온도, 평균 습도] 배열
     */
    private BigDecimal[] calculateAverages(List<SensorResponse> responses) {
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

        return new BigDecimal[]{avgTemp, avgHumid};
    }

    /**
     * 습도 상태를 판단하고, 필요 시 알림을 보냅니다.
     * @param userProduct 사용자 제품 정보
     * @param humidity 평균 습도
     * @return 습도 상태 (DANGER, WARNING, NORMAL)
     */
    private HumidityStatus determineHumidityStatus(UserProduct userProduct, BigDecimal humidity) {
        double humidValue = humidity.doubleValue();
        if (humidValue < 20 || humidValue > 80) {
            // 습도가 위험 범위에 있을 경우 알림 전송
            if (userProduct.getIsReceiveNotification()) {
                emailService.sendHumidResult(userProduct.getUser().getEmail(), humidity);
            }
            return HumidityStatus.DANGER;
        } else if ((humidValue >= 20 && humidValue < 30) || (humidValue > 60 && humidValue <= 80)) {
            return HumidityStatus.WARNING;
        } else {
            return HumidityStatus.NORMAL;
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void deleteOldSensorData() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7); // 7일 전 날짜
        sensorDataRepository.deleteOldSensorData(sevenDaysAgo); // 7일 이상 된 데이터 삭제
        log.info("7일이 지난 SensorData 레코드 삭제 완료");
    }
    //------------------------------------------------------------------------------------------------------------------

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
    //------------------------------------------------------------------------------------------------------------------

    public ResponseDto todayMySensorListResult(User user, Long userProductId) {
        UserProduct userProduct = userProductRepository.findById(userProductId)
                .orElseThrow(()-> new NotFoundException("유저의 상품이 아닙니다."));
        List<SensorData> todaySensorData = sensorDataRepository.findAllByRecordedAtDateAndUserProduct(LocalDate.now(),userProduct);
        List<CurrentSensorData> todaySensorResponses = todaySensorData.stream().map(CurrentSensorData::from).toList();
        return new ResponseDto(HttpStatus.OK.value(),"오늘 2시간마다의 평균 온도 습도 조회 성공",todaySensorResponses);
    }
    //------------------------------------------------------------------------------------------------------------------

    public ResponseDto weekMySensorListResult(User user, Long userProductId) {
        UserProduct userProduct = userProductRepository.findById(userProductId)
                .orElseThrow(() -> new NotFoundException("유저의 상품이 아닙니다."));

        // 최신순으로 조회
        List<SensorData> weekSensorData = sensorDataRepository.findAllByUserProductOrderByRecordedAtDesc(userProduct);

        // 날짜별로 그룹화하고 DTO로 변환 (순서 유지)
        List<SensorDataByDateDto> weekSensorResponses = weekSensorData.stream()
                .collect(Collectors.groupingBy(
                        sensorData -> sensorData.getRecordedAt().toLocalDate(),
                        LinkedHashMap::new, // 순서 유지
                        Collectors.mapping(CurrentSensorData::from, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> new SensorDataByDateDto(entry.getKey(), entry.getValue()))
                .toList();

        return new ResponseDto(HttpStatus.OK.value(), "일주일치 날짜별 평균 온도 습도 조회 성공", weekSensorResponses);
    }
    public ResponseDto weekAvgMySensorListResult(User user, Long userProductId) {
        UserProduct userProduct = userProductRepository.findById(userProductId)
                .orElseThrow(() -> new NotFoundException("유저의 상품이 아닙니다."));

        List<SensorData> weekSensorData = sensorDataRepository.findAllByUserProductOrderByRecordedAtDesc(userProduct);
        List<SensorDataByAvg> weekSensorResponses = calculateWeeklyAverages(weekSensorData);

        return new ResponseDto(HttpStatus.OK.value(), "일주일치 날짜별 평균 온도 습도 조회 성공", weekSensorResponses);
    }

    // 날짜별 평균 계산 및 DTO 변환
    private List<SensorDataByAvg> calculateWeeklyAverages(List<SensorData> weekSensorData) {
        return weekSensorData.stream()
                .collect(Collectors.groupingBy(
                        sensorData -> sensorData.getRecordedAt().toLocalDate(),
                        LinkedHashMap::new, // 순서 유지
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateDailyAverage
                        )
                ))
                .entrySet().stream()
                .map(entry -> new SensorDataByAvg(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 하루치 데이터의 평균 계산
    private List<TodaySensorData> calculateDailyAverage(List<SensorData> dailyData) {
        double avgTemp = dailyData.stream()
                .mapToDouble(sensor -> sensor.getTemperature().doubleValue())
                .average()
                .orElse(0.0);
        double avgHumid = dailyData.stream()
                .mapToDouble(sensor -> sensor.getHumidity().doubleValue())
                .average()
                .orElse(0.0);

        HumidityStatus humidityStatus = determineHumidityStatusWithoutNotification(BigDecimal.valueOf(avgHumid));

        return Collections.singletonList(new TodaySensorData(avgTemp, avgHumid, humidityStatus));
    }

    // 알림 없이 습도 상태만 결정
    private HumidityStatus determineHumidityStatusWithoutNotification(BigDecimal humidity) {
        double humidValue = humidity.doubleValue();
        if (humidValue < 20 || humidValue > 80) {
            return HumidityStatus.DANGER;
        } else if ((humidValue >= 20 && humidValue < 30) || (humidValue > 60 && humidValue <= 80)) {
            return HumidityStatus.WARNING;
        } else {
            return HumidityStatus.NORMAL;
        }
    }
}
