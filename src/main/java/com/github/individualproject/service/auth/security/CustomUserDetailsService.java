package com.github.individualproject.service.auth.security;

import com.github.individualproject.repository.role.Role;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userDetails.CustomUserDetails;
import com.github.individualproject.repository.userRole.UserRole;
import com.github.individualproject.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(()->new NotFoundException(email +"에 해당하는 user는 존재하지 않습니다."));
       return CustomUserDetails.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getUserRoles().stream().map(UserRole::getRole).map(Role::getRoleName).collect(Collectors.toList()))
                .build();

    }
}
