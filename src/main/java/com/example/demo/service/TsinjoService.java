package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TsinjoService {
    private final DonationRepository donationRepository;
    private final HelpRepository helpRepository;
    private final PaymentRepository paymentRepository;
    private final VolaService volaService;

    public List<Object> getAllTransactionsOrderedByDate() {
        log.debug("Récupération de toutes les transactions");

        List<Object> allTransactions = new ArrayList<>();

        try {
            List<Donation> donations = donationRepository.findAllOrderByCreationDatetimeDesc();
            List<Help> helps = helpRepository.findAllOrderByCreationDatetimeDesc();

            allTransactions.addAll(donations);
            allTransactions.addAll(helps);

            allTransactions.sort(Comparator.comparing(
                    transaction -> transaction instanceof Donation
                            ? ((Donation) transaction).getCreationDatetime()
                            : ((Help) transaction).getCreationDatetime(),
                    Comparator.reverseOrder()
            ));

            log.info("Récupération de {} transactions ({} dons, {} aides)",
                    allTransactions.size(), donations.size(), helps.size());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des transactions", e);
        }

        return allTransactions;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitDonation(String donorEmail, String donorName, String paymentId) {
        log.info("Soumission asynchrone d'un don - Donateur: {}, Paiement: {}", donorName, paymentId);

        volaService.verifyPayment(paymentId)
                .subscribe(
                        payment -> {
                            if (payment != null) {
                                Donor donor = new Donor(donorEmail, donorName);
                                Donation donation = new Donation(donor, payment);
                                donationRepository.save(donation);
                                log.info("Don sauvegardé avec succès pour {} - Montant: {}",
                                        donorName, payment.getAmount());
                            } else {
                                log.warn("Paiement non trouvé ou invalide: {}", paymentId);
                            }
                        },
                        error -> log.error("Erreur lors de la vérification du paiement {}", paymentId, error)
                );
    }

    @Scheduled(fixedRate = 30000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePendingPayments() {
        log.debug("Vérification des paiements en attente");

        paymentRepository.findByStatus(Payment.PaymentStatus.VERIFYING)
                .forEach(payment ->
                        volaService.verifyPayment(payment.getId())
                                .subscribe(
                                        updatedPayment -> updatePaymentStatus(payment, updatedPayment),
                                        error -> log.warn("Erreur lors de la mise à jour du paiement {}", payment.getId(), error)
                                )
                );
    }

    private void updatePaymentStatus(Payment payment, Payment updatedPayment) {
        if (updatedPayment != null && updatedPayment.getStatus() != Payment.PaymentStatus.VERIFYING) {
            payment.setStatus(updatedPayment.getStatus());
            paymentRepository.save(payment);
            log.info("Paiement {} mis à jour: {} -> {}",
                    payment.getId(), Payment.PaymentStatus.VERIFYING, updatedPayment.getStatus());
        }
    }
}