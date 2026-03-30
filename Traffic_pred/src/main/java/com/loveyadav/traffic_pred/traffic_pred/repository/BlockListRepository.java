package com.loveyadav.traffic_pred.traffic_pred.repository;

import com.loveyadav.traffic_pred.traffic_pred.entity.BlockListedTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockListRepository extends JpaRepository<BlockListedTokens,Long> {
    boolean existsByToken(String token);
}
