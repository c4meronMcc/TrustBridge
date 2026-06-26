'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';

// ─── TYPES ─────────────────────────────────────────────────────────────────
interface Milestone {
    id: string;
    title: string;
    amount: number;
    sequence_amount?: number;
}

interface ProposalData {
    title: string;
    description: string;
    clientName: string;
    freelancerName: string;
    clientEmail: string;
    value?: number;
    amount?: number;
    currency: string;
    status: string;
    milestones: Milestone[];
}

const getCurrencySymbol = (currencyCode: string = 'GBP') => {
    const symbols: Record<string, string> = { GBP: '£', USD: '$', EUR: '€' };
    return symbols[currencyCode.toUpperCase()] || currencyCode + ' ';
};

export default function ProposalReview() {
    const router = useRouter();
    const params = useParams<{ token: string }>();
    const token = params.token;

    // ─── STATE ─────────────────────────────────────────────────────────────────
    const [proposal, setProposal] = useState<ProposalData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isAccepting, setIsAccepting] = useState(false);
    const [isDeclined, setIsDeclined] = useState(false);

    // ─── FETCH DATA ────────────────────────────────────────────────────────────
    useEffect(() => {
        const fetchProposalData = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/invite/${token}`, {
                    method: 'GET',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                });

                if (!response.ok) throw new Error('Failed to fetch proposal data');

                const data = await response.json();
                setProposal(data);
            } catch (error) {
                console.error('Error fetching proposal data:', error);
                setError("This invitation link is invalid or has expired.");
            } finally {
                setIsLoading(false);
            }
        }

        if (token) fetchProposalData();
    }, [token]);

    // ─── ACTIONS (THE FIX IS HERE) ─────────────────────────────────────────────
    const handleAccept = async () => {
        setIsAccepting(true);

        try {
            // 1. Tip the backend Dominoes!
            // This activates the job, unlocks M1, and generates the Stripe token.
            const response = await fetch(`http://localhost:8080/api/invite/accepted/${token}`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
            });

            if (!response.ok) throw new Error('Failed to accept the proposal.');

            // 2. Extract the Stripe clientSecret from the backend response
            const data = await response.json();
            const clientSecret = data.clientSecret;

            if (!clientSecret) {
                throw new Error("Payment token generation failed.");
            }

            // 3. Prepare the vault for the checkout page
            if (proposal && proposal.milestones) {
                const firstMilestone = proposal.milestones.find((ms, index) =>
                    (ms.sequence_amount || index + 1) === 1
                );

                if (firstMilestone) {
                    const checkoutPayload = {
                        jobTitle: proposal.title,
                        freelancerName: proposal.freelancerName,
                        clientName: proposal.clientName || proposal.clientEmail?.split('@')[0] || 'Client',
                        currency: proposal.currency,
                        currentMilestone: firstMilestone,
                        clientSecret: clientSecret
                    };

                    sessionStorage.setItem(`trustbridge_checkout_${token}`, JSON.stringify(checkoutPayload));
                }
            }

            // 4. Safely navigate away ONLY after everything succeeds
            router.push(`/invite/checkout/${token}`);

        } catch (err: any) {
            console.error('Error during acceptance:', err);
            // Optional: you could set an error state here to show an alert on the UI
            alert("Something went wrong activating the contract. Please try again.");
            setIsAccepting(false); // Reset button if it fails
        }
    };

    // ─── LOADING SCREEN ────────────────────────────────────────────────────────
    if (isLoading) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-[#0e1410] flex flex-col items-center justify-center font-sans">
                <i className="ti ti-loader-2 text-[30px] text-[#3FCD6B] animate-spin mb-3"></i>
                <p className="text-[13px] font-medium text-[#0F5525] dark:text-[#3FCD6B]">Loading secure proposal...</p>
            </div>
        );
    }

    // ─── ERROR SCREEN ──────────────────────────────────────────────────────────
    if (error || !proposal) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-[#0e1410] flex items-center justify-center p-4 font-sans">
                <div className="bg-white dark:bg-[#161d18] p-8 rounded-[24px] border-[0.5px] border-red-200 dark:border-red-900/50 max-w-[640px] w-full text-center">
                    <i className="ti ti-alert-triangle text-[40px] text-red-500 mb-4 block"></i>
                    <h2 className="text-[20px] font-medium text-gray-900 dark:text-gray-100 mb-2">Invitation Unavailable</h2>
                    <p className="text-[14px] text-gray-500 dark:text-gray-400">{error}</p>
                </div>
            </div>
        );
    }

    // ─── HELPER VARIABLES ──────────────────────────────────────────────────────
    const clientName = proposal.clientName || 'Client';
    const capitalizedClientName = clientName.charAt(0).toUpperCase() + clientName.slice(1);
    const totalAmount = proposal.value || proposal.amount || 0;
    const currencySymbol = getCurrencySymbol(proposal.currency);

    const sortedMilestones = [...(proposal.milestones || [])].sort(
        (a, b) => (a.sequence_amount || 0) - (b.sequence_amount || 0)
    );

    // ─── MAIN UI ───────────────────────────────────────────────────────────────
    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-[#0e1410] pt-10 px-4 pb-12 font-sans transition-colors duration-200">

            {/* ── Brand ── */}
            <div className="flex items-center justify-center gap-2.5 mb-8">
                <div className="w-[34px] h-[34px] bg-[#3FCD6B] rounded-[9px] flex items-center justify-center">
                    <i className="ti ti-shield-check text-[18px] text-[#0F5525]"></i>
                </div>
                <span className="text-[20px] font-medium text-[#0F5525] dark:text-[#3FCD6B] tracking-tight">TrustBridge</span>
            </div>

            {/* ── Card ── */}
            <div className="bg-white dark:bg-[#161d18] rounded-3xl border-[0.5px] border-[#dde4d9] dark:border-[#2a3a2d] max-w-[640px] mx-auto overflow-hidden shadow-sm dark:shadow-none">

                {/* ── Hero ── */}
                <div className="bg-[#0F5525] pt-10 px-8 pb-8 text-center relative overflow-hidden">
                    <div className="absolute w-[220px] h-[220px] bg-[#0d4820] dark:bg-[#0b3d1b] rounded-full -top-20 -right-14 pointer-events-none"></div>
                    <div className="absolute w-[140px] h-[140px] bg-[#0d4820] dark:bg-[#0b3d1b] rounded-full -bottom-12 -left-8 pointer-events-none"></div>

                    <div className="relative z-10 w-[52px] h-[52px] rounded-full bg-[#3FCD6B] flex items-center justify-center mx-auto mb-4">
                        <i className="ti ti-file-invoice text-[26px] text-[#0F5525]"></i>
                    </div>

                    <h1 className="relative z-10 text-[22px] font-medium text-white mb-1.5">
                        Hi {capitalizedClientName}, here&#39;s your proposal
                    </h1>
                    <p className="relative z-10 text-[14px] text-white/65 leading-relaxed">
                        {proposal.freelancerName} has invited you to review and fund this project via secure escrow.
                    </p>

                    <div className="relative z-10 inline-flex items-center gap-1.5 bg-white/[0.12] border-[0.5px] border-white/20 rounded-full py-1.5 px-3.5 mt-3.5 text-[12px] text-white/80">
                        <i className="ti ti-lock-check text-[13px] text-[#3FCD6B]"></i>
                        FCA-regulated escrow · funds protected
                    </div>
                </div>

                {/* ── Body ── */}
                <div className="p-8">

                    {/* Project Details */}
                    <p className="text-[11px] font-medium text-[#8fa38b] dark:text-[#5a7a5f] uppercase tracking-[0.08em] mb-3">
                        Project details
                    </p>
                    <div className="bg-[#f8faf6] dark:bg-[#1c2620] border-[0.5px] border-[#dde4d9] dark:border-[#2a3a2d] rounded-2xl py-5 px-6 mb-8">
                        <h2 className="text-[17px] font-medium text-gray-900 dark:text-gray-100 mb-1.5">{proposal.title}</h2>
                        <p className="text-[14px] text-gray-500 dark:text-gray-400 leading-[1.65] mb-5 whitespace-pre-wrap">
                            {proposal.description}
                        </p>
                        <div className="flex items-baseline justify-between pt-4 border-t-[0.5px] border-[#dde4d9] dark:border-[#2a3a2d]">
                            <span className="text-[13px] text-gray-500 dark:text-gray-400">Total contract value</span>
                            <span className="text-[26px] font-medium text-[#0F5525] dark:text-[#3FCD6B] tracking-tight">
                                {currencySymbol}{totalAmount.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
                            </span>
                        </div>
                    </div>

                    {/* Payment Schedule */}
                    {sortedMilestones.length > 0 && (
                        <div className="mb-8">
                            <p className="text-[11px] font-medium text-[#8fa38b] dark:text-[#5a7a5f] uppercase tracking-[0.08em] mb-3">
                                Payment schedule
                            </p>

                            <div className="relative pl-9">
                                {/* Track line */}
                                <div className="absolute left-[15px] top-[22px] bottom-[22px] w-px bg-[#d5e8cb] dark:bg-[#2a3a2d]"></div>

                                {sortedMilestones.map((ms, index) => (
                                    <div
                                        key={index}
                                        className={`relative flex items-start justify-between ${
                                            index < sortedMilestones.length - 1 ? 'pb-6' : 'pb-0'
                                        }`}
                                    >
                                        {/* Dot */}
                                        <div className="absolute -left-[29px] top-0.5 w-7 h-7 rounded-full bg-[#E1F5EE] dark:bg-[#1c2f22] border-2 border-[#3FCD6B] dark:border-[#2e7d4f] flex items-center justify-center text-[11px] font-medium text-[#0F5525] dark:text-[#3FCD6B] shrink-0">
                                            {ms.sequence_amount ?? index + 1}
                                        </div>

                                        {/* Label */}
                                        <div className="flex-1 min-w-0">
                                            <p className="text-[14px] font-medium text-gray-900 dark:text-gray-100 mb-0.5 truncate">
                                                {ms.title}
                                            </p>
                                            <p className="text-[12px] text-gray-400 dark:text-gray-500">Released on your approval</p>
                                        </div>

                                        {/* Amount */}
                                        <span className="text-[15px] font-medium text-gray-900 dark:text-gray-100 whitespace-nowrap ml-3 pt-px">
                                            {currencySymbol}{ms.amount.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Trust Banner */}
                    <div className="bg-[#E6F1FB] dark:bg-[#0f1e2e] border-[0.5px] border-[#b8d6f5] dark:border-[#1a3a5c] rounded-2xl py-5 px-6 mb-8 flex gap-3.5 items-start">
                        <div className="w-[34px] h-[34px] bg-[#185FA5] dark:bg-[#1a4a7a] rounded-[10px] flex items-center justify-center shrink-0">
                            <i className="ti ti-lock text-[17px] text-[#E6F1FB]"></i>
                        </div>
                        <div>
                            <p className="text-[13px] font-medium text-[#0C447C] dark:text-[#60a5d4] mb-1">
                                How TrustBridge escrow works
                            </p>
                            <p className="text-[13px] text-[#185FA5] dark:text-[#4a8ab8] leading-relaxed">
                                Your funds are held in an FCA-compliant account. Money is only released to{' '}
                                {proposal.freelancerName} once you approve each milestone as delivered.
                            </p>
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="border-t-[0.5px] border-[#edf0eb] dark:border-[#2a3a2d] pt-6 flex flex-col sm:flex-row gap-2.5">
                        <button
                            onClick={() => setIsDeclined(true)}
                            disabled={isAccepting || isDeclined}
                            className="
                                w-full sm:w-auto sm:min-w-[120px] py-3.5 px-6
                                bg-transparent border-[0.5px] border-[#cdd6c9] dark:border-[#2a3a2d]
                                rounded-[14px] text-[14px] font-medium
                                text-gray-500 dark:text-gray-400
                                transition-colors hover:bg-[#f4f6f1] dark:hover:bg-[#1c2620]
                                disabled:opacity-50 disabled:cursor-not-allowed
                            "
                        >
                            Decline
                        </button>

                        <button
                            onClick={handleAccept}
                            disabled={isAccepting || isDeclined}
                            className="
                                flex-1 py-4 px-6 bg-[#3FCD6B] hover:bg-[#35bd60]
                                border-none rounded-[14px]
                                text-[15px] font-medium text-[#0F5525]
                                flex items-center justify-center gap-2 tracking-tight
                                transition-colors
                                disabled:opacity-70 disabled:cursor-not-allowed disabled:pointer-events-none
                            "
                        >
                            {isAccepting ? (
                                <>
                                    <i className="ti ti-loader-2 text-[18px] animate-spin"></i>
                                    Preparing secure checkout…
                                </>
                            ) : (
                                <>
                                    <i className="ti ti-credit-card text-[18px]"></i>
                                    Accept &amp; fund escrow
                                </>
                            )}
                        </button>
                    </div>

                </div>
            </div>

            {/* Footer */}
            <div className="text-center mt-6 px-4">
                <p className="text-[12px] text-[#9baa97] dark:text-[#4a6050] leading-[1.7]">
                    Secure transactions powered by{' '}
                    <span className="text-[#3FCD6B] font-medium">TrustBridge</span>
                    <br />
                    Registered under the Financial Conduct Authority (FCA)
                </p>
            </div>

        </div>
    );
}