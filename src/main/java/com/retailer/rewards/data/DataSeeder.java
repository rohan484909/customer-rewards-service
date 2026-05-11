package com.retailer.rewards.data;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the H2 database with a realistic 3-month dataset
 * covering 5 customers and ~40 transactions.
 *
 * Dates are set relative to today so the /api/rewards endpoint always
 * returns data regardless of when the app is run.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository    customerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            log.info("Database already seeded – skipping.");
            return;
        }

        // ----------------------------------------------------------------
        // Customers
        // ----------------------------------------------------------------
        Customer alice   = customerRepository.save(Customer.builder().name("Alice Johnson").email("alice@example.com").build());
        Customer bob     = customerRepository.save(Customer.builder().name("Bob Martinez").email("bob@example.com").build());
        Customer carol   = customerRepository.save(Customer.builder().name("Carol White").email("carol@example.com").build());
        Customer david   = customerRepository.save(Customer.builder().name("David Kim").email("david@example.com").build());
        Customer emma    = customerRepository.save(Customer.builder().name("Emma Davis").email("emma@example.com").build());

        // ----------------------------------------------------------------
        // Helper: dates relative to today
        // ----------------------------------------------------------------
        LocalDate today     = LocalDate.now();
        LocalDate month1End = today.minusMonths(2).withDayOfMonth(1);   // 3 months ago (start of that month)
        LocalDate month2    = today.minusMonths(1).withDayOfMonth(1);   // 2 months ago
        LocalDate month3    = today.withDayOfMonth(1);                  // current month

        // ----------------------------------------------------------------
        // Transactions  (amount chosen to showcase all point tiers)
        // ----------------------------------------------------------------
        transactionRepository.saveAll(List.of(

            // ── Alice Johnson ──────────────────────────────────────────
            // Month 1: $120 → 90 pts,  $80 → 30 pts,  $45 → 0 pts
            txn(alice, "120.00", month1End.plusDays(2)),
            txn(alice, "80.00",  month1End.plusDays(10)),
            txn(alice, "45.00",  month1End.plusDays(20)),

            // Month 2: $200 → 250 pts, $110 → 70 pts
            txn(alice, "200.00", month2.plusDays(3)),
            txn(alice, "110.00", month2.plusDays(15)),

            // Month 3: $55 → 5 pts, $130 → 110 pts
            txn(alice, "55.00",  month3.plusDays(1)),
            txn(alice, "130.00", month3.plusDays(8)),

            // ── Bob Martinez ───────────────────────────────────────────
            // Month 1: $30 → 0 pts, $75 → 25 pts, $150 → 150 pts
            txn(bob, "30.00",  month1End.plusDays(5)),
            txn(bob, "75.00",  month1End.plusDays(12)),
            txn(bob, "150.00", month1End.plusDays(25)),

            // Month 2: $95 → 45 pts, $180 → 210 pts
            txn(bob, "95.00",  month2.plusDays(7)),
            txn(bob, "180.00", month2.plusDays(22)),

            // Month 3: $220 → 290 pts
            txn(bob, "220.00", month3.plusDays(5)),

            // ── Carol White ────────────────────────────────────────────
            // Month 1: $60 → 10 pts, $100 → 50 pts
            txn(carol, "60.00",  month1End.plusDays(3)),
            txn(carol, "100.00", month1End.plusDays(18)),

            // Month 2: $135 → 120 pts, $48 → 0 pts, $90 → 40 pts
            txn(carol, "135.00", month2.plusDays(4)),
            txn(carol, "48.00",  month2.plusDays(11)),
            txn(carol, "90.00",  month2.plusDays(19)),

            // Month 3: $160 → 170 pts, $72 → 22 pts
            txn(carol, "160.00", month3.plusDays(2)),
            txn(carol, "72.00",  month3.plusDays(9)),

            // ── David Kim ──────────────────────────────────────────────
            // Month 1: $250 → 350 pts, $99 → 49 pts
            txn(david, "250.00", month1End.plusDays(1)),
            txn(david, "99.00",  month1End.plusDays(14)),

            // Month 2: $300 → 450 pts
            txn(david, "300.00", month2.plusDays(6)),

            // Month 3: $175 → 200 pts, $88 → 38 pts, $50 → 0 pts
            txn(david, "175.00", month3.plusDays(3)),
            txn(david, "88.00",  month3.plusDays(10)),
            txn(david, "50.00",  month3.plusDays(15)),

            // ── Emma Davis ─────────────────────────────────────────────
            // Month 1: $110 → 70 pts, $65 → 15 pts
            txn(emma, "110.00", month1End.plusDays(7)),
            txn(emma, "65.00",  month1End.plusDays(21)),

            // Month 2: $40 → 0 pts, $145 → 140 pts, $78 → 28 pts
            txn(emma, "40.00",  month2.plusDays(2)),
            txn(emma, "145.00", month2.plusDays(13)),
            txn(emma, "78.00",  month2.plusDays(25)),

            // Month 3: $195 → 240 pts
            txn(emma, "195.00", month3.plusDays(6))
        ));

        log.info("Database seeded: {} customers, {} transactions",
                customerRepository.count(), transactionRepository.count());
    }

    private Transaction txn(Customer customer, String amount, LocalDate date) {
        return Transaction.builder()
                .customerId(customer.getId())
                .amount(new BigDecimal(amount))
                .transactionDate(date)
                .build();
    }
}
