package com.github.individualproject.service.redis;

import com.github.individualproject.repository.sensorData.SensorData;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.web.dto.sensor.SensorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;
    private final RedisTemplate<String, SensorResponse> sensorResponseRedisTemplate;
    private final RedisTemplate<String,UserProduct> userProductRedisTemplate;
    private final RedisTemplate<String,String> StringRedisTemplate;
    private final UserProductRepository userProductRepository;
    private static final String CACHE_PREFIX = "sensor:";
    private static final String CACHE_USERPRODUCT ="user_product:";
    private static final String CACHE_USERPRODUCTBYCLIENTID ="user_product_client:";
    private static final long CACHE_TTL_SECONDS = 24 * 60 * 60;

    public String getData(String key){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }
    public void setData(String key,String value, long duration){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration= Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }
    public void addSensorResponseLatest(String topic, SensorResponse sensorResponse) {
        String latestKey = CACHE_PREFIX + topic + ":latest";
        log.info("최신 sensorKey: {}", latestKey);
        sensorResponseRedisTemplate.opsForValue().set(latestKey, sensorResponse);
    }
    // 신규: 이전 데이터 가져오기
    public SensorResponse getPreviousSensorResponse(String topic) {
        String latestKey = CACHE_PREFIX + topic + ":latest";
        return sensorResponseRedisTemplate.opsForValue().get(latestKey);
    }
    public void addSensorResponse(String topic,SensorResponse sensorResponse) {
        String sensorKey = CACHE_PREFIX + topic;
        log.info("sensorKey : "+sensorKey);
        sensorResponseRedisTemplate.opsForList().rightPush(sensorKey, sensorResponse);
    }
    // --- 추가: Set에 값 추가 ---
    public void addToSet(String setKey, String value) {
        StringRedisTemplate.opsForSet().add(setKey, value);
    }

    // --- 추가: Set 멤버 조회 ---
    public Set<String> getSetMembers(String setKey) {
        Set<String> members = StringRedisTemplate.opsForSet().members(setKey);
        return members != null ? members : Collections.emptySet();
    }

    // --- 추가: 키 삭제 ---
    public void deleteKey(String key) {
        StringRedisTemplate.delete(key);
    }
    public void deleteAllSensorResponse(){
        String pattern = CACHE_PREFIX+"*";
        Set<String> keys = sensorResponseRedisTemplate.keys(pattern);
        sensorResponseRedisTemplate.delete(keys);
    }
    public List<SensorResponse> getAllSensor(){
        String pattern = CACHE_PREFIX+"*";
        Set<String> sensorKeys = sensorResponseRedisTemplate.keys(pattern);
        if (sensorKeys.isEmpty()){
            return Collections.emptyList();
        }
        List<SensorResponse> allSensorResponse = new ArrayList<>();
        for (String sensorKey : sensorKeys){
            List<SensorResponse> sensorResponses = sensorResponseRedisTemplate.opsForList().range(sensorKey,0,-1);
            allSensorResponse.addAll(sensorResponses);
        }
        return allSensorResponse;
    }
    // 토픽별 SensorResponse 조회
    public List<SensorResponse> getSensorResponsesByTopic(String topic) {
        String sensorKey = CACHE_PREFIX + topic;
        List<SensorResponse> responses = sensorResponseRedisTemplate.opsForList().range(sensorKey, 0, -1);
        return responses != null ? responses : Collections.emptyList();
    }

    // 토픽별 데이터 삭제
    public void deleteSensorResponsesByTopic(String topic) {
        String sensorKey = CACHE_PREFIX + topic;
        sensorResponseRedisTemplate.delete(sensorKey);
    }

    // 모든 토픽 키 조회
    public Set<String> getAllSensorKeys() {
        String pattern = CACHE_PREFIX + "*";
        Set<String> keys = sensorResponseRedisTemplate.keys(pattern);
        return keys != null ? keys : Collections.emptySet();
    }
    //생성 로직
    public UserProduct getUserProductByTopic(String topic){
        String cacheKey  = CACHE_USERPRODUCT + topic;
        UserProduct userProduct = userProductRedisTemplate.opsForValue().get(cacheKey);
        if (userProduct == null){
            UserProduct findUserProduct = userProductRepository.findByMqttTopic(topic)
                    .orElseThrow(()-> new NotFoundException("토픽에 해당하는 상품을 찾을 수 없거나 유저가 일치하지 않습니다."));
            Duration expireDuration= Duration.ofSeconds(CACHE_TTL_SECONDS);
            userProductRedisTemplate.opsForValue().set(cacheKey,findUserProduct,expireDuration);
            return findUserProduct;
        }
        return userProduct;
    }
    //삭제 로직(갱신이 필요해서 만들었음)
    public void deleteUserProductCache(String topic) {
        String cacheKey = CACHE_USERPRODUCT + topic;
        Boolean deleted = userProductRedisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("캐시 삭제 성공: {}", cacheKey);
        } else {
            log.warn("캐시 삭제 실패 또는 키 없음: {}", cacheKey);
        }
    }
    public UserProduct getUserProductByClientId(String clientId){
        String cacheKey  = CACHE_USERPRODUCT + clientId;
        UserProduct userProduct = userProductRedisTemplate.opsForValue().get(cacheKey);
        if (userProduct == null){
            UserProduct findUserProduct = userProductRepository.findByClientId(clientId)
                    .orElseThrow(()-> new NotFoundException("토픽에 해당하는 상품을 찾을 수 없거나 유저가 일치하지 않습니다."));
            Duration expireDuration= Duration.ofSeconds(CACHE_TTL_SECONDS);
            userProductRedisTemplate.opsForValue().set(cacheKey,findUserProduct,expireDuration);
            log.info("여기서 불러오니?2");
            return findUserProduct;
        }
        log.info("여기서 불러오니?");
        return userProduct;
    }


}
