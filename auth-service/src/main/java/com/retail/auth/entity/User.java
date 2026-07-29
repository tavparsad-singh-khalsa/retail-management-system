package com.retail.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter // lombok annotations
@Setter // "
@NoArgsConstructor // "
@AllArgsConstructor // "
@Builder // "
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 15)
    private String phone;

    @Column(unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY) // This is a performance optimization. It tells Hibernate not to fetch the user's role from the database automatically when loading the user. It will only query the database for the role if you explicitly call user.getRole() in your Java code
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean forcePasswordChange = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean accountLocked = false;

    private LocalDateTime accountLockedUntil;

    private LocalDateTime lastLoginAt;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}