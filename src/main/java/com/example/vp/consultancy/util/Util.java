package com.example.vp.consultancy.util;

import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

public class Util {

    private static UserProfileRepository userProfileRepository = null;
    private static UserRepository userRepository = null;

    public Util(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        Util.userProfileRepository = userProfileRepository;
        Util.userRepository = userRepository;
    }
    public static UserProfile getCurrentUserProfile() {
        String userMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByMobile(userMobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
    }
}
