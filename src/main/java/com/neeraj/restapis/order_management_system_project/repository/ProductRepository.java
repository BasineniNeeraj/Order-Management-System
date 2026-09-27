package com.neeraj.restapis.order_management_system_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neeraj.restapis.order_management_system_project.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	boolean existsByNameIgnoreCase(String name);
	List<Product> findByActiveTrue();
    Optional<Product> findByProductIdAndActiveTrue(Long productId);
}
