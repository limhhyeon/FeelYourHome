package com.github.individualproject.repository.user;

import java.util.Optional;

public interface QUserRepository {
    Optional<User> findByEmailWithRoles(String email);
}
