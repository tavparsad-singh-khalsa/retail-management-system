package com.retail.auth.repository;

import com.retail.auth.entity.Role;
import com.retail.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByRole(Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("""
       SELECT u
       FROM User u
       JOIN FETCH u.role
       WHERE u.username = :username
       """)
    Optional<User> findByUsernameWithRole(
            @Param("username") String username
    );
}