package com.github.individualproject.repository.refreshToken;

import com.github.individualproject.repository.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByEmail(String email);

    boolean existsByUser(User user);
    Optional<RefreshToken> findByUser(User user);
}
