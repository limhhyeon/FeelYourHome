package com.github.individualproject.web.controller;

import com.github.individualproject.service.product.ProductService;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.product.BuyProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    @PostMapping("/product")
    public ResponseDto buyProduct(@RequestBody BuyProduct buyProduct){
        return productService.buyProductResult(buyProduct);
    }
}
