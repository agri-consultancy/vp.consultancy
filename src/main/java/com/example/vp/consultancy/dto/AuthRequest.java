package com.example.vp.consultancy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String mobile;
    private String password;
}