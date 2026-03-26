package com.chernyugov.trading.repository;

import com.chernyugov.trading.entity.OrderDomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderDomainEntity, Long> {
}
