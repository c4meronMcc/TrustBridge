'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
// 1. IMPORT STRIPE
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';

// Import the form component we will build next
import CheckoutForm from './CheckoutForm';
import MockCheckoutForm from './MockCheckoutForm';

// 2. INITIALIZE STRIPE OUTSIDE THE COMPONENT
// This ensures Stripe doesn't reload on every single React re-render.
// Make sure to use the exact environment variable name you set in Step 2.
const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY!);

// ─── TYPES ─────────────────────────────────────────────────────────────────
interface Milestone {
    id: string;
    title: string;
    amount: number;
    sequence_amount?: number;
}

interface CheckoutData {
    jobTitle: string;
    freelancerName: string;
    clientName: string;
    clientEmail: string;
    currency: string;
    currentMilestone: Milestone;
    clientSecret: string;
    paymentRequestId: string;
    provider: 'stripe' | 'mock';
}

const getCurrencySymbol = (currencyCode: string = 'GBP') => {
    const symbols: Record<string, string> = { GBP: '£', USD: '$', EUR: '€' };
    return symbols[currencyCode.toUpperCase()] || currencyCode + ' ';
};

export default function CheckoutPage() {
    const params = useParams<{ token: string }>();
    const token = params.token;

    // ─── STATE ─────────────────────────────────────────────────────────────────
    const [checkout, setCheckout] = useState<CheckoutData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // ─── FETCH DATA FROM SESSION STORAGE ───────────────────────────────────────
    useEffect(() => {
        try {
            const cacheKey = `trustbridge_checkout_${token}`;
            const cachedData = sessionStorage.getItem(cacheKey);

            if (cachedData) {
                const parsedData = JSON.parse(cachedData);
                setCheckout(parsedData);
            } else {
                setError('No payment data found for this specific job. Please return to the proposal link and accept it again.');
            }
        } catch (err) {
            console.error('Failed to parse checkout data:', err);
            setError('Unable to load this payment. The data may be invalid or expired.');
        } finally {
            setTimeout(() => setIsLoading(false), 400);
        }
    }, [token]);

    // ─── LOADING & ERROR SCREENS (Keep your existing beautiful UI here) ────────
    if (isLoading) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-[#0e1410] flex flex-col items-center justify-center font-sans">
                <i className="ti ti-loader-2 text-[30px] text-[#3FCD6B] animate-spin mb-3"></i>
                <p className="text-[13px] font-medium text-[#0F5525] dark:text-[#3FCD6B]">Loading secure payment...</p>
            </div>
        );
    }

    if (error || !checkout || (!checkout.clientSecret && checkout.provider !== 'mock')) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-[#0e1410] flex items-center justify-center p-4 font-sans">
                <div className="bg-white dark:bg-[#161d18] p-8 rounded-[24px] border-[0.5px] border-red-200 dark:border-red-900/50 max-w-[640px] w-full text-center">
                    <i className="ti ti-alert-triangle text-[40px] text-red-500 mb-4 block"></i>
                    <h2 className="text-[20px] font-medium text-gray-900 dark:text-gray-100 mb-2">Payment Unavailable</h2>
                    <p className="text-[14px] text-gray-500 dark:text-gray-400">{error || "Missing security token."}</p>
                </div>
            </div>
        );
    }

    const sym = getCurrencySymbol(checkout.currency);
    const fmtAmount = checkout.currentMilestone.amount.toLocaleString('en-GB', { minimumFractionDigits: 2 });

    // 3. THE STRIPE OPTIONS
    const appearance = {
        // 💥 FIX: Change 'none' to 'flat'. This strips the default shadows
        // and borders, letting your custom rules take over perfectly.
        theme: 'flat' as const,
        variables: {
            fontFamily: 'ui-sans-serif, system-ui, sans-serif',
            colorText: '#1c2620',
            colorDanger: '#df1b41',
            colorBackground: 'transparent',
        },
        rules: {
            '.Input': {
                border: '1px solid #dde4d9',
                borderRadius: '12px',
                padding: '14px 16px',
                backgroundColor: '#ffffff',
                boxShadow: '0 1px 2px rgba(0, 0, 0, 0.02)',
                transition: 'all 0.2s ease',
            },
            '.Input:focus': {
                borderColor: '#3FCD6B',
                boxShadow: '0 0 0 1px #3FCD6B',
                outline: 'none',
            },
            '.Label': {
                color: '#4b5563',
                fontSize: '13px',
                fontWeight: '500',
                marginBottom: '6px',
            },
            '.Tab': {
                border: '1px solid #dde4d9',
                borderRadius: '10px',
                padding: '10px 14px',
                backgroundColor: '#ffffff',
            },
            '.Tab--selected': {
                borderColor: '#3FCD6B',
                boxShadow: '0 0 0 1px #3FCD6B',
            }
        }
    };

    const options = {
        clientSecret: checkout.clientSecret,
        appearance,
    };

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

            <div className="max-w-[1024px] mx-auto grid grid-cols-1 lg:grid-cols-12 gap-6 lg:items-start">

                {/* ── LEFT COLUMN: The Stripe/Mock Provider ── */}
                <div className="lg:col-span-7 order-2 lg:order-1 space-y-4">
                    <div className="bg-white dark:bg-[#161d18] rounded-2xl border-[0.5px] border-[#dde4d9] dark:border-[#2a3a2d] overflow-hidden p-6">

                        {checkout.provider === 'mock' ? (
                            <MockCheckoutForm
                                amount={fmtAmount}
                                symbol={sym}
                                jobToken={token}
                                paymentRequestId={checkout.paymentRequestId}
                            />
                        ) : (
                            <Elements stripe={stripePromise} options={options}>
                                <CheckoutForm
                                    amount={fmtAmount}
                                    symbol={sym}
                                    jobToken={token}
                                    clientSecret={checkout.clientSecret}
                                    clientName={checkout.clientName}
                                    clientEmail={checkout.clientEmail}
                                />
                            </Elements>
                        )}

                    </div>
                </div>

                {/* ── RIGHT COLUMN: Order Summary & Trust Details ── */}
                {/* (Keep your exact existing right-hand column code here!) */}
                <div className="lg:col-span-5 order-1 lg:order-2 space-y-4 lg:sticky lg:top-6">
                    {/* ... your existing summary box ... */}
                </div>

            </div>
        </div>
    );
}