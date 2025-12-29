package com.kobe.pokekernle.domain.order.repository;

import com.kobe.pokekernle.domain.order.entity.Order;
import com.kobe.pokekernle.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);
}

