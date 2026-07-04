package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.dto.ConsultantRegistrationRequest;
import com.example.vp.consultancy.dto.FarmerRegistrationRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.entity.Address;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.entity.UserRole;
import com.example.vp.consultancy.exception.DuplicateResourceException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.AddressRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService.
 * Handles all user management operations.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                         UserProfileRepository userProfileRepository,
                         AddressRepository addressRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerConsultant(ConsultantRegistrationRequest request) {
        Assert.notNull(request, "Consultant registration request cannot be null");
        Assert.hasText(request.getMobile(), "Mobile is required");
        Assert.hasText(request.getEmail(), "Email is required");
        Assert.hasText(request.getPassword(), "Password is required");

        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new DuplicateResourceException("Mobile number already registered");
        }

        if (userProfileRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CONSULTANT);
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedUser);
        userProfile.setEmail(request.getEmail());
        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());

        userProfileRepository.save(userProfile);

        return convertToUserResponse(savedUser);
    }

    @Override
    public UserResponse registerFarmer(FarmerRegistrationRequest request, Long consultantId) {
        Assert.notNull(request, "Farmer registration request cannot be null");
        Assert.notNull(consultantId, "Consultant ID cannot be null");
        Assert.hasText(request.getMobile(), "Mobile is required");
        Assert.hasText(request.getEmail(), "Email is required");
        Assert.hasText(request.getPassword(), "Password is required");

        User consultant = userRepository.findById(consultantId)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new DuplicateResourceException("Mobile number already registered");
        }

        if (userProfileRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered");
        }

        User farmer = new User();
        farmer.setMobile(request.getMobile());
        farmer.setPassword(passwordEncoder.encode(request.getPassword()));
        farmer.setRole(UserRole.FARMER);
        farmer.setStatus("ACTIVE");

        User savedFarmer = userRepository.save(farmer);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedFarmer);
        userProfile.setEmail(request.getEmail());
        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());
        userProfile.setConsultant(consultant);

        Address address = new Address();
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        
        Address savedAddress = addressRepository.save(address);
        userProfile.setAddress(savedAddress);

        userProfileRepository.save(userProfile);

        return convertToUserResponse(savedFarmer);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        Assert.notNull(userId, "User ID cannot be null");
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return convertToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllFarmers(Long consultantId) {
        Assert.notNull(consultantId, "Consultant ID cannot be null");
        
        userRepository.findById(consultantId)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));
        
        List<User> farmers = userProfileRepository.findByConsultantId(consultantId)
            .stream()
            .map(up -> up.getUser())
            .collect(Collectors.toList());
        
        return farmers.stream()
            .map(this::convertToUserResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String mobile = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
        
        User user = userRepository.findByMobile(mobile)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        
        return convertToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByMobile(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + username));
        
        return new org.springframework.security.core.userdetails.User(
            user.getMobile(), 
            user.getPassword(), 
            getAuthorities(user)
        );
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setMobile(user.getMobile());
        response.setRole(user.getRole().toString());
        response.setStatus(user.getStatus());
        return response;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
}
