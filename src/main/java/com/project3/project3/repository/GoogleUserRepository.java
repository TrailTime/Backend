package com.project3.project3.repository;

import com.project3.project3.model.GoogleUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleUserRepository extends MongoRepository<GoogleUser, String> {
    Optional<GoogleUser> findByGoogleUserId(String googleUserId);
}

