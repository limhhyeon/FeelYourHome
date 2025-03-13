package com.github.individualproject.repository.userProduct;

import com.github.individualproject.repository.product.Product;
import com.github.individualproject.repository.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct,Long> {
    Boolean existsByProduct(Product product);
    Page<UserProduct> findAllByUser(User user, Pageable pageable);
}
