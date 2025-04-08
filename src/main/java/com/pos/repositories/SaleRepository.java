package com.pos.repositories;

import com.pos.models.Sale;
import com.pos.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCashier(User cashier);

    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    BigDecimal getTotalSalesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s WHERE s.status = :status ORDER BY s.saleDate DESC")
    List<Sale> findByStatus(Sale.SaleStatus status);
}