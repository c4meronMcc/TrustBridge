"use client";

import React, { useState, useEffect } from 'react';

// ─── DATA INTERFACES ─────────────────────────────────────────────────────────

interface DashboardMetrics {
    trustScore: number;
    inEscrow: number;
    activeJobsCount: number;
    pendingRelease: number;
    paidOut: number;
    paidOutGrowth: number;
}

interface Activity {
    id: string;
    type: 'success' | 'info' | 'warning' | 'default';
    icon: string;
    title: string;
    subtitle: string;
    time: string;
    details: string;
}

interface EscrowJob {
    id: string;
    initials: string;
    name: string;
    client: string;
    progress: number;
    amount: number;
    status: 'Pending' | 'Funded' | 'In review' | 'Complete';
    details: {
        row1Label: string; row1Value: string;
        row2Label: string; row2Value: string;
        row3Label: string; row3Value: string;
    };
}

interface DashboardData {
    user: { firstName: string; lastName: string; role: string; initials: string };
    metrics: DashboardMetrics;
    activities: Activity[];
    jobs: EscrowJob[];
}

export default function TrustBridgeDashboard() {
    // ─── STATE ─────────────────────────────────────────────────────────────────
    const [data, setData] = useState<DashboardData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // UI State
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [openActivity, setOpenActivity] = useState<string | null>(null);
    const [openEscrow, setOpenEscrow] = useState<string | null>(null);
    const [chartRange, setChartRange] = useState('3M');

    // ─── DATA FETCHING (API READY) ─────────────────────────────────────────────
    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setIsLoading(true);

                // ==========================================
                // TODO: REPLACE WITH YOUR SPRING BOOT API
                // const response = await fetch('http://localhost:8080/api/dashboard', {
                //   headers: { 'Authorization': `Bearer ${token}` }
                // });
                // if (!response.ok) throw new Error('Failed to fetch data');
                // const apiData = await response.json();
                // setData(apiData);
                // ==========================================

                // Simulating API latency and response
                setTimeout(() => {
                    setData({
                        user: { firstName: 'Jamie', lastName: 'Sullivan', role: 'Freelancer', initials: 'JS' },
                        metrics: {
                            trustScore: 98, inEscrow: 9400, activeJobsCount: 2,
                            pendingRelease: 3250, paidOut: 6100, paidOutGrowth: 18
                        },
                        activities: [
                            { id: 'a1', type: 'success', icon: 'ti-check', title: 'Funds released — Branding project', subtitle: '£2,400 · Acme Studio', time: '9:14 am', details: 'Milestone 3 of 3 approved by Acme Studio. £2,400 transferred to your TrustBridge wallet. Available to withdraw instantly.' },
                            { id: 'a2', type: 'info', icon: 'ti-shield-lock', title: 'New job created', subtitle: '£3,250 · UX redesign phase 2 · Nova Labs', time: 'Yesterday', details: 'Job created with 2 milestones. Awaiting client deposit of £3,250 before work begins. Contract signed by both parties.' },
                            { id: 'a3', type: 'default', icon: 'ti-arrow-down', title: 'Escrow funded by client', subtitle: '£6,150 · Backend API build · Redwood Inc', time: '8 May', details: 'Redwood Inc deposited £6,150 into escrow via Stripe. Funds are held securely. Work is now authorised to begin on milestone 1.' },
                            { id: 'a4', type: 'warning', icon: 'ti-alert-triangle', title: 'Milestone review requested', subtitle: 'Pulse Analytics · Milestone 2', time: '7 May', details: 'Client has requested a review of milestone 2 deliverables. You have 5 days to respond before automatic escalation. Check your inbox for their notes.' }
                        ],
                        jobs: [
                            { id: 'e1', initials: 'NL', name: 'UX redesign phase 2', client: 'Nova Labs', progress: 25, amount: 3250, status: 'Pending', details: { row1Label: 'Milestone 1 of 2', row1Value: '25% complete', row2Label: 'Deposit', row2Value: 'Awaiting', row3Label: 'Deadline', row3Value: '28 May 2026' } },
                            { id: 'e2', initials: 'RI', name: 'Backend API build', client: 'Redwood Inc', progress: 60, amount: 6150, status: 'Funded', details: { row1Label: 'Milestone 2 of 3', row1Value: '60% complete', row2Label: 'Deposit', row2Value: 'Confirmed', row3Label: 'Deadline', row3Value: '15 Jun 2026' } },
                            { id: 'e3', initials: 'PA', name: 'Analytics dashboard', client: 'Pulse Analytics', progress: 80, amount: 4800, status: 'In review', details: { row1Label: 'Milestone 4 of 5', row1Value: '80% complete', row2Label: 'Status', row2Value: 'Client reviewing', row3Label: 'Deadline', row3Value: '20 May 2026' } },
                            { id: 'e4', initials: 'AS', name: 'Branding & identity', client: 'Acme Studio', progress: 100, amount: 2400, status: 'Complete', details: { row1Label: 'All milestones', row1Value: 'Approved', row2Label: 'Payout', row2Value: 'Released 10 May', row3Label: 'Audit log', row3Value: 'Immutable ✓' } }
                        ]
                    });
                    setIsLoading(false);
                }, 800);

            } catch (err: any) {
                setError(err.message);
                setIsLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    // ─── HELPER STYLES ─────────────────────────────────────────────────────────
    const getActivityStyles = (type: Activity['type']) => {
        switch (type) {
            case 'success': return 'bg-[#EAF3DE] text-[#3B6D11]';
            case 'info': return 'bg-[#E6F1FB] text-[#185FA5]';
            case 'warning': return 'bg-[#FAEEDA] text-[#854F0B]';
            default: return 'bg-[#E1F5EE] text-[#0F6E56]';
        }
    };

    const getJobBadgeStyles = (status: EscrowJob['status']) => {
        switch (status) {
            case 'Pending': return 'bg-[#FAEEDA] text-[#854F0B]';
            case 'Funded': return 'bg-[#E1F5EE] text-[#0F6E56]';
            case 'In review': return 'bg-[#E6F1FB] text-[#185FA5]';
            case 'Complete': return 'bg-[#EAF3DE] text-[#3B6D11]';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getJobDetailValueColor = (status: EscrowJob['status'], label: string) => {
        if (label === 'Deposit' && status === 'Pending') return 'text-[#BA7517]';
        if (label === 'Deposit' && status === 'Funded') return 'text-[#0F5525]';
        if (label === 'Status' && status === 'In review') return 'text-[#185FA5]';
        if ((label === 'Payout' || label === 'Audit log') && status === 'Complete') return 'text-[#0F5525]';
        return 'text-slate-900 font-medium';
    };

    if (isLoading) return <div className="h-screen flex items-center justify-center bg-[#f5f7f2]"><div className="animate-pulse flex flex-col items-center gap-3"><div className="w-10 h-10 bg-[#3FCD6B] rounded-lg"></div><div className="text-[#0F5525] font-medium text-sm">Loading Dashboard...</div></div></div>;
    if (error) return <div className="h-screen flex items-center justify-center bg-[#f5f7f2]"><div className="text-red-600 bg-red-50 p-4 rounded-lg">Error loading data: {error}</div></div>;
    if (!data) return null;

    return (
        <div className="flex h-screen bg-[#f5f7f2] font-sans text-slate-800 overflow-hidden selection:bg-[#3FCD6B]/30">

            {/* ── Mobile Sidebar Overlay ── */}
            {sidebarOpen && (
                <div className="fixed inset-0 bg-slate-900/40 z-40 lg:hidden backdrop-blur-sm transition-opacity" onClick={() => setSidebarOpen(false)} />
            )}

            {/* ── Sidebar ── */}
            <div className={`fixed inset-y-0 left-0 w-[240px] bg-[#0F5525] flex flex-col z-50 transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0 ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
                <div className="p-5 border-b border-white/10 flex items-center justify-between lg:justify-start gap-3">
                    <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 bg-[#3FCD6B] rounded-lg flex items-center justify-center shrink-0 shadow-sm">
                            <i className="ti ti-shield-check text-[#0F5525] text-lg"></i>
                        </div>
                        <span className="text-white text-[16px] font-semibold tracking-tight">TrustBridge</span>
                    </div>
                    <button className="lg:hidden text-white/70 hover:text-white" onClick={() => setSidebarOpen(false)}>
                        <i className="ti ti-x text-xl"></i>
                    </button>
                </div>

                <nav className="flex-1 py-4 overflow-y-auto hide-scrollbar">
                    <div className="px-3 mb-2">
                        <div className="text-[10px] text-white/40 uppercase tracking-wider px-3 pb-1.5 font-semibold">Overview</div>
                        <div className="flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer text-[13px] bg-[#3FCD6B]/15 text-[#3FCD6B] font-medium mb-1">
                            <i className="ti ti-layout-dashboard text-lg"></i> Dashboard
                        </div>
                        <div className="flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer text-[13px] text-white/70 hover:bg-white/5 hover:text-white transition-colors mb-1">
                            <i className="ti ti-shield-lock text-lg"></i> Jobs
                            <span className="ml-auto bg-[#3FCD6B] text-[#0F5525] text-[10px] font-semibold py-0.5 px-2 rounded-full shadow-sm">{data.metrics.activeJobsCount}</span>
                        </div>
                        <div className="flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer text-[13px] text-white/70 hover:bg-white/5 hover:text-white transition-colors">
                            <i className="ti ti-clock text-lg"></i> Activity
                        </div>
                    </div>

                    <div className="px-3 mb-2 mt-5">
                        <div className="text-[10px] text-white/40 uppercase tracking-wider px-3 pb-1.5 font-semibold">Finance</div>
                        <div className="flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer text-[13px] text-white/70 hover:bg-white/5 hover:text-white transition-colors mb-1">
                            <i className="ti ti-wallet text-lg"></i> Balance
                        </div>
                        <div className="flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer text-[13px] text-white/70 hover:bg-white/5 hover:text-white transition-colors mb-1">
                            <i className="ti ti-receipt text-lg"></i> Invoices
                        </div>
                        <div className="flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer text-[13px] text-white/70 hover:bg-white/5 hover:text-white transition-colors">
                            <i className="ti ti-arrow-up-right text-lg"></i> Withdrawals
                        </div>
                    </div>
                </nav>

                <div className="p-4 border-t border-white/10 flex items-center gap-3 cursor-pointer hover:bg-white/5 transition-colors">
                    <div className="w-9 h-9 rounded-full bg-[#3FCD6B] flex items-center justify-center text-xs font-semibold text-[#0F5525] shrink-0 shadow-sm">{data.user.initials}</div>
                    <div className="flex-1 min-w-0">
                        <div className="text-[13px] text-white font-medium truncate">{data.user.firstName} {data.user.lastName}</div>
                        <div className="text-[11px] text-white/50 truncate">{data.user.role}</div>
                    </div>
                    <i className="ti ti-chevron-right text-white/30 text-sm"></i>
                </div>
            </div>

            {/* ── Main Content Area ── */}
            <div className="flex-1 flex flex-col overflow-hidden w-full">

                {/* Topbar */}
                <div className="bg-white border-b border-slate-200 px-4 lg:px-8 py-3.5 flex items-center justify-between z-10 shrink-0">
                    <div className="flex items-center gap-3 lg:gap-0">
                        <button className="lg:hidden w-8 h-8 flex items-center justify-center text-slate-500 hover:text-slate-800 rounded-md hover:bg-slate-50" onClick={() => setSidebarOpen(true)}>
                            <i className="ti ti-menu-2 text-xl"></i>
                        </button>
                        <div>
                            <div className="text-[15px] font-semibold text-slate-900 tracking-tight">Good morning, {data.user.firstName} 👋</div>
                            <div className="text-[12px] text-slate-500 mt-0.5 font-medium">Sunday, 10 May 2026</div>
                        </div>
                    </div>
                    <div className="flex items-center gap-2.5">
                        <button className="hidden sm:flex w-9 h-9 rounded-lg border border-slate-200 bg-white items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-slate-700 transition-colors shadow-sm">
                            <i className="ti ti-search text-[17px]"></i>
                        </button>
                        <button className="relative w-9 h-9 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-slate-700 transition-colors shadow-sm">
                            <i className="ti ti-bell text-[17px]"></i>
                            <span className="absolute top-2 right-2.5 w-1.5 h-1.5 rounded-full bg-[#3FCD6B] ring-2 ring-white"></span>
                        </button>
                        <button className="bg-[#0F5525] hover:bg-[#3FCD6B] text-white border-none rounded-lg py-2 px-4 text-[13px] font-semibold flex items-center gap-1.5 transition-colors shadow-sm">
                            <i className="ti ti-plus text-[15px]"></i> <span className="hidden sm:inline">New job</span>
                        </button>
                    </div>
                </div>

                {/* Scrollable Dashboard Body */}
                <div className="flex-1 p-4 lg:p-8 flex flex-col gap-4 lg:gap-5 overflow-y-auto">

                    {/* Top Row: Graph & Trust Score */}
                    <div className="grid grid-cols-1 lg:grid-cols-[1fr_240px] gap-4 lg:gap-5">
                        {/* Graph Area */}
                        <div className="bg-white rounded-xl border border-slate-200 p-4 lg:p-5 shadow-sm">
                            <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                                <span className="text-[14px] font-semibold text-slate-800">Earnings over time</span>
                                <div className="flex gap-1 bg-slate-50 p-1 rounded-lg border border-slate-100">
                                    {['1M', '3M', '6M', '1Y'].map(range => (
                                        <button key={range} onClick={() => setChartRange(range)} className={`text-[11px] px-3 py-1.5 rounded-md cursor-pointer font-medium transition-colors ${chartRange === range ? 'bg-white text-[#0F5525] shadow-sm ring-1 ring-slate-200' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-100'}`}>
                                            {range}
                                        </button>
                                    ))}
                                </div>
                            </div>
                            <div className="relative h-[120px] w-full mt-2">
                                <svg className="w-full h-full overflow-visible" viewBox="0 0 380 90" preserveAspectRatio="none">
                                    <defs>
                                        <linearGradient id="gfill" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%" stopColor="#3FCD6B" stopOpacity="0.2"/>
                                            <stop offset="100%" stopColor="#3FCD6B" stopOpacity="0"/>
                                        </linearGradient>
                                    </defs>
                                    <path d="M0,72 C20,68 35,60 55,55 C75,50 85,58 105,48 C125,38 135,42 155,32 C175,22 185,30 205,22 C225,14 240,18 260,12 C280,6 295,14 315,8 C335,4 355,10 380,6" fill="none" stroke="#3FCD6B" strokeWidth="2.5"/>
                                    <path d="M0,72 C20,68 35,60 55,55 C75,50 85,58 105,48 C125,38 135,42 155,32 C175,22 185,30 205,22 C225,14 240,18 260,12 C280,6 295,14 315,8 C335,4 355,10 380,6 L380,90 L0,90 Z" fill="url(#gfill)"/>
                                    <line x1="0" y1="90" x2="380" y2="90" stroke="#e2e8f0" strokeWidth="1"/>
                                    <text x="0" y="105" fontSize="10" fill="#64748b" fontWeight="500">Mar</text>
                                    <text x="120" y="105" fontSize="10" fill="#64748b" fontWeight="500">Apr</text>
                                    <text x="248" y="105" fontSize="10" fill="#64748b" fontWeight="500">May</text>
                                    <circle cx="380" cy="6" r="4" fill="#fff" stroke="#3FCD6B" strokeWidth="2"/>
                                    <text x="345" y="-5" fontSize="11" fill="#0F5525" fontWeight="600">£{data.metrics.paidOut.toLocaleString()}</text>
                                </svg>
                            </div>
                        </div>

                        {/* Trust Score Area */}
                        <div className="bg-[#0F5525] rounded-xl p-5 flex flex-col justify-between shadow-sm relative overflow-hidden">
                            {/* Soft decorative gradient matching user aesthetic preference */}
                            <div className="absolute top-0 right-0 w-32 h-32 bg-[#3FCD6B] opacity-10 blur-2xl rounded-full translate-x-10 -translate-y-10"></div>

                            <div className="relative z-10">
                                <div className="text-[12px] text-white/70 font-medium">Trust score</div>
                                <div className="text-[42px] font-semibold text-[#3FCD6B] tracking-tight leading-none my-2">{data.metrics.trustScore}</div>
                                <div className="text-[11px] text-white/60 flex items-center gap-1.5 font-medium">
                                    <i className="ti ti-shield-check text-[14px] text-[#3FCD6B]"></i> Verified freelancer
                                </div>
                            </div>
                            <div className="mt-5 relative z-10">
                                <div className="bg-black/20 rounded-full h-1.5 w-full overflow-hidden">
                                    <div className="bg-[#3FCD6B] h-full rounded-full transition-all duration-1000 ease-out" style={{ width: `${data.metrics.trustScore}%` }}></div>
                                </div>
                                <div className="flex justify-between mt-2">
                                    <span className="text-[10px] text-white/40 font-medium">0</span>
                                    <span className="text-[10px] text-white/40 font-medium">100</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Metrics Row */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 lg:gap-5">
                        <div className="bg-white rounded-xl border border-slate-200 p-4 lg:p-5 shadow-sm transition-shadow hover:shadow-md">
                            <div className="text-[12px] font-medium text-slate-500 mb-1">In escrow</div>
                            <div className="text-[24px] font-semibold text-slate-900 tracking-tight">£{data.metrics.inEscrow.toLocaleString()}</div>
                            <div className="text-[12px] mt-2 flex items-center gap-1.5 text-[#0F5525] font-medium bg-[#E1F5EE] w-fit px-2 py-0.5 rounded-md">
                                <i className="ti ti-shield-lock text-[13px]"></i> {data.metrics.activeJobsCount} active jobs
                            </div>
                        </div>
                        <div className="bg-white rounded-xl border border-slate-200 p-4 lg:p-5 shadow-sm transition-shadow hover:shadow-md">
                            <div className="text-[12px] font-medium text-slate-500 mb-1">Pending release</div>
                            <div className="text-[24px] font-semibold text-slate-900 tracking-tight">£{data.metrics.pendingRelease.toLocaleString()}</div>
                            <div className="text-[12px] mt-2 flex items-center gap-1.5 text-[#BA7517] font-medium bg-[#FAEEDA] w-fit px-2 py-0.5 rounded-md">
                                <i className="ti ti-clock text-[13px]"></i> Awaiting approval
                            </div>
                        </div>
                        <div className="bg-white rounded-xl border border-slate-200 p-4 lg:p-5 shadow-sm transition-shadow hover:shadow-md">
                            <div className="text-[12px] font-medium text-slate-500 mb-1">Paid out (May)</div>
                            <div className="text-[24px] font-semibold text-slate-900 tracking-tight">£{data.metrics.paidOut.toLocaleString()}</div>
                            <div className="text-[12px] mt-2 flex items-center gap-1.5 text-[#0F5525] font-medium bg-[#E1F5EE] w-fit px-2 py-0.5 rounded-md">
                                <i className="ti ti-trending-up text-[13px]"></i> +{data.metrics.paidOutGrowth}% vs Apr
                            </div>
                        </div>
                    </div>

                    {/* Bottom Grid: Activity & Active Jobs */}
                    <div className="grid grid-cols-1 xl:grid-cols-[1fr_320px] gap-4 lg:gap-5 pb-4">

                        {/* Activity Feed */}
                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden h-fit">
                            <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
                <span className="text-[13px] font-semibold text-slate-800 flex items-center gap-2">
                  <div className="p-1 bg-[#E1F5EE] rounded text-[#0F5525]"><i className="ti ti-clock text-[15px]"></i></div> Recent activity
                </span>
                                <button className="text-[12px] font-medium text-[#0F5525] flex items-center gap-1 hover:text-[#3FCD6B] transition-colors">
                                    View all <i className="ti ti-arrow-right text-[13px]"></i>
                                </button>
                            </div>
                            <div className="flex flex-col divide-y divide-slate-100">
                                {data.activities.map((act) => (
                                    <div key={act.id} className="group">
                                        <div className="flex items-start gap-3.5 p-4 cursor-pointer hover:bg-slate-50 transition-colors" onClick={() => setOpenActivity(openActivity === act.id ? null : act.id)}>
                                            <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 shadow-sm ${getActivityStyles(act.type)}`}>
                                                <i className={`ti ${act.icon} text-[15px]`}></i>
                                            </div>
                                            <div className="flex-1 min-w-0 pt-0.5">
                                                <div className="text-[13px] font-semibold text-slate-800 truncate group-hover:text-[#0F5525] transition-colors">{act.title}</div>
                                                <div className="text-[12px] text-slate-500 mt-0.5 truncate">{act.subtitle}</div>
                                                <div className="text-[11px] text-[#0F5525] mt-2 flex items-center gap-1 font-semibold opacity-80 group-hover:opacity-100 transition-opacity">
                                                    <i className={`ti ti-chevron-down text-[13px] transition-transform duration-200 ${openActivity === act.id ? 'rotate-180' : ''}`}></i>
                                                    {openActivity === act.id ? 'Hide details' : 'View details'}
                                                </div>
                                            </div>
                                            <div className="text-[11px] font-medium text-slate-400 shrink-0 pt-1">{act.time}</div>
                                        </div>

                                        {openActivity === act.id && (
                                            <div className="bg-slate-50/80 px-4 py-3.5 pl-[62px] text-[12.5px] text-slate-600 leading-relaxed border-t border-slate-100/50 shadow-inner shadow-slate-100/50">
                                                {act.details}
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Active Jobs (Escrow) */}
                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden h-fit">
                            <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
                <span className="text-[13px] font-semibold text-slate-800 flex items-center gap-2">
                  <div className="p-1 bg-[#E1F5EE] rounded text-[#0F5525]"><i className="ti ti-briefcase text-[15px]"></i></div> Active jobs
                </span>
                            </div>
                            <div className="flex flex-col divide-y divide-slate-100">
                                {data.jobs.map((job) => (
                                    <div key={job.id} className="group">
                                        <div className="flex items-center gap-3 p-4 cursor-pointer hover:bg-slate-50 transition-colors" onClick={() => setOpenEscrow(openEscrow === job.id ? null : job.id)}>
                                            <div className="w-9 h-9 rounded-full bg-[#E1F5EE] text-[#0F6E56] flex items-center justify-center text-[11px] font-bold tracking-wide shrink-0 shadow-sm">{job.initials}</div>
                                            <div className="flex-1 min-w-0">
                                                <div className="text-[13px] font-semibold text-slate-800 truncate group-hover:text-[#0F5525] transition-colors">{job.name}</div>
                                                <div className="text-[11px] font-medium text-slate-500 truncate mt-0.5">{job.client}</div>
                                                <div className="h-1.5 bg-slate-100 rounded-full mt-2.5 overflow-hidden">
                                                    <div className="h-full bg-[#3FCD6B] rounded-full transition-all duration-700 ease-out relative" style={{ width: `${job.progress}%` }}>
                                                        {/* Subtle shimmer effect on progress bar */}
                                                        <div className="absolute top-0 inset-x-0 h-full bg-gradient-to-r from-transparent via-white/30 to-transparent -translate-x-full animate-[shimmer_2s_infinite]"></div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="text-right shrink-0 flex flex-col items-end">
                                                <div className="text-[13px] font-bold text-slate-800 tracking-tight">£{job.amount.toLocaleString()}</div>
                                                <span className={`inline-block text-[10px] px-2 py-0.5 rounded-md font-semibold mt-1.5 shadow-sm ${getJobBadgeStyles(job.status)}`}>{job.status}</span>
                                            </div>
                                            <i className={`ti ti-chevron-down text-slate-400 text-[15px] ml-1 transition-transform duration-200 ${openEscrow === job.id ? 'rotate-180' : ''}`}></i>
                                        </div>

                                        {openEscrow === job.id && (
                                            <div className="px-5 py-3 pl-[64px] bg-slate-50/80 text-[11.5px] border-t border-slate-100/50 shadow-inner shadow-slate-100/50">
                                                <div className="flex justify-between py-1.5 border-b border-slate-200/70">
                                                    <span className="text-slate-500">{job.details.row1Label}</span>
                                                    <span className="font-semibold text-slate-800">{job.details.row1Value}</span>
                                                </div>
                                                <div className="flex justify-between py-1.5 border-b border-slate-200/70">
                                                    <span className="text-slate-500">{job.details.row2Label}</span>
                                                    <span className={`font-semibold ${getJobDetailValueColor(job.status, job.details.row2Label)}`}>{job.details.row2Value}</span>
                                                </div>
                                                <div className="flex justify-between py-1.5">
                                                    <span className="text-slate-500">{job.details.row3Label}</span>
                                                    <span className={`font-semibold ${getJobDetailValueColor(job.status, job.details.row3Label)}`}>{job.details.row3Value}</span>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>

                    </div>
                </div>
            </div>

            {/* Global styles for the shimmer effect */}
            <style dangerouslySetInnerHTML={{__html: `
        @keyframes shimmer { 100% { transform: translateX(100%); } }
        .hide-scrollbar::-webkit-scrollbar { display: none; }
        .hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
      `}} />
        </div>
    );
}