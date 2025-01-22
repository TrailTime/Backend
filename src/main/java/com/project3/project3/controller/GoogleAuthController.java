package com.project3.project3.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project3.project3.model.AuthResponse;
import com.project3.project3.model.GoogleUser;
import com.project3.project3.service.GoogleUserService;
import com.project3.project3.service.JwtService;
import com.project3.project3.service.MilestonesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;

public class GoogleAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleUserService googleUserService;

    @Autowired
    private MilestonesService milestonesService;

    private final String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
    private final String googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
    private final String googleRedirectUri = System.getenv("GOOGLE_REDIRECT_URI");

    @GetMapping("/google-login")
    public RedirectView googleLogin() {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri +
                "&response_type=code" +
                "&scope=profile email";

        return new RedirectView(googleAuthUrl);
    }

    @GetMapping("/google-callback")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code) {
        try {
            String googleToken = exchangeCodeForGoogleToken(code);
            if (googleToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google authentication failed.");
            }

            GoogleUser googleUser = fetchGoogleUserProfile(googleToken);
            if (googleUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to retrieve Google user information.");
            }

            GoogleUser savedGoogleUser = googleUserService.findOrCreateGoogleUser(googleUser);
            milestonesService.createMilestones(savedGoogleUser.getGoogleUserId());
            String jwtToken = jwtService.generateToken(savedGoogleUser.getGoogleUserId(), savedGoogleUser.getAuthorities());
            return ResponseEntity.ok(new AuthResponse(jwtToken));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred during Google authentication.");
        }
    }

    // Exchange authorization code for Google access token
    private String exchangeCodeForGoogleToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenRequestUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "code=" + code + "&client_id=" + googleClientId + "&client_secret=" + googleClientSecret + "&redirect_uri=" + googleRedirectUri + "&grant_type=authorization_code";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenRequestUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.getBody());
                return jsonNode.get("access_token").asText();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    // Fetch Google user profile from access token
    private GoogleUser fetchGoogleUserProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String profileRequestUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(profileRequestUrl, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode profileJson = mapper.readTree(response.getBody());
                String googleUserId = profileJson.get("sub").asText();
                String email = profileJson.get("email").asText();
                String name = profileJson.get("name").asText();
                String picture = profileJson.has("picture") ? profileJson.get("picture").asText() : "profile-pictures/default_profile_picture.jpeg";
                return GoogleUser.googleUserFactory(googleUserId, name, picture, email);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}



