package com.jaffnabasket.backend.user.repository;

import com.jaffnabasket.backend.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}
