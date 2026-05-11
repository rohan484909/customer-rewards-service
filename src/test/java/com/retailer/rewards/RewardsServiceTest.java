package com.retailer.rewards;

import com.retailer.rewards.model.*;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RewardsService rewardsService;

    // -----------------------------------------------------------------------
    // calculatePoints – pure unit tests (no mocks needed)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("$120 purchase should earn 90 points (2×$20 + 1×$50)")
    void points_120dollars() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("120"))).isEqualTo(90L);
    }

    @Test
    @DisplayName("$100 purchase should earn 50 points (only $50–$100 band)")
    void points_100dollars() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("100"))).isEqualTo(50L);
    }

    @Test
    @DisplayName("$75 purchase should earn 25 points (only $50–$75 band)")
    void points_75dollars() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("75"))).isEqualTo(25L);
    }

    @Test
    @DisplayName("$50 purchase should earn 0 points (exactly at threshold)")
    void points_50dollars() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("50"))).isEqualTo(0L);
    }

    @Test
    @DisplayName("$30 purchase should earn 0 points (below $50 threshold)")
    void points_30dollars() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("30"))).isEqualTo(0L);
    }

    @Test
    @DisplayName("$200 purchase should earn 250 points (2×$100 + 1×$50)")
    void points_200dollars() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("200"))).isEqualTo(250L);
    }

    @Test
    @DisplayName("Null amount should earn 0 points")
    void points_null() {
        assertThat(rewardsService.calculatePoints(null)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Zero amount should earn 0 points")
    void points_zero() {
        assertThat(rewardsService.calculatePoints(BigDecimal.ZERO)).isEqualTo(0L);
    }

    // -----------------------------------------------------------------------
    // getCustomerRewards – integration-style tests with mocks
    // -----------------------------------------------------------------------

    // Sets up the customerRepository mock only in tests that actually call
    // getCustomerRewards(), so Mockito's strict mode doesn't flag it as unused.
    private void aliceStub() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(
                Customer.builder()
                        .id(1L)
                        .name("Alice Johnson")
                        .email("alice@example.com")
                        .build()
        ));
    }

    @Test
    @DisplayName("Monthly totals and grand total are computed correctly")
    void getCustomerRewards_correctTotals() {
        aliceStub();

        LocalDate march10 = LocalDate.of(LocalDate.now().getYear(), 3, 10);
        LocalDate april5  = LocalDate.of(LocalDate.now().getYear(), 4, 5);
        LocalDate may20   = LocalDate.of(LocalDate.now().getYear(), 5, 20);

        Transaction t1 = Transaction.builder().customerId(1L)
                .amount(new BigDecimal("120")).transactionDate(march10).build();  // 90 pts
        Transaction t2 = Transaction.builder().customerId(1L)
                .amount(new BigDecimal("80")).transactionDate(april5).build();    // 30 pts
        Transaction t3 = Transaction.builder().customerId(1L)
                .amount(new BigDecimal("200")).transactionDate(may20).build();    // 250 pts

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2, t3));

        CustomerRewardSummary summary = rewardsService.getCustomerRewards(1L);

        assertThat(summary.getTotalPoints()).isEqualTo(370L);  // 90 + 30 + 250
        assertThat(summary.getMonthlyRewards()).hasSize(3);
    }

    @Test
    @DisplayName("Customer with no transactions has 0 total points")
    void getCustomerRewards_noTransactions() {
        aliceStub();

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        CustomerRewardSummary summary = rewardsService.getCustomerRewards(1L);

        assertThat(summary.getTotalPoints()).isZero();
        assertThat(summary.getMonthlyRewards()).isEmpty();
    }
}