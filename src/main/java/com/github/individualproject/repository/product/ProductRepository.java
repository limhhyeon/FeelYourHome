package com.github.individualproject.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    Boolean existsByProductCode(String code);
    Optional<Product> findByProductCode(String productCode);
}
