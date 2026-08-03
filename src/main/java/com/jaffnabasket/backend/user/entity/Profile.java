package com.jaffnabasket.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Shares its primary key with {@link User} (id == user.id) since the spec lists
 * no separate id for Profile, only "userId (FK)".
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Profile {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private PreferredLanguage preferredLanguage;

    private String avatarUrl;
}
