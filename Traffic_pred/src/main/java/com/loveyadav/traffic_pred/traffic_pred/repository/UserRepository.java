package com.loveyadav.traffic_pred.traffic_pred.repository;

import com.loveyadav.traffic_pred.traffic_pred.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail(String email);
    List<User> findAll();
}
