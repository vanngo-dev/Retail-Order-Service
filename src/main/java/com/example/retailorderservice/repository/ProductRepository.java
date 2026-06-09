package com.example.retailorderservice.repository;

import com.example.retailorderservice.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    Optional<Product> findBySkuIgnoreCase(String sku);

    Page<Product> findByActive(Boolean active, Pageable pageable);

    Page<Product> findBySkuIgnoreCase(String sku, Pageable pageable);

    Page<Product> findBySkuIgnoreCaseAndActive(String sku, Boolean active, Pageable pageable);
}
