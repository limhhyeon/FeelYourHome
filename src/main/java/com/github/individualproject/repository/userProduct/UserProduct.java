package com.github.individualproject.repository.userProduct;

import com.github.individualproject.repository.base.BaseEntity;
import com.github.individualproject.repository.product.Product;
import com.github.individualproject.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "userProductId")
@Entity
@Table(name = "user_product")
public class UserProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_product_id")
    private Long userProductId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "mqtt_topic",length = 255,nullable = false)
    private String mqttTopic;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING) // DB의 ENUM을 문자열로 매핑
    private Status status; // 기본값

    @Column(name = "client_id",length = 50) // 새로 추가
    private String clientId;

    public static UserProduct of(User user, Product product,String mqttTopic,String clientId){
        return UserProduct.builder()
                .user(user)
                .product(product)
                .mqttTopic(mqttTopic)
                .clientId(clientId)
                .status(Status.INACTIVE)
                .build();
    }
    public void updateStatus(Status status){
        this.status=status;
    }
}
