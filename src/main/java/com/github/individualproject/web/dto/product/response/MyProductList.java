package com.github.individualproject.web.dto.product.response;

import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.userProduct.UserProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class MyProductList {
    private final Long userProductId;
    private final String productCode;

    public static MyProductList from(UserProduct userProduct){
        return MyProductList.builder()
                .userProductId(userProduct.getUserProductId())
                .productCode(userProduct.getProduct().getProductCode())
                .build();
    }

}
