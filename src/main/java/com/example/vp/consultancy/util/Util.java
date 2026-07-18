package com.example.vp.consultancy.util;

import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.UserProfileRepository;
import org.springframework.security.core.context.SecurityContextHolder;

public class Util {

    private static UserProfileRepository userProfileRepository = null;

    public Util(UserProfileRepository userProfileRepository) {
        Util.userProfileRepository = userProfileRepository;
    }
    public static UserProfile getCurrentUserProfile() {
        String userMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userProfileRepository.findByUser_Mobile(userMobile)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
    }
}
