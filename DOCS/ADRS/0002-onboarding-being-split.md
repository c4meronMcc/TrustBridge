# Onboarding being split

* **Status:** Accepted
* **Date:** 16.06.2026

## Context
Stripe and Escrow.com need KYC checks to onboard new users. However, they are not the exact same, and this means that we would have to transfer and hold important information such as the ID card and National Insurance number of our users before handing it to onboarding. This would make us liable if that information was to be leaked.

## Decision
I am going to split the onboarding process into two parts: KYC on account creation for stripe and then, when the user wants to do a payment over £10,000, then they have to do the escrow.com KYC check. This will only be implemented until we reach phase 2 of trustbridge because we will have more time to release a strict and secure implementation to have a dual onboarding making a smooth experience for our users.

## Consequences
This will make the onboarding process safer for both us and our users.