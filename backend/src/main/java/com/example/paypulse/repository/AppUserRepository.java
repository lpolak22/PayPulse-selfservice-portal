package com.example.paypulse.repository;

import com.example.paypulse.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = "organisation")
    List<AppUser> findAllByDeletedAtIsNullOrderByLastNameAscFirstNameAsc();
}
