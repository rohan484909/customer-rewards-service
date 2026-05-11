package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing reward points earned by a customer in a single month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReward {

    private int year;
    private int month;
    private String monthName;
    private long points;
    private int transactionCount;
}
