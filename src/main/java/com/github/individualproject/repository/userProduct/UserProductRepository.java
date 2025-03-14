package com.github.individualproject.repository.userProduct;

import com.github.individualproject.repository.product.Product;
import com.github.individualproject.repository.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct,Long> {
    Boolean existsByProduct(Product product);
    Page<UserProduct> findAllByUser(User user, Pageable pageable);
    //전체 불러오는 건 db 부담이 크므로 필요한 필드만 불러오기
    @Query("SELECT up.mqttTopic FROM UserProduct up WHERE up.mqttTopic IS NOT NULL")
    Page<String> findAllMqttTopics(Pageable pageable);
    @Query("SELECT up.mqttTopic FROM UserProduct up WHERE up.status = 'ACTIVE'")
    Page<String> findActiveMqttTopicsByActive(Pageable pageable);

    Optional<UserProduct> findByMqttTopic(String mqttTopic);

    Optional<UserProduct> findByClientId(String clientId);
}
