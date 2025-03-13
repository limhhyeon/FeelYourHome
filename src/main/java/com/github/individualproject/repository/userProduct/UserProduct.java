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

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    public static UserProduct of(User user, Product product){
        return UserProduct.builder()
                .user(user)
                .product(product)
                .build();
    }
}
