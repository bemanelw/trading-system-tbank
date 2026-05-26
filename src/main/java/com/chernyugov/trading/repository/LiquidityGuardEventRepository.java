package com.chernyugov.trading.repository;

import com.chernyugov.trading.entity.LiquidityGuardEventEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidityGuardEventRepository extends JpaRepository<LiquidityGuardEventEntity, Long> {
    default List<LiquidityGuardEventEntity> findLastEvents() {
        return findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
}
