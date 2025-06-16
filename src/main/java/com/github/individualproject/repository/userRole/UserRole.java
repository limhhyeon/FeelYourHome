package com.github.individualproject.repository.userRole;

import com.github.individualproject.repository.role.Role;
import com.github.individualproject.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "userRoleId")
@Entity
@Table(name = "user_role")
public class UserRole {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Integer userRoleId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public static UserRole createUserRole(Role role, User user){
        return UserRole.builder()
                .role(role)
                .user(user)
                .build();
    }
}
