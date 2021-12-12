package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.User;
import com.bannergress.backend.exceptions.VerificationFailedException;
import com.bannergress.backend.exceptions.VerificationStateException;
import com.bannergress.backend.repositories.UserRepository;
import com.bannergress.backend.services.UserMappingService;
import com.bannergress.backend.services.UserService;
import com.bannergress.backend.services.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private VerificationService verificationService;

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
    public void verify(String userId) throws VerificationStateException, VerificationFailedException {
        User user = getOrCreate(userId);
        if (user.getVerificationAgent() == null || user.getVerificationToken() == null) {
            throw new VerificationStateException();
        }
        String agentName = verificationService.verify(user.getVerificationAgent(), user.getVerificationToken());
        userMappingService.setAgentName(userId, agentName);
        clearClaim(userId);
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
