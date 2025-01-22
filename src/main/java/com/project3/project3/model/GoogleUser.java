package com.project3.project3.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleUser implements UserDetails {

    private String googleUserId;
    private String googleName;
    private String profilePictureUrl;
    private String email;
    private List<String> authorities;

    public GoogleUser() {}

    public GoogleUser(String googleUserId, String googleName, String profilePictureUrl, String email, List<String> authorities) {
        this.googleUserId = googleUserId;
        this.googleName = googleName;
        this.profilePictureUrl = profilePictureUrl;
        this.email = email;
        this.authorities = authorities;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public void setGoogleUserId(String googleUserId) {
        this.googleUserId = googleUserId;
    }

    public String getGoogleName() {
        return googleName;
    }

    public void setGoogleName(String googleName) {
        this.googleName = googleName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getAuthoritiesList() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null; // No password for GoogleUser
    }

    @Override
    public String getUsername() {
        return email; // Using email as the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Assuming Google accounts do not expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Assuming Google accounts are not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Assuming credentials are always valid
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static GoogleUser googleUserFactory(String googleUserId, String googleName, String profilePictureUrl, String email) {
        return new GoogleUser(
                googleUserId,
                googleName,
                profilePictureUrl,
                email,
                Collections.singletonList("ROLE_USER")
        );
    }
}

