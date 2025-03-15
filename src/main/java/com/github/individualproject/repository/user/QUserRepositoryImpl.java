package com.github.individualproject.repository.user;

import com.github.individualproject.repository.role.QRole;
import com.github.individualproject.repository.userRole.QUserRole;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
@RequiredArgsConstructor
@Slf4j
public class QUserRepositoryImpl implements QUserRepository{
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public Optional<User> findByEmailWithRoles(String email) {
        QUser user = QUser.user;
        QUserRole userRole = QUserRole.userRole;
        QRole role = QRole.role;

        User foundUserByEmail = jpaQueryFactory.selectFrom(user)
                .join(user.userRoles,userRole).fetchJoin()
                .join(userRole.role,role).fetchJoin()
                .where(user.email.eq(email))
                .fetchOne();
        return Optional.ofNullable(foundUserByEmail);
    }
}
