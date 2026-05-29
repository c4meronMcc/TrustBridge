"use client";

import React, {useState} from 'react';
import {useRouter} from "next/navigation";

type PanelState = 'login' | '2fa' | 'success' | 'forgot';

const inputCls =
    "w-full text-[13px] py-1.5 px-2.5 rounded-lg border border-[#dde3e0] dark:border-neutral-800 bg-white dark:bg-neutral-950 text-[#111] dark:text-white outline-none transition-all focus:border-[#3FCD6B] focus:ring-[3px] focus:ring-[#3FCD6B]/10 dark:focus:ring-transparent placeholder-slate-400 dark:placeholder-neutral-600";

export default function TrustBridgeSignUp() {
    const [activePanel, setActivePanel] = useState<PanelState>('login');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [role, setRole] = useState<'freelancer' | 'client'>('freelancer');
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [showError, setShowError] = useState(false);
    const [redirectProgress, setRedirectProgress] = useState(0);

    const route = useRouter();

    const handleSuccess = () => {
        setActivePanel('success');
        setTimeout(() => setRedirectProgress(100), 100);
    };

    const handleRedirectLogin = () => {
        route.push('/login');
    }

    const handleSignUpSubmit = async () => {
        if (!email || !password || !firstName || !lastName || !phone || password !== confirmPassword) {
            setShowError(true);
            return;
        }
        setShowError(false);

        const cleanPhoneNumber = phone.replace(/\s+/g, '');

        const payload = {
            email: email,
            phoneNumber: cleanPhoneNumber,
            password: password,
            firstName: firstName,
            lastName: lastName,
            role: role
        };

        try {
            const response = await fetch('http://localhost:8080/api/auth/register', {
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

            localStorage.setItem('email', email);
            window.location.href = '/signUp/verification';
        } catch (error: any) {
            console.error("Signup Error:", error);
            setShowError(true);
        }
    };

    return (
        <div className="min-h-screen flex flex-col bg-[#f0f2f0] dark:bg-neutral-950 text-slate-900 font-sans">

            {/* ── Top nav ── */}
            <div className="flex items-center justify-between p-4 md:px-6">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-[#0F5525] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-lg flex items-center justify-center shrink-0">
                        <i className="ti ti-shield-check text-[#3FCD6B] text-lg" aria-hidden="true" />
                    </div>
                    <span className="text-[17px] font-semibold text-[#0F5525] dark:text-white tracking-tight">TrustBridge</span>
                </div>
                <button onClick={handleRedirectLogin} className="bg-white dark:bg-transparent text-[#0F5525] dark:text-neutral-300 border-[1.5px] border-[#0F5525] dark:border-neutral-700 rounded-lg px-4 py-1.5 text-[13px] font-medium transition-colors hover:bg-[#3FCD6B] dark:hover:bg-neutral-800 hover:border-[#3FCD6B] dark:hover:border-neutral-600 hover:text-white dark:hover:text-white">
                    Login
                </button>
            </div>

            {/* ── Centred card ── */}
            <div className="flex-1 flex items-center justify-center p-4 pb-6">
                <div className="bg-white dark:bg-neutral-900 rounded-2xl shadow-[0_8px_40px_rgba(0,0,0,0.13),0_2px_8px_rgba(0,0,0,0.07)] dark:shadow-none dark:border dark:border-neutral-800 w-full max-w-sm overflow-hidden">


                    {/* ══════════════════════════════
                        SIGN UP PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'login' && (
                        <div>
                            <div className="pt-5 px-6 pb-2">
                                <div className="text-[17px] font-semibold text-[#111] dark:text-white mb-0.5">Create your account</div>
                                <div className="text-[12px] text-[#777] dark:text-neutral-400">Join TrustBridge to start your journey</div>
                            </div>

                            <div className="px-6 pb-5">

                                {showError && (
                                    <div className="flex items-center gap-2 bg-[#FEF0F0] dark:bg-red-900/10 border border-[#F5C6C6] dark:border-red-900/50 rounded-lg py-1.5 px-3 text-[12px] text-[#A32D2D] dark:text-red-400 mb-2">
                                        <i className="ti ti-alert-circle text-[14px] shrink-0" aria-hidden="true" />
                                        Please fill in all fields correctly.
                                    </div>
                                )}

                                {/* Name */}
                                <div className="grid grid-cols-2 gap-2 mb-2">
                                    <div>
                                        <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">First name</label>
                                        <input type="text" placeholder="John" value={firstName} onChange={e => setFirstName(e.target.value)} className={inputCls} />
                                    </div>
                                    <div>
                                        <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">Last name</label>
                                        <input type="text" placeholder="Doe" value={lastName} onChange={e => setLastName(e.target.value)} className={inputCls} />
                                    </div>
                                </div>

                                {/* Email */}
                                <div className="mb-2">
                                    <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">Email address</label>
                                    <input type="email" placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)} className={inputCls} />
                                </div>

                                {/* Phone */}
                                <div className="mb-2">
                                    <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">Phone number</label>
                                    <div className="flex gap-1.5">
                                        <select className="text-[13px] py-1.5 px-2 rounded-lg border border-[#dde3e0] dark:border-neutral-800 bg-white dark:bg-neutral-950 text-[#111] dark:text-white outline-none focus:border-[#3FCD6B] dark:focus:border-neutral-700">
                                            <option value="+44">🇬🇧 +44</option>
                                            <option value="+1">🇺🇸 +1</option>
                                            <option value="+33">🇫🇷 +33</option>
                                            <option value="+49">🇩🇪 +49</option>
                                        </select>
                                        <input type="tel" placeholder="7700 900000" value={phone} onChange={e => setPhone(e.target.value)} className={`flex-1 ${inputCls}`} />
                                    </div>
                                </div>

                                {/* Password + Confirm */}
                                <div className="grid grid-cols-2 gap-2 mb-3">
                                    <div>
                                        <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">Password</label>
                                        <div className="relative">
                                            <input type={showPassword ? "text" : "password"} placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)} className={`${inputCls} pr-8`} />
                                            <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-2 top-1/2 -translate-y-1/2 text-[#bbb] dark:text-neutral-500 hover:text-[#777] dark:hover:text-neutral-300">
                                                <i className={`ti ${showPassword ? 'ti-eye-off' : 'ti-eye'} text-[13px]`} />
                                            </button>
                                        </div>
                                    </div>
                                    <div>
                                        <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">Confirm</label>
                                        <div className="relative">
                                            <input type={showConfirmPassword ? "text" : "password"} placeholder="••••••••" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} className={`${inputCls} pr-8`} />
                                            <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} className="absolute right-2 top-1/2 -translate-y-1/2 text-[#bbb] dark:text-neutral-500 hover:text-[#777] dark:hover:text-neutral-300">
                                                <i className={`ti ${showConfirmPassword ? 'ti-eye-off' : 'ti-eye'} text-[13px]`} />
                                            </button>
                                        </div>
                                    </div>
                                </div>

                                {/* Role */}
                                <div className="mb-3">
                                    <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">I am a</label>
                                    <div className="grid grid-cols-2 gap-2">
                                        <button type="button" onClick={() => setRole('freelancer')}
                                                className={`flex items-center justify-center gap-1.5 py-1.5 px-3 rounded-lg border-2 text-[12px] font-medium transition-all ${role === 'freelancer' ? 'border-[#0F5525] dark:border-[#3FCD6B] bg-[#E1F5EE] dark:bg-[#3FCD6B]/10 text-[#0F5525] dark:text-[#3FCD6B]' : 'border-[#eee] dark:border-neutral-800 bg-white dark:bg-neutral-950 text-[#555] dark:text-neutral-400 hover:border-[#ccc] dark:hover:border-neutral-700'}`}>
                                            <i className="ti ti-user-star text-[13px]" aria-hidden="true" />
                                            Freelancer
                                        </button>
                                        <button type="button" onClick={() => setRole('client')}
                                                className={`flex items-center justify-center gap-1.5 py-1.5 px-3 rounded-lg border-2 text-[12px] font-medium transition-all ${role === 'client' ? 'border-[#0F5525] dark:border-[#3FCD6B] bg-[#E1F5EE] dark:bg-[#3FCD6B]/10 text-[#0F5525] dark:text-[#3FCD6B]' : 'border-[#eee] dark:border-neutral-800 bg-white dark:bg-neutral-950 text-[#555] dark:text-neutral-400 hover:border-[#ccc] dark:hover:border-neutral-700'}`}>
                                            <i className="ti ti-building-store text-[13px]" aria-hidden="true" />
                                            Client
                                        </button>
                                    </div>
                                </div>

                                {/* Submit */}
                                <button onClick={handleSignUpSubmit} className="w-full bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 rounded-lg py-2 text-[13px] font-medium flex items-center justify-center gap-1.5 mb-3 transition-colors hover:bg-[#3FCD6B] dark:hover:opacity-90">
                                    Create account
                                </button>

                                {/* Social sign up */}
                                <div className="flex items-center gap-2 mb-2.5">
                                    <div className="flex-1 h-px bg-[#eee] dark:bg-neutral-800" />
                                    <span className="text-[11px] text-[#bbb] dark:text-neutral-500">or sign up with</span>
                                    <div className="flex-1 h-px bg-[#eee] dark:bg-neutral-800" />
                                </div>
                                <div className="grid grid-cols-2 gap-2">
                                    <button onClick={handleSuccess} className="flex items-center justify-center gap-2 py-1.5 px-2.5 rounded-lg border border-[#dde3e0] dark:border-neutral-800 bg-white dark:bg-neutral-950 text-[13px] font-medium text-[#222] dark:text-neutral-300 transition-colors hover:bg-[#3FCD6B] dark:hover:bg-neutral-800 hover:border-[#3FCD6B] dark:hover:border-neutral-700 hover:text-white dark:hover:text-white group">
                                        <svg width="15" height="15" viewBox="0 0 48 48" className="group-hover:opacity-90">
                                            <path d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.1 7.9 3l5.7-5.7C34.1 6.5 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20c11 0 20-9 20-20 0-1.2-.1-2.3-.4-3.5z" fill="#FFC107"/>
                                            <path d="M6.3 14.7l6.6 4.8C14.7 16 19.1 13 24 13c3.1 0 5.8 1.1 7.9 3l5.7-5.7C34.1 6.5 29.3 4 24 4 16.3 4 9.7 8.3 6.3 14.7z" fill="#FF3D00"/>
                                            <path d="M24 44c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.4 35.5 26.8 36 24 36c-5.2 0-9.7-3.3-11.3-8H6.1C9.4 36.1 16.2 44 24 44z" fill="#4CAF50"/>
                                            <path d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.2-4.1 5.6l6.2 5.2C37 38.9 44 33 44 24c0-1.2-.1-2.3-.4-3.5z" fill="#1976D2"/>
                                        </svg>
                                        Google
                                    </button>
                                    <button onClick={handleSuccess} className="flex items-center justify-center gap-2 py-1.5 px-2.5 rounded-lg border border-[#dde3e0] dark:border-neutral-800 bg-white dark:bg-neutral-950 text-[13px] font-medium text-[#222] dark:text-neutral-300 transition-colors hover:bg-[#3FCD6B] dark:hover:bg-neutral-800 hover:border-[#3FCD6B] dark:hover:border-neutral-700 hover:text-white dark:hover:text-white group">
                                        <svg width="14" height="14" viewBox="0 0 814 1000" className="group-hover:invert dark:invert">
                                            <path d="M788.1 340.9c-5.8 4.5-108.2 62.2-108.2 190.5 0 148.4 130.3 200.9 134.2 202.2-.6 3.2-20.7 71.9-68.7 141.9-42.8 61.6-87.5 123.1-155.5 123.1s-85.5-39.5-164-39.5c-76 0-103.7 40.8-165.9 40.8s-105-57.8-155.5-127.4C46 790.7 0 663 0 541.8c0-207.5 135.4-317.3 268.5-317.3 70.1 0 128.4 46.4 172.5 46.4 42.3 0 109.2-49.9 190.5-49.9 30.8 0 108.2 2.6 168.6 75.3zm-234.6-172.9c32.1-38.2 54.7-91.4 54.7-144.7 0-7.4-.6-14.9-1.9-21.1-51.9 2-113.1 34.7-149.5 78.2-28.2 32.8-55.3 85.4-55.3 139.4 0 8 1.3 16 1.9 18.6 3.2.6 8.4 1.3 13.6 1.3 46.4 0 102.9-31.5 136.5-71.7z" className="fill-[#111]" />
                                        </svg>
                                        Apple
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ══════════════════════════════
                        SUCCESS PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'success' && (
                        <div>
                            <div className="pt-5 px-6 pb-2">
                                <div className="text-[17px] font-semibold text-[#111] dark:text-white mb-0.5">Account created!</div>
                            </div>

                            <div className="px-6 pb-5 text-center py-3">
                                <div className="w-12 h-12 rounded-full bg-[#E1F5EE] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 flex items-center justify-center mx-auto mb-3">
                                    <i className="ti ti-check text-[24px] text-[#0F5525] dark:text-[#3FCD6B]" />
                                </div>
                                <div className="text-[12px] text-[#888] dark:text-neutral-400 leading-relaxed mb-4">
                                    Your TrustBridge account is ready. Redirecting you to your dashboard…
                                </div>
                                <div className="h-1 bg-[#eee] dark:bg-neutral-800 rounded-full overflow-hidden">
                                    <div className="h-full bg-[#3FCD6B] rounded-full transition-all duration-[2500ms] ease-linear" style={{ width: `${redirectProgress}%` }} />
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ══════════════════════════════
                        FORGOT PASSWORD PANEL
                    ══════════════════════════════ */}
                    {activePanel === 'forgot' && (
                        <div>
                            <div className="pt-5 px-6 pb-2">
                                <div className="text-[17px] font-semibold text-[#111] dark:text-white mb-0.5">Reset your password</div>
                                <div className="text-[12px] text-[#777] dark:text-neutral-400">Enter your email and we&#39;ll send a reset link</div>
                            </div>

                            <div className="px-6 pb-5">
                                <div className="mb-3">
                                    <label className="block text-[11px] font-medium text-[#333] dark:text-neutral-300 mb-1">Email address</label>
                                    <input type="email" placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)} className={inputCls} />
                                    <div className="text-[11px] text-[#999] dark:text-neutral-500 mt-1">We&#39;ll send a reset link if this email is registered.</div>
                                </div>

                                <button className="w-full bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 rounded-lg py-2 text-[13px] font-medium flex items-center justify-center gap-1.5 transition-colors hover:bg-[#3FCD6B] dark:hover:opacity-90">
                                    <i className="ti ti-send text-[14px]" /> Send reset link
                                </button>

                                <div onClick={() => setActivePanel('login')} className="text-[12px] text-[#aaa] dark:text-neutral-500 cursor-pointer flex items-center gap-1 mt-3 justify-center transition-colors hover:text-[#3FCD6B] dark:hover:text-[#3FCD6B]">
                                    <i className="ti ti-arrow-left text-[13px]" /> Back
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

function setError(message: any) {
    throw new Error("Function not implemented.");
}