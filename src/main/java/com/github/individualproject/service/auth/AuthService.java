package com.github.individualproject.service.auth;

import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.repository.refreshToken.RefreshToken;
import com.github.individualproject.repository.refreshToken.RefreshTokenRepository;
import com.github.individualproject.repository.role.Role;
import com.github.individualproject.repository.role.RoleRepository;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userRole.UserRole;
import com.github.individualproject.repository.userRole.UserRoleRepository;
import com.github.individualproject.service.exception.*;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.auth.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    @Transactional
    public ResponseDto signUpResult(SignUp signUp) {
        //이메일 존재하는지 확인
        if (userRepository.existsByEmail(signUp.getEmail())){
            return new ResponseDto(HttpStatus.CONFLICT.value(), "이미 존재하는 이메일입니다.");
        }
        //비밀번호와 비밀번호 체크란과 동일한지 확인
        if (!signUp.getPassword().equals(signUp.getPasswordCheck())){
            return new ResponseDto(HttpStatus.BAD_REQUEST.value(), "비밀번호가 비밀번호 체크란과 동일하지 않습니다.");
        }
        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new NotFoundException("역할이 존재하지 않습니다."));
        User signUpUser = User.createUser(signUp, passwordEncoder);
        userRepository.save(signUpUser);
        UserRole userRole = UserRole.createUserRole(role,signUpUser);
        userRoleRepository.save(userRole);
        return new ResponseDto(HttpStatus.CREATED.value(),signUpUser.getName() + "님 회원가입 성공했습니다.");

    }


    public ResponseDto duplicateCheckResult(DuplicateCheck duplicateCheck) {
        if (userRepository.existsByEmail(duplicateCheck.getDuplicateCheck())){
            return new ResponseDto(HttpStatus.CONFLICT.value(), "이미 존재하는 이메일입니다.");
        }else {
            return new ResponseDto(HttpStatus.OK.value(),"사용 가능한 이메일입니다.");
        }
    }
    @Transactional
    public String loginResult(Login login) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmailWithRoles(login.getEmail())
                    .orElseThrow(() -> new NotFoundException("User를 찾을 수 없습니다."));
            List<String> roles = user.getUserRoles()
                    .stream().map(UserRole::getRole)
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            //refreshToken 존재 여부 확인 후
            if (!refreshTokenRepository.existsByUser(user)){
                createRefreshToken(user,login.getEmail());
            }
            return jwtTokenProvider.createToken(login.getEmail(), roles);
        }catch (Exception e) {
            e.printStackTrace();
            throw new NotAcceptException("로그인 정보가 일치하지 않습니다..");
        }
    }

public ResponseDto refreshToken(String accessToken, HttpServletResponse response) {
    String email = jwtTokenProvider.getEmail(accessToken);
    if (email == null) {
        // 만약 이메일을 추출할 수 없다면, 토큰이 잘못되었거나 만료되었음을 의미
        throw new TokenValidateException("토큰이 잘못되었습니다.");
    }
    User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(()->new NotFoundException(email+ "에 해당하는 유저가 존재하지 않습니다."));
    RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
            .orElseThrow(()-> new NotFoundException(user.getName() + "님의 refresh 토큰이 존재하지 않습니다."));
    deleteCookie(response);
    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
        refreshTokenRepository.delete(refreshToken);
        throw new TokenValidateException("refreshToken이 만료되었습니다. 다시 로그인 해주세요");
    }
    String newAccessToken = jwtTokenProvider.createRefreshToken(email);
    createCookie(newAccessToken,response);
    return new ResponseDto(HttpStatus.CREATED.value(),"새로운 토큰이 발급되었습니다.");
}


    public void logoutResult(User user, HttpServletResponse response) {
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        deleteCookie(response);
    }
    //쿠키 생성
    public void createCookie(String newAccessToken,HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("Authorization", newAccessToken)
                .httpOnly(true)
                .secure(true) // https 환경에서 true로 설정
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("None") // SameSite 설정
                .build();
        response.addHeader("Set-Cookie",cookie.toString());
    }
    //리프레쉬 토큰 생성
    public void createRefreshToken(User user,String email){
        String refresh = jwtTokenProvider.createRefreshToken(email);
        RefreshToken refreshToken = RefreshToken.of(user,refresh);
        System.out.println(refreshToken);
        refreshTokenRepository.save(refreshToken);
    }
    //쿠키 삭제
    private void deleteCookie(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("Authorization",null)
                .httpOnly(true)
                .secure(true) // https 환경에서 true로 설정
                .path("/")
                .maxAge(0)
                .sameSite("None") // SameSite 설정
                .build();
        response.addHeader("Set-Cookie",cookie.toString());
    }

    public ResponseDto loginValidRequest(User user) {
        return new ResponseDto(HttpStatus.OK.value(),"토큰이 유효합니다.", UserInfo.of(user));
    }
}
