"use client";

import React, { useState, useRef, KeyboardEvent, ChangeEvent } from 'react';
import { useRouter } from 'next/navigation';

type PanelState = 'login' | '2fa' | 'success' | 'magic' | 'magic-sent' | 'forgot';

export default function TrustBridgeLogin() {
    const [activePanel, setActivePanel] = useState<PanelState>('login');
    const [email, setEmail] = useState('jamie@example.com');
    const [password, setPassword] = useState('password123');
    const [showPassword, setShowPassword] = useState(false);
    const [showError, setShowError] = useState(false);
    const [otp, setOtp] = useState(['4', '8', '2', '', '', '']);
    const [redirectProgress, setRedirectProgress] = useState(0);

    const router = useRouter();
    const otpRefs = useRef<(HTMLInputElement | null)[]>([]);

    const tryLogin = () => {
        if (!email || !password) {
            setShowError(true);
            return;
        }
        setShowError(false);
        setActivePanel('2fa');
    };

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
                <button onClick={() => router.push('/signUp')} className="bg-white text-[#0F5525] border-[1.5px] border-[#0F5525] rounded-lg px-4 py-1.5 text-[13px] font-medium transition-colors hover:bg-[#3FCD6B] hover:border-[#3FCD6B] hover:text-white">
                    Sign up
                </button>
            </div>

            {/* ── Centred card ── */}
            <div className="flex-1 flex items-center justify-center p-4 pb-6">
                <div className="bg-white rounded-2xl shadow-[0_8px_40px_rgba(0,0,0,0.13),0_2px_8px_rgba(0,0,0,0.07)] w-full max-w-92.5 overflow-hidden">


                    {/* ══════════════════════════════
                        LOGIN PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'login' && (
                        <div>
                            <div className="pt-6 px-6 pb-4 md:pt-7.5 md:px-7 md:pb-4.5">
                                <div className="text-[19px] font-semibold text-[#111] mb-1">Welcome back</div>
                                <div className="text-[13px] text-[#777]">Sign in to your account to continue</div>
                            </div>

                            <div className="px-6 pb-6 md:px-7 md:pb-7">
                                {showError && (
                                    <div className="flex items-center gap-2 bg-[#FEF0F0] border border-[#F5C6C6] rounded-lg py-2 px-3 text-[12px] text-[#A32D2D] mb-3.5">
                                        <i className="ti ti-alert-circle text-[15px] shrink-0" aria-hidden="true"></i>
                                        Incorrect email or password. Please try again.
                                    </div>
                                )}

                                {/* Email */}
                                <div className="mb-3.5">
                                    <label className="block text-[12px] font-medium text-[#333] mb-1.5">Email address</label>
                                    <input type="email" placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)}
                                           className="w-full text-[13px] py-2 px-3 rounded-lg border border-[#dde3e0] bg-white text-[#111] outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10" />
                                </div>

                                {/* Password */}
                                <div className="mb-3.5">
                                    <label className="block text-[12px] font-medium text-[#333] mb-1.5">Password</label>
                                    <div className="relative">
                                        <input type={showPassword ? "text" : "password"} placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)}
                                               className="w-full text-[13px] py-2 pl-3 pr-9 rounded-lg border border-[#dde3e0] bg-white text-[#111] outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10" />
                                        <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#bbb] hover:text-[#777]">
                                            {showPassword ? <i className="ti ti-eye-off text-[15px]"></i> : <i className="ti ti-eye text-[15px]"></i>}
                                        </button>
                                    </div>
                                </div>

                                {/* Forgot password */}
                                <div className="text-[11px] text-[#0F5525] float-right -mt-2 mb-3.5 cursor-pointer font-medium hover:text-[#3FCD6B]" onClick={() => setActivePanel('forgot')}>
                                    Forgot password?
                                </div>
                                <div className="clear-both h-1"></div>

                                {/* Sign-in button */}
                                <button onClick={tryLogin} className="w-full bg-[#0F5525] text-white rounded-lg p-2.5 text-[13px] font-medium flex items-center justify-center gap-1.5 mb-3 transition-colors hover:bg-[#3FCD6B]">
                                    <i className="ti ti-login text-[14px]"></i> Sign in
                                </button>

                                {/* Divider */}
                                <div className="flex items-center gap-2.5 mb-4">
                                    <div className="flex-1 h-px bg-[#eee]"></div>
                                    <span className="text-[11px] text-[#bbb]">or</span>
                                    <div className="flex-1 h-px bg-[#eee]"></div>
                                </div>

                                {/* Social buttons */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-2 mb-4">
                                    <button onClick={handleSuccess} className="flex items-center justify-center gap-2 py-2 px-2.5 rounded-lg border border-[#dde3e0] bg-white text-[13px] font-medium text-[#222] transition-colors hover:bg-[#3FCD6B] hover:border-[#3FCD6B] hover:text-white group">
                                        <svg width="15" height="15" viewBox="0 0 48 48" className="group-hover:opacity-90">
                                            <path d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.1 7.9 3l5.7-5.7C34.1 6.5 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20c11 0 20-9 20-20 0-1.2-.1-2.3-.4-3.5z" fill="#FFC107"/>
                                            <path d="M6.3 14.7l6.6 4.8C14.7 16 19.1 13 24 13c3.1 0 5.8 1.1 7.9 3l5.7-5.7C34.1 6.5 29.3 4 24 4 16.3 4 9.7 8.3 6.3 14.7z" fill="#FF3D00"/>
                                            <path d="M24 44c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.4 35.5 26.8 36 24 36c-5.2 0-9.7-3.3-11.3-8H6.1C9.4 36.1 16.2 44 24 44z" fill="#4CAF50"/>
                                            <path d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.2-4.1 5.6l6.2 5.2C37 38.9 44 33 44 24c0-1.2-.1-2.3-.4-3.5z" fill="#1976D2"/>
                                        </svg>
                                        Google
                                    </button>
                                    <button onClick={handleSuccess} className="flex items-center justify-center gap-2 py-2 px-2.5 rounded-lg border border-[#dde3e0] bg-white text-[13px] font-medium text-[#222] transition-colors hover:bg-[#3FCD6B] hover:border-[#3FCD6B] hover:text-white group">
                                        <svg width="14" height="14" viewBox="0 0 814 1000" className="group-hover:invert">
                                            <path d="M788.1 340.9c-5.8 4.5-108.2 62.2-108.2 190.5 0 148.4 130.3 200.9 134.2 202.2-.6 3.2-20.7 71.9-68.7 141.9-42.8 61.6-87.5 123.1-155.5 123.1s-85.5-39.5-164-39.5c-76 0-103.7 40.8-165.9 40.8s-105-57.8-155.5-127.4C46 790.7 0 663 0 541.8c0-207.5 135.4-317.3 268.5-317.3 70.1 0 128.4 46.4 172.5 46.4 42.3 0 109.2-49.9 190.5-49.9 30.8 0 108.2 2.6 168.6 75.3zm-234.6-172.9c32.1-38.2 54.7-91.4 54.7-144.7 0-7.4-.6-14.9-1.9-21.1-51.9 2-113.1 34.7-149.5 78.2-28.2 32.8-55.3 85.4-55.3 139.4 0 8 1.3 16 1.9 18.6 3.2.6 8.4 1.3 13.6 1.3 46.4 0 102.9-31.5 136.5-71.7z" fill="#111"/>
                                        </svg>
                                        Apple
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}


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

                                <div className="grid grid-cols-6 gap-1.5 my-3">
                                    {otp.map((digit, index) => (
                                        <input key={index} ref={el => { otpRefs.current[index] = el; }} maxLength={1} value={digit}
                                               onChange={e => handleOtpChange(e, index)}
                                               onKeyDown={e => handleOtpKeyDown(e, index)}
                                               className={`w-full aspect-square text-center text-[18px] font-medium border rounded-lg outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10 ${digit ? 'border-[#3FCD6B] bg-[#f0fdf5]' : 'border-[#dde3e0] bg-white'}`} />
                                    ))}
                                </div>

                                <div className="text-[11px] text-[#bbb] text-center mb-4">
                                    Didn&#39;t receive a code? <span className="text-[#0F5525] cursor-pointer font-medium hover:underline">Resend</span>
                                </div>

                                <button onClick={handleSuccess} className="w-full bg-[#0F5525] text-white rounded-lg p-2.5 text-[13px] font-medium flex items-center justify-center gap-1.5 transition-colors hover:bg-[#3FCD6B]">
                                    <i className="ti ti-shield-check text-[14px]"></i> Verify &amp; sign in
                                </button>

                                <div onClick={() => setActivePanel('login')} className="text-[12px] text-[#aaa] cursor-pointer flex items-center gap-1 mt-3.5 justify-center transition-colors hover:text-[#3FCD6B]">
                                    <i className="ti ti-arrow-left text-[13px]"></i> Back to login
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
                                    <div className="h-full bg-[#3FCD6B] rounded-full transition-all duration-2500 ease-linear" style={{ width: `${redirectProgress}%` }}></div>
                                </div>
                            </div>
                        </div>
                    )}


                    {/* ══════════════════════════════
                        MAGIC LINK PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'magic' && (
                        <div>
                            <div className="pt-6 px-6 pb-4 md:pt-7.5 md:px-7 md:pb-4.5">
                                <div className="text-[19px] font-semibold text-[#111] mb-1">Magic link login</div>
                                <div className="text-[13px] text-[#777]">Get a one-click login link sent to your email</div>
                            </div>

                            <div className="px-6 pb-6 md:px-7 md:pb-7">
                                <div className="w-11 h-11 rounded-full bg-[#E1F5EE] flex items-center justify-center mx-auto mb-4">
                                    <i className="ti ti-mail-forward text-[20px] text-[#0F5525]"></i>
                                </div>

                                <div className="mb-4">
                                    <label className="block text-[12px] font-medium text-[#333] mb-1.5">Email address</label>
                                    <input type="email" placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)}
                                           className="w-full text-[13px] py-2 px-3 rounded-lg border border-[#dde3e0] bg-white text-[#111] outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10" />
                                    <div className="text-[11px] text-[#999] mt-1">We&#39;ll send a secure one-click login link to this address.</div>
                                </div>

                                <button onClick={() => setActivePanel('magic-sent')} className="w-full bg-[#0F5525] text-white rounded-lg p-2.5 text-[13px] font-medium flex items-center justify-center gap-1.5 transition-colors hover:bg-[#3FCD6B]">
                                    <i className="ti ti-send text-[14px]"></i> Send magic link
                                </button>

                                <div onClick={() => setActivePanel('login')} className="text-[12px] text-[#aaa] cursor-pointer flex items-center gap-1 mt-3.5 justify-center transition-colors hover:text-[#3FCD6B]">
                                    <i className="ti ti-arrow-left text-[13px]"></i> Back to login
                                </div>
                            </div>
                        </div>
                    )}


                    {/* ══════════════════════════════
                        MAGIC LINK SENT PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'magic-sent' && (
                        <div>
                            <div className="pt-6 px-6 pb-4 md:pt-7.5 md:px-7 md:pb-4.5">
                                <div className="text-[19px] font-semibold text-[#111] mb-1">Check your inbox</div>
                                <div className="text-[13px] text-[#777]">Your magic link is on its way</div>
                            </div>

                            <div className="px-6 pb-6 md:px-7 md:pb-7 text-center">
                                <div className="w-12 h-12 rounded-full bg-[#E1F5EE] flex items-center justify-center mx-auto mb-3.5 mt-2">
                                    <i className="ti ti-mail-check text-[24px] text-[#0F5525]"></i>
                                </div>
                                <div className="text-[12px] text-[#888] leading-relaxed">
                                    A link has been sent to <strong className="text-[#111]">{email}</strong>. It expires in 15 minutes.
                                </div>

                                <div onClick={() => setActivePanel('login')} className="text-[12px] text-[#aaa] cursor-pointer flex items-center gap-1 mt-5 justify-center transition-colors hover:text-[#3FCD6B]">
                                    <i className="ti ti-arrow-left text-[13px]"></i> Back to login
                                </div>
                            </div>
                        </div>
                    )}


                    {/* ══════════════════════════════
                        FORGOT PASSWORD PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'forgot' && (
                        <div>
                            <div className="pt-6 px-6 pb-4 md:pt-7.5 md:px-7 md:pb-4.5">
                                <div className="text-[19px] font-semibold text-[#111] mb-1">Reset your password</div>
                                <div className="text-[13px] text-[#777]">Enter your email and we&#39;ll send a reset link</div>
                            </div>

                            <div className="px-6 pb-6 md:px-7 md:pb-7">
                                <div className="mb-4">
                                    <label className="block text-[12px] font-medium text-[#333] mb-1.5">Email address</label>
                                    <input type="email" placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)}
                                           className="w-full text-[13px] py-2 px-3 rounded-lg border border-[#dde3e0] bg-white text-[#111] outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10" />
                                    <div className="text-[11px] text-[#999] mt-1">We&#39;ll send a reset link if this email is registered.</div>
                                </div>

                                <button onClick={() => setActivePanel('magic-sent')} className="w-full bg-[#0F5525] text-white rounded-lg p-2.5 text-[13px] font-medium flex items-center justify-center gap-1.5 transition-colors hover:bg-[#3FCD6B]">
                                    <i className="ti ti-send text-[14px]"></i> Send reset link
                                </button>

                                <div onClick={() => setActivePanel('login')} className="text-[12px] text-[#aaa] cursor-pointer flex items-center gap-1 mt-3.5 justify-center transition-colors hover:text-[#3FCD6B]">
                                    <i className="ti ti-arrow-left text-[13px]"></i> Back to login
                                </div>
                            </div>
                        </div>
                    )}


                </div>
            </div>
        </div>
    );
}