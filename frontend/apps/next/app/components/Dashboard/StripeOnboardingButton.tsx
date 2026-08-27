"use client";

import { useState } from "react";

export default function StripeOnboardingButton() {
    const [loading, setLoading] = useState(false);
    const [verified, setVerified] = useState(false);

    const handleOnboarding = async () => {
        setLoading(true);
        try {
            const response = await fetch("http://localhost:8080/api/v1/freelancer/stripe/onboarding", {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            // 💥 NEW ERROR LOGGING
            if (!response.ok) {
                // Grab the exact error message your Spring Boot server sent back
                const errorData = await response.text();
                console.error(`Backend returned Status ${response.status}:`, errorData);
                throw new Error(`Failed to generate link. Status: ${response.status}`);
            }

            const data = await response.json();

            if (data.verified) {
                // mock mode — no redirect, just flip local UI state
                setVerified(true);
            } else if (data.stripeUrl) {
                window.location.href = data.stripeUrl;
            }

        } catch (error) {
            console.error("Onboarding error:", error);
            alert("Something went wrong connecting to TrustBridge payments.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6 bg-white rounded-lg border border-gray-200 shadow-sm max-w-md">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Identity Verification
            </h3>
            <p className="text-sm text-gray-500 mb-4">
                To accept milestone payouts, you need to verify your identity and link your bank account via our secure payment partner, Stripe.
            </p>
            {verified ? (
                <div className="w-full px-4 py-2 bg-green-50 text-green-700 font-medium rounded border border-green-200 flex items-center justify-center gap-2">
                    <i className="ti ti-circle-check text-[18px]"></i>
                    Identity verified (mock)
                </div>
            ) : (
                <button
                    onClick={handleOnboarding}
                    disabled={loading}
                    className="w-full px-4 py-2 bg-[#10a37f] text-white font-medium rounded hover:bg-[#0e906f] transition-colors disabled:opacity-50"
                >
                    {loading ? "Connecting..." : "Verify Identity to Get Paid"}
                </button>
            )}
        </div>
    );
}