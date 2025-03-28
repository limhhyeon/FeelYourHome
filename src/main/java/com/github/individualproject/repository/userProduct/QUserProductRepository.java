package com.github.individualproject.repository.userProduct;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QUserProductRepository {
    Page<String> findAllMqttTopics(Pageable pageable);
    Page<String> findActiveMqttTopicsByActive(Pageable pageable);
}
