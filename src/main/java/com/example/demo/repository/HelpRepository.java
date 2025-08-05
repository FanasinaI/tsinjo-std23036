package com.example.demo.repository;

import com.example.demo.model.Help;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface HelpRepository extends JpaRepository<Help, Long> {
    @Query("SELECT h FROM Help h ORDER BY h.creationDatetime DESC")
    List<Help> findAllOrderByCreationDatetimeDesc();
}