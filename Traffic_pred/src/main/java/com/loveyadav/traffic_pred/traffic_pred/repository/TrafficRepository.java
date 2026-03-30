package com.loveyadav.traffic_pred.traffic_pred.repository;

import com.loveyadav.traffic_pred.traffic_pred.entity.TrafficRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrafficRepository extends JpaRepository<TrafficRecord,Long> {
    //Most searched routes
    @Query("SELECT t.source, t.destination, COUNT(t) as cnt " +
            "FROM TrafficRecord t GROUP BY t.source, t.destination ORDER BY cnt DESC")
    List<Object[]> getPopularRoutes();

    //Traffic distribution
    @Query("SELECT t.predictedTraffic, COUNT(t) FROM TrafficRecord t GROUP BY t.predictedTraffic")
    List<Object[]> getTrafficDistribution();

    //Peak hours
    @Query("SELECT HOUR(t.timestamp), COUNT(t) FROM TrafficRecord t GROUP BY HOUR(t.timestamp)")
    List<Object[]> getPeakHours();


    List<TrafficRecord> findByUserId(Long userId);

    List<TrafficRecord> findAll();
}
