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
    private final UserProductRepository userProductRepository;
    private static final String CACHE_PREFIX = "sensor:";
    private static final String CACHE_USERPRODUCT ="user_product:";
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
    public void addSensorResponse(String topic,SensorResponse sensorResponse) {
        String sensorKey = CACHE_PREFIX + topic;
        log.info("sensorKey : "+sensorKey);
        sensorResponseRedisTemplate.opsForList().rightPush(sensorKey, sensorResponse);
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


}
