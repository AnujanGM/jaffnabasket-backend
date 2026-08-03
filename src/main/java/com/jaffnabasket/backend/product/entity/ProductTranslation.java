package com.jaffnabasket.backend.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "locale"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Locale locale;

    @Column(nullable = false)
    private String name;

    private String shortDescription;

    @Column(columnDefinition = "text")
    private String longDescription;

    private String seoTitle;

    private String seoDescription;
}
