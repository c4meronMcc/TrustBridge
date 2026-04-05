package com.trustbridge;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.JobStatus;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.UserRole;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestRepository paymentRequestRepository;

    @Autowired
    public DataSeeder(UserRepository userRepository,
                      JobRepository jobRepository,
                      MilestoneRepository milestoneRepository,
                      PaymentRequestRepository paymentRequestRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.milestoneRepository = milestoneRepository;
        this.paymentRequestRepository = paymentRequestRepository;
    }

    @Override
    public void run(String @NonNull ... args) {
        // Only run this if the database is completely empty
        if (paymentRequestRepository.count() == 0) {
            System.out.println("🌱 Database is empty! Seeding test data...");

            // 1. Create a Freelancer
            Users freelancer = Users.builder()
                    .firstName("Alice")
                    .lastName("Developer")
                    .email("alice@example.com")
                    .password("password")
                    .userRole(UserRole.role.FREELANCER)
                    .build();
            freelancer = userRepository.save(freelancer);

            // 2. Create a Client
            Users client = Users.builder()
                    .firstName("Bob")
                    .lastName("Client")
                    .email("bob@example.com")
                    .password("password")
                    .userRole(UserRole.role.CLIENT)
                    .build();
            client = userRepository.save(client);

            // 3. Create a Job
            Jobs job = Jobs.builder()
                    .title("E-commerce Website Build")
                    .description("Full stack e-commerce site using React and Spring Boot.")
                    .totalAmount(new BigDecimal("5000.00"))
                    .currency("GBP")
                    .status(JobStatus.jobStatus.AWAITING_PAYMENT)
                    .freelancer(freelancer)
                    .client(client)
                    .build();
            job = jobRepository.save(job);

            // 4. Create a Milestone
            Milestones milestone = new Milestones();
            milestone.setTitle("Phase 1: Initial Design and Setup");
            milestone.setAmount(new BigDecimal("1000.00"));
            milestone.setSequenceOrder(1);
            milestone.setStatus(MilestoneStatus.milestoneStatus.AWAITING_PAYMENT);
            milestone.setJob(job);
            milestone = milestoneRepository.save(milestone);

            // 5. Create a Payment Request for that Milestone
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .amount(new BigDecimal("1000.00"))
                    .status(PaymentRequestStatus.PENDING)
                    .expiresAt(OffsetDateTime.now().plusDays(7))
                    .milestone(milestone)
                    .build();
            paymentRequest = paymentRequestRepository.save(paymentRequest);

            System.out.println("✅ Seeding Complete!");
            System.out.println("🚀 ========================================================= 🚀");
            System.out.println("COPY THIS PAYMENT REQUEST ID TO TEST STRIPE: " + paymentRequest.getId());
            System.out.println("🚀 ========================================================= 🚀");
        } else {
            System.out.println("👍 Database already contains data. Skipping seeder.");
        }
    }
}

