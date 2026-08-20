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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.slf4j.LoggerFactory;
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

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
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
        logger.info("Registering consultant with mobile: {}", request.getMobile());

        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            logger.error("Mobile number {} already registered", request.getMobile());
            throw new DuplicateResourceException("Mobile number already registered");
        }

        if (userProfileRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("Email {} already registered", request.getEmail());
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

        logger.info("Saving user profile for consultant with email: {}", request.getEmail());
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        logger.info("Saved user profile for consultant with email: {}", request.getEmail());

        return convertToUserResponse(savedUserProfile);
    }

    @Override
    public com.example.vp.consultancy.dto.UserResponse createAdmin(com.example.vp.consultancy.dto.ConsultantRegistrationRequest request) {
        Assert.notNull(request, "Admin registration request cannot be null");
        Assert.hasText(request.getMobile(), "Mobile is required");
        Assert.hasText(request.getEmail(), "Email is required");
        logger.info("Creating admin with mobile: {}", request.getMobile());

        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            logger.error("Mobile number {} already registered", request.getMobile());
            throw new DuplicateResourceException("Mobile number already registered");
        }

        if (userProfileRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("Email {} already registered", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ADMIN);
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedUser);
        userProfile.setEmail(request.getEmail());
        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());
        logger.info("Saving user profile for admin with email: {}", request.getEmail());

        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        logger.info("Saved user profile for admin with email: {}", request.getEmail());

        return convertToUserResponse(savedUserProfile);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "consultantFarmers", allEntries = true),
            @CacheEvict(cacheNames = "farmersPortfolio", allEntries = true),
            @CacheEvict(cacheNames = "consultantActiveSummary", allEntries = true)
    })
    public UserResponse registerFarmer(FarmerRegistrationRequest request, Long consultantId) {
        Assert.notNull(request, "Farmer registration request cannot be null");
        Assert.notNull(consultantId, "Consultant ID cannot be null");
        Assert.hasText(request.getMobile(), "Mobile is required");
        logger.info("Registering farmer with mobile: {} under consultant ID: {}", request.getMobile(), consultantId);

        User consultant = userRepository.findById(consultantId)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));
        logger.info("Found consultant with ID: {} for farmer registration", consultantId);

        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            logger.error("Mobile number {} already registered", request.getMobile());
            throw new DuplicateResourceException("Mobile number already registered");
        }

//        if (userProfileRepository.findByEmail(request.getEmail()).isPresent()) {
//            logger.error("Email {} already registered", request.getEmail());
//            throw new DuplicateResourceException("Email already registered");
//        }

        User farmer = new User();
        farmer.setMobile(request.getMobile());
//        String generatedPassword = generateRandomPassword(10);
        String generatedPassword = request.getMobile().substring(request.getMobile().length() - 4); // Last 4 digits of mobile
        farmer.setPassword(passwordEncoder.encode(generatedPassword));
        farmer.setRole(UserRole.FARMER);
        farmer.setStatus("ACTIVE");

        logger.info("Saving farmer with mobile: {} and generated password", request.getMobile());
        User savedFarmer = userRepository.save(farmer);
        logger.info("Saved farmer with mobile: {}", request.getMobile());

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedFarmer);
        userProfile.setEmail(request.getEmail());
        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());
        userProfile.setConsultant(consultant);
        userProfile.setSector(request.getSector());

        Address address = new Address();
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        logger.info("Saving address for farmer with mobile: {}", request.getMobile());
        
        Address savedAddress = addressRepository.save(address);
        userProfile.setAddress(savedAddress);

        logger.info("Saving user profile for farmer with email: {}", request.getEmail());
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        logger.info("Saved user profile for farmer with email: {}", request.getEmail());

        // Build response including the generated password so the consultant receives it
        UserResponse response = convertToUserResponse(savedUserProfile);
        response.setFirstName(userProfile.getFirstName());
        response.setLastName(userProfile.getLastName());
        response.setEmail(userProfile.getEmail());
        response.setGeneratedPassword(generatedPassword);

        return response;
    }

    /**
     * Generates a random alphanumeric password of the requested length.
     */
    private String generateRandomPassword(int length) {
        logger.info("Generating random password of length: {}", length);

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        logger.info("Generated random password: {}", sb.toString());
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        Assert.notNull(userId, "User ID cannot be null");
        logger.info("Fetching user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        logger.info("Found user with ID: {}", userId);
        
        return convertToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "consultantFarmers", key = "#consultantId")
    public List<UserResponse> getAllFarmers(Long consultantId) {
        Assert.notNull(consultantId, "Consultant ID cannot be null");
        logger.info("Fetching all farmers for consultant ID: {}", consultantId);
        
        userRepository.findById(consultantId)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));
        logger.info("Consultant with ID: {} exists, proceeding to fetch farmers", consultantId);
        
        List<User> farmers = userProfileRepository.findByConsultantId(consultantId)
            .stream()
            .map(up -> up.getUser())
            .collect(Collectors.toList());
        logger.info("Found {} farmers for consultant ID: {}", farmers.size(), consultantId);
        
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
        logger.info("Fetching current user with mobile: {}", mobile);

        User user = userRepository.findByMobile(mobile)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        logger.info("Found current user with mobile: {}", mobile);

        return convertToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public com.example.vp.consultancy.dto.UserDetailsResponse getCurrentUserDetails() {
        String mobile = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
        logger.info("Fetching current user details with mobile: {}", mobile);

        User user = userRepository.findByMobile(mobile)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        logger.info("Found current user details with mobile: {}", mobile);

        com.example.vp.consultancy.dto.UserDetailsResponse details = new com.example.vp.consultancy.dto.UserDetailsResponse();
        details.setId(user.getId());
        details.setMobile(user.getMobile());
        details.setRole(user.getRole() != null ? user.getRole().toString() : null);
        details.setStatus(user.getStatus());
        logger.info("Populating user details for user ID: {}", user.getId());

        // Populate profile and address if available
        userProfileRepository.findByUserId(user.getId()).ifPresent(up -> {
            details.setFirstName(up.getFirstName());
            details.setLastName(up.getLastName());
            details.setEmail(up.getEmail());
            if (up.getConsultant() != null) {
                details.setConsultantId(up.getConsultant().getId());
                logger.info("Populating consultant ID for user ID: {}: {}", user.getId(), up.getConsultant().getId());
            }
            if (up.getAddress() != null) {
                logger.info("Populating address details for user ID: {}", user.getId());
                com.example.vp.consultancy.dto.UserDetailsResponse.AddressDto addr = new com.example.vp.consultancy.dto.UserDetailsResponse.AddressDto();
                addr.setId(up.getAddress().getId());
                addr.setAddressLine(up.getAddress().getAddressLine());
                addr.setCity(up.getAddress().getCity());
                addr.setDistrict(up.getAddress().getDistrict());
                addr.setState(up.getAddress().getState());
                addr.setPostalCode(up.getAddress().getPostalCode());
                details.setAddress(addr);
            }
        });

        return details;
    }

    @Override
    public boolean findByMobile(String number) {
        return userRepository.findByMobile(number).isPresent();
    }

    @Override
    public void createUser(User admin) {
        userRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByMobile(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + username));
        logger.info("Loaded user by username: {}", username);
        
        return new org.springframework.security.core.userdetails.User(
            user.getMobile(), 
            user.getPassword(), 
            getAuthorities(user)
        );
    }

    private UserResponse convertToUserResponse(UserProfile user) {
        UserResponse response = new UserResponse();
        response.setId(user.getUser().getId());
        response.setMobile(user.getUser().getMobile());
        response.setRole(user.getUser().getRole().toString());
        response.setStatus(user.getUser().getStatus());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        return response;
    }

    /**
     * Convert a User entity to UserResponse. If a UserProfile exists for the user,
     * include profile fields (firstName, lastName, email).
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setMobile(user.getMobile());
        response.setRole(user.getRole() != null ? user.getRole().toString() : null);
        response.setStatus(user.getStatus());

        // Try to populate profile fields when available
        if (user != null && user.getId() != null) {
            userProfileRepository.findByUserId(user.getId()).ifPresent(up -> {
                response.setFirstName(up.getFirstName());
                response.setLastName(up.getLastName());
                response.setEmail(up.getEmail());
            });
        }

        return response;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
}
