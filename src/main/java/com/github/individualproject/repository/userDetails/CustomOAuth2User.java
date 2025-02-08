package com.github.individualproject.repository.userDetails;

import com.github.individualproject.repository.user.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
//public class CustomOAuth2User implements OAuth2User {
//    private Map<String, Object> attributes;
//    private List<String> authorities;
//
//    @Override
//    public Map<String, Object> getAttributes() {
//        return attributes;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
//    }
//
//    @Override
//    public String getName() {
//        return null;
//    }
//}
