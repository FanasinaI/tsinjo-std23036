package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "helps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Help {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, length = 1000)
    private String accidentDescription;

    @Builder.Default
    @Column(nullable = false)
    private Instant creationDatetime = Instant.now();

    public Help(Beneficiary beneficiary, Payment payment, String accidentDescription) {
        this.beneficiary = beneficiary;
        this.payment = payment;
        this.accidentDescription = accidentDescription;
    }
}
