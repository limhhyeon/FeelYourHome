package com.github.individualproject.config.auth;


import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.repository.user.CurrentUser;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userDetails.CustomUserDetails;
import com.github.individualproject.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class JwtUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(User.class) &&
                parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication+"입니다");

        if (authentication == null || !(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new NotFoundException("인증 정보가 없습니다.");
        }

        // 인증 객체에서 principal 가져오기
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new NotFoundException("잘못된 인증 정보입니다.");
        }

        // UserDetails로 변환
        CustomUserDetails userDetails = (CustomUserDetails) principal;

        // 데이터베이스에서 유저 조회
        return userRepository.findByEmailWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
    }
}
