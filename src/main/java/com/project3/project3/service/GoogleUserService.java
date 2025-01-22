package com.project3.project3.service;

import com.project3.project3.model.GoogleUser;
import com.project3.project3.repository.GoogleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GoogleUserService {

    @Autowired
    private GoogleUserRepository googleUserRepository;

    public GoogleUser findOrCreateGoogleUser(GoogleUser googleUser) {
        Optional<GoogleUser> existingUser = googleUserRepository.findByGoogleUserId(googleUser.getGoogleUserId());

        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        return googleUserRepository.save(googleUser);
    }
}

