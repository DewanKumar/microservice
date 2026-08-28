package com.microservices.payment_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.internal.build.AllowNonPortable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Column(nullable = false)
    private Long orderId;

//    @Column(nullable = false)
    private Long userId;

//    @Column(nullable = false)
    private Double amount;

//    @Column(nullable = false)
    private String paymentMethod;

//    @Column(nullable = false)
    private String status;

}