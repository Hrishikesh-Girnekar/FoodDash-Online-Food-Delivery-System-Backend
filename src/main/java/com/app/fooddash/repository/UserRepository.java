package com.app.fooddash.repository;

import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RoleType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    List<User> findByRoles_Name(RoleType roleType);
    
    
}
