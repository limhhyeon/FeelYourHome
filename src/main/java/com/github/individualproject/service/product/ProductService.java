package com.github.individualproject.service.product;

import com.github.individualproject.repository.product.Product;
import com.github.individualproject.repository.product.ProductRepository;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userDetails.CustomUserDetails;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.repository.userProduct.UserProductRepository;
import com.github.individualproject.service.exception.BadRequestException;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.product.RegistrationProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;
    private final UserRepository userRepository;

    public ResponseDto buyProductResult(RegistrationProduct registrationProduct)  {
        if (productRepository.existsByProductCode(registrationProduct.getProductCode())){
            throw new BadRequestException("이미 판매가 진행된 상품 코드 : " + registrationProduct.getProductCode()+ " 입니다.");
        }
        Product product = Product.from(registrationProduct);
        productRepository.save(product);
        return new ResponseDto(HttpStatus.CREATED.value(), registrationProduct.getProductCode() + "가 정상적으로 구매되었습니다.");
    }

    public ResponseDto productRegistrationResult(CustomUserDetails customUserDetails, RegistrationProduct registrationProduct) {
        User user = userRepository.findByEmailFetchJoin(customUserDetails.getEmail())
                .orElseThrow(()-> new NotFoundException("유저 정보가 없습니다."));
        String productCode = registrationProduct.getProductCode();
        //상품 코드 유효한지 확인
        Product product =productRepository.findByProductCode(productCode)
                .orElseThrow(()-> new NotFoundException("상품 코드가 유효하지 않습니다."));
        //이미 등록한 상품인지 확인
        if (userProductRepository.existsByProduct(product)){
            throw new BadRequestException("이미 등록한 상품입니다.");
        }
        //상품 등록
        UserProduct userProduct = UserProduct.of(user,product);
        userProductRepository.save(userProduct);
        return new ResponseDto(HttpStatus.CREATED.value(),productCode + " 상품이 정상적으로 등록되었습니다.");
    }
}
