package com.example.demo.conf.controller;

import com.example.demo.endpoint.rest.controller.TsinjoController;
import com.example.demo.model.*;
import com.example.demo.service.TsinjoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TsinjoController.class)
@Import(ThymeleafTestConfig.class)
class TsinjoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TsinjoService tsinjoService;

    @Test
    void index_shouldReturnViewWithTransactions() throws Exception {
        Payment payment = Payment.builder()
                .id("payment-123")
                .amount(new BigDecimal("50.00"))
                .paymentMethod("Carte bancaire")
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        Donor donor = Donor.builder()
                .email("john.doe@hei.school")
                .fullName("John Doe")
                .build();

        Beneficiary beneficiary = Beneficiary.builder()
                .email("marie.martin@hei.school")
                .fullName("Marie Martin")
                .build();

        Donation donation = Donation.builder()
                .donor(donor)
                .payment(payment)
                .build();

        Help help = Help.builder()
                .beneficiary(beneficiary)
                .payment(payment)
                .accidentDescription("Accident test")
                .build();

        when(tsinjoService.getAllTransactionsOrderedByDate())
                .thenReturn(Arrays.asList(donation, help));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("transactions"));
    }

    @Test
    void submitDonation_shouldCallServiceAndRedirect() throws Exception {
        String email = "test@hei.school";
        String name = "Test User";
        String paymentId = "payment-123";

        mockMvc.perform(post("/donations")
                        .param("donorEmail", email)
                        .param("donorName", name)
                        .param("paymentId", paymentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(tsinjoService).submitDonation(email, name, paymentId);
    }

    @Test
    void submitDonation_shouldReturnBadRequestWhenMissingParameters() throws Exception {
        mockMvc.perform(post("/donations"))
                .andExpect(status().isBadRequest());
    }
}
class ThymeleafTestConfig {

    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }
}
