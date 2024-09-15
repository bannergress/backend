package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.User;
import com.bannergress.backend.repositories.UserRepository;
import com.bannergress.backend.services.UserMappingService;
import com.bannergress.backend.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private UserMappingService userMappingService;

    @Override
    public User getOrCreate(String userId) {
        Optional<User> optionalUser = repository.findById(userId);
        return optionalUser.orElseGet(() -> {
            User user = new User();
            user.setId(userId);
            return repository.save(user);
        });
    }

    @Override
    public void claim(String userId, String agent) {
        User user = getOrCreate(userId);
        user.setVerificationAgent(agent);
        user.setVerificationToken(UUID.randomUUID());
    }

    @Override
    public void unlink(String userId) {
        userMappingService.setAgentName(userId, null);
    }

    @Override
    public void clearClaim(String userId) {
        User user = getOrCreate(userId);
        user.setVerificationAgent(null);
        user.setVerificationToken(null);
    }
}
