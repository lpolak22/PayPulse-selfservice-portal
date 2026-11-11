package com.example.paypulse.repository;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

    @Override
    @EntityGraph(attributePaths = "organisation")
    Page<AppUser> findAll(Specification<AppUser> specification, Pageable pageable);

    @Query("""
            select u from AppUser u join fetch u.organisation
            where u.idUser = :userId
              and u.organisation.idOrganisation = :organisationId
              and u.deletedAt is null
            """)
    Optional<AppUser> findUndeletedInOrganisation(Long userId, Long organisationId);

    @Query("""
            select u from AppUser u join fetch u.organisation
            where lower(u.email) = lower(:email) and u.deletedAt is null
            """)
    Optional<AppUser> findUndeletedByEmail(String email);

    @Query("""
            select count(u) > 0 from AppUser u
            where lower(u.email) = lower(:email) and u.deletedAt is null
            """)
    boolean existsUndeletedByEmail(String email);

    @Query("""
            select count(u) > 0 from AppUser u
            where lower(u.email) = lower(:email) and u.deletedAt is null and u.idUser <> :excludedUserId
            """)
    boolean existsUndeletedByEmailExcludingUser(String email, Long excludedUserId);

    @Query("""
            select count(u) > 0 from AppUser u
            where u.organisation.idOrganisation = :organisationId
              and u.role = :role
              and u.active = true
              and u.deletedAt is null
              and u.idUser <> :excludedUserId
            """)
    boolean existsOtherActiveUserWithRole(Long organisationId, UserRole role, Long excludedUserId);

    @Query("select u.updatedAt from AppUser u where u.idUser = :userId")
    Optional<LocalDateTime> findUpdatedAt(Long userId);
}
