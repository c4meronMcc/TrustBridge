"use client";

import React, { useState, useRef, KeyboardEvent, ChangeEvent, useEffect } from 'react';
import { useRouter } from 'next/navigation';

type PanelState = 'signUp' | '2fa' | 'success' | 'magic' | 'magic-sent' | 'forgot';

export default function TrustBridgeVerification() {
    const [activePanel, setActivePanel] = useState<PanelState>('2fa');
    const [otp, setOtp] = useState(['', '', '', '', '', '']);
    const [redirectProgress, setRedirectProgress] = useState(0);
    const [showError, setShowError] = useState(false);
    const router = useRouter();
    const otpRefs = useRef<(HTMLInputElement | null)[]>([]);

    // Safely get email from localStorage (checking if window exists for Next.js SSR compatibility)
    const email = typeof window !== 'undefined' ? window.localStorage.getItem('email') : null;

    // ── AUTO-FOCUS EFFECT ──
    // This runs once when the component mounts, focusing the first input box instantly
    useEffect(() => {
        if (activePanel === '2fa' && otpRefs.current[0]) {
            otpRefs.current[0].focus();
        }
    }, [activePanel]);

    const handleSuccess = () => {
        setActivePanel('success');
        setTimeout(() => setRedirectProgress(100), 100);
    };

    const handleOtpChange = (e: ChangeEvent<HTMLInputElement>, index: number) => {
        const value = e.target.value;
        const newOtp = [...otp];
        newOtp[index] = value;
        setOtp(newOtp);
        if (value && index < 5) otpRefs.current[index + 1]?.focus();
    };

    const handleOtpKeyDown = (e: KeyboardEvent<HTMLInputElement>, index: number) => {
        if (e.key === 'Backspace' && !otp[index] && index > 0) {
            otpRefs.current[index - 1]?.focus();
        }
    };

    const handleSignUpSubmit = async () => {
        const payload = {
            email: email,
            verificationCode: otp.join(''),
        };

        try {
            const response = await fetch('http://localhost:8080/api/auth/verificationCode', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed. Please check your details.');
            }

            // Success! Move back to login
            window.location.href = '/login';
        } catch (error: any) {
            console.error("Signup Error:", error);
            setShowError(true);
        }
    };

    const resendVerificationCode = async () => {

        try {
            const response = await fetch('http://localhost:8080/api/auth/resendVerificationCode', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: String(email)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed. Please check your details.');
            }

        } catch (error: any) {
            console.error("Signup Error:", error);
            setShowError(true);
        }
    }

    return (
        <div className="min-h-screen flex flex-col bg-[#f0f2f0] text-slate-900 font-sans">

            {/* ── Top nav ── */}
            <div className="flex items-center justify-between p-4 md:px-6">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-[#0F5525] rounded-lg flex items-center justify-center shrink-0">
                        <i className="ti ti-shield-check text-[#3FCD6B] text-lg" aria-hidden="true"></i>
                    </div>
                    <span className="text-[17px] font-semibold text-[#0F5525] tracking-tight">TrustBridge</span>
                </div>
                <button onClick={() => router.push('/signUp')}
                        className="bg-white text-[#0F5525] border-[1.5px] border-[#0F5525] rounded-lg px-4 py-1.5 text-[13px] font-medium transition-colors hover:bg-[#3FCD6B] hover:border-[#3FCD6B] hover:text-white">
                    ← back
                </button>
            </div>

            {/* ── Centred card ── */}
            <div className="flex-1 flex items-center justify-center p-4 pb-6">
                <div className="bg-white rounded-2xl shadow-[0_8px_40px_rgba(0,0,0,0.13),0_2px_8px_rgba(0,0,0,0.07)] w-full max-w-[400px] overflow-hidden">

                    {/* ══════════════════════════════
                        2FA PANEL
                    ══════════════════════════════ */}
                    {activePanel === '2fa' && (
                        <div>
                            <div className="pt-6 px-6 pb-4 md:pt-7.5 md:px-7 md:pb-4.5">
                                <div className="text-[19px] font-semibold text-[#111] mb-1">Two-step verification</div>
                                <div className="text-[13px] text-[#777]">Confirm it&#39;s you to keep your account secure</div>
                            </div>

                            <div className="px-6 pb-6 md:px-7 md:pb-7">
                                <div className="text-center mb-4">
                                    <div className="w-11 h-11 rounded-full bg-[#E1F5EE] flex items-center justify-center mx-auto mb-2.5">
                                        <i className="ti ti-device-mobile text-[20px] text-[#0F5525]"></i>
                                    </div>
                                    <div className="text-[12px] text-[#777] leading-relaxed">
                                        Enter the 6-digit code from your authenticator app or sent to <strong className="text-[#111]">+44 •••• ••7821</strong>
                                    </div>
                                </div>

                                {showError && (
                                    <div className="flex items-center gap-2 bg-[#FEF0F0] border border-[#F5C6C6] rounded-lg py-2 px-3 text-[12px] text-[#A32D2D] mb-3.5">
                                        <i className="ti ti-alert-circle text-[15px] shrink-0" aria-hidden="true"></i>
                                        Verification failed. Please check the code and try again.
                                    </div>
                                )}

                                <div className="grid grid-cols-6 gap-1.5 my-3">
                                    {otp.map((digit, index) => (
                                        <input key={index} ref={el => { otpRefs.current[index] = el; }} maxLength={1} value={digit}
                                               onChange={e => handleOtpChange(e, index)}
                                               onKeyDown={e => handleOtpKeyDown(e, index)}
                                               className={`w-full aspect-square text-center text-[18px] font-medium border rounded-lg outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10 ${digit ? 'border-[#3FCD6B] bg-[#f0fdf5]' : 'border-[#dde3e0] bg-white'}`}/>
                                    ))}
                                </div>

                                <div className="text-[11px] text-[#bbb] text-center mb-4">
                                    Didn&#39;t receive a code? <span onClick={resendVerificationCode} className="text-[#0F5525] cursor-pointer font-medium hover:underline">Resend</span>
                                </div>

                                <button onClick={handleSignUpSubmit}
                                        className="w-full bg-[#0F5525] text-white rounded-lg p-2.5 text-[13px] font-medium flex items-center justify-center gap-1.5 transition-colors hover:bg-[#3FCD6B]">
                                    <i className="ti ti-shield-check text-[14px]"></i> Verify &amp; sign in
                                </button>

                                <div onClick={() => router.push('/signUp')}
                                     className="text-[12px] text-[#aaa] cursor-pointer flex items-center gap-1 mt-3.5 justify-center transition-colors hover:text-[#3FCD6B]">
                                    <i className="ti ti-arrow-left text-[13px]"></i> Back to signup
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ══════════════════════════════
                        SUCCESS PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'success' && (
                        <div>
                            <div className="pt-6 px-6 pb-4 md:pt-7.5 md:px-7 md:pb-4.5">
                                <div className="text-[19px] font-semibold text-[#111] mb-1">You&#39;re signed in</div>
                            </div>

                            <div className="px-6 pb-6 md:px-7 md:pb-7 text-center py-3">
                                <div className="w-12 h-12 rounded-full bg-[#E1F5EE] flex items-center justify-center mx-auto mb-3.5">
                                    <i className="ti ti-check text-[24px] text-[#0F5525]"></i>
                                </div>
                                <div className="text-[12px] text-[#888] leading-relaxed mb-5">
                                    Identity verified. Redirecting you to your dashboard…
                                </div>
                                <div className="h-1 bg-[#eee] rounded-full overflow-hidden">
                                    <div className="h-full bg-[#3FCD6B] rounded-full transition-all duration-[2500ms] ease-linear" style={{width: `${redirectProgress}%`}}></div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}