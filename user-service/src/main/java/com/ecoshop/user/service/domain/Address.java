package com.ecoshop.user.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_addresses_user_id", columnList = "user_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Address extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "label", length = 50)
    private String label; // HOME, OFFICE, OTHER

    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "line1", nullable = false, length = 255)
    private String line1;

    @Column(name = "line2", length = 255)
    private String line2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 2)
    @Builder.Default
    private String country = "IN";

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}
