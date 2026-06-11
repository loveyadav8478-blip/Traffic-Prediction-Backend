package com.loveyadav.traffic_pred.repository;

import com.loveyadav.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrafficRepository extends JpaRepository<TrafficRecord,Long> {
    @Query("""
    SELECT t.predictedTraffic, COUNT(t)
    FROM TrafficRecord t
    WHERE t.user = :user
    GROUP BY t.predictedTraffic
    """)
    List<Object[]> getTrafficDistributionByUser(User user);

    @Query("""
    SELECT HOUR(t.timestamp), COUNT(t)
    FROM TrafficRecord t
    WHERE t.user = :user
    GROUP BY HOUR(t.timestamp)
    """)
    List<Object[]> getPeakHoursByUser(User user);

    @Query("""
    SELECT t.source, t.destination, COUNT(t)
    FROM TrafficRecord t
    WHERE t.user = :user
    GROUP BY t.source, t.destination
    ORDER BY COUNT(t) DESC
    """)
    List<Object[]> getPopularRoutesByUser(User user);


    List<TrafficRecord> findByUser(User user);

    List<TrafficRecord> findAll();

    void deleteByUserId(Long userId);
}
