"use client";

import React, { useState, useCallback, use } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
    ArrowLeft,
    ShieldCheck,
    FileText,
    File,
    AlertCircle,
    Loader2,
    CheckCircle2,
    Briefcase,
    XCircle,
    Landmark,
    UploadCloud,
    X,
    MessageSquareWarning
} from "lucide-react";

// ---------------------------------------------------------------------------
// PAGE COMPONENT
// ---------------------------------------------------------------------------

export default function ClientDisputePage({
                                              params,
                                          }: {
    params: Promise<{ milestoneId: string }>;
}) {
    const router = useRouter();
    const searchParams = useSearchParams();

    // Resolve the milestoneId from the folder path [milestoneId]
    const resolvedParams = use(params);
    const { milestoneId } = resolvedParams;
    const jobId = searchParams.get("jobId");

    // Form States
    const [reason, setReason] = useState("");
    const [proposedAmount, setProposedAmount] = useState<string>("");
    const [files, setFiles] = useState<File[]>([]);

    // Action States
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [actionSuccess, setActionSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // ---------------------------------------------------------------------------
    // HANDLERS
    // ---------------------------------------------------------------------------

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const selectedFiles = Array.from(e.target.files);
            setFiles((prev) => [...prev, ...selectedFiles]);
        }
    };

    const removeFile = (index: number) => {
        setFiles((prev) => prev.filter((_, i) => i !== index));
    };

    const formatFileSize = (bytes: number): string => {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    };

    const handleReturnToJob = () => {
        if (jobId) {
            router.push(`/dashboard/client/jobs/${jobId}`);
        } else {
            router.push('/dashboard/client/home');
        }
    };

    const handleSubmitDispute = async () => {
        if (!reason || reason.trim().length === 0) {
            setError("Please provide a reason for the dispute.");
            return;
        }
        if (!proposedAmount || parseFloat(proposedAmount) <= 0) {
            setError("Please provide a valid proposed settlement amount.");
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            // 1. Retrieve the token from Session Storage
            const reviewToken = sessionStorage.getItem('reviewToken');

            const formData = new FormData();

            const dto = {
                milestoneId: milestoneId,
                clientProposedAmount: parseFloat(proposedAmount),
                reason: reason.trim(),
            };

            formData.append("dto", new Blob([JSON.stringify(dto)], { type: "application/json" }));

            files.forEach((file) => {
                formData.append("files", file);
            });

            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/v1/disputes/open-dispute`,
                {
                    method: "POST",
                    credentials: "include",
                    // 2. Add the token to the Headers (only if it exists)
                    headers: {
                        ...(reviewToken ? { "X-Review-Token": reviewToken } : {})
                    },
                    body: formData,
                }
            );

            if (!response.ok) {
                const errText = await response.text();
                throw new Error(errText || "Failed to submit dispute.");
            }

            // 3. Optional: Clean up the session storage since we are done with it
            sessionStorage.removeItem('reviewToken');

            setActionSuccess(true);
        } catch (err: any) {
            setError(err.message || "An unexpected error occurred while submitting.");
        } finally {
            setIsSubmitting(false);
        }
    };

    // ---------------------------------------------------------------------------
    // SUCCESS VIEW
    // ---------------------------------------------------------------------------

    if (actionSuccess) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex items-center justify-center p-6">
                <div className="max-w-lg w-full text-center">
                    <div className="w-20 h-20 bg-[#0A3D1A] dark:bg-[#3FCD6B]/10 dark:border dark:border-[#3FCD6B]/30 rounded-3xl flex items-center justify-center mx-auto mb-8 shadow-[0_0_40px_rgba(63,205,107,0.25)]">
                        <CheckCircle2 size={36} className="text-[#3FCD6B]" />
                    </div>
                    <h1 className="text-3xl font-black tracking-tight text-slate-900 dark:text-white mb-3" style={{ fontFamily: "Syne, sans-serif" }}>
                        Dispute Submitted
                    </h1>
                    <p className="text-slate-500 dark:text-neutral-400 mb-10 text-sm leading-relaxed">
                        Your dispute has been officially logged. The freelancer will be notified and our moderation team will review the evidence shortly. Escrow funds are temporarily frozen.
                    </p>
                    <button
                        onClick={handleReturnToJob}
                        className="bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] px-8 py-3.5 rounded-xl font-bold transition-transform active:scale-[0.98] inline-flex items-center gap-2 shadow-lg shadow-[#3FCD6B]/20"
                    >
                        Return to Job <ArrowLeft size={16} className="rotate-180" />
                    </button>
                </div>
            </div>
        );
    }

    // ---------------------------------------------------------------------------
    // MAIN RENDER
    // ---------------------------------------------------------------------------

    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 font-sans text-slate-900 dark:text-neutral-100 selection:bg-[#3FCD6B]/30 pb-24">
            {/* Sticky Header */}
            <div className="sticky top-0 z-40 bg-[#f4f6f1]/80 dark:bg-neutral-950/80 backdrop-blur-md border-b border-slate-200 dark:border-neutral-800">
                <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
                    <button
                        onClick={() => router.back()}
                        className="group flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-slate-900 dark:text-neutral-400 dark:hover:text-white transition-colors"
                    >
                        <ArrowLeft size={16} className="transition-transform group-hover:-translate-x-1" />
                        Cancel Dispute
                    </button>
                    <div className="flex items-center gap-2 text-xs font-semibold text-slate-400 dark:text-neutral-500 bg-white dark:bg-neutral-900 px-3 py-1.5 rounded-full border border-slate-200 dark:border-neutral-800">
                        <Briefcase size={14} /> {milestoneId}
                    </div>
                </div>
            </div>

            <div className="max-w-6xl mx-auto px-6 pt-10 w-full">
                {/* Hero Section */}
                <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-8 mb-16">
                    <div className="max-w-2xl">
                        <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-red-700 bg-red-100 dark:bg-red-500/10 dark:text-red-400 px-3 py-1.5 rounded-full uppercase tracking-wider mb-4">
                            <MessageSquareWarning size={12} /> Dispute Resolution
                        </span>
                        <h1 className="text-4xl md:text-5xl font-black tracking-tight text-slate-900 dark:text-white mb-4 leading-tight" style={{ fontFamily: "Syne, sans-serif" }}>
                            Open a Dispute
                        </h1>
                        <p className="text-slate-500 dark:text-neutral-400 font-medium">
                            Please provide detailed reasoning and evidence. Funds will remain securely in escrow until a resolution is reached.
                        </p>
                    </div>
                </div>

                {/* Main Content Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-12">

                    {/* Left Column: Form Fields */}
                    <div className="space-y-6">

                        {/* Error Alert */}
                        {error && (
                            <div className="bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 rounded-2xl p-4 flex items-start gap-3 text-red-600 dark:text-red-400">
                                <AlertCircle size={20} className="shrink-0 mt-0.5" />
                                <p className="text-sm font-medium leading-relaxed">{error}</p>
                            </div>
                        )}

                        {/* Reason Section */}
                        <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                            <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                <FileText size={16} className="text-[#3FCD6B]" />
                                Reason for Dispute
                            </h2>
                            <p className="text-sm text-slate-500 dark:text-neutral-500 mb-5">
                                Explain clearly why you are contesting this milestone. (Max 2000 characters)
                            </p>

                            <textarea
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="E.g., The deliverables do not meet the agreed-upon requirements..."
                                className="w-full h-40 bg-slate-50 dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 rounded-xl p-4 text-sm text-slate-700 dark:text-neutral-200 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#3FCD6B]/50 focus:border-[#3FCD6B] transition-all resize-none"
                                maxLength={2000}
                            />
                            <div className="text-right mt-2 text-xs text-slate-400 dark:text-neutral-500">
                                {reason.length}/2000
                            </div>
                        </div>

                        {/* Evidence Upload Section */}
                        <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                            <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                <File size={16} className="text-[#3FCD6B]" />
                                Attach Evidence
                            </h2>
                            <p className="text-sm text-slate-500 dark:text-neutral-500 mb-5">
                                Upload screenshots, files, or documents that support your claim.
                            </p>

                            {/* Upload Dropzone Placeholder */}
                            <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed border-slate-200 dark:border-neutral-700 rounded-2xl bg-slate-50 dark:bg-neutral-800/50 hover:bg-slate-100 dark:hover:bg-neutral-800 cursor-pointer transition-colors mb-4">
                                <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                    <UploadCloud size={28} className="text-slate-400 dark:text-neutral-500 mb-2" />
                                    <p className="text-sm text-slate-500 dark:text-neutral-400 font-medium">
                                        Click to upload files
                                    </p>
                                </div>
                                <input
                                    type="file"
                                    multiple
                                    className="hidden"
                                    onChange={handleFileChange}
                                />
                            </label>

                            {/* File List */}
                            {files.length > 0 && (
                                <div className="space-y-2 mt-4">
                                    {files.map((file, idx) => (
                                        <div key={idx} className="flex items-center justify-between bg-slate-50 dark:bg-neutral-800/50 rounded-xl px-4 py-3 border border-slate-100 dark:border-neutral-700/50">
                                            <div className="flex items-center gap-3 min-w-0">
                                                <div className="w-8 h-8 bg-white dark:bg-neutral-900 rounded-lg flex items-center justify-center border border-slate-200 dark:border-neutral-700 shrink-0">
                                                    <File size={14} className="text-slate-400" />
                                                </div>
                                                <div className="min-w-0">
                                                    <p className="text-sm font-semibold text-slate-800 dark:text-neutral-200 truncate">
                                                        {file.name}
                                                    </p>
                                                    <p className="text-xs text-slate-400 dark:text-neutral-500">
                                                        {formatFileSize(file.size)}
                                                    </p>
                                                </div>
                                            </div>
                                            <button
                                                onClick={() => removeFile(idx)}
                                                className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                            >
                                                <X size={16} />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Right Column: Action Card */}
                    <div className="space-y-6 lg:sticky lg:top-24 h-fit">
                        <div className="bg-[#0A3D1A] dark:bg-neutral-900 rounded-3xl p-8 text-white shadow-xl relative overflow-hidden border border-[#0A3D1A] dark:border-neutral-800">
                            {/* Decorative Background Blurs */}
                            <div className="absolute -top-12 -right-12 w-40 h-40 bg-[#3FCD6B]/15 blur-3xl rounded-full pointer-events-none" />
                            <div className="absolute -bottom-8 -left-8 w-32 h-32 bg-red-500/10 blur-2xl rounded-full pointer-events-none" />

                            <h3 className="text-xs font-bold text-white/60 dark:text-neutral-500 uppercase tracking-widest mb-6 relative z-10 flex items-center gap-2">
                                <ShieldCheck size={16} /> TrustBridge Dispute
                            </h3>

                            {/* Settlement Input */}
                            <div className="relative z-10 mb-8">
                                <label className="block text-sm text-white/80 dark:text-neutral-400 mb-3 font-medium">
                                    Proposed Refund Amount
                                </label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <span className="text-xl font-bold text-[#3FCD6B]">£</span>
                                    </div>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        value={proposedAmount}
                                        onChange={(e) => setProposedAmount(e.target.value)}
                                        placeholder="0.00"
                                        className="w-full bg-white/10 dark:bg-neutral-800 border border-white/20 dark:border-neutral-700 rounded-xl py-3 pl-10 pr-4 text-2xl font-black text-white placeholder-white/30 focus:outline-none focus:ring-2 focus:ring-[#3FCD6B]/50 transition-all"
                                    />
                                </div>
                                <p className="text-[11px] text-white/50 mt-2">
                                    Enter the amount you believe should be returned to you from the escrow balance.
                                </p>
                            </div>

                            {/* Action Buttons */}
                            <div className="relative z-10 space-y-3 pt-4 border-t border-white/10 dark:border-neutral-800">
                                <button
                                    onClick={handleSubmitDispute}
                                    disabled={isSubmitting}
                                    className="w-full bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] py-3.5 rounded-xl text-sm font-bold transition-transform active:scale-[0.98] flex items-center justify-center gap-2 shadow-lg shadow-[#3FCD6B]/20 disabled:opacity-50"
                                >
                                    {isSubmitting ? <Loader2 size={16} className="animate-spin" /> : <Landmark size={16} />}
                                    Submit Dispute
                                </button>
                            </div>

                            <p className="relative z-10 text-[11px] text-white/50 mt-5 leading-relaxed text-center">
                                By submitting this dispute, you initiate a formal resolution process. Misuse of the dispute system may result in account penalties.
                            </p>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    );
}