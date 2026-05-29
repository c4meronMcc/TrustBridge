package com.trustbridge;

import com.trustbridge.Domain.Entities.*;
import com.trustbridge.Domain.Enums.JobStatus;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.UserRole;
import com.trustbridge.Domain.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Explicitly check if our dummy user exists to avoid duplicate key crashes
        if (userRepository.findByEmail("jamie@example.com").isEmpty()) {
            seedDatabase();
        } else {
            log.info("👍 Database already contains Jamie. Skipping seeder.");
        }
    }

    private void seedDatabase() {
        log.info("🌱 SEEDING TRUSTBRIDGE DATABASE...");

        // ─── 1. CREATE USERS ──────────────────────────────────────────────────

        Users freelancer = Users.builder()
                .email("jamie@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jamie")
                .lastName("Sullivan")
                .userRole(UserRole.role.FREELANCER) // Adjust to your specific enum structure
                .isVerified(true)
                .build();
        userRepository.save(freelancer);

        Users client = Users.builder()
                .email("client@novalabs.com")
                .password(passwordEncoder.encode("securepass"))
                .firstName("Nova")
                .lastName("Labs")
                .userRole(UserRole.role.CLIENT)
                .isVerified(true)
                .build();
        userRepository.save(client);


        // ─── 2. CREATE AWAITING_PAYMENT JOB ───────────────────────────────────

        Jobs job1 = Jobs.builder()
                .freelancer(freelancer)
                .client(client)
                .title("UX Redesign Phase 2")
                .totalAmount(new BigDecimal("3250.00"))
                .currency("GBP")
                .status(JobStatus.jobStatus.PAID_OUT) // Matches your SQL CHECK constraint
                .build();
        jobRepository.save(job1);

        Milestones milestone1 = Milestones.builder()
                .job(job1)
                .title("Initial Wireframes")
                .amount(new BigDecimal("3250.00"))
                .sequenceOrder(1) // NOT NULL constraint
                .status(MilestoneStatus.milestoneStatus.PAID_OUT) // Matches your SQL CHECK constraint
                .build();
        milestoneRepository.save(milestone1);

        PaymentRequest pr1 = PaymentRequest.builder()
                .milestone(milestone1)
                .amount(new BigDecimal("3250.00")) // NOT NULL constraint
                .expiresAt(OffsetDateTime.now().plusDays(7)) // NOT NULL constraint
                .status(PaymentRequestStatus.PAID) // Matches your SQL CHECK constraint
                .build();
        paymentRequestRepository.save(pr1);


        // ─── 3. CREATE IN_PROGRESS JOB (Active Escrow) ────────────────────────

        Jobs job2 = Jobs.builder()
                .freelancer(freelancer)
                .client(client)
                .title("Backend API Build")
                .totalAmount(new BigDecimal("6150.00"))
                .currency("GBP")
                .status(JobStatus.jobStatus.PAID_OUT) // Matches your SQL CHECK constraint
                .build();
        jobRepository.save(job2);

        Milestones milestone2 = Milestones.builder()
                .job(job2)
                .title("Database Schema & Auth")
                .amount(new BigDecimal("6150.00"))
                .sequenceOrder(1)
                .status(MilestoneStatus.milestoneStatus.PAID_OUT) // Matches your SQL CHECK constraint
                .build();
        milestoneRepository.save(milestone2);

        PaymentRequest pr2 = PaymentRequest.builder()
                .milestone(milestone2)
                .amount(new BigDecimal("6150.00"))
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .status(PaymentRequestStatus.PAID) // Matches your SQL CHECK constraint
                .build();
        paymentRequestRepository.save(pr2);


        // ─── 4. CREATE PAID_OUT JOB (Completed historical data) ───────────────

        Jobs job3 = Jobs.builder()
                .freelancer(freelancer)
                .client(client)
                .title("Branding & Identity")
                .totalAmount(new BigDecimal("2400.00"))
                .currency("GBP")
                .status(JobStatus.jobStatus.IN_PROGRESS) // Your SQL uses PAID_OUT, not COMPLETED
                .build();
        jobRepository.save(job3);

        Milestones milestone3 = Milestones.builder()
                .job(job3)
                .title("Final Logo & Brand Guidelines")
                .amount(new BigDecimal("2400.00"))
                .sequenceOrder(1)
                .status(MilestoneStatus.milestoneStatus.IN_PROGRESS) // Your SQL uses PAID_OUT, not COMPLETED
                .build();
        milestoneRepository.save(milestone3);

        PaymentRequest pr3 = PaymentRequest.builder()
                .milestone(milestone3)
                .amount(new BigDecimal("2400.00"))
                .expiresAt(OffsetDateTime.now().minusDays(2)) // Already expired/paid in the past
                .status(PaymentRequestStatus.PAID)
                .build();
        paymentRequestRepository.save(pr3);

        log.info("✅ DATABASE SEEDED SUCCESSFULLY!");
    }
}