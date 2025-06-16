package com.github.individualproject.config.mqtt;

//import com.github.iottest.config.MqttProperties;

import com.github.individualproject.repository.userProduct.UserProductRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MqttConfig {
    private final UserProductRepository userProductRepository;
    @Value("${mqtt.broker}")
    private String brokerHost;
    @Value("${mqtt.channel}")
    private String channel;
    @Value("${mqtt.server-clientid}")
    private String serverClientId;
    @Value("${mqtt.password}")
    private String password;

    @Bean
    public MqttClient mqttStatusClient() throws MqttException {
        String broker = brokerHost;
        String clientId = "ServerStatusClient_" + System.currentTimeMillis(); // 고유 ID
        MqttClient client = new MqttClient(broker, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setPassword(password.toCharArray());
        client.connect(options);


        return client;
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerHost});
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10); // 연결 타임아웃 설정
        options.setKeepAliveInterval(60); // Keep-alive 간격 설정
        options.setPassword(password.toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }


@Bean
public MqttPahoMessageDrivenChannelAdapter inbound() {  // 반환 타입 변경
    MqttPahoMessageDrivenChannelAdapter adapter = createAndStartAdapter();
    if (!waitForAdapterStart(adapter)) {
        log.error("MQTT 어댑터가 시작되지 않음. 구독 초기화 실패.");
        return adapter;
    }

    restoreSubscriptions(adapter);
    return adapter;
}

    private MqttPahoMessageDrivenChannelAdapter createAndStartAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(serverClientId, mqttClientFactory());

        adapter.setOutputChannelName(channel);
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.start(); // 명시적 시작
        log.info("MqttPahoMessageDrivenChannelAdapter 빈 생성 및 시작 완료");

        return adapter;
    }

    private boolean waitForAdapterStart(MqttPahoMessageDrivenChannelAdapter adapter) {
        int retries = 5;
        while (!adapter.isRunning() && retries > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            retries--;
        }
        return adapter.isRunning();
    }

    private void restoreSubscriptions(MqttPahoMessageDrivenChannelAdapter adapter) {
        int page = 0;
        int size = 1000;
        Page<String> topics;
        do {
            topics = userProductRepository.findActiveMqttTopicsByActive(PageRequest.of(page, size));
            for (String topic : topics) {
                if (!topic.isEmpty()) {
                    adapter.addTopic(topic);
                    log.info("복원된 구독: {}", topic);
                }
            }
            page++;
        } while (topics.hasNext());
        log.info("모든 구독 복원 완료");
    }


}
