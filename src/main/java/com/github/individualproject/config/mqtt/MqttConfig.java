package com.github.individualproject.config.mqtt;

//import com.github.iottest.config.MqttProperties;

import com.github.individualproject.repository.userProduct.UserProductRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
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
public class MqttConfig {
    private final UserProductRepository userProductRepository;
    private MqttPahoMessageDrivenChannelAdapter adapter;
    @Bean
    public MqttClient mqttStatusClient() throws MqttException {
        String broker = "tcp://43.202.80.85:1883";
        String clientId = "ServerStatusClient_" + System.currentTimeMillis(); // 고유 ID
        MqttClient client = new MqttClient(broker, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        client.connect(options);


        return client;
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://43.202.80.85"});
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10); // 연결 타임아웃 설정
        options.setKeepAliveInterval(60); // Keep-alive 간격 설정
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

//    @Bean
//    public MessageProducer inbound() {
//        MqttPahoMessageDrivenChannelAdapter adapter =
//                new MqttPahoMessageDrivenChannelAdapter("myServerMqtt", mqttClientFactory());
//        adapter.setOutputChannelName("mqttInputChannel");
//        adapter.setCompletionTimeout(5000);
//        adapter.setQos(1);
//        return adapter;
//    }
    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {  // 반환 타입 변경
        MqttPahoMessageDrivenChannelAdapter adapters =
                new MqttPahoMessageDrivenChannelAdapter("myServerMqtt", mqttClientFactory());
        this.adapter = adapters;
        adapter.setOutputChannelName("mqttInputChannel");
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        new Thread(() -> {
            while (!adapters.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            int page = 0;
            int size = 1000;
            Page<String> topics;
            do {
                topics = userProductRepository.findActiveMqttTopicsByActive(PageRequest.of(page, size));
                for (String topic : topics) {
                    if (!topic.isEmpty()) {
                        adapters.addTopic(topic);
                        System.out.println("복원된 구독: " + topic);
                    }
                }
                page++;
            } while (topics.hasNext());

            System.out.println("모든 구독 복원 완료");
        }).start();

        return adapters;
    }
    @PreDestroy
    public void destroy() {
        if (adapter != null && adapter.isRunning()) {
            adapter.stop();
            System.out.println("MqttAdapter 종료: 구독 정리 완료");
        }
    }

}
