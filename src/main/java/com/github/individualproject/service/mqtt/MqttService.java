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
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {
    private final UserProductRepository userProductRepository;
    private final MqttClient mqttStatusClient;
    private final MqttPahoMessageDrivenChannelAdapter mqttAdapter;



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
                try {
                    mqttStatusClient.reconnect();
                    mqttStatusClient.subscribe("device/status/#", 1);
                    log.info("상태 MQTT 재연결 성공");
                } catch (MqttException e) {
                    log.error("재연결 실패: {}", e.getMessage());
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String clientId = topic.substring("device/status/".length()); // "ESP32_DHT22_Client2"
                String payload = new String(message.getPayload());
                log.info("상태 메시지 수신 - 토픽: {}, 페이로드: {}", topic, payload);

                // clientId로 UserProduct 조회 (매핑 필요)
                UserProduct userProduct = userProductRepository.findByClientId(clientId)
                        .orElse(null);
                if (userProduct== null){
                    return;
                }

                // 상태 업데이트
                if ("OFF".equals(payload)) {
                    userProduct.updateStatus(Status.INACTIVE);
                    log.info("기계 {} 꺼짐 상태로 업데이트", clientId);
                    mqttAdapter.removeTopic(userProduct.getMqttTopic());
                } else if ("ON".equals(payload)) {
                    userProduct.updateStatus(Status.ACTIVE);
                    log.info("기계 {} 켜짐 상태로 업데이트", clientId);
                    mqttAdapter.addTopic(userProduct.getMqttTopic());
                } else {
                    log.warn("알 수 없는 상태: {}", payload);
                    return;
                }

                userProductRepository.save(userProduct);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }
    @PreDestroy
    public void destroy() {
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
}
