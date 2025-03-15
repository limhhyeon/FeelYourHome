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
import com.github.individualproject.web.dto.product.request.BuyProduct;
import com.github.individualproject.web.dto.product.request.ChangeNotification;
import com.github.individualproject.web.dto.product.request.ChangeTemp;
import com.github.individualproject.web.dto.product.request.RegistrationProduct;
import com.github.individualproject.web.dto.product.response.MyProductList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;
    private final UserRepository userRepository;
    private final MqttPahoMessageDrivenChannelAdapter mqttAdapter;
    @Value("${sub.path}")
    private String PATH;

    //상품 구매
    public ResponseDto buyProductResult(BuyProduct buyProduct)  {
        if (productRepository.existsByProductCode(buyProduct.getProductCode())){
            throw new BadRequestException("이미 판매가 진행된 상품 코드 : " + buyProduct.getProductCode()+ " 입니다.");
        }
        Product product = Product.from(buyProduct);
        productRepository.save(product);
        return new ResponseDto(HttpStatus.CREATED.value(), buyProduct.getProductCode() + "가 정상적으로 구매되었습니다.");
    }

    //내가 산 상품 등록
    @Transactional
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
        String topic = PATH+productCode;
        String clientId = product.getClientId();

        //상품 등록
        UserProduct userProduct = UserProduct.of(user,product,topic,clientId);
        userProductRepository.save(userProduct);

        // MQTT 토픽 생성 및 구독 추가
        mqttAdapter.addTopic(topic);
        System.out.println("구독 추가: " + topic);

        return new ResponseDto(HttpStatus.CREATED.value(),productCode + " 상품이 정상적으로 등록되었습니다.");
    }

    //내 상품 리스트 조회
    public ResponseDto myProductListResult(CustomUserDetails customUserDetails,Integer page) {
        User user = userRepository.findByEmailFetchJoin(customUserDetails.getEmail())
                .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
        Pageable pageable = PageRequest.of(page,10);
        Page<UserProduct> userProducts = userProductRepository.findAllByUser(user,pageable);
        Page<MyProductList> myProductLists = userProducts.map(MyProductList::from);
        return new ResponseDto(HttpStatus.OK.value(),"내 등록 상품 리스트 조회 성공",myProductLists);


    }

    public ResponseDto changeTempResult(CustomUserDetails customUserDetails, ChangeTemp changeTemp) {
        User user = userRepository.findByEmailFetchJoin(customUserDetails.getEmail())
                .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
        UserProduct userProduct = userProductRepository.findById(changeTemp.getUserProductId())
                .orElseThrow(()-> new NotFoundException("유저 상품을 찾을 수 없습니다."));
        userProduct.changeTemp(changeTemp.getChangeTemp());
        userProductRepository.save(userProduct);
        return new ResponseDto(HttpStatus.OK.value(),"희망 온도 차이 : " +changeTemp.getChangeTemp()+"로 변경되었습니다.");
    }

    public ResponseDto notificationChangeResult(CustomUserDetails customUserDetails, ChangeNotification changeNotification) {
        User user = userRepository.findByEmailFetchJoin(customUserDetails.getEmail())
                .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
        UserProduct userProduct = userProductRepository.findById(changeNotification.getUserProductId())
                .orElseThrow(()-> new NotFoundException("유저 상품을 찾을 수 없습니다."));
        userProduct.changeIsNotification(changeNotification.getIs());
        userProductRepository.save(userProduct);
        return new ResponseDto(HttpStatus.OK.value(),"알림 설정이 : " +changeNotification.getIs()+"로 변경되었습니다.");

    }
}
