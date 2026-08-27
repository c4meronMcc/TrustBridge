package com.trustbridge.Features.Dashboard.Freelancer.Service;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.StripeAccount;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.StripeAccountRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.*;
import com.trustbridge.Features.Payments.Provider.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobRepository jobRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;
    private final StripeAccountRepository stripeAccountRepository;
    private final PaymentGateway paymentGateway;

    /**
     * Retrieves and assembles dashboard data for a freelancer based on their email.
     *
     * @param email the email of the freelancer whose dashboard data is to be retrieved
     * @return a {@code DashboardDataDto} containing the freelancer's dashboard information, including:
     *         <li>- User details (first name, last name)</li>
     *         <li>- Trust score</li>
     *         <li>- Payouts status</li>
     *         <li>- Financial metrics (funds in escrow holding, funds pending, funds paid out)</li>
     *         <li>- Earning graph data over the last 12 months</li>
     *         <li>- Top 5 active jobs</li>
     *         <li>- Top 5 jobs awaiting payment</li>
     *         <li>- Top 5 jobs with recent activity</li>
     *         <li>- Recent activity feed</li>
     * @throws RuntimeException if no user is found associated with the provided email
     */
    public DashboardDataDto getDashboardData(String email) {

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // financial metrics
        FinancialMetricsDto metrics = milestoneRepository.getFreelancerFinancialMetrics(user.getId());

        boolean isPayoutsEnabled = paymentGateway.getProviderName().equals("mock") ||
                stripeAccountRepository.findByUserId(user.getId())
                .map(StripeAccount::getPayoutsEnabled)
                .orElse(false);

        // Earning graph data
        List<EarningDataPointDto> earningsData= milestoneRepository.getEarningsLast12Months(user.getId());

        List<Milestones> recentMilestones = milestoneRepository.findTop10ByJobFreelancerIdOrderByUpdatedAtDesc(user.getId());

        // Job and milestone data
        List<JobSummaryDto> top5ActiveJobs = jobRepository.findActiveJobByUserId(user.getId());
        List<JobSummaryDto> top5JobsAwaitingPayment = jobRepository.findAllJobsAwaitingPayment(user.getId());
        List<JobSummaryDto> top5RecentActivityJobs = jobRepository.findRecentActivityByFreelancerId(user.getId());

        return (new DashboardDataDto(
                user.getFirstName(),
                user.getLastName(),
                67,
                isPayoutsEnabled,
                metrics.fundsInEscrowHolding(),
                metrics.fundsPending(),
                metrics.fundsPaidOut(),
                earningsData,
                top5ActiveJobs,
                top5JobsAwaitingPayment,
                top5RecentActivityJobs,
                generateActivityFeed(recentMilestones)
        ));
    }

    /**
     * Generates an activity feed based on a list of recent milestones.
     * The activity feed includes key events and descriptions for display in the UI.
     *
     * @param recentMilestones a list of {@code Milestones} objects representing the most recent milestones
     *                         associated with a freelancer's jobs
     * @return a list of {@code AuditLogEntryDto} objects, where each entry contains the event details
     *         including event type, description, associated milestone, and other related metadata
     */
    private List<AuditLogEntryDto> generateActivityFeed(List<Milestones> recentMilestones) {
        return recentMilestones.stream().map(m -> {

            // Map the strict database enum to the UI event tags
            String eventType = switch (m.getStatus()) {
                case PAID_OUT -> "FUNDS_RELEASED";
                case APPROVED, SUBMITTED -> "MILESTONE_APPROVED";
                case IN_PROGRESS -> "ESCROW_FUNDED";
                case AWAITING_PAYMENT -> "AWAITING_PAYMENT";
                case LOCKED -> "JOB_CREATED";
                // Map all dispute and cancelled states to a generic update to avoid breaking the UI
                case  DISPUTE_NEGOTIATION, DISPUTE_ARBITRATION , DISPUTE_RESOLVED, CANCELLED -> "UPDATE";
                default -> "UPDATE";
            };

            String clientName = m.getJob().getClient().getFirstName() + " " + m.getJob().getClient().getLastName();

            // Build a clean description based on the event
            String description = switch (eventType) {
                case "FUNDS_RELEASED" -> "Funds released — " + m.getJob().getTitle();
                case "MILESTONE_APPROVED" -> "Milestone review — " + m.getTitle();
                case "ESCROW_FUNDED" -> "Escrow funded — " + m.getTitle();
                case "AWAITING_PAYMENT" -> "Deposit required — " + m.getJob().getTitle();
                case "JOB_CREATED" -> "New job created — " + m.getJob().getTitle();
                default -> "Update — " + m.getTitle();
            };

            return new AuditLogEntryDto(
                    m.getId().toString(),
                    eventType,
                    description,
                    m.getAmount(),
                    clientName,
                    m.getUpdatedAt()
            );
        }).toList();
    }
}
