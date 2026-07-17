"use client";

import React, { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useJobWorkspace}  from "../../../../components/context/JobWorkSpaceContext";
import {
    CheckCircle2,
    Circle,
    Clock,
    ShieldCheck,
    ArrowRight,
    ArrowLeft,
    User,
    Briefcase,
    Lock,
    Loader2,
    AlertCircle
} from "lucide-react";

// --- TYPES ---
interface MilestoneSummaryDto {
    scopeItems: any[];
    milestoneId: string;
    orderIndex: number;
    title: string;
    milestoneAmount: number;
    status: string;
}

interface JobSummaryDto {
    jobId: string;
    title: string;
    clientName: string;
    totalJobAmount: number;
    progressPercentage: number;
    status: string;
    currentMilestoneTitle: string;
    DepositStatus: string;
    deadline: string;
}

interface JobAndMilestoneData {
    jobSummaryDto: JobSummaryDto;
    milestoneSummaries: MilestoneSummaryDto[];
}

export default function JobDetails({ params }: { params?: Promise<{ jobId?: string }> }) {
    const router = useRouter();
    const [apiData, setApiData] = useState<JobAndMilestoneData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const resolvedParams = params ? React.use(params) : {};
    const currentJobId = resolvedParams.jobId;

    const { setJobData } = useJobWorkspace();

    // --- API FETCH ---
    const fetchJobData = useCallback(async () => {
        if (!currentJobId) {
            setError("No Job ID provided in the URL.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const response = await fetch(`http://localhost:8080/api/job/milestone-summary?jobId=${currentJobId}`, {
                method: "GET",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
            });

            if (response.status === 401 || response.status === 403) {
                throw new Error("Unauthorized. Please log in again.");
            }

            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }

            const data: JobAndMilestoneData = await response.json();

            // 1. Set local state for this page
            setApiData(data);

            // 2. Hydrate the Context for the Request Release page
            setJobData(data);

        } catch (err: any) {
            setError(err.message || "An unexpected error occurred.");
        } finally {
            setIsLoading(false);
        }
    }, [currentJobId]);

    useEffect(() => {
        fetchJobData();
    }, [fetchJobData]);

    const handleRequestRelease = (milestoneId: string) => {
        // 1. Find the specific milestone from your local state
        const targetMilestone = milestones.find(m => m.milestoneId === milestoneId);

        if (targetMilestone) {
            // 2. Build the payload the Request Release page expects
            const payload = {
                ...targetMilestone,
                jobTitle: job.title,
                clientName: job.clientName,
                scopeItems: targetMilestone.scopeItems || []
            };

            // 3. Save it to storage using a unique key
            sessionStorage.setItem(`trustbridge_milestone_${milestoneId}`, JSON.stringify(payload));
        }

        // 4. Navigate to the release page
        router.push(`/dashboard/freelancer/jobs/${currentJobId}/release/${milestoneId}`);
    };

    // --- UI HELPERS ---
    const formatCurrency = (amount: number) =>
        `£${(amount || 0).toLocaleString('en-GB', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

    // --- LOADING & ERROR STATES ---
    if (isLoading) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex flex-col items-center justify-center text-[#0A3D1A] dark:text-[#3FCD6B]">
                <div className="w-14 h-14 bg-white dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-2xl flex items-center justify-center shadow-lg dark:shadow-none animate-pulse mb-6">
                    <ShieldCheck className="text-[#3FCD6B]" size={28} />
                </div>
                <div className="flex gap-2">
                    {[0, 1, 2].map(i => (
                        <div key={i} className="w-2 h-2 bg-[#3FCD6B] rounded-full animate-bounce" style={{ animationDelay: `${i * 150}ms` }} />
                    ))}
                </div>
            </div>
        );
    }

    if (error || !apiData || !apiData.jobSummaryDto) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex items-center justify-center p-6">
                <div className="bg-white dark:bg-neutral-900 text-red-600 dark:text-red-400 p-8 rounded-3xl border border-red-100 dark:border-red-900/30 max-w-md w-full text-center shadow-sm">
                    <AlertCircle size={48} className="mx-auto mb-4 opacity-80" />
                    <p className="text-xl font-bold mb-2 tracking-tight">Escrow Retrieval Failed</p>
                    <p className="text-sm text-slate-500 dark:text-neutral-400 mb-8">{error || "Invalid job data returned from server."}</p>
                    <button
                        onClick={() => router.push('/dashboard/freelancer')}
                        className="w-full bg-[#0A3D1A] dark:bg-white text-white dark:text-neutral-950 py-3 rounded-xl font-bold transition-opacity hover:opacity-90"
                    >
                        Return to Dashboard
                    </button>
                </div>
            </div>
        );
    }

    const job = apiData.jobSummaryDto;
    const milestones = apiData.milestoneSummaries || [];
    const totalHeldInEscrow = milestones
        .filter(m => m.status === "FUNDED" || m.status === "IN_PROGRESS")
        .reduce((sum, m) => sum + (m.milestoneAmount || 0), 0);

    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 font-sans text-slate-900 dark:text-neutral-100 flex flex-col selection:bg-[#3FCD6B]/30 pb-24">

            {/* TOP NAVIGATION */}
            <div className="sticky top-0 z-40 bg-[#f4f6f1]/80 dark:bg-neutral-950/80 backdrop-blur-md border-b border-slate-200 dark:border-neutral-800">
                <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
                    <button
                        onClick={() => router.back()}
                        className="group flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-slate-900 dark:text-neutral-400 dark:hover:text-white transition-colors"
                    >
                        <ArrowLeft size={16} className="transition-transform group-hover:-translate-x-1" />
                        Back to Workspace
                    </button>
                    <div className="flex items-center gap-2 text-xs font-semibold text-slate-400 dark:text-neutral-500 bg-white dark:bg-neutral-900 px-3 py-1.5 rounded-full border border-slate-200 dark:border-neutral-800">
                        <Briefcase size={14} /> {job.jobId}
                    </div>
                </div>
            </div>

            <div className="max-w-6xl mx-auto px-6 pt-10 w-full">
                {/* HERO SECTION */}
                <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-8 mb-16">
                    <div className="max-w-2xl">
                        <h1 className="text-4xl md:text-5xl font-black tracking-tight text-slate-900 dark:text-white mb-4 leading-tight" style={{ fontFamily: 'Syne, sans-serif' }}>
                            {job.title}
                        </h1>
                        <div className="flex items-center gap-4 text-sm font-medium text-slate-600 dark:text-neutral-400">
                            <span className="flex items-center gap-2 bg-white dark:bg-neutral-900 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-neutral-800">
                                <User size={16} className="text-[#3FCD6B]" />
                                Client: {job.clientName || 'Unknown'}
                            </span>
                        </div>
                    </div>
                    <div className="text-left lg:text-right">
                        <p className="text-xs font-bold text-slate-400 dark:text-neutral-500 uppercase tracking-widest mb-2">Total Escrow Value</p>
                        <p className="text-4xl font-black text-[#0A3D1A] dark:text-[#3FCD6B] tracking-tighter">
                            {formatCurrency(job.totalJobAmount)}
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-12">

                    {/* LEFT COLUMN: EDITORIAL TIMELINE */}
                    <div className="space-y-8">
                        <h2 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-3">
                            <Clock size={20} className="text-[#3FCD6B]" />
                            Orchestration Timeline
                        </h2>

                        <div className="relative pl-4 md:pl-0">
                            {/* Vertical Line */}
                            <div className="absolute top-8 bottom-8 left-[31px] md:left-[39px] w-px bg-slate-200 dark:bg-neutral-800" />

                            <div className="space-y-8">
                                {milestones.map((milestone, index) => {
                                    // Determine timeline node visual based on status
                                    const isComplete = milestone.status === "PAID_OUT";
                                    const isActive = milestone.status === "FUNDED" || milestone.status === "IN_PROGRESS";

                                    return (
                                        <div key={milestone.milestoneId} className="relative flex items-start gap-6 group">

                                            {/* Timeline Node */}
                                            <div className={`relative z-10 w-12 h-12 rounded-full flex items-center justify-center shrink-0 border-[3px] transition-colors duration-300 ${
                                                isComplete
                                                    ? 'bg-[#EAF3DE] border-[#3FCD6B] text-[#3B6D11] dark:bg-neutral-900 dark:border-[#3FCD6B] dark:text-[#3FCD6B]'
                                                    : isActive
                                                        ? 'bg-[#0A3D1A] border-[#0A3D1A] text-[#3FCD6B] dark:bg-[#3FCD6B] dark:border-[#3FCD6B] dark:text-neutral-950 shadow-[0_0_15px_rgba(63,205,107,0.4)]'
                                                        : 'bg-slate-100 border-white text-slate-400 dark:bg-neutral-900 dark:border-neutral-950 dark:text-neutral-600'
                                            }`}>
                                                {isComplete ? <CheckCircle2 size={20} /> : <span className="font-bold text-sm">{index + 1}</span>}
                                            </div>

                                            {/* Milestone Card */}
                                            <div className={`flex-1 bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border transition-all duration-300 ${
                                                isActive
                                                    ? 'border-slate-300 dark:border-neutral-700 shadow-md dark:shadow-none'
                                                    : 'border-slate-100 dark:border-neutral-800/50 opacity-80 hover:opacity-100'
                                            }`}>
                                                <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-4 mb-6">
                                                    <div>
                                                        <h3 className="text-xl font-bold text-slate-900 dark:text-white tracking-tight">{milestone.title}</h3>
                                                        <p className="text-sm font-semibold text-[#0A3D1A] dark:text-[#3FCD6B] mt-1">
                                                            {formatCurrency(milestone.milestoneAmount)}
                                                        </p>
                                                    </div>

                                                    {/* Status Badge */}
                                                    <div className="shrink-0">
                                                        {milestone.status === "PENDING" && <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-amber-700 bg-amber-50 dark:bg-amber-500/10 dark:text-amber-500 px-3 py-1.5 rounded-full uppercase tracking-wider"><Circle size={12} /> Awaiting Deposit</span>}
                                                        {(milestone.status === "FUNDED" || milestone.status === "IN_PROGRESS") && <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-[#0F6E56] bg-[#E1F5EE] dark:bg-[#3FCD6B]/10 dark:text-[#3FCD6B] px-3 py-1.5 rounded-full uppercase tracking-wider"><Lock size={12} /> In Escrow</span>}
                                                        {milestone.status === "PAID_OUT" && <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-blue-700 bg-blue-50 dark:bg-blue-500/10 dark:text-blue-400 px-3 py-1.5 rounded-full uppercase tracking-wider"><CheckCircle2 size={12} /> Paid Out</span>}
                                                    </div>
                                                </div>

                                                {/* Action Area */}
                                                <div className="pt-4 border-t border-slate-100 dark:border-neutral-800 flex items-center justify-between">
                                                    {milestone.status === "PENDING" && (
                                                        <p className="text-sm text-slate-500 dark:text-neutral-500 font-medium">
                                                            Waiting for client to secure funds via vIBAN.
                                                        </p>
                                                    )}

                                                    {(milestone.status === "FUNDED" || milestone.status === "IN_PROGRESS") && (
                                                        <>
                                                            <p className="text-sm text-slate-600 dark:text-neutral-400 font-medium hidden sm:block">
                                                                Funds are secure. Ready for submission.
                                                            </p>
                                                            <button
                                                                onClick={() => handleRequestRelease(milestone.milestoneId)}
                                                                disabled={isProcessing}
                                                                className="w-full sm:w-auto bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] px-6 py-3 rounded-xl text-sm font-bold transition-transform active:scale-[0.98] disabled:opacity-70 flex items-center justify-center gap-2"
                                                            >
                                                                Request Release <ArrowRight size={16} />
                                                            </button>
                                                        </>
                                                    )}

                                                    {milestone.status === "PAID_OUT" && (
                                                        <p className="text-sm text-slate-500 dark:text-neutral-500 font-medium flex items-center gap-2">
                                                            <CheckCircle2 size={16} className="text-[#3FCD6B]" />
                                                            Routed to your bank account.
                                                        </p>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    </div>

                    {/* RIGHT COLUMN: VAULT WIDGET */}
                    <div className="space-y-6 lg:sticky lg:top-24 h-fit">
                        <div className="bg-[#0A3D1A] dark:bg-neutral-900 rounded-3xl p-8 text-white shadow-xl relative overflow-hidden border border-[#0A3D1A] dark:border-neutral-800">
                            {/* Glassmorphic Orbs */}
                            <div className="absolute -top-12 -right-12 w-40 h-40 bg-[#3FCD6B]/20 blur-3xl rounded-full pointer-events-none" />
                            <div className="absolute -bottom-8 -left-8 w-32 h-32 bg-[#3FCD6B]/10 blur-2xl rounded-full pointer-events-none" />

                            <h3 className="text-xs font-bold text-white/60 dark:text-neutral-500 uppercase tracking-widest mb-6 relative z-10 flex items-center gap-2">
                                <ShieldCheck size={16} /> TrustBridge Vault
                            </h3>

                            <div className="relative z-10 mb-8">
                                <p className="text-sm text-white/80 dark:text-neutral-400 mb-2 font-medium">Currently Held in Escrow</p>
                                <p className="text-5xl font-black text-[#3FCD6B] tracking-tighter">
                                    {formatCurrency(totalHeldInEscrow)}
                                </p>
                            </div>

                            <div className="pt-6 border-t border-white/10 dark:border-neutral-800 relative z-10 space-y-4">
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-white/60 dark:text-neutral-400 font-medium">Awaiting Deposit</span>
                                    <span className="font-bold text-white">
                                        {formatCurrency(milestones.filter(m => m.status === "PENDING").reduce((sum, m) => sum + (m.milestoneAmount || 0), 0))}
                                    </span>
                                </div>
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-white/60 dark:text-neutral-400 font-medium">Released</span>
                                    <span className="font-bold text-white">
                                        {formatCurrency(milestones.filter(m => m.status === "PAID_OUT").reduce((sum, m) => sum + (m.milestoneAmount || 0), 0))}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="bg-white dark:bg-neutral-900 border border-slate-200 dark:border-neutral-800 rounded-3xl p-6 shadow-sm">
                            <h3 className="font-bold text-slate-900 dark:text-white mb-2 flex items-center gap-2">
                                <Lock size={16} className="text-slate-400" /> Escrow Guarantee
                            </h3>
                            <p className="text-sm text-slate-500 dark:text-neutral-400 leading-relaxed mb-4">
                                Funds are held in a secure, FCA-regulated partner vault. TrustBridge acts strictly as an orchestration layer—funds are only released when both parties agree.
                            </p>
                            <button className="text-sm font-bold text-[#0A3D1A] dark:text-[#3FCD6B] flex items-center gap-1.5 hover:underline group">
                                View Contract Terms <ArrowRight size={14} className="transition-transform group-hover:translate-x-1" />
                            </button>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    );
}