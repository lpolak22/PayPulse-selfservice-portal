package com.example.paypulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class AppUser {

    public static final String EMAIL_UNIQUE_INDEX = "ux_app_user_email_lower";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_organisation", nullable = false)
    private Organisation organisation;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 16)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "password_hash", length = 60)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_first_login")
    private Boolean isFirstLogin = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected AppUser() {
    }

    public static AppUser createPending(Organisation organisation, String firstName, String lastName,
                                        String email, String phone, UserRole role) {
        AppUser user = new AppUser();
        user.organisation = organisation;
        user.changeFirstName(firstName);
        user.changeLastName(lastName);
        user.changeEmail(email);
        user.changePhone(phone);
        user.changeRole(role);
        user.changeStatus(UserStatus.PENDING_ACTIVATION);
        return user;
    }

    public void changeFirstName(String firstName) {
        this.firstName = firstName.strip();
    }

    public void changeLastName(String lastName) {
        this.lastName = lastName.strip();
    }

    public void changeEmail(String email) {
        this.email = email.strip();
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
        syncActiveFlag();
    }

    public void completeActivation(String passwordHash) {
        this.passwordHash = passwordHash;
        this.isFirstLogin = false;
        changeStatus(UserStatus.ACTIVE);
    }

    public void markActivationResent(LocalDateTime resentAt) {
        this.updatedAt = resentAt;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        syncActiveFlag();
    }

    private void syncActiveFlag() {
        this.active = status == UserStatus.ACTIVE && deletedAt == null;
    }

    public Long getIdUser() {
        return idUser;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Boolean getIsFirstLogin() {
        return isFirstLogin;
    }

    public void setIsFirstLogin(Boolean isFirstLogin) {
        this.isFirstLogin = isFirstLogin;
    }
}
