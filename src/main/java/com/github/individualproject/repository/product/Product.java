package com.github.individualproject.repository.product;

import com.github.individualproject.repository.base.BaseEntity;
import com.github.individualproject.web.dto.product.request.BuyProduct;
import com.github.individualproject.web.dto.product.request.RegistrationProduct;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "client_id", nullable = false, length = 255, unique = true)
    private String clientId;

    public static Product from(BuyProduct buyProduct){
        return Product.builder()
                .productCode(buyProduct.getProductCode())
                .clientId(buyProduct.getClientId())
                .build();
    }

}