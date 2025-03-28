package com.github.individualproject.web.dto.product.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
@Getter
@AllArgsConstructor
@ToString
@Builder
public class ChangeNotification {
    private final Long userProductId;
    private final Boolean is;
}
