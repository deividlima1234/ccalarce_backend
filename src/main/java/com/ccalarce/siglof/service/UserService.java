package com.ccalarce.siglof.service;

import com.ccalarce.siglof.dto.RegisterRequest;
import com.ccalarce.siglof.dto.UpdateUserRequest;
import com.ccalarce.siglof.dto.UserDto;
import com.ccalarce.siglof.model.entity.User;
import com.ccalarce.siglof.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return mapToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @com.ccalarce.siglof.annotation.Auditable(action = "CREATE_USER")
    public UserDto createUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    @com.ccalarce.siglof.annotation.Auditable(action = "UPDATE_USER")
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getActive() != null)
            user.setActive(request.getActive());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToDto(userRepository.save(user));
    }

    @Transactional
    @com.ccalarce.siglof.annotation.Auditable(action = "DELETE_USER")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }
}
