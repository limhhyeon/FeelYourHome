package com.github.individualproject.repository.user;

import com.github.individualproject.repository.base.BaseEntity;
import com.github.individualproject.repository.userRole.UserRole;
import com.github.individualproject.web.dto.auth.SignUp;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "userId")
@Entity
@Table(name ="user")
public class User extends BaseEntity {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "name", length = 20, nullable = false)
    private String name;
    @Column(name = "email", length = 100, nullable = false)
    private String email;
    @Column(name = "password", length = 200)
    private String password;
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;
    @Column(name = "phone_number", length = 11,nullable = false)
    private String phoneNumber;
    @Column(name = "signup_type", length = 20,nullable = false)
    private String signupType;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserRole> userRoles;




    public static User createUser(SignUp signUp, PasswordEncoder passwordEncoder){

        return User.builder()
                .name(signUp.getName())
                .email(signUp.getEmail())
                .password(passwordEncoder.encode(signUp.getPassword()))
                .birthday(signUp.getBirthday())
                .phoneNumber(signUp.getPhoneNumber())
                .signupType("일반")
                .build();
    }


}
