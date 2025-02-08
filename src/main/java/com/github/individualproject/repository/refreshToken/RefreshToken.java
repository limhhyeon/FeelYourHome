package com.github.individualproject.repository.refreshToken;

import com.github.individualproject.repository.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public RefreshToken(String email,User user, String token, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.email = email;
        this.user=user;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void updateToken(String newRefreshToken) {
        this.token=newRefreshToken;
    }
}
