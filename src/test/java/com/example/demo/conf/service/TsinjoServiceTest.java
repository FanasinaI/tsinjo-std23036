package com.example.demo.conf.service;

import com.example.demo.model.*;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.HelpRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.TsinjoService;
import com.example.demo.service.VolaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TsinjoServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private HelpRepository helpRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VolaService volaService;

    @InjectMocks
    private TsinjoService tsinjoService;

    private Donation testDonation;
    private Help testHelp;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        Donor donor = Donor.builder()
                .email("donor@hei.school")
                .fullName("John Donor")
                .build();

        Beneficiary beneficiary = Beneficiary.builder()
                .email("beneficiary@hei.school")
                .fullName("Jane Beneficiary")
                .build();

        testPayment = Payment.builder()
                .id("payment-123")
                .amount(new BigDecimal("100.00"))
                .paymentMethod("Card")
                .build();

        testDonation = Donation.builder()
                .donor(donor)
                .payment(testPayment)
                .build();

        testHelp = Help.builder()
                .beneficiary(beneficiary)
                .payment(testPayment)
                .accidentDescription("Accident description")
                .build();
    }

    @Test
    void getAllTransactionsOrderedByDate_shouldReturnSortedTransactions() {
        when(donationRepository.findAllOrderByCreationDatetimeDesc())
                .thenReturn(List.of(testDonation));
        when(helpRepository.findAllOrderByCreationDatetimeDesc())
                .thenReturn(List.of(testHelp));

        List<Object> result = tsinjoService.getAllTransactionsOrderedByDate();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(donationRepository).findAllOrderByCreationDatetimeDesc();
        verify(helpRepository).findAllOrderByCreationDatetimeDesc();
    }

    @Test
    void submitDonation_shouldSaveWhenPaymentVerified() {
        Payment verifiedPayment = Payment.builder()
                .id("payment-123")
                .amount(new BigDecimal("50.00"))
                .paymentMethod("Card")
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        when(volaService.verifyPayment(anyString()))
                .thenReturn(Mono.just(verifiedPayment));
        when(donationRepository.save(any(Donation.class)))
                .thenReturn(testDonation);

        tsinjoService.submitDonation("test@hei.school", "Test User", "payment-123");

        verify(volaService).verifyPayment("payment-123");
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void updatePendingPayments_shouldUpdateStatusWhenChanged() {
        Payment verifyingPayment = Payment.builder()
                .id("payment-123")
                .amount(new BigDecimal("50.00"))
                .paymentMethod("Card")
                .status(Payment.PaymentStatus.VERIFYING)
                .build();

        Payment updatedPayment = Payment.builder()
                .id("payment-123")
                .amount(new BigDecimal("50.00"))
                .paymentMethod("Card")
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        when(paymentRepository.findByStatus(Payment.PaymentStatus.VERIFYING))
                .thenReturn(List.of(verifyingPayment));
        when(volaService.verifyPayment("payment-123"))
                .thenReturn(Mono.just(updatedPayment));

        tsinjoService.updatePendingPayments();

        verify(paymentRepository).findByStatus(Payment.PaymentStatus.VERIFYING);
        verify(volaService).verifyPayment("payment-123");
        verify(paymentRepository).save(verifyingPayment);
        assertEquals(Payment.PaymentStatus.SUCCEEDED, verifyingPayment.getStatus());
    }
}