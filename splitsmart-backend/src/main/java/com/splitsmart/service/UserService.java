package com.splitsmart.service;

import com.splitsmart.dto.request.ChangePasswordRequest;
import com.splitsmart.dto.request.UpdateProfileRequest;
import com.splitsmart.dto.response.UserDTO;
import com.splitsmart.exception.BadRequestException;
import com.splitsmart.exception.ResourceNotFoundException;
import com.splitsmart.model.User;
import com.splitsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO getProfile(User currentUser) {
        return toDTO(currentUser);
    }

    public UserDTO updateProfile(User currentUser, UpdateProfileRequest req) {
        if (req.getName() != null && !req.getName().isBlank()) {
            currentUser.setName(req.getName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(currentUser.getEmail()) &&
                    userRepository.existsByEmail(req.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            currentUser.setEmail(req.getEmail());
        }
        if (req.getMonthlySpendingLimit() != null) {
            currentUser.setMonthlySpendingLimit(req.getMonthlySpendingLimit());
        }
        userRepository.save(currentUser);
        return toDTO(currentUser);
    }

    public void changePassword(User currentUser, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        currentUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(currentUser);
    }

    public UserDTO searchByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));
        return toDTO(user);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .monthlySpendingLimit(user.getMonthlySpendingLimit())
                .build();
    }
}
