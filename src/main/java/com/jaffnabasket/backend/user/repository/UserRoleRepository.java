package com.jaffnabasket.backend.user.repository;

import com.jaffnabasket.backend.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUser_Id(UUID userId);

    Optional<UserRole> findByUser_IdAndRole_Id(UUID userId, UUID roleId);

    void deleteByUser_IdAndRole_Id(UUID userId, UUID roleId);
}
