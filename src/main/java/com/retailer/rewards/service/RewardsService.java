package com.retailer.rewards.service;

import com.retailer.rewards.model.*;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for all reward-point calculations.
 *
 * <p>Points rule:
 * <ul>
 *   <li>2 points per dollar spent <strong>over $100</strong> in a transaction</li>
 *   <li>1 point per dollar spent <strong>between $50 and $100</strong> (inclusive of $50)</li>
 *   <li>No points for the first $50 or below</li>
 * </ul>
 *
 * <p>Example: $120 purchase → 2×$20 + 1×$50 = 90 points
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsService {

    private static final BigDecimal TIER_ONE_THRESHOLD  = new BigDecimal("50");
    private static final BigDecimal TIER_TWO_THRESHOLD  = new BigDecimal("100");

    private final TransactionRepository transactionRepository;
    private final CustomerRepository    customerRepository;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Calculate rewards for ALL customers over the last 3 months.
     */
    public List<CustomerRewardSummary> getAllCustomerRewards() {
        LocalDate end   = LocalDate.now();
        LocalDate start = end.minusMonths(3).withDayOfMonth(1);

        List<Long> customerIds =
                transactionRepository.findDistinctCustomerIdsByDateBetween(start, end);

        return customerIds.stream()
                .map(id -> buildSummary(id, start, end))
                .sorted(Comparator.comparing(CustomerRewardSummary::getCustomerName))
                .collect(Collectors.toList());
    }

    /**
     * Calculate rewards for a single customer over the last 3 months.
     */
    public CustomerRewardSummary getCustomerRewards(Long customerId) {
        LocalDate end   = LocalDate.now();
        LocalDate start = end.minusMonths(3).withDayOfMonth(1);
        return buildSummary(customerId, start, end);
    }

    /**
     * Calculate rewards for a single customer within an explicit date range.
     */
    public CustomerRewardSummary getCustomerRewardsForPeriod(
            Long customerId, LocalDate start, LocalDate end) {
        return buildSummary(customerId, start, end);
    }

    /**
     * Core point-calculation for a single dollar amount.
     *
     * @param amount purchase amount (must be positive)
     * @return reward points earned
     */
    public long calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        long points = 0;

        if (amount.compareTo(TIER_TWO_THRESHOLD) > 0) {
            // Dollars above $100 → 2 pts each
            BigDecimal above100 = amount.subtract(TIER_TWO_THRESHOLD);
            points += above100.longValue() * 2;

            // The $50–$100 band is fully used → 1 pt × 50
            points += TIER_TWO_THRESHOLD.subtract(TIER_ONE_THRESHOLD).longValue();

        } else if (amount.compareTo(TIER_ONE_THRESHOLD) > 0) {
            // Only the $50–$100 band applies → 1 pt each
            BigDecimal between50and100 = amount.subtract(TIER_ONE_THRESHOLD);
            points += between50and100.longValue();
        }
        // Amounts ≤ $50 → 0 points

        log.debug("Amount ${} → {} points", amount, points);
        return points;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private CustomerRewardSummary buildSummary(
            Long customerId, LocalDate start, LocalDate end) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new NoSuchElementException("Customer not found: " + customerId));

        List<Transaction> transactions =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        customerId, start, end);

        // Group transactions by Year+Month
        Map<String, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(t -> {
                    LocalDate d = t.getTransactionDate();
                    return d.getYear() + "-" + d.getMonthValue();
                }));

        List<MonthlyReward> monthlyRewards = byMonth.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int year  = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);

                    List<Transaction> monthTxns = entry.getValue();
                    long monthPoints = monthTxns.stream()
                            .mapToLong(t -> calculatePoints(t.getAmount()))
                            .sum();

                    return MonthlyReward.builder()
                            .year(year)
                            .month(month)
                            .monthName(Month.of(month)
                                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                            .points(monthPoints)
                            .transactionCount(monthTxns.size())
                            .build();
                })
                .sorted(Comparator.comparingInt(MonthlyReward::getYear)
                        .thenComparingInt(MonthlyReward::getMonth))
                .collect(Collectors.toList());

        long totalPoints = monthlyRewards.stream()
                .mapToLong(MonthlyReward::getPoints)
                .sum();

        return CustomerRewardSummary.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .monthlyRewards(monthlyRewards)
                .totalPoints(totalPoints)
                .build();
    }
}
