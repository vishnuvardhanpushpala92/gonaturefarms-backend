package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.pending = true OR p.pending IS NULL")
    List<Product> findByPendingTrue();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id IN :ids")
    List<Product> findAllByIdWithVariants(List<Long> ids);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants")
    List<Product> findAllWithVariants();
}
