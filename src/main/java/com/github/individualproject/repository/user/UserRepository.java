package com.github.individualproject.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);

    @Query(" SELECT u FROM User u JOIN FETCH u.userRoles ur JOIN FETCH ur.role WHERE u.email = :email")
    Optional<User> findByEmailFetchJoin(String email);
}
