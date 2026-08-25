"use client";

import React, { useState, useEffect, useCallback, use } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
    ArrowLeft,
    ShieldCheck,
    Link2,
    FileText,
    File,
    Download,
    ExternalLink,
    AlertCircle,
    Loader2,
    CheckCircle2,
    Globe,
    Briefcase,
    CheckSquare,
    Square,
    Clock,
    XCircle,
    Landmark,
    ArrowRight
} from "lucide-react";

// ---------------------------------------------------------------------------
// TYPES (Mapped from Spring Boot DTOs)
// ---------------------------------------------------------------------------

interface ScopeItemDto {
    id: string;
    description: string;
    isCompleted: boolean;
}

interface SubmissionFileDto {
    fileId: string;
    fileName: string;
    fileSizeBytes: number;
    downloadUrl: string;
}

interface MilestoneSubmissionReviewDto {
    submissionId: string;
    milestoneId: string;
    submittedAt: string;
    notes: string;
    deliverableLink: string;
    scopeItems: ScopeItemDto[];
    files: SubmissionFileDto[];
    milestoneTitle: string;
    freelancerName: string;
    milestoneAmount: number;
}

// ---------------------------------------------------------------------------
// PAGE COMPONENT
// ---------------------------------------------------------------------------

export default function ClientReviewPage({
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
    const reviewToken = searchParams.get("token");

    const [submissionData, setSubmissionData] = useState<MilestoneSubmissionReviewDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Action states
    const [isApproving, setIsApproving] = useState(false);
    const [isRejecting, setIsRejecting] = useState(false);
    const [actionSuccess, setActionSuccess] = useState<"approved" | "rejected" | null>(null);

    // ---------------------------------------------------------------------------
    // DATA FETCH
    // ---------------------------------------------------------------------------

    const fetchSubmission = useCallback(async () => {
        try {
            setIsLoading(true);
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/milestone/review/${milestoneId}`,
                { credentials: "include" }
            );

            if (!response.ok) throw new Error(`Server returned ${response.status}`);

            const data: MilestoneSubmissionReviewDto = await response.json();
            setSubmissionData(data);
        } catch (err: any) {
            setError(err.message || "Could not load submission details.");
        } finally {
            setIsLoading(false);
        }
    }, [milestoneId]);

    useEffect(() => {
        fetchSubmission();
    }, [fetchSubmission]);

    // ---------------------------------------------------------------------------
    // ACTIONS (State Machine Triggers)
    // ---------------------------------------------------------------------------

    const handleApprove = async () => {
        if (isApproving || isRejecting) return;
        setIsApproving(true);
        try {
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/milestone/approve/${milestoneId}`,
                { method: "POST", credentials: "include" }
            );
            if (!response.ok) throw new Error("Approval failed");
            setActionSuccess("approved");
        } catch (err: any) {
            setError(err.message || "Failed to process approval.");
            setIsApproving(false);
        }
    };

    const isBrowserViewable = (fileName: string) => {
        const viewableExtensions = ['.pdf', '.jpg', '.jpeg', '.png', '.gif', '.webp', '.txt'];
        const lowerName = fileName.toLowerCase();
        return viewableExtensions.some(ext => lowerName.endsWith(ext));
    };

    const [changesFeedback, setChangesFeedback] = useState("");

    const handleRequestChanges = async () => {
        if (isApproving || isRejecting) return;
        setIsRejecting(true);
        try {
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/milestone/request-changes/${milestoneId}`,
                {
                    method: "POST",
                    credentials: "include",
                    headers: { "Content-Type": "application/json",
                    "X-Review-Token": reviewToken ?? ""},
                    body: JSON.stringify({ feedback: changesFeedback}),
                }
            );
            if (!response.ok) throw new Error("Request changes failed");
            setActionSuccess("rejected");
        } catch (err: any) {
            setError(err.message || "Failed to request changes.");
            setIsRejecting(false);
        }
    };

    // ---------------------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------------------

    const formatCurrency = (amount: number) =>
        `£${(amount || 0).toLocaleString("en-GB", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        })}`;

    const formatFileSize = (bytes: number): string => {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    };

    const formatDate = (isoString: string) => {
        return new Date(isoString).toLocaleString("en-GB", {
            day: "numeric",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    };

    const handleReturnToJob = () => {
        if (jobId) {
            router.push(`/dashboard/client/jobs/${jobId}`);
        } else {
            router.push('/dashboard/client/home');
        }
    };

    const handleFileAction = async (downloadUrl: string, fileName: string, action: "open" | "download") => {
        try {
            // 1. Fetch the file securely, explicitly including your session cookies
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}${downloadUrl}?download=${action === 'download'}`,
                { method: "GET", credentials: "include" }
            );

            if (!response.ok) {
                throw new Error("You do not have permission to access this file, or it is missing.");
            }

            // 2. Convert the secure response into a local browser Blob
            const blob = await response.blob();
            const blobUrl = window.URL.createObjectURL(blob);

            // 3. Execute the user's action
            if (action === "open") {
                window.open(blobUrl, "_blank");
            } else {
                const link = document.createElement("a");
                link.href = blobUrl;
                link.download = fileName;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }

            // 4. Clean up the memory after 1 second
            setTimeout(() => window.URL.revokeObjectURL(blobUrl), 1000);

        } catch (err: any) {
            console.error("File access error:", err);
            alert(err.message);
        }
    };

    // ---------------------------------------------------------------------------
    // LOADING & ERROR STATES
    // ---------------------------------------------------------------------------

    if (isLoading) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex flex-col items-center justify-center text-[#0A3D1A] dark:text-[#3FCD6B]">
                <div className="w-14 h-14 bg-white dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-2xl flex items-center justify-center shadow-lg dark:shadow-none mb-6">
                    <Loader2 className="animate-spin text-[#3FCD6B]" size={28} />
                </div>
            </div>
        );
    }

    if (error && !submissionData) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex items-center justify-center p-6">
                <div className="bg-white dark:bg-neutral-900 text-red-600 dark:text-red-400 p-8 rounded-3xl border border-red-100 dark:border-red-900/30 max-w-md w-full text-center shadow-sm">
                    <AlertCircle size={48} className="mx-auto mb-4 opacity-80" />
                    <p className="text-xl font-bold mb-2 tracking-tight">Could Not Load Submission</p>
                    <p className="text-sm text-slate-500 dark:text-neutral-400 mb-8">{error}</p>
                    <button onClick={() => router.back()} className="w-full bg-[#0A3D1A] dark:bg-white text-white dark:text-neutral-950 py-3 rounded-xl font-bold hover:opacity-90 transition-opacity">
                        Go Back
                    </button>
                </div>
            </div>
        );
    }

    if (actionSuccess) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex items-center justify-center p-6">
                <div className="max-w-lg w-full text-center">
                    <div className="w-20 h-20 bg-[#0A3D1A] dark:bg-[#3FCD6B]/10 dark:border dark:border-[#3FCD6B]/30 rounded-3xl flex items-center justify-center mx-auto mb-8 shadow-[0_0_40px_rgba(63,205,107,0.25)]">
                        {actionSuccess === "approved" ? (
                            <CheckCircle2 size={36} className="text-[#3FCD6B]" />
                        ) : (
                            <XCircle size={36} className="text-amber-500" />
                        )}
                    </div>
                    <h1 className="text-3xl font-black tracking-tight text-slate-900 dark:text-white mb-3" style={{ fontFamily: "Syne, sans-serif" }}>
                        {actionSuccess === "approved" ? "Milestone Approved" : "Changes Requested"}
                    </h1>
                    <p className="text-slate-500 dark:text-neutral-400 mb-10 text-sm leading-relaxed">
                        {actionSuccess === "approved"
                            ? "Escrow funds have been successfully released to the freelancer via instant open-banking transfer."
                            : "The freelancer has been notified to revise their submission based on your feedback."}
                    </p>
                    <button
                        onClick={handleReturnToJob}
                        className="bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] px-8 py-3.5 rounded-xl font-bold transition-transform active:scale-[0.98] inline-flex items-center gap-2 shadow-lg shadow-[#3FCD6B]/20"
                    >
                        Return to Job <ArrowRight size={16} />
                    </button>
                </div>
            </div>
        );
    }

    const data = submissionData!;

    // ---------------------------------------------------------------------------
    // MAIN RENDER
    // ---------------------------------------------------------------------------

    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 font-sans text-slate-900 dark:text-neutral-100 selection:bg-[#3FCD6B]/30 pb-24">
            <div className="sticky top-0 z-40 bg-[#f4f6f1]/80 dark:bg-neutral-950/80 backdrop-blur-md border-b border-slate-200 dark:border-neutral-800">
                <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
                    <button
                        onClick={handleReturnToJob}
                        className="group flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-slate-900 dark:text-neutral-400 dark:hover:text-white transition-colors"
                    >
                        <ArrowLeft size={16} className="transition-transform group-hover:-translate-x-1" />
                        Back to Workspace
                    </button>
                    <div className="flex items-center gap-2 text-xs font-semibold text-slate-400 dark:text-neutral-500 bg-white dark:bg-neutral-900 px-3 py-1.5 rounded-full border border-slate-200 dark:border-neutral-800">
                        <Briefcase size={14} /> {milestoneId.split('-')[0]}
                    </div>
                </div>
            </div>

            <div className="max-w-6xl mx-auto px-6 pt-10 w-full">
                <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-8 mb-16">
                    <div className="max-w-2xl">
                        <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-[#0F6E56] bg-[#E1F5EE] dark:bg-[#3FCD6B]/10 dark:text-[#3FCD6B] px-3 py-1.5 rounded-full uppercase tracking-wider mb-4">
                            <Clock size={12} /> Submitted {formatDate(data.submittedAt)}
                        </span>
                        <h1 className="text-4xl md:text-5xl font-black tracking-tight text-slate-900 dark:text-white mb-4 leading-tight" style={{ fontFamily: "Syne, sans-serif" }}>
                            Review Deliverables
                        </h1>
                        <p className="text-slate-500 dark:text-neutral-400 font-medium">
                            Milestone: <span className="text-slate-700 dark:text-neutral-200 font-semibold">{data.milestoneTitle}</span>
                            {" · "}
                            Freelancer: <span className="text-slate-700 dark:text-neutral-200 font-semibold">{data.freelancerName}</span>
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-12">
                    <div className="space-y-6">
                        {data.scopeItems && data.scopeItems.length > 0 && (
                            <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                                <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                    <CheckSquare size={16} className="text-[#3FCD6B]" />
                                    Freelancer Scope Checklist
                                </h2>
                                <p className="text-sm text-slate-500 dark:text-neutral-500 mb-5">
                                    Items the freelancer marked as completed for this submission.
                                </p>
                                <div className="space-y-2.5">
                                    {data.scopeItems.map((item) => (
                                        <div key={item.id} className="flex items-start gap-3 bg-slate-50 dark:bg-neutral-800/50 p-3.5 rounded-xl border border-slate-100 dark:border-neutral-700/50">
                                            {item.isCompleted ? (
                                                <CheckSquare size={18} className="text-[#3FCD6B] shrink-0 mt-0.5" />
                                            ) : (
                                                <Square size={18} className="text-slate-300 dark:text-neutral-600 shrink-0 mt-0.5" />
                                            )}
                                            <span className={`text-sm leading-relaxed ${item.isCompleted ? "text-slate-700 dark:text-neutral-200" : "text-slate-400 dark:text-neutral-500"}`}>
                                                {item.description}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {data.deliverableLink && (
                            <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                                <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                    <Link2 size={16} className="text-[#3FCD6B]" />
                                    Deliverable Link
                                </h2>
                                <a
                                    href={data.deliverableLink.match(/^https?:\/\//) ? data.deliverableLink : `https://${data.deliverableLink}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="mt-4 flex items-center justify-between bg-slate-50 dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 rounded-xl p-4 hover:border-[#3FCD6B] hover:bg-[#3FCD6B]/5 transition-colors group"
                                >
                                    <div className="flex items-center gap-3 overflow-hidden">
                                        <Globe size={20} className="text-slate-400 group-hover:text-[#3FCD6B] transition-colors shrink-0" />
                                        <span className="text-sm text-slate-700 dark:text-neutral-200 truncate font-medium">
                                            {data.deliverableLink}
                                        </span>
                                    </div>
                                    <ArrowRight size={16} className="text-slate-400 group-hover:text-[#3FCD6B] shrink-0" />
                                </a>
                            </div>
                        )}

                        {data.notes && (
                            <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                                <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                    <FileText size={16} className="text-[#3FCD6B]" />
                                    Submission Notes
                                </h2>
                                <div className="mt-4 bg-slate-50 dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 rounded-xl p-5">
                                    <p className="text-sm text-slate-700 dark:text-neutral-200 whitespace-pre-wrap font-mono leading-relaxed">
                                        {data.notes}
                                    </p>
                                </div>
                            </div>
                        )}

                        {data.files && data.files.length > 0 && (
                            <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                                <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                    <File size={16} className="text-[#3FCD6B]" />
                                    Attached Files
                                </h2>
                                <div className="mt-4 space-y-2">
                                    {data.files.map((file) => (
                                        <div key={file.fileId} className="flex items-center justify-between bg-slate-50 dark:bg-neutral-800/50 rounded-xl px-4 py-3 border border-slate-100 dark:border-neutral-700/50">
                                            <div className="flex items-center gap-3 min-w-0">
                                                <div className="w-8 h-8 bg-white dark:bg-neutral-900 rounded-lg flex items-center justify-center border border-slate-200 dark:border-neutral-700 shrink-0">
                                                    <File size={14} className="text-slate-400" />
                                                </div>
                                                <div className="min-w-0">
                                                    <p className="text-sm font-semibold text-slate-800 dark:text-neutral-200 truncate">
                                                        {file.fileName}
                                                    </p>
                                                    <p className="text-xs text-slate-400 dark:text-neutral-500">
                                                        {formatFileSize(file.fileSizeBytes)}
                                                    </p>
                                                </div>
                                            </div>

                                            {/* DUAL ACTION BUTTONS */}
                                            <div className="flex items-center gap-2 shrink-0">

                                                {/* 1. Only show Open in New Tab if the browser can actually read it */}
                                                {isBrowserViewable(file.fileName) && (
                                                    <button
                                                        onClick={() => handleFileAction(file.downloadUrl, file.fileName, "open")}
                                                        title="Open in new tab"
                                                        className="w-8 h-8 flex items-center justify-center rounded-lg bg-white dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 text-slate-600 dark:text-neutral-300 hover:text-[#3FCD6B] hover:border-[#3FCD6B] transition-colors"
                                                    >
                                                        <ExternalLink size={14} />
                                                    </button>
                                                )}

                                                {/* 2. ALWAYS show the Download Button */}
                                                <button
                                                    onClick={() => handleFileAction(file.downloadUrl, file.fileName, "download")}
                                                    title="Download file"
                                                    className="w-8 h-8 flex items-center justify-center rounded-lg bg-white dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 text-slate-600 dark:text-neutral-300 hover:text-[#3FCD6B] hover:border-[#3FCD6B] transition-colors"
                                                >
                                                    <Download size={14} />
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="space-y-6 lg:sticky lg:top-24 h-fit">
                        <div className="bg-[#0A3D1A] dark:bg-neutral-900 rounded-3xl p-8 text-white shadow-xl relative overflow-hidden border border-[#0A3D1A] dark:border-neutral-800">
                            <div className="absolute -top-12 -right-12 w-40 h-40 bg-[#3FCD6B]/15 blur-3xl rounded-full pointer-events-none" />
                            <div className="absolute -bottom-8 -left-8 w-32 h-32 bg-[#3FCD6B]/10 blur-2xl rounded-full pointer-events-none" />

                            <h3 className="text-xs font-bold text-white/60 dark:text-neutral-500 uppercase tracking-widest mb-6 relative z-10 flex items-center gap-2">
                                <ShieldCheck size={16} /> TrustBridge Decision
                            </h3>

                            <div className="relative z-10 mb-8">
                                <p className="text-sm text-white/80 dark:text-neutral-400 mb-2 font-medium">
                                    Escrow Amount
                                </p>
                                <p className="text-5xl font-black text-[#3FCD6B] tracking-tighter">
                                    {formatCurrency(data.milestoneAmount)}
                                </p>
                            </div>

                            <div className="relative z-10 space-y-3 pt-4 border-t border-white/10 dark:border-neutral-800">
                                <button
                                    onClick={handleApprove}
                                    disabled={isApproving || isRejecting}
                                    className="w-full bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] py-3.5 rounded-xl text-sm font-bold transition-transform active:scale-[0.98] flex items-center justify-center gap-2 shadow-lg shadow-[#3FCD6B]/20 disabled:opacity-50"
                                >
                                    {isApproving ? <Loader2 size={16} className="animate-spin" /> : <Landmark size={16} />}
                                    Approve &amp; Release Funds
                                </button>

                                <button
                                    onClick={handleRequestChanges}
                                    disabled={isApproving || isRejecting}
                                    className="w-full bg-white/5 hover:bg-white/10 text-white border border-white/10 py-3.5 rounded-xl text-sm font-bold transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                                >
                                    {isRejecting ? <Loader2 size={16} className="animate-spin" /> : <XCircle size={16} />}
                                    Request Changes
                                </button>
                            </div>

                            <p className="relative z-10 text-[11px] text-white/50 mt-5 leading-relaxed text-center">
                                Approving will instantly release funds to the freelancer via our secure pay-by-bank rail. This action cannot be reversed.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}