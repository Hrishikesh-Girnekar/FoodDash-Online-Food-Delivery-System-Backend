package com.app.fooddash.repository;

import com.app.fooddash.entity.Role;
import com.app.fooddash.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);
}
