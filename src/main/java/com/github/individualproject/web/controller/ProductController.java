package com.github.individualproject.web.controller;

import com.github.individualproject.repository.user.CurrentUser;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.userDetails.CustomUserDetails;
import com.github.individualproject.service.product.ProductService;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.product.request.BuyProduct;
import com.github.individualproject.web.dto.product.request.ChangeNotification;
import com.github.individualproject.web.dto.product.request.ChangeTemp;
import com.github.individualproject.web.dto.product.request.RegistrationProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    //실제 상품 구매시 상품이 db에 등록되는 api 임시로 설정하는 가정
    @PostMapping("/buy")
    public ResponseDto buyProduct(@RequestBody BuyProduct buyProduct){
        return productService.buyProductResult(buyProduct);
    }
    //유저가 산 상품에 대해 등록하는 api
    @PostMapping("")
    public ResponseDto productRegistration(@CurrentUser User user, @RequestBody RegistrationProduct registrationProduct){
        return productService.productRegistrationResult(user,registrationProduct);
    }
    //자신이 등록 한 상품 리스트 보기
    @GetMapping("")
    public ResponseDto myProductList(@CurrentUser User user,@RequestParam(defaultValue = "0",value = "page",required = false) Integer page){
        return productService.myProductListResult(user,page);
    }
    @PutMapping("/temp")
    public ResponseDto myTempChange(@CurrentUser User user, @RequestBody ChangeTemp changeTemp){
        return productService.changeTempResult(user,changeTemp);
    }
    @PutMapping("/notification")
    public ResponseDto notificationChange(@CurrentUser User user, @RequestBody ChangeNotification changeNotification){
        return productService.notificationChangeResult(user,changeNotification);
    }
}
