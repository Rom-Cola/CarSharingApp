package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.exception.RegistrationException;
import com.loievroman.carsharingapp.mapper.UserMapper;
import com.loievroman.carsharingapp.model.Role;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.repository.RoleRepository;
import com.loievroman.carsharingapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
                    "Can't register user. User with email=%s already registered."
                            .formatted(requestDto.getEmail())
            );
        }
        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Role userRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Role " + Role.RoleName.CUSTOMER.name()
                                + "not found in the database"));

        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }
}
