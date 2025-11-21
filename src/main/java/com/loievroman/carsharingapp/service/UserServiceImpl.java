package com.loievroman.carsharingapp.service;

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
import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "Can't register user. User with email=%s already registered.".formatted(
                            requestDto.getEmail()));
        }
        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Role userRole = roleRepository.findByName(Role.RoleName.CUSTOMER).orElseThrow(
                () -> new EntityNotFoundException(
                        "Role " + Role.RoleName.CUSTOMER.name() + "not found in the database"));

        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateRole(Long userId, UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + userId));

        Set<Role> newRoles = requestDto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName).orElseThrow(
                        () -> new EntityNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);

        return userMapper.toProfileDto(user);
    }

    @Override
    public UserProfileResponseDto getMyProfile(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return userMapper.toProfileDto(currentUser);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateMyProfile(Authentication authentication,
                                                  UserProfileUpdateRequestDto requestDto) {
        User currentUser = (User) authentication.getPrincipal();

        if (requestDto.getEmail() != null) {
            currentUser.setEmail(requestDto.getEmail());
        }
        if (requestDto.getFirstName() != null) {
            currentUser.setFirstName(requestDto.getFirstName());
        }
        if (requestDto.getLastName() != null) {
            currentUser.setLastName(requestDto.getLastName());
        }

        User updatedUser = userRepository.save(currentUser);

        return userMapper.toProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public UserProfileResponseDto addRoleToUser(Long userId, UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + userId));

        requestDto.getRoles().forEach(roleName -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
            user.getRoles().add(role);
        });

        return userMapper.toProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDto removeRoleFromUser(Long userId,
                                                     UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + userId));

        requestDto.getRoles().forEach(roleName -> user.getRoles()
                .remove(roleRepository.findByName(roleName).orElseThrow(
                        () -> new EntityNotFoundException("Role not found: " + roleName))));

        return userMapper.toProfileDto(user);
    }

}
