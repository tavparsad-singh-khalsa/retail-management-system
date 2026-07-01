package com.retail.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.retail.auth.entity.User;
import com.retail.auth.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSecurityService {
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;

    @Transactional
    public void recordFailedLogin(User user){
        int failedLoginAttempts =
                user.getFailedLoginAttempts();

        failedLoginAttempts++;

        user.setFailedLoginAttempts(
                failedLoginAttempts
        );

        if(failedLoginAttempts >= 5){
            user.setAccountLocked(true);
            user.setAccountLockedUntil(
                    LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_DURATION_MINUTES)
            );
        }
        userRepository.save(user);
    }

    @Transactional
    public void recordSuccessfulLogin(
            User user){
        user.setAccountLocked(false);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        userRepository.save(user);
    }

    @Transactional
    public User unlockAccount(User user){
        user.setAccountLocked(false);
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }

}