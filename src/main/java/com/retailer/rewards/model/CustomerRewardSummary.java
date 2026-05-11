package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a full reward summary for one customer:
 * monthly breakdown + grand total.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewardSummary {

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private List<MonthlyReward> monthlyRewards;
    private long totalPoints;
}
