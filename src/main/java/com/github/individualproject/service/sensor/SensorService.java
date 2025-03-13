package com.github.individualproject.service.sensor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.individualproject.repository.sensorData.SensorDataRepository;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.web.dto.sensor.SensorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorDataRepository sensorDataRepository;
    private final UserProductRepository userProductRepository;

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
    }
}
