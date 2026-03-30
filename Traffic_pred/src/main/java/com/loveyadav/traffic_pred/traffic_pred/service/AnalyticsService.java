package com.loveyadav.traffic_pred.traffic_pred.service;

import com.loveyadav.traffic_pred.traffic_pred.repository.TrafficRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private TrafficRepository trafficRepository;

    //Popular Routes
    public List<Map<String,Object>> getPopularRoutes(){
        List<Object[]> popularRoutes = trafficRepository.getPopularRoutes();
        List<Map<String,Object>> result = new ArrayList<>();
        for(Object[] row : popularRoutes){
            Map<String,Object> mp = new HashMap<>();
            mp.put("source",row[0]);
            mp.put("destination",row[1]);
            mp.put("Count",row[2]);
            result.add(mp);
        }
        return result;
    }

//    Traffic Distribution
    public List<Map<String,Object>> getTrafficStats(){
        List<Object[]> trafficDistribution = trafficRepository.getTrafficDistribution();
        List<Map<String,Object>> result = new ArrayList<>();
        for(Object[] row : trafficDistribution){
            Map<String,Object> map = new HashMap<>();
            int level = (int)row[0];
            String label = "";
            if(level==0) label = "Low Traffic";
            else if(level==1) label = "Medium Traffic";
            else if(level==2)label = "High Traffic";
            map.put("traffic level",label);
            map.put("Count",row[1]);
            result.add(map);
        }
        return result;
    }

//    Peak Hours
    public List<Map<String,Object>> getPeakHours(){
        List<Object[]> peakHours = trafficRepository.getPeakHours();
        List<Map<String,Object>> result = new ArrayList<>();
        for(Object[] row : peakHours){
            Map<String,Object> mp = new HashMap<>();
            mp.put("hours",row[0]);
            mp.put("requests",row[1]);
            result.add(mp);
        }
        return result;
    }
}
