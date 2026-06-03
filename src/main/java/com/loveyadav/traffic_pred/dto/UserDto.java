package com.loveyadav.traffic_pred.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loveyadav.traffic_pred.entity.Role;
import lombok.Data;

@Data
public class UserDto {
    private String name;
    private String email;
    private String password;
    private Role role;
}
