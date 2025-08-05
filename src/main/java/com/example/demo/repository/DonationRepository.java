package com.example.demo.repository;

import com.example.demo.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    @Query("SELECT d FROM Donation d ORDER BY d.creationDatetime DESC")
    List<Donation> findAllOrderByCreationDatetimeDesc();
}