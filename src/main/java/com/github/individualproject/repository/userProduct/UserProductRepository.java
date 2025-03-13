package com.github.individualproject.repository.userProduct;

import com.github.individualproject.repository.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct,Long> {
    Boolean existsByProduct(Product product);
}
