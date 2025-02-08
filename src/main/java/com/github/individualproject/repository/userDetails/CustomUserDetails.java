package com.github.individualproject.repository.userDetails;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomUserDetails implements UserDetails, OAuth2User {
    private Integer userId;
    private String email;
    private String password;
    private List<String> authorities;
    private Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
    //계정 만료되지 않았는지 확인 true면 유효하다
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    //계정이 잠겨있는지 확인 true면 잠겨있지 않음
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    //비밀번호가 만료되었는지 확인 true면 유효함
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    //계정이 활성화되었는지 확인 true면 활성화되어있다는 거임
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }
}
