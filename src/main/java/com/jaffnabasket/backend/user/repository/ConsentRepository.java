package com.jaffnabasket.backend.user.repository;

import com.jaffnabasket.backend.user.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsentRepository extends JpaRepository<Consent, UUID> {
}
