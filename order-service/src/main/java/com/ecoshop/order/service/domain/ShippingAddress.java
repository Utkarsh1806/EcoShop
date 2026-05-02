package com.ecoshop.order.service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingAddress implements Serializable {

    @Column(name = "ship_recipient_name", length = 200)
    private String recipientName;

    @Column(name = "ship_phone", length = 20)
    private String phone;

    @Column(name = "ship_line1", length = 255)
    private String line1;

    @Column(name = "ship_line2", length = 255)
    private String line2;

    @Column(name = "ship_city", length = 100)
    private String city;

    @Column(name = "ship_state", length = 100)
    private String state;

    @Column(name = "ship_postal_code", length = 20)
    private String postalCode;

    @Column(name = "ship_country", length = 2)
    private String country;
}
