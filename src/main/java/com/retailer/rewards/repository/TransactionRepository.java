package com.retailer.rewards.repository;

import com.retailer.rewards.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Fetch all transactions for a given customer within a date window.
     */
    List<Transaction> findByCustomerIdAndTransactionDateBetween(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Fetch all transactions within a date window (all customers).
     */
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Return the distinct customer IDs that have transactions in the period.
     */
    @Query("SELECT DISTINCT t.customerId FROM Transaction t " +
           "WHERE t.transactionDate BETWEEN :start AND :end")
    List<Long> findDistinctCustomerIdsByDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
