package com.example.demo.endpoint.rest.controller;

import com.example.demo.service.TsinjoService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class TsinjoController {
    private static final Logger log = LoggerFactory.getLogger(TsinjoController.class);

    private final TsinjoService tsinjoService;

    @GetMapping("/")
    public String index(Model model) {
        log.info("Affichage de la page principale Tsinjo");
        model.addAttribute("transactions", tsinjoService.getAllTransactionsOrderedByDate());
        return "index";
    }

    @PostMapping("/donations")
    public String submitDonation(@RequestParam String donorEmail,
                                 @RequestParam String donorName,
                                 @RequestParam String paymentId) {
        log.info("Réception d'un nouveau don de {} ({})", donorName, donorEmail);
        try {
            tsinjoService.submitDonation(donorEmail, donorName, paymentId);
            log.info("Don soumis avec succès pour {}", donorName);
        } catch (Exception e) {
            log.error("Erreur lors de la soumission du don: {}", e.getMessage());
        }
        return "redirect:/";
    }
}
