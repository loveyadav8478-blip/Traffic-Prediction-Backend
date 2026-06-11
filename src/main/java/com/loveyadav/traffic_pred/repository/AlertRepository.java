package com.loveyadav.traffic_pred.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loveyadav.traffic_pred.entity.Alert;
import com.loveyadav.traffic_pred.alert.AlertType;
import com.loveyadav.traffic_pred.alert.Severity;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findTop50ByOrderByTimestampDesc();

    List<Alert> findByTimestampAfterOrderByTimestampDesc(LocalDateTime since);

    List<Alert> findByTypeOrderByTimestampDesc(AlertType type);

    List<Alert> findBySeverityOrderByTimestampDesc(Severity severity);

    long countByBroadcastedFalse();
}