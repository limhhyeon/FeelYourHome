package com.github.individualproject.service.mqtt;

import com.github.individualproject.repository.userProduct.Status;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.service.redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {
    private final UserProductRepository userProductRepository;
    private final MqttClient mqttStatusClient;
    private final MqttPahoMessageDrivenChannelAdapter mqttAdapter;
    private final RedisUtil redisUtil;
    private static final String REDIS_KEY_PREFIX = "device_status:";
    private static final String CHANGED_CLIENTS_KEY = "changed_clientIds";




    @PostConstruct
    public void init() throws MqttException {
        log.info("MqttService 초기화 시작");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        if (!mqttStatusClient.isConnected()) {
            mqttStatusClient.connect(options);
        }
        // 상태 토픽 구독
        mqttStatusClient.subscribe("device/status/#", 1);
        mqttStatusClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("상태 MQTT 연결 끊김: {}", cause.getMessage());

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String clientId = topic.substring("device/status/".length()); // "ESP32_DHT22_Client2"
                String payload = new String(message.getPayload());
                log.info("상태 메시지 수신 - 토픽: {}, 페이로드: {}", topic, payload);

                // clientId로 UserProduct 조회 (매핑 필요)
                UserProduct userProduct = redisUtil.getUserProductByClientId(clientId);

                String redisKey = REDIS_KEY_PREFIX + clientId;
                // 상태 업데이트
                if ("OFF".equals(payload)) {
                    redisUtil.setData(redisKey, Status.INACTIVE.name(), 86400);
                    log.info("기계 {} 꺼짐 상태로 업데이트", clientId);
                    redisUtil.addToSet(CHANGED_CLIENTS_KEY, clientId); // RedisUtil로 Set 추가
                    mqttAdapter.removeTopic(userProduct.getMqttTopic());
                } else if ("ON".equals(payload)) {
                    redisUtil.setData(redisKey, Status.ACTIVE.name(), 86400);
                    log.info("기계 {} 켜짐 상태로 업데이트", clientId);
                    redisUtil.addToSet(CHANGED_CLIENTS_KEY, clientId); // RedisUtil로 Set 추가
                    mqttAdapter.addTopic(userProduct.getMqttTopic());
                } else {
                    log.warn("알 수 없는 상태: {}", payload);
                    return;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }
    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("컨텍스트 종료 이벤트 수신 - Redis 상태 저장 시작");
        saveRedisStateToDb();
    }
    @PreDestroy
    public void destroy() {
        log.info("MqttService 종료 시작");
        cleanupMqttConnection();
    }
    private void cleanupMqttConnection() {
        try {
            if (mqttStatusClient.isConnected()) {
                mqttStatusClient.unsubscribe("device/status/#");
                mqttStatusClient.disconnect();
                mqttStatusClient.close();
                log.info("MqttService 종료: 상태 클라이언트 정리 완료");
            }
        } catch (MqttException e) {
            log.error("MqttService 종료 중 오류: {}", e.getMessage());
        }
    }

    // --- 추가: Redis 상태를 DB에 저장하는 메서드 ---
    private void saveRedisStateToDb() {
        Set<String> changedClientIds = redisUtil.getSetMembers(CHANGED_CLIENTS_KEY);
        if (changedClientIds != null && !changedClientIds.isEmpty()) {
            for (String clientId : changedClientIds) {
                String redisKey = REDIS_KEY_PREFIX + clientId;
                String statusStr = redisUtil.getData(redisKey);
                log.info("키 값 : " + statusStr);
                if (statusStr != null) {
                    UserProduct userProduct = userProductRepository.findByClientId(clientId).orElse(null);
                    if (userProduct != null) {
                        Status status = Status.valueOf(statusStr);
                        userProduct.updateStatus(status);
                        userProductRepository.save(userProduct);
                        log.info("서버 종료 전 마지막 상태 저장: clientId={}, status={}", clientId, status);
                    }
                }
            }
            redisUtil.deleteKey(CHANGED_CLIENTS_KEY);
        }
    }
}
