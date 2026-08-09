'use client';

import React, { useState } from 'react';
import {
    useStripe,
    useElements,
    CardElement,
    ExpressCheckoutElement // 👈 We kept Apple/Google Pay!
} from '@stripe/react-stripe-js';

interface CheckoutFormProps {
    amount: string;
    symbol: string;
    jobToken: string;
    clientName: string;
    clientEmail: string;
    clientSecret: string;
}

export default function CheckoutForm({ amount, symbol, jobToken, clientName, clientEmail, clientSecret }: CheckoutFormProps) {
    const stripe = useStripe();
    const elements = useElements();

    const [message, setMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [activeSection, setActiveSection] = useState<'bank' | 'other'>('bank');

    // ─── 1. TRUE OPEN BANKING LOGIC (TRUELAYER STYLE) ────────────────────────
    const handleBankTransfer = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!stripe) return;
        setIsLoading(true);

        // 💥 THE FIX: Use the dedicated confirmPayByBankPayment method.
        // This satisfies TypeScript and fires the instant bank redirect!
        const { error } = await stripe.confirmPayByBankPayment(clientSecret, {
            payment_method: {
                billing_details: {
                    name: clientName,
                    email: clientEmail
                },
            },
            return_url: `http://localhost:3000/invite/success/${jobToken}`,
        });

        if (error) {
            setMessage(error.message || "Bank transfer failed.");
        }
        setIsLoading(false);
    };

    // ─── 2. CARD PAYMENT LOGIC ────────────────────────────────────────────────
    const handleCardPayment = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!stripe || !elements) return;
        setIsLoading(true);

        const cardElement = elements.getElement(CardElement);
        if (!cardElement) return;

        const { error } = await stripe.confirmCardPayment(clientSecret, {
            payment_method: {
                card: cardElement,
                billing_details: { name: clientName, email: clientEmail },
            },
        });

        if (error) {
            setMessage(error.message || "Card payment failed.");
        } else {
            window.location.href = `http://localhost:3000/invite/success/${jobToken}`;
        }
        setIsLoading(false);
    };

    // ─── 3. APPLE PAY / GOOGLE PAY LOGIC ──────────────────────────────────────
    const handleExpressPayment = async () => {
        if (!stripe || !elements) return;
        setIsLoading(true);
        const { error } = await stripe.confirmPayment({
            elements,
            clientSecret,
            confirmParams: {
                return_url: `http://localhost:3000/invite/success/${jobToken}`,
            },
        });
        if (error) setMessage(error.message || "Wallet payment failed.");
        setIsLoading(false);
    };

    // ─── THE CUSTOM UI ────────────────────────────────────────────────────────
    return (
        <div className="space-y-4">

            {/* 💥 SECTION 1: PAY BY BANK (THE HERO) */}
            <div className={`border-2 rounded-2xl transition-all duration-300 overflow-hidden ${
                activeSection === 'bank'
                    ? 'border-[#3FCD6B] bg-white ring-4 ring-[#3FCD6B]/10'
                    : 'border-[#dde4d9] bg-[#f8faf6] hover:border-[#b8c5b3] cursor-pointer'
            }`}>

                {/* Accordion Header */}
                <div onClick={() => setActiveSection('bank')} className="p-5 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center ${activeSection === 'bank' ? 'bg-[#3FCD6B]/20 text-[#0F5525]' : 'bg-gray-200 text-gray-500'}`}>
                            <i className="ti ti-building-bank text-[20px]"></i>
                        </div>
                        <div>
                            <h3 className="font-semibold text-[#1c2620] text-[16px]">Pay by Bank</h3>
                            <p className="text-[13px] text-gray-500">Instant Open Banking Transfer</p>
                        </div>
                    </div>
                    <div className="flex items-center gap-3">
                        <span className="bg-[#e6f8ec] text-[#0F5525] text-[11px] font-bold px-2.5 py-1 rounded-full tracking-wide">
                            RECOMMENDED
                        </span>
                        <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center ${activeSection === 'bank' ? 'border-[#3FCD6B]' : 'border-gray-300'}`}>
                            {activeSection === 'bank' && <div className="w-2.5 h-2.5 bg-[#3FCD6B] rounded-full"></div>}
                        </div>
                    </div>
                </div>

                {/* Accordion Body */}
                {activeSection === 'bank' && (
                    <div className="px-5 pb-5 pt-2 animate-in fade-in slide-in-from-top-2 duration-300">
                        <form onSubmit={handleBankTransfer} className="border-t border-gray-100 pt-5">

                            <div className="mb-5 flex items-start gap-3 bg-[#f8faf6] p-4 rounded-xl border border-[#dde4d9]">
                                <i className="ti ti-lock-square-rounded text-[#3FCD6B] text-[20px] mt-0.5"></i>
                                <p className="text-[13.5px] leading-relaxed text-[#1c2620]">
                                    You will be securely redirected to select your bank (e.g., Monzo, Barclays, HSBC) and authorize the payment directly in your banking app. No account numbers required.
                                </p>
                            </div>

                            <button disabled={isLoading} className="w-full py-4 mt-2 bg-[#3FCD6B] hover:bg-[#35bd60] rounded-xl text-[15px] font-semibold text-[#0F5525] shadow-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2">
                                {isLoading ? (
                                    <><i className="ti ti-loader-2 animate-spin text-[18px]"></i> Redirecting to Bank...</>
                                ) : (
                                    `Proceed to Bank • ${symbol}${amount}`
                                )}
                            </button>
                        </form>
                    </div>
                )}
            </div>

            {/* 💥 SECTION 2: OTHER OPTIONS (APPLE PAY / CARDS) */}
            <div className={`border-2 rounded-2xl transition-all duration-300 overflow-hidden ${
                activeSection === 'other'
                    ? 'border-gray-800 bg-white ring-4 ring-gray-800/10'
                    : 'border-[#dde4d9] bg-[#f8faf6] hover:border-[#b8c5b3] cursor-pointer'
            }`}>

                {/* Accordion Header */}
                <div onClick={() => setActiveSection('other')} className="p-5 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center ${activeSection === 'other' ? 'bg-gray-800 text-white' : 'bg-gray-200 text-gray-500'}`}>
                            <i className="ti ti-credit-card text-[20px]"></i>
                        </div>
                        <div>
                            <h3 className="font-semibold text-[#1c2620] text-[16px]">Other Options</h3>
                            <p className="text-[13px] text-gray-500">Apple Pay, Google Pay, Cards</p>
                        </div>
                    </div>
                    <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center ${activeSection === 'other' ? 'border-gray-800' : 'border-gray-300'}`}>
                        {activeSection === 'other' && <div className="w-2.5 h-2.5 bg-gray-800 rounded-full"></div>}
                    </div>
                </div>

                {/* Accordion Body */}
                {activeSection === 'other' && (
                    <div className="px-5 pb-5 pt-2 animate-in fade-in slide-in-from-top-2 duration-300">
                        <div className="border-t border-gray-100 pt-5 space-y-6">

                            <div className="min-h-[48px]">
                                <ExpressCheckoutElement onConfirm={handleExpressPayment} />
                            </div>

                            <div className="flex items-center gap-3">
                                <div className="h-px bg-gray-200 flex-1"></div>
                                <span className="text-[12px] font-medium text-gray-400 uppercase tracking-wider">Or pay with card</span>
                                <div className="h-px bg-gray-200 flex-1"></div>
                            </div>

                            <form onSubmit={handleCardPayment} className="space-y-4">
                                <div className="p-3.5 bg-white border border-[#dde4d9] rounded-xl shadow-sm">
                                    <CardElement options={{ style: { base: { fontSize: '15px', color: '#1c2620', '::placeholder': { color: '#aab7c4' } } } }} />
                                </div>
                                <button disabled={isLoading} className="w-full py-4 bg-gray-900 hover:bg-gray-800 rounded-xl text-[15px] font-semibold text-white shadow-sm transition-all disabled:opacity-50">
                                    {isLoading ? "Processing..." : `Pay ${symbol}${amount} securely`}
                                </button>
                            </form>
                        </div>
                    </div>
                )}
            </div>

            {/* Error States */}
            {message && (
                <div className="p-4 mt-4 bg-red-50 text-red-600 text-[13px] font-medium rounded-xl border border-red-100 flex items-start gap-2">
                    <i className="ti ti-alert-circle text-[18px]"></i>
                    <p>{message}</p>
                </div>
            )}
        </div>
    );
}