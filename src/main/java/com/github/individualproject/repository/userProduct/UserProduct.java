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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "mqtt_topic",length = 255,nullable = false)
    private String mqttTopic;

    public static UserProduct of(User user, Product product,String mqttTopic){
        return UserProduct.builder()
                .user(user)
                .product(product)
                .mqttTopic(mqttTopic)
                .build();
    }
}
