package com.neeraj.restapis.order_management_system_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.neeraj.restapis.order_management_system_project.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT DISTINCT o FROM Order o " + "JOIN FETCH o.user " + "LEFT JOIN FETCH o.orderItems oi "
			+ "LEFT JOIN FETCH oi.product")
	List<Order> findAllWithItems();

	@Query("SELECT DISTINCT o FROM Order o " + "JOIN FETCH o.user " + "LEFT JOIN FETCH o.orderItems oi "
			+ "LEFT JOIN FETCH oi.product " + "WHERE o.orderId = :orderId")
	Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

	@Query("SELECT DISTINCT o FROM Order o " + "JOIN FETCH o.user u " + "LEFT JOIN FETCH o.orderItems oi "
			+ "LEFT JOIN FETCH oi.product " + "WHERE u.userId = :userId")
	List<Order> findByUserIdWithItems(@Param("userId") Long userId);

	@Query("SELECT DISTINCT o FROM Order o " + "JOIN FETCH o.user u " + "LEFT JOIN FETCH o.orderItems oi "
			+ "LEFT JOIN FETCH oi.product " + "WHERE u.email = :email")
	List<Order> findByUserEmailWithItems(@Param("email") String email);
}