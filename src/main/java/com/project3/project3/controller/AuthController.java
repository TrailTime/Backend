package com.project3.project3.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project3.project3.model.AuthRequest;
import com.project3.project3.model.AuthResponse;
import com.project3.project3.model.User;
import com.project3.project3.service.JwtService;
import com.project3.project3.service.MilestonesService;
import com.project3.project3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private MilestonesService milestonesService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
            Optional<User> userOptional = userService.findUserByUsername(authRequest.getUsername());

            if (userOptional.isPresent()) {
                String userId = userOptional.get().getId();
                String token = jwtService.generateToken(userId, userDetails.getAuthorities());
                return ResponseEntity.ok(new AuthResponse(token));
            } else {
                return ResponseEntity.status(404).body("User not found with username: " + authRequest.getUsername());
            }

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("An internal server error occurred");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            user.setProfilePictureUrl("profile-pictures/default_profile_picture.jpeg");
            user.setRoles(Collections.singletonList("ROLE_USER"));
            User savedUser = userService.saveUser(user);
            milestonesService.createMilestones(savedUser.getId());
            return ResponseEntity.ok("User registered successfully. Please log in.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while creating the account.");
        }
    }
}



