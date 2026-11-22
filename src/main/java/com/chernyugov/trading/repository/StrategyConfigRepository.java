package com.chernyugov.trading.repository;

import com.chernyugov.trading.entity.StrategyConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategyConfigRepository extends JpaRepository<StrategyConfigEntity, Long> {
    boolean existsByName(String name);

    List<StrategyConfigEntity> findAllByOrderByNameAsc();
}
