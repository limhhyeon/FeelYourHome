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
import com.github.individualproject.service.exception.ExpiredException;
import com.github.individualproject.service.exception.InvalidTokenException;
import com.github.individualproject.service.exception.NotAcceptException;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.auth.DuplicateCheck;
import com.github.individualproject.web.dto.auth.Login;
import com.github.individualproject.web.dto.auth.SignUp;
import com.github.individualproject.web.dto.auth.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public TokenDto loginResult(Login login) {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userRepository.findByEmailFetchJoin(login.getEmail())
                    .orElseThrow(() -> new NotFoundException(login.getEmail() + "에 해당하는 유저를 찾을 수가 없습니다."));

            String email = user.getEmail();
            List<String> roles = user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());

            String accessToken = jwtTokenProvider.createToken(email, roles);
            String refreshToken = jwtTokenProvider.createRefreshToken(email);

            // 기존 리프레시 토큰 조회
            Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByEmail(email);

            if (existingTokenOpt.isPresent()) {
                // 기존 리프레시 토큰이 존재하면 업데이트
                RefreshToken existingToken = existingTokenOpt.get();
                existingToken.setToken(refreshToken); // 새로운 리프레시 토큰으로 업데이트
                existingToken.setExpiresAt(LocalDateTime.now().plusDays(8)); // 만료일 업데이트
                refreshTokenRepository.save(existingToken); // DB에 저장
            } else {
                // 기존 리프레시 토큰이 없으면 새로 생성
                RefreshToken refreshTokenEntity = new RefreshToken(email, user, refreshToken, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
                refreshTokenRepository.save(refreshTokenEntity); // DB에 저장
            }

            return new TokenDto(accessToken, refreshToken);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotAcceptException("아이디 또는 비밀번호를 잘못 입력하셨습니다.");
        }
    }
//    public TokenDto loginResult(Login login) {
//        try {
//            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            User user = userRepository.findByEmailFetchJoin(login.getEmail())
//                    .orElseThrow(() -> new NotFoundException(login.getEmail() + "에 해당하는 유저를 찾을 수 없습니다."));
//            String email = user.getEmail();
//            List<String> roles = user.getUserRoles().stream()
//                    .map(UserRole::getRole)
//                    .map(Role::getRoleName)
//                    .collect(Collectors.toList());
//
//            String accessToken = jwtTokenProvider.createToken(email, roles);
//            String refreshToken = jwtTokenProvider.createRefreshToken(email);
//
//            // 리프레시 토큰을 DB에 저장
//            RefreshToken refreshTokenEntity = new RefreshToken(user.getEmail(), user, refreshToken, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
//            refreshTokenRepository.save(refreshTokenEntity);
//
//            return new TokenDto(accessToken, refreshToken);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotAcceptException("아이디 또는 비밀번호를 잘못 입력하셨습니다.");
//        }
//    }
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        // 리프레시 토큰으로 사용자 이메일 추출
        String userEmail = jwtTokenProvider.getEmail(refreshToken);

        // DB에서 리프레시 토큰 조회
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("리프레시 토큰을 찾을 수 없습니다."));

        // 사용자 정보 조회
        User user = userRepository.findByEmailFetchJoin(userEmail)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        // 리프레시 토큰 유효성 검사
        if (!jwtTokenProvider.validRefreshToken(refreshToken)) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 새로운 액세스 토큰 생성
        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.createToken(userEmail, roles);

        // 새로운 리프레시 토큰 생성
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userEmail);
        refreshTokenEntity.updateToken(newRefreshToken);
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
//        refreshTokenRepository.save(refreshTokenEntity);


        return new TokenDto(newAccessToken, newRefreshToken);
    }
}
