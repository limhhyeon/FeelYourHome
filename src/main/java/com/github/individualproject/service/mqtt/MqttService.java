package com.github.individualproject.service.mqtt;

import com.github.individualproject.repository.userProduct.Status;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.service.exception.NotFoundException;
import jakarta.annotation.PostConstruct;
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
}
