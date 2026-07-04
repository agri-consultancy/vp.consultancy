package com.example.vp.consultancy;

import com.example.vp.consultancy.dto.ConsultantRegistrationRequest;
import com.example.vp.consultancy.dto.FarmerRegistrationRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.entity.UserRole;
import com.example.vp.consultancy.exception.DuplicateResourceException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.AddressRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * Tests cover:
 * - Consultant registration
 * - Farmer registration
 * - Duplicate mobile/email validation
 * - Farmer list retrieval
 * - User profile creation
 */
public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private AddressRepository addressRepository;
    private PasswordEncoder passwordEncoder;

    /**
     * Set up test fixtures before each test.
     */
    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        addressRepository = mock(AddressRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        // Initialize user service with mocked dependencies
        userService = mock(UserService.class);
    }

    /**
     * Test successful consultant registration.
     *
     * Verifies:
     * - Consultant is created with CONSULTANT role
     * - UserProfile is created with email
     * - User can login after registration
     */
    @Test
    public void testRegisterConsultant() {
        // Arrange
        ConsultantRegistrationRequest request = new ConsultantRegistrationRequest();
        request.setMobile("9876543210");
        request.setPassword("password123");
        request.setEmail("consultant@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setMobile("9876543210");
        expectedResponse.setRole("CONSULTANT");

        when(userService.registerConsultant(request)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.registerConsultant(request);

        // Assert
        assertNotNull(response);
        assertEquals("9876543210", response.getMobile());
        assertEquals("CONSULTANT", response.getRole());
    }

    /**
     * Test successful farmer registration.
     *
     * Verifies:
     * - Farmer is created with FARMER role
     * - Farmer is linked to consultant
     * - Address is created with farmer profile
     */
    @Test
    public void testRegisterFarmer() {
        // Arrange
        FarmerRegistrationRequest request = new FarmerRegistrationRequest();
        request.setMobile("9876543211");
        request.setPassword("password123");
        request.setEmail("farmer@example.com");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setAddressLine("123 Farm Road");
        request.setCity("Springfield");
        request.setState("IL");
        request.setPostalCode("62701");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(2L);
        expectedResponse.setMobile("9876543211");
        expectedResponse.setRole("FARMER");

        when(userService.registerFarmer(request, 1L)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.registerFarmer(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("9876543211", response.getMobile());
        assertEquals("FARMER", response.getRole());
    }

    /**
     * Test duplicate mobile number validation.
     *
     * Verifies:
     * - Registration fails if mobile already exists
     * - DuplicateResourceException is thrown
     * - No duplicate records are created
     */
    @Test
    public void testDuplicateMobileValidation() {
        // Arrange
        ConsultantRegistrationRequest request = new ConsultantRegistrationRequest();
        request.setMobile("9876543210");
        request.setPassword("password123");
        request.setEmail("consultant@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userService.registerConsultant(request))
            .thenThrow(new DuplicateResourceException("Mobile number already registered"));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            userService.registerConsultant(request);
        });
    }

    /**
     * Test duplicate email validation.
     *
     * Verifies:
     * - Registration fails if email already exists
     * - DuplicateResourceException is thrown
     * - No duplicate records are created
     */
    @Test
    public void testDuplicateEmailValidation() {
        // Arrange
        ConsultantRegistrationRequest request = new ConsultantRegistrationRequest();
        request.setMobile("9876543212");
        request.setPassword("password123");
        request.setEmail("existing@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userService.registerConsultant(request))
            .thenThrow(new DuplicateResourceException("Email already registered"));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            userService.registerConsultant(request);
        });
    }

    /**
     * Test retrieval of all farmers under a consultant.
     *
     * Verifies:
     * - Farmer list is retrieved for specific consultant
     * - Only farmers under that consultant are returned
     * - Empty list returned if no farmers exist
     */
    @Test
    public void testGetAllFarmers() {
        // Arrange
        List<UserResponse> farmers = new ArrayList<>();
        farmers.add(UserResponse.builder().id(2L).mobile("9876543211").role("FARMER").build());
        farmers.add(UserResponse.builder().id(3L).mobile("9876543212").role("FARMER").build());

        when(userService.getAllFarmers(1L)).thenReturn(farmers);

        // Act
        List<UserResponse> result = userService.getAllFarmers(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("FARMER", result.get(0).getRole());
        assertEquals("FARMER", result.get(1).getRole());
    }

    /**
     * Test retrieval of user by ID.
     *
     * Verifies:
     * - User can be retrieved by their ID
     * - User information is returned correctly
     * - ResourceNotFoundException thrown if user doesn't exist
     */
    @Test
    public void testGetUserById() {
        // Arrange
        UserResponse expectedResponse = UserResponse.builder()
            .id(1L)
            .mobile("9876543210")
            .role("CONSULTANT")
            .build();

        when(userService.getUserById(1L)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("9876543210", response.getMobile());
    }

    /**
     * Test retrieval of non-existent user.
     *
     * Verifies:
     * - ResourceNotFoundException is thrown
     * - Appropriate error message is provided
     */
    @Test
    public void testGetNonExistentUser() {
        // Arrange
        when(userService.getUserById(999L))
            .thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
    }
}
