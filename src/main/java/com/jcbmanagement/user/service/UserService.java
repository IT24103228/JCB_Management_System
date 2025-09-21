package com.jcbmanagement.user.service;

import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String firstName, String lastName, String username, String password, String role, String email, String contactNumber, String address) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // Hash password
        user.setRole(role);
        user.setEmail(email);
        user.setContactNumber(contactNumber);
        user.setAddress(address);
        return userRepository.save(user);
    }

    public User registerUser(String username, String password, String role, String email) {
        return registerUser("", "", username, password, role, email, null, null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUserRole(Long userId, String newRole) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(newRole.toUpperCase());
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public long countTotalUsers() {
        return userRepository.count();
    }

    public User updateCustomerAccount(Long userId, String username, String email, String password) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!user.getUsername().equals(username)) {
                Optional<User> existingUser = userRepository.findByUsername(username);
                if (existingUser.isPresent() && !existingUser.get().getUserID().equals(userId)) {
                    throw new IllegalArgumentException("Username already exists");
                }
                user.setUsername(username);
            }

            // Update email
            user.setEmail(email);

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            // Note: Role is NOT updated - customers cannot change their own role

            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found with ID: " + userId);
    }
}
