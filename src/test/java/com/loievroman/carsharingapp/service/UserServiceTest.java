package com.loievroman.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserProfileUpdateRequestDto;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.dto.user.UserRoleUpdateRequestDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.exception.RegistrationException;
import com.loievroman.carsharingapp.mapper.UserMapper;
import com.loievroman.carsharingapp.model.Role;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.repository.RoleRepository;
import com.loievroman.carsharingapp.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encodedPassword");

        customerRole = new Role();
        customerRole.setId(1L);
        customerRole.setName(Role.RoleName.CUSTOMER);
        user.setRoles(Set.of(customerRole));
    }

    @Test
    @DisplayName("register_NewUser_ReturnsUserResponseDto")
    void register_NewUser_ReturnsUserResponseDto() throws RegistrationException {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setEmail("test@example.com");

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleName.CUSTOMER))
                .thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(expectedResponse);

        // When
        UserResponseDto actualResponse = userService.register(requestDto);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
    }

    @Test
    @DisplayName("register_ExistingUser_ThrowsRegistrationException")
    void register_ExistingUser_ThrowsRegistrationException() {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(RegistrationException.class, () -> userService.register(requestDto));
    }

    @Test
    @DisplayName("updateRoles_ValidUserIdAndRoles_ReturnsUserProfileResponseDto")
    void updateRoles_ValidUserIdAndRoles_ReturnsUserProfileResponseDto() {
        // Given
        Long userId = 1L;
        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto();
        Role managerRole = new Role();
        managerRole.setName(Role.RoleName.MANAGER);
        requestDto.setRoles(Set.of(Role.RoleName.MANAGER));

        UserProfileResponseDto expectedResponse = new UserProfileResponseDto();
        expectedResponse.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(Role.RoleName.MANAGER)).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileDto(any(User.class))).thenReturn(expectedResponse);

        // When
        UserProfileResponseDto actualResponse = userService.updateRoles(userId, requestDto);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
    }

    @Test
    @DisplayName("updateRoles_InvalidUserId_ThrowsEntityNotFoundException")
    void updateRoles_InvalidUserId_ThrowsEntityNotFoundException() {
        // Given
        Long invalidUserId = 99L;
        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto();
        requestDto.setRoles(Set.of(Role.RoleName.CUSTOMER));

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> userService.updateRoles(invalidUserId, requestDto));
    }

    @Test
    @DisplayName("getMyProfile_ValidUser_ReturnsUserProfileResponseDto")
    void getMyProfile_ValidUser_ReturnsUserProfileResponseDto() {
        // Given
        UserProfileResponseDto expectedResponse = new UserProfileResponseDto();
        expectedResponse.setId(user.getId());
        expectedResponse.setEmail(user.getEmail());

        when(userMapper.toProfileDto(user)).thenReturn(expectedResponse);

        // When
        UserProfileResponseDto actualResponse = userService.getMyProfile(user);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
    }

    @Test
    @DisplayName("updateMyProfile_ValidUserAndRequest_ReturnsUserProfileResponseDto")
    void updateMyProfile_ValidUserAndRequest_ReturnsUserProfileResponseDto() {
        // Given
        UserProfileUpdateRequestDto requestDto = new UserProfileUpdateRequestDto();
        requestDto.setFirstName("Jane");

        UserProfileResponseDto expectedResponse = new UserProfileResponseDto();
        expectedResponse.setId(user.getId());
        expectedResponse.setFirstName("Jane");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileDto(any(User.class))).thenReturn(expectedResponse);

        // When
        UserProfileResponseDto actualResponse = userService.updateMyProfile(user, requestDto);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
    }
}
