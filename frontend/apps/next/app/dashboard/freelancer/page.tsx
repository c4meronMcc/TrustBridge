"use client";

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';

// ─── ENVIRONMENT ──────────────────────────────────────────────────────────────
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

// ─── DATA INTERFACES ─────────────────────────────────────────────────────────

interface EarningDataPointDto {
    month: string; // ISO format: "2025-01"
    total: number;
}

interface AuditLogEntryDto {
    eventId: string;
    eventType: 'FUNDS_RELEASED' | 'ESCROW_FUNDED' | 'AWAITING_PAYMENT' | 'JOB_CREATED' | 'MILESTONE_APPROVED';
    description: string;
    amountGbp: number;
    relatedPartyName: string;
    occurredAt: string; // ISO timestamp
}

interface JobSummaryDto {
    jobId: string;
    title: string;
    clientName: string;
    totalJobAmount: number;
    progressPercentage: number;
    status: string;
    currentMilestone: string;
    depositStatus: string;
    deadline: string;
}

interface DashboardData {
    firstName: string;
    lastName: string;
    trustScore: number;
    fundsInEscrowHolding: number;
    fundsPending: number;
    fundsPaidOut: number;
    earningsChart: EarningDataPointDto[] | null;
    activeJobs: JobSummaryDto[];
    awaitingPaymentJobs: JobSummaryDto[];
    recentlyCompletedJobs: JobSummaryDto[];
    recentActivity: AuditLogEntryDto[];
}

// ─── CHART TYPES ──────────────────────────────────────────────────────────────

interface ChartPoint {
    x: number;
    y: number;
    month: string;
    total: number;
    monthlyEarning: number;
}

interface ChartResult {
    path: string;
    fill: string;
    points: ChartPoint[];
    periodTotal: number;
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────

const getJobBadgeStyles = (status: string): string => {
    switch (status) {
        case 'AWAITING_PAYMENT': return 'bg-[#FAEEDA] text-[#854F0B] dark:bg-transparent dark:border dark:border-amber-500/30 dark:text-amber-500';
        case 'IN_PROGRESS':     return 'bg-[#E1F5EE] text-[#0F6E56] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 dark:text-[#3FCD6B]';
        case 'IN_REVIEW':
        case 'SUBMITTED':       return 'bg-[#E6F1FB] text-[#185FA5] dark:bg-transparent dark:border dark:border-blue-500/30 dark:text-blue-400';
        case 'PAID_OUT':
        case 'COMPLETED':       return 'bg-[#EAF3DE] text-[#3B6D11] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 dark:text-[#3FCD6B]';
        default:                return 'bg-gray-100 text-gray-700 dark:bg-transparent dark:border dark:border-neutral-700 dark:text-neutral-400';
    }
};

const getActivityConfig = (eventType: AuditLogEntryDto['eventType']) => {
    switch (eventType) {
        case 'FUNDS_RELEASED':
        case 'MILESTONE_APPROVED':
            return { style: 'bg-[#EAF3DE] text-[#3B6D11] dark:bg-transparent dark:border dark:border-neutral-800 dark:text-[#3FCD6B]', icon: 'ti-check', label: 'Funds released' };
        case 'ESCROW_FUNDED':
        case 'JOB_CREATED':
            return { style: 'bg-[#E6F1FB] text-[#185FA5] dark:bg-transparent dark:border dark:border-neutral-800 dark:text-blue-400', icon: 'ti-shield-lock', label: 'Escrow funded' };
        case 'AWAITING_PAYMENT':
            return { style: 'bg-[#FAEEDA] text-[#854F0B] dark:bg-transparent dark:border dark:border-neutral-800 dark:text-amber-500', icon: 'ti-alert-triangle', label: 'Action required' };
        default:
            return { style: 'bg-[#E1F5EE] text-[#0F6E56] dark:bg-transparent dark:border dark:border-neutral-800 dark:text-[#3FCD6B]', icon: 'ti-info-circle', label: 'Update' };
    }
};

const formatRelativeTime = (isoTimestamp: string): string => {
    const diff = Date.now() - new Date(isoTimestamp).getTime();
    const hours = Math.floor(diff / 3_600_000);
    if (hours < 1)  return 'Just now';
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days === 1) return 'Yesterday';
    if (days < 7)   return `${days}d ago`;
    return new Date(isoTimestamp).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
};

const formatCurrency = (amount: number): string =>
    `£${(amount ?? 0).toLocaleString('en-GB', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;

// ─── SUB-COMPONENTS ───────────────────────────────────────────────────────────

interface StatCardProps {
    label: string;
    value: string;
    badge: React.ReactNode;
}

const StatCard: React.FC<StatCardProps> = ({ label, value, badge }) => (
    <div className="bg-white dark:bg-transparent rounded-2xl border border-slate-200 dark:border-neutral-800 p-5 shadow-sm dark:shadow-none hover:border-slate-300 dark:hover:border-neutral-700 transition-colors duration-200">
        <div className="text-[12px] font-semibold text-slate-500 dark:text-neutral-400 uppercase tracking-wider mb-1.5">{label}</div>
        <div className="text-[26px] font-bold text-slate-900 dark:text-white tracking-tight leading-none">{value}</div>
        <div className="mt-3">{badge}</div>
    </div>
);

interface StatusBadgeProps {
    className: string;
    icon: string;
    children: React.ReactNode;
}

const StatusBadge: React.FC<StatusBadgeProps> = ({ className, icon, children }) => (
    <span className={`inline-flex items-center gap-1.5 text-[11px] px-2.5 py-1 rounded-lg font-semibold ${className}`}>
        <i className={`ti ${icon} text-[12px]`}></i>
        {children}
    </span>
);

// ─── MAIN DASHBOARD ───────────────────────────────────────────────────────────

export default function TrustBridgeDashboard() {
    const router = useRouter();

    const [data, setData]             = useState<DashboardData | null>(null);
    const [isLoading, setIsLoading]   = useState(true);
    const [error, setError]           = useState<string | null>(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [openActivity, setOpenActivity] = useState<string | null>(null);
    const [openEscrow, setOpenEscrow] = useState<string | null>(null);
    const [chartRange, setChartRange] = useState<'1M' | '3M' | '6M' | '1Y'>('3M');
    const [hoverIndex, setHoverIndex] = useState<number | null>(null);
    const [loggingOut, setLoggingOut] = useState(false);
    const svgRef = useRef<SVGSVGElement>(null);

    // ── DATA FETCH ────────────────────────────────────────────────────────────
    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setIsLoading(true);

                const response = await fetch(`${API_BASE}/api/dashboard/freelancer`, {
                    method: 'GET',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                });

                if (response.status === 401) {
                    router.push('/login');
                    return;
                }

                if (!response.ok) {
                    throw new Error(`Server returned ${response.status}: ${response.statusText}`);
                }

                const apiData: DashboardData = await response.json();
                setData(apiData);

            } catch (err) {
                console.error("Fetch error:", err);
                setError(err instanceof Error ? err.message : 'An unexpected error occurred');
            } finally {
                setIsLoading(false);
            }
        };

        fetchDashboardData();
    }, [router]);

    // ── LOGOUT ────────────────────────────────────────────────────────────────
    const handleLogout = useCallback(async () => {
        if (loggingOut) return;
        setLoggingOut(true);
        try {
            await fetch(`${API_BASE}/api/auth/logout`, {
                method: 'POST',
                credentials: 'include',
            });
        } catch {
            // Session expiry on server is best-effort; always redirect
        } finally {
            router.push('/login');
        }
    }, [loggingOut, router]);

    // ── CHART GENERATION ──────────────────────────────────────────────────────
    const generateChart = useCallback((earningsChart: EarningDataPointDto[] | null): ChartResult => {
        const WIDTH    = 800;
        const HEIGHT   = 200;
        const BASELINE = 155;
        const PAD_X    = 40;
        const INNER_W  = WIDTH - PAD_X * 2;

        const monthsToShow = chartRange === '1M' ? 1 : chartRange === '3M' ? 3 : chartRange === '6M' ? 6 : 12;

        const timeline: string[] = [];
        const now = new Date();
        for (let i = Math.max(monthsToShow - 1, 1); i >= 0; i--) {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            timeline.push(d.toLocaleString('en-GB', { month: 'short' }));
        }

        const flat: ChartResult = {
            path: `M${PAD_X},${BASELINE} L${WIDTH - PAD_X},${BASELINE}`,
            fill: `M${PAD_X},${BASELINE} L${WIDTH - PAD_X},${BASELINE} L${WIDTH - PAD_X},${HEIGHT} L${PAD_X},${HEIGHT} Z`,
            points: [],
            periodTotal: 0,
        };

        const parseBackendMonth = (isoMonth: string): string => {
            const [year, month] = isoMonth.split('-').map(Number);
            if (!year || !month) return '';
            return new Date(year, month - 1, 1).toLocaleString('en-GB', { month: 'short' });
        };

        let running     = 0;
        let periodTotal = 0;

        const paddedData = timeline.map(label => {
            const backendPoint = (earningsChart ?? []).find(
                d => parseBackendMonth(d.month) === label
            );
            const monthly = backendPoint?.total ?? 0;
            running     += monthly;
            periodTotal += monthly;
            return { month: label, monthly, cumulative: running };
        });

        if (running === 0) return { ...flat, periodTotal: 0 };

        const maxVal = Math.max(...paddedData.map(d => d.cumulative), 1);

        const points: ChartPoint[] = paddedData.map((d, i) => ({
            x: paddedData.length === 1
                ? WIDTH / 2
                : PAD_X + (i / (paddedData.length - 1)) * INNER_W,
            y: BASELINE - (d.cumulative / maxVal) * 110,
            month: d.month,
            total: d.cumulative,
            monthlyEarning: d.monthly,
        }));

        let path = `M${points[0].x},${points[0].y}`;
        for (let i = 1; i < points.length; i++) {
            const prev = points[i - 1];
            const curr = points[i];
            const cpX = (prev.x + curr.x) / 2;
            path += ` C${cpX},${prev.y} ${cpX},${curr.y} ${curr.x},${curr.y}`;
        }

        const fill = `${path} L${points[points.length - 1].x},${BASELINE} L${points[0].x},${BASELINE} Z`;

        return { path, fill, points, periodTotal };
    }, [chartRange]);

    // ── CHART HOVER ───────────────────────────────────────────────────────────
    const handleChartMouseMove = useCallback((e: React.MouseEvent<SVGSVGElement>) => {
        if (!svgRef.current) return;
        const rect   = svgRef.current.getBoundingClientRect();
        const scaleX = 800 / rect.width;
        const mouseX = (e.clientX - rect.left) * scaleX;

        const chart = generateChart(data?.earningsChart ?? null);
        if (!chart.points.length) return;

        let closestIdx = 0;
        let minDiff    = Infinity;
        chart.points.forEach((pt, idx) => {
            const diff = Math.abs(pt.x - mouseX);
            if (diff < minDiff) { minDiff = diff; closestIdx = idx; }
        });
        setHoverIndex(closestIdx);
    }, [data?.earningsChart, generateChart]);

    // ── LOADING / ERROR STATES ────────────────────────────────────────────────
    if (isLoading) return (
        <div className="h-screen flex items-center justify-center bg-[#f4f6f1] dark:bg-neutral-950">
            <div className="flex flex-col items-center gap-4">
                <div className="w-12 h-12 bg-[#0F5525] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-2xl flex items-center justify-center shadow-lg dark:shadow-none animate-pulse">
                    <i className="ti ti-shield-check text-[#3FCD6B] text-2xl"></i>
                </div>
                <div className="text-[#0F5525] dark:text-[#3FCD6B] font-semibold text-sm tracking-wide">Loading your dashboard…</div>
                <div className="flex gap-1.5">
                    {[0, 1, 2].map(i => (
                        <div key={i} className="w-1.5 h-1.5 bg-[#3FCD6B] rounded-full animate-bounce" style={{ animationDelay: `${i * 150}ms` }} />
                    ))}
                </div>
            </div>
        </div>
    );

    if (error) return (
        <div className="h-screen flex items-center justify-center bg-[#f4f6f1] dark:bg-neutral-950 p-4">
            <div className="bg-white dark:bg-transparent dark:border-neutral-800 rounded-2xl border border-red-100 shadow-sm dark:shadow-none p-8 max-w-md w-full text-center">
                <div className="w-12 h-12 bg-red-50 dark:bg-transparent dark:border dark:border-red-500/30 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <i className="ti ti-alert-triangle text-red-500 text-2xl"></i>
                </div>
                <div className="text-slate-900 dark:text-white font-semibold text-lg mb-2">Unable to load dashboard</div>
                <div className="text-slate-500 dark:text-neutral-400 text-sm mb-6">{error}</div>
                <button
                    onClick={() => window.location.reload()}
                    className="bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 rounded-xl py-2.5 px-6 text-sm font-semibold hover:bg-[#1a7a38] dark:hover:opacity-90 transition-opacity"
                >
                    Try again
                </button>
            </div>
        </div>
    );

    if (!data) return null;

    const rawJobs = [
        ...(data.activeJobs ?? []),
        ...(data.awaitingPaymentJobs ?? []),
        ...(data.recentlyCompletedJobs ?? []),
    ];

    const allJobs = Array.from(
        new Map(rawJobs.map(job => [job.jobId, job])).values()
    );

    const chart = generateChart(data.earningsChart);

    const userInitials = data.firstName && data.lastName
        ? `${data.firstName.charAt(0)}${data.lastName.charAt(0)}`.toUpperCase()
        : 'TB';

    const greeting = (() => {
        const h = new Date().getHours();
        if (h < 12) return 'Good morning';
        if (h < 18) return 'Good afternoon';
        return 'Good evening';
    })();

    const clampTooltipX = (x: number) => Math.max(50, Math.min(x, 750));

    return (
        <div className="flex h-screen bg-[#f4f6f1] dark:bg-neutral-950 font-sans text-slate-800 dark:text-neutral-200 overflow-hidden selection:bg-[#3FCD6B]/30">

            {/* Mobile overlay */}
            {sidebarOpen && (
                <div
                    className="fixed inset-0 bg-slate-900/50 dark:bg-black/80 z-40 lg:hidden backdrop-blur-sm"
                    onClick={() => setSidebarOpen(false)}
                    aria-hidden="true"
                />
            )}

            {/* ── SIDEBAR ── */}
            <aside
                className={`fixed inset-y-0 left-0 w-[248px] bg-[#0A3D1A] dark:bg-neutral-900 dark:border-r dark:border-neutral-800 flex flex-col z-50 transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0 ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}
                aria-label="Primary navigation"
            >
                <div className="p-5 border-b border-white/10 dark:border-neutral-800 flex items-center justify-between lg:justify-start gap-3">
                    <div className="flex items-center gap-2.5">
                        <div className="w-9 h-9 bg-[#3FCD6B] rounded-xl flex items-center justify-center shrink-0 shadow-md dark:shadow-none">
                            <i className="ti ti-shield-check text-[#0A3D1A] dark:text-neutral-950 text-xl"></i>
                        </div>
                        <span className="text-white text-[17px] font-bold tracking-tight">TrustBridge</span>
                    </div>
                    <button
                        className="lg:hidden text-white/60 hover:text-white p-1 rounded-lg hover:bg-white/10 dark:hover:bg-neutral-800 transition-colors"
                        onClick={() => setSidebarOpen(false)}
                        aria-label="Close sidebar"
                    >
                        <i className="ti ti-x text-lg"></i>
                    </button>
                </div>

                <nav className="flex-1 py-5 overflow-y-auto">
                    <div className="px-3 space-y-0.5">
                        <div className="text-[10px] text-white/35 dark:text-neutral-500 uppercase tracking-widest px-3 pb-2 font-bold">Workspace</div>

                        <div className="flex items-center gap-3 py-2.5 px-3 rounded-xl cursor-pointer text-[13px] bg-[#3FCD6B]/20 dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 text-[#3FCD6B] font-semibold">
                            <i className="ti ti-layout-dashboard text-lg"></i>
                            Dashboard
                        </div>

                        {[
                            { icon: 'ti-briefcase', label: 'Jobs', count: data.activeJobs?.length ?? 0 },
                            { icon: 'ti-file-invoice', label: 'Invoices', count: null },
                            { icon: 'ti-wallet', label: 'Wallet', count: null },
                        ].map(item => (
                            <div
                                key={item.label}
                                className="flex items-center gap-3 py-2.5 px-3 rounded-xl cursor-pointer text-[13px] text-white/60 dark:text-neutral-400 hover:bg-white/8 dark:hover:bg-neutral-800 hover:text-white dark:hover:text-neutral-200 transition-colors"
                            >
                                <i className={`ti ${item.icon} text-lg`}></i>
                                {item.label}
                                {item.count !== null && item.count > 0 && (
                                    <span className="ml-auto bg-[#3FCD6B] text-[#0A3D1A] dark:text-neutral-900 text-[10px] font-bold py-0.5 px-2 rounded-full">
                                        {item.count}
                                    </span>
                                )}
                            </div>
                        ))}

                        <div className="text-[10px] text-white/35 dark:text-neutral-500 uppercase tracking-widest px-3 pt-5 pb-2 font-bold">Account</div>

                        {[
                            { icon: 'ti-user', label: 'Profile' },
                            { icon: 'ti-settings', label: 'Settings' },
                        ].map(item => (
                            <div
                                key={item.label}
                                className="flex items-center gap-3 py-2.5 px-3 rounded-xl cursor-pointer text-[13px] text-white/60 dark:text-neutral-400 hover:bg-white/8 dark:hover:bg-neutral-800 hover:text-white dark:hover:text-neutral-200 transition-colors"
                            >
                                <i className={`ti ${item.icon} text-lg`}></i>
                                {item.label}
                            </div>
                        ))}
                    </div>
                </nav>

                <button
                    className="p-4 border-t border-white/10 dark:border-neutral-800 flex items-center gap-3 hover:bg-white/5 dark:hover:bg-neutral-800 transition-colors w-full text-left disabled:opacity-60"
                    onClick={handleLogout}
                    disabled={loggingOut}
                    aria-label="Log out"
                >
                    <div className="w-9 h-9 rounded-full bg-[#3FCD6B] flex items-center justify-center text-[12px] font-bold text-[#0A3D1A] dark:text-neutral-900 shrink-0 shadow-sm dark:shadow-none">
                        {userInitials}
                    </div>
                    <div className="flex-1 min-w-0 text-left">
                        <div className="text-[13px] text-white font-semibold truncate">
                            {data.firstName} {data.lastName}
                        </div>
                        <div className="text-[11px] text-white/45 dark:text-neutral-500 truncate">
                            {loggingOut ? 'Signing out…' : 'Click to log out'}
                        </div>
                    </div>
                    <i className="ti ti-logout text-white/30 dark:text-neutral-600 text-sm shrink-0"></i>
                </button>
            </aside>

            {/* ── MAIN CONTENT ── */}
            <div className="flex-1 flex flex-col overflow-hidden min-w-0">

                {/* Topbar */}
                <header className="bg-white dark:bg-neutral-950 border-b border-slate-200 dark:border-neutral-800 px-4 lg:px-8 py-4 flex items-center justify-between z-10 shrink-0 shadow-sm dark:shadow-none">
                    <div className="flex items-center gap-3">
                        <button
                            className="lg:hidden w-9 h-9 flex items-center justify-center text-slate-500 dark:text-neutral-400 hover:text-slate-800 dark:hover:text-white rounded-xl hover:bg-slate-100 dark:hover:bg-neutral-900 transition-colors"
                            onClick={() => setSidebarOpen(true)}
                            aria-label="Open sidebar"
                        >
                            <i className="ti ti-menu-2 text-xl"></i>
                        </button>
                        <div>
                            <div className="text-[15px] font-bold text-slate-900 dark:text-white tracking-tight">
                                {greeting}, {data.firstName} 👋
                            </div>
                            <div className="text-[12px] text-slate-400 dark:text-neutral-500 mt-0.5 font-medium">
                                {new Date().toLocaleDateString('en-GB', {
                                    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
                                })}
                            </div>
                        </div>
                    </div>
                    <div className="flex items-center gap-2">
                        <button className="hidden sm:flex items-center gap-2 text-[13px] font-semibold text-slate-600 dark:text-neutral-300 border border-slate-200 dark:border-neutral-800 rounded-xl py-2 px-4 hover:bg-slate-50 dark:hover:bg-neutral-900 transition-colors">
                            <i className="ti ti-bell text-[15px]"></i>
                            <span>Notifications</span>
                        </button>
                        <button onClick={() => router.push('/dashboard/freelancer/jobs/new')} className="bg-[#0F5525] dark:bg-[#3FCD6B] hover:bg-[#1a7a38] dark:hover:opacity-90 text-white dark:text-neutral-950 rounded-xl py-2 px-4 text-[13px] font-semibold flex items-center gap-1.5 transition-opacity shadow-sm dark:shadow-none">
                            <i className="ti ti-plus text-[15px]"></i>
                            <span className="hidden sm:inline">New job</span>
                        </button>
                    </div>
                </header>

                {/* Scrollable body */}
                <main className="flex-1 p-4 lg:p-8 flex flex-col gap-5 overflow-y-auto">

                    {/* ── ROW 1: Chart + Trust Score ── */}
                    <div className="grid grid-cols-1 lg:grid-cols-[1fr_220px] gap-5">

                        {/* Earnings Chart */}
                        <div className="bg-white dark:bg-transparent rounded-2xl border border-slate-200 dark:border-neutral-800 p-5 shadow-sm dark:shadow-none">
                            <div className="flex flex-wrap items-start justify-between gap-3 mb-5">
                                <div>
                                    <div className="text-[12px] font-semibold text-slate-500 dark:text-neutral-400 uppercase tracking-wider">Earnings over time</div>
                                    <div className="text-[30px] font-bold text-slate-900 dark:text-white mt-1 tracking-tight leading-none">
                                        {formatCurrency(chart.periodTotal)}
                                    </div>
                                    <div className="text-[12px] text-slate-400 dark:text-neutral-500 mt-1">
                                        {chartRange === '1M' ? 'This month' : chartRange === '3M' ? 'Last 3 months' : chartRange === '6M' ? 'Last 6 months' : 'This year'}
                                    </div>
                                </div>
                                <div className="flex gap-1 bg-slate-50 dark:bg-transparent p-1 rounded-xl border border-slate-100 dark:border-neutral-800" role="group" aria-label="Chart range">
                                    {(['1M', '3M', '6M', '1Y'] as const).map(range => (
                                        <button
                                            key={range}
                                            onClick={() => { setChartRange(range); setHoverIndex(null); }}
                                            className={`text-[11px] px-3.5 py-1.5 rounded-lg font-semibold transition-all ${
                                                chartRange === range
                                                    ? 'bg-white dark:bg-neutral-800 text-[#0F5525] dark:text-[#3FCD6B] shadow-sm ring-1 ring-slate-200 dark:ring-neutral-700'
                                                    : 'text-slate-500 dark:text-neutral-500 hover:text-slate-700 dark:hover:text-neutral-300 hover:bg-white/50 dark:hover:bg-neutral-800/50'
                                            }`}
                                            aria-pressed={chartRange === range}
                                        >
                                            {range}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="relative w-full">
                                <svg
                                    ref={svgRef}
                                    className="w-full h-auto overflow-visible cursor-crosshair"
                                    viewBox="0 0 800 200"
                                    onMouseMove={handleChartMouseMove}
                                    onMouseLeave={() => setHoverIndex(null)}
                                    role="img"
                                    aria-label="Earnings line chart"
                                >
                                    <defs>
                                        <linearGradient id="gfill" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%"   stopColor="#3FCD6B" stopOpacity="0.22" />
                                            <stop offset="100%" stopColor="#3FCD6B" stopOpacity="0" />
                                        </linearGradient>
                                    </defs>

                                    {[40, 80, 120].map(y => (
                                        <line key={y} x1="40" y1={y} x2="760" y2={y}
                                              className="stroke-[#f1f5f9] dark:stroke-neutral-800/50" strokeWidth="1" vectorEffect="non-scaling-stroke" />
                                    ))}

                                    <line x1="0" y1="155" x2="800" y2="155"
                                          className="stroke-[#e2e8f0] dark:stroke-neutral-800" strokeWidth="1.5" vectorEffect="non-scaling-stroke" />

                                    <path d={chart.fill} fill="url(#gfill)" />

                                    <path d={chart.path} fill="none" stroke="#3FCD6B" strokeWidth="2.5"
                                          strokeLinecap="round" strokeLinejoin="round" vectorEffect="non-scaling-stroke" />

                                    {chart.points.map((pt, i) => (
                                        <text key={`lbl-${i}`} x={pt.x} y="182"
                                              fontSize="12" fontWeight="600"
                                              className="fill-[#94a3b8] dark:fill-neutral-500"
                                              textAnchor="middle" fontFamily="system-ui">
                                            {pt.month}
                                        </text>
                                    ))}

                                    {hoverIndex !== null && chart.points[hoverIndex] && (() => {
                                        const pt  = chart.points[hoverIndex];
                                        const tx  = clampTooltipX(pt.x);
                                        const TW  = 80;

                                        return (
                                            <g>
                                                <line x1={pt.x} y1="0" x2={pt.x} y2="155"
                                                      className="stroke-[#94a3b8] dark:stroke-neutral-600" strokeWidth="1.5"
                                                      strokeDasharray="5 4" vectorEffect="non-scaling-stroke" />

                                                <circle cx={pt.x} cy={pt.y} r="5.5"
                                                        className="fill-white dark:fill-neutral-950 stroke-[#0F5525] dark:stroke-[#3FCD6B]" strokeWidth="3"
                                                        vectorEffect="non-scaling-stroke" />

                                                <rect x={tx - TW / 2} y={pt.y - 44} width={TW} height="32"
                                                      rx="6" className="fill-[#0F5525] dark:fill-[#3FCD6B]" />
                                                <text x={tx} y={pt.y - 23}
                                                      fontSize="13" fontWeight="700"
                                                      className="fill-white dark:fill-neutral-950"
                                                      textAnchor="middle" fontFamily="system-ui">
                                                    {formatCurrency(pt.total)}
                                                </text>
                                            </g>
                                        );
                                    })()}
                                </svg>
                            </div>
                        </div>

                        {/* Trust Score */}
                        <div className="bg-[#0A3D1A] dark:bg-transparent dark:border dark:border-neutral-800 rounded-2xl p-5 flex flex-col justify-between shadow-sm dark:shadow-none relative overflow-hidden">
                            <div className="absolute -top-6 -right-6 w-28 h-28 bg-[#3FCD6B]/15 dark:bg-[#3FCD6B]/10 blur-2xl rounded-full" aria-hidden="true" />
                            <div className="absolute -bottom-4 -left-4 w-20 h-20 bg-[#3FCD6B]/10 dark:bg-[#3FCD6B]/5 blur-xl rounded-full" aria-hidden="true" />
                            <div className="relative z-10">
                                <div className="text-[11px] text-white/50 font-semibold uppercase tracking-widest">Trust Score</div>
                                <div className="text-[52px] font-black text-[#3FCD6B] tracking-tight leading-none mt-2">
                                    {data.trustScore ?? 0}
                                </div>
                                <div className="text-[11px] text-white/60 flex items-center gap-1.5 mt-2 font-medium">
                                    <i className="ti ti-shield-check text-[#3FCD6B] text-[13px]"></i>
                                    Verified freelancer
                                </div>
                            </div>
                            <div className="mt-5 relative z-10">
                                <div className="flex justify-between text-[10px] text-white/40 font-semibold mb-1.5">
                                    <span>Score</span>
                                    <span>{data.trustScore ?? 0} / 100</span>
                                </div>
                                <div className="bg-black/30 dark:bg-neutral-800 rounded-full h-2 w-full overflow-hidden border border-transparent dark:border-neutral-700/50">
                                    <div
                                        className="bg-[#3FCD6B] h-full rounded-full transition-all duration-1000 ease-out"
                                        style={{ width: `${data.trustScore ?? 0}%` }}
                                        role="progressbar"
                                        aria-valuenow={data.trustScore ?? 0}
                                        aria-valuemin={0}
                                        aria-valuemax={100}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* ── ROW 2: Stat Cards ── */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
                        <StatCard
                            label="In escrow"
                            value={formatCurrency(data.fundsInEscrowHolding)}
                            badge={
                                <StatusBadge className="bg-[#E1F5EE] text-[#0F6E56] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 dark:text-[#3FCD6B]" icon="ti-shield-lock">
                                    {data.activeJobs?.length ?? 0} active {data.activeJobs?.length === 1 ? 'job' : 'jobs'}
                                </StatusBadge>
                            }
                        />
                        <StatCard
                            label="Pending release"
                            value={formatCurrency(data.fundsPending)}
                            badge={
                                <StatusBadge className="bg-[#FAEEDA] text-[#854F0B] dark:bg-transparent dark:border dark:border-amber-500/30 dark:text-amber-500" icon="ti-clock">
                                    Awaiting approval
                                </StatusBadge>
                            }
                        />
                        <StatCard
                            label="Paid out this month"
                            value={formatCurrency(data.fundsPaidOut)}
                            badge={
                                <StatusBadge className="bg-[#EAF3DE] text-[#3B6D11] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 dark:text-[#3FCD6B]" icon="ti-check">
                                    Cleared
                                </StatusBadge>
                            }
                        />
                    </div>

                    {/* ── ROW 3: Activity + Jobs ── */}
                    <div className="grid grid-cols-1 xl:grid-cols-[1fr_340px] gap-5 pb-4">

                        {/* Recent Activity */}
                        <div className="bg-white dark:bg-transparent rounded-2xl border border-slate-200 dark:border-neutral-800 shadow-sm dark:shadow-none overflow-hidden">
                            <div className="px-5 py-4 border-b border-slate-100 dark:border-neutral-800 flex items-center justify-between bg-slate-50/60 dark:bg-transparent">
                                <span className="text-[13px] font-bold text-slate-800 dark:text-white flex items-center gap-2.5">
                                    <div className="p-1.5 bg-[#E1F5EE] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-lg">
                                        <i className="ti ti-clock text-[#0F6E56] dark:text-[#3FCD6B] text-[14px]"></i>
                                    </div>
                                    Recent activity
                                </span>
                                <span className="text-[11px] text-slate-400 dark:text-neutral-500 font-medium">
                                    {data.recentActivity?.length ?? 0} events
                                </span>
                            </div>

                            {!data.recentActivity?.length ? (
                                <div className="p-8 text-center text-slate-400 dark:text-neutral-500 text-sm">
                                    No recent activity to display.
                                </div>
                            ) : (
                                <div className="divide-y divide-slate-100 dark:divide-neutral-800">
                                    {data.recentActivity.map((event) => {
                                        const cfg = getActivityConfig(event.eventType);
                                        return (
                                            <div key={event.eventId} className="group">
                                                <button
                                                    className="w-full flex items-start gap-3.5 p-4 hover:bg-slate-50 dark:hover:bg-neutral-900 transition-colors text-left"
                                                    onClick={() => setOpenActivity(
                                                        openActivity === event.eventId ? null : event.eventId
                                                    )}
                                                    aria-expanded={openActivity === event.eventId}
                                                >
                                                    <div className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 shadow-sm dark:shadow-none ${cfg.style}`}>
                                                        <i className={`ti ${cfg.icon} text-[15px]`}></i>
                                                    </div>
                                                    <div className="flex-1 min-w-0 pt-0.5">
                                                        <div className="text-[13px] font-semibold text-slate-800 dark:text-neutral-200 truncate group-hover:text-[#0F5525] dark:group-hover:text-white transition-colors">
                                                            {event.description}
                                                        </div>
                                                        <div className="text-[12px] text-slate-500 dark:text-neutral-400 mt-0.5 truncate">
                                                            {formatCurrency(event.amountGbp)} · {event.relatedPartyName}
                                                        </div>
                                                        <div className="text-[11px] text-[#0F5525] dark:text-[#3FCD6B] mt-1.5 flex items-center gap-1 font-semibold opacity-70 group-hover:opacity-100">
                                                            <i className={`ti ti-chevron-down text-[12px] transition-transform duration-200 ${openActivity === event.eventId ? 'rotate-180' : ''}`}></i>
                                                            {openActivity === event.eventId ? 'Hide details' : 'View details'}
                                                        </div>
                                                    </div>
                                                    <time
                                                        dateTime={event.occurredAt}
                                                        className="text-[11px] font-medium text-slate-400 dark:text-neutral-500 shrink-0 pt-0.5"
                                                    >
                                                        {formatRelativeTime(event.occurredAt)}
                                                    </time>
                                                </button>

                                                {openActivity === event.eventId && (
                                                    <div className="bg-slate-50 dark:bg-neutral-900/50 px-4 py-3.5 pl-[66px] text-[12.5px] text-slate-600 dark:text-neutral-300 leading-relaxed border-t border-slate-100 dark:border-neutral-800">
                                                        <div className="grid grid-cols-2 gap-x-4 gap-y-1.5">
                                                            <span className="text-slate-400 dark:text-neutral-500">Event type</span>
                                                            <span className="font-semibold text-slate-700 dark:text-neutral-200">{event.eventType.replace(/_/g, ' ')}</span>
                                                            <span className="text-slate-400 dark:text-neutral-500">Amount</span>
                                                            <span className="font-semibold text-slate-700 dark:text-neutral-200">{formatCurrency(event.amountGbp)}</span>
                                                            <span className="text-slate-400 dark:text-neutral-500">Party</span>
                                                            <span className="font-semibold text-slate-700 dark:text-neutral-200">{event.relatedPartyName}</span>
                                                            <span className="text-slate-400 dark:text-neutral-500">Timestamp</span>
                                                            <span className="font-semibold text-slate-700 dark:text-neutral-200">
                                                                {new Date(event.occurredAt).toLocaleString('en-GB')}
                                                            </span>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        {/* All Jobs */}
                        <div className="bg-white dark:bg-transparent rounded-2xl border border-slate-200 dark:border-neutral-800 shadow-sm dark:shadow-none overflow-hidden">
                            <div className="px-5 py-4 border-b border-slate-100 dark:border-neutral-800 flex items-center justify-between bg-slate-50/60 dark:bg-transparent">
                                <span className="text-[13px] font-bold text-slate-800 dark:text-white flex items-center gap-2.5">
                                    <div className="p-1.5 bg-[#E1F5EE] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-lg">
                                        <i className="ti ti-briefcase text-[#0F6E56] dark:text-[#3FCD6B] text-[14px]"></i>
                                    </div>
                                    All jobs
                                </span>
                                <span className="text-[11px] text-slate-400 dark:text-neutral-500 font-medium">
                                    {allJobs.length} total
                                </span>
                            </div>

                            {!allJobs.length ? (
                                <div className="p-8 text-center">
                                    <div className="w-12 h-12 bg-slate-50 dark:bg-neutral-900 rounded-2xl flex items-center justify-center mx-auto mb-3 border border-transparent dark:border-neutral-800">
                                        <i className="ti ti-briefcase text-slate-300 dark:text-neutral-600 text-2xl"></i>
                                    </div>
                                    <div className="text-slate-500 dark:text-neutral-400 text-sm font-medium">No jobs yet</div>
                                    <div className="text-slate-400 dark:text-neutral-500 text-xs mt-1">Create your first job to get started.</div>
                                </div>
                            ) : (
                                <div className="divide-y divide-slate-100 dark:divide-neutral-800">
                                    {allJobs.map((job, index) => {
                                        const amount       = job.totalJobAmount ?? 0;
                                        const statusLabel  = (job.status ?? 'PENDING').replace(/_/g, ' ');
                                        const initials     = job.clientName
                                            ? job.clientName.substring(0, 2).toUpperCase()
                                            : '??';
                                        const key          = job.jobId || `job-${index}`;
                                        const progress     = Math.min(Math.max(job.progressPercentage ?? 0, 0), 100);

                                        return (
                                            <div key={key} className="group">
                                                <button
                                                    className="w-full flex items-center gap-3 p-4 hover:bg-slate-50 dark:hover:bg-neutral-900 transition-colors text-left"
                                                    onClick={() => setOpenEscrow(openEscrow === key ? null : key)}
                                                    aria-expanded={openEscrow === key}
                                                >
                                                    <div className="w-9 h-9 rounded-full bg-[#E1F5EE] dark:bg-transparent dark:border dark:border-neutral-700 text-[#0F6E56] dark:text-[#3FCD6B] flex items-center justify-center text-[11px] font-bold tracking-wide shrink-0 shadow-sm dark:shadow-none">
                                                        {initials}
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <div className="text-[13px] font-semibold text-slate-800 dark:text-neutral-200 truncate group-hover:text-[#0F5525] dark:group-hover:text-white transition-colors">
                                                            {job.title || 'Untitled job'}
                                                        </div>
                                                        <div className="text-[11px] text-slate-500 dark:text-neutral-400 truncate mt-0.5">
                                                            {job.clientName || 'Unknown client'}
                                                        </div>
                                                        <div className="h-1.5 bg-slate-100 dark:bg-neutral-800 rounded-full mt-2 overflow-hidden w-full">
                                                            <div
                                                                className="h-full bg-[#3FCD6B] rounded-full transition-all duration-700 ease-out"
                                                                style={{ width: `${progress}%` }}
                                                                role="progressbar"
                                                                aria-valuenow={progress}
                                                                aria-valuemin={0}
                                                                aria-valuemax={100}
                                                                aria-label={`${job.title} progress`}
                                                            />
                                                        </div>
                                                    </div>
                                                    <div className="text-right shrink-0 flex flex-col items-end ml-2">
                                                        <div className="text-[13px] font-bold text-slate-900 dark:text-white tracking-tight">
                                                            {formatCurrency(amount)}
                                                        </div>
                                                        <span className={`mt-1.5 text-[10px] px-2 py-0.5 rounded-lg font-semibold ${getJobBadgeStyles(job.status)}`}>
                                                            {statusLabel}
                                                        </span>
                                                    </div>
                                                    <i className={`ti ti-chevron-down text-slate-300 dark:text-neutral-600 text-[14px] ml-1 transition-transform duration-200 ${openEscrow === key ? 'rotate-180' : ''}`} />
                                                </button>

                                                {openEscrow === key && (
                                                    <div className="px-5 py-3.5 pl-[64px] bg-slate-50 dark:bg-neutral-900/50 text-[11.5px] border-t border-slate-100 dark:border-neutral-800">
                                                        <div className="space-y-1.5">
                                                            {[
                                                                { label: 'Current phase',   value: job.currentMilestone || 'Processing' },
                                                                { label: 'Deposit status',  value: job.depositStatus    || 'Awaiting' },
                                                                { label: 'Deadline',        value: job.deadline ? new Date(job.deadline).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : 'Not set' },
                                                                { label: 'Progress',        value: `${progress}%` },
                                                            ].map(row => (
                                                                <div key={row.label} className="flex justify-between py-1 border-b border-slate-200/60 dark:border-neutral-800/60 last:border-0">
                                                                    <span className="text-slate-400 dark:text-neutral-500">{row.label}</span>
                                                                    <span className="font-semibold text-slate-700 dark:text-neutral-300">{row.value}</span>
                                                                </div>
                                                            ))}
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
}