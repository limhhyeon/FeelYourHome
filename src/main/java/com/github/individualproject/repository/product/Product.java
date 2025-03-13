package com.github.individualproject.repository.product;

import com.github.individualproject.repository.base.BaseEntity;
import com.github.individualproject.web.dto.product.BuyProduct;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "productId")
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", nullable = false, length = 50, unique = true)
    private String productCode;

    public static Product from(BuyProduct buyProduct){
        return Product.builder()
                .productCode(buyProduct.getProductCode())
                .build();
    }

}