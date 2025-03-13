package com.github.individualproject.service.product;

import com.github.individualproject.repository.product.Product;
import com.github.individualproject.repository.product.ProductRepository;
import com.github.individualproject.service.exception.BadRequestException;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.product.BuyProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    public ResponseDto buyProductResult(BuyProduct buyProduct)  {
        if (productRepository.existsByProductCode(buyProduct.getProductCode())){
            throw new BadRequestException("이미 판매가 진행된 상품 코드 : " + buyProduct.getProductCode()+ " 입니다.");
        }
        Product product = Product.from(buyProduct);
        productRepository.save(product);
        return new ResponseDto(HttpStatus.CREATED.value(), buyProduct.getProductCode() + "가 정상적으로 구매되었습니다.");
    }
}
