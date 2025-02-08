package com.github.individualproject.repository.role;

import com.github.individualproject.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "roleId")
@Entity
@Table(name = "role")
public class Role {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    @Column(name = "role_name", length = 20,nullable = false)
    private String roleName;
}
