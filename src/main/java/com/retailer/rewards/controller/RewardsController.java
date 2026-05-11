package com.retailer.rewards.controller;

import com.retailer.rewards.model.CustomerRewardSummary;
import com.retailer.rewards.service.RewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST endpoints for the customer rewards program.
 *
 * <pre>
 * GET /api/rewards                      → all customers, last 3 months
 * GET /api/rewards/{customerId}         → single customer, last 3 months
 * GET /api/rewards/{customerId}/period  → single customer, custom date range
 * </pre>
 */
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // allow the demo frontend to call the API
public class RewardsController {

    private final RewardsService rewardsService;

    /**
     * Returns a reward summary for every customer that had at least one
     * transaction in the last three calendar months.
     */
    @GetMapping
    public ResponseEntity<List<CustomerRewardSummary>> getAllRewards() {
        List<CustomerRewardSummary> summaries = rewardsService.getAllCustomerRewards();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Returns the reward summary for a single customer over the last three months.
     *
     * @param customerId the customer's database ID
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerRewardSummary> getCustomerRewards(
            @PathVariable Long customerId) {
        try {
            CustomerRewardSummary summary = rewardsService.getCustomerRewards(customerId);
            return ResponseEntity.ok(summary);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Returns the reward summary for a single customer within a custom date range.
     *
     * @param customerId the customer's database ID
     * @param start      start date inclusive (ISO format: yyyy-MM-dd)
     * @param end        end date inclusive (ISO format: yyyy-MM-dd)
     */
    @GetMapping("/{customerId}/period")
    public ResponseEntity<CustomerRewardSummary> getCustomerRewardsForPeriod(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            CustomerRewardSummary summary =
                    rewardsService.getCustomerRewardsForPeriod(customerId, start, end);
            return ResponseEntity.ok(summary);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
