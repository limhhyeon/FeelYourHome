package com.github.individualproject.web.dto.product.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class BuyProduct {
    private final String productCode;
    private final String clientId;
}
