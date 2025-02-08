package com.github.individualproject.repository.userRole;

import com.github.individualproject.repository.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findUserRolesByUser(User user);
}
