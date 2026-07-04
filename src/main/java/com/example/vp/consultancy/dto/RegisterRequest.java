package com.example.vp.consultancy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String mobile;
    private String password;
    private String address;
    private String verityName;
    private Double totalLand;
    private Integer totalPlants;
}