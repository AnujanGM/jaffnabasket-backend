package com.jaffnabasket.backend.user.repository;

import com.jaffnabasket.backend.user.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}
