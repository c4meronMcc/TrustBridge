"use client";

import {JobWorkspaceProvider, useJobWorkspace} from "../../../../../../components/context/JobWorkSpaceContext";
import React, { useState, useEffect, useCallback, useRef, DragEvent } from "react";
import { useRouter } from "next/navigation";
import {
    ArrowLeft,
    ArrowRight,
    ShieldCheck,
    Upload,
    Link2,
    FileText,
    File,
    X,
    Plus,
    AlertCircle,
    Loader2,
    Lock,
    CheckCircle2,
    Globe,
    Briefcase,
    ChevronDown,
    ChevronUp,
    Fingerprint,
    Square,
    CheckSquare,
} from "lucide-react";

// ---------------------------------------------------------------------------
// TYPES
// ---------------------------------------------------------------------------

interface MilestoneDetailsDto {
    milestoneId: string;
    title: string;
    milestoneAmount: number;
    status: string;
    jobTitle: string;
    clientName: string;
    description?: string;
    scopeItems?: string[];
    deadline?: string;
}

interface UploadedFile {
    id: string;
    file: File;
}

interface ScopeItem {
    id: string;
    text: string;
    checked: boolean;
}

// ---------------------------------------------------------------------------
// PAGE COMPONENT
// ---------------------------------------------------------------------------

export default function RequestReleasePage({
                                               params,
                                           }: {
    params: Promise<{ jobId: string; milestoneId: string }>;
}) {
    const router = useRouter();
    const resolvedParams = React.use(params);
    const { jobId, milestoneId } = resolvedParams;

    // API state
    const [milestoneDetails, setMilestoneDetails] = useState<MilestoneDetailsDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitSuccess, setSubmitSuccess] = useState(false);

    // Form state
    const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
    const [dragActive, setDragActive] = useState(false);
    const [deliverableLink, setDeliverableLink] = useState("");
    const [notes, setNotes] = useState("");
    const [scopeItems, setScopeItems] = useState<ScopeItem[]>([]);
    const [newScopeItemText, setNewScopeItemText] = useState("");
    const [scopeExpanded, setScopeExpanded] = useState(true);
    const [declarationAccepted, setDeclarationAccepted] = useState(false);

    const fileInputRef = useRef<HTMLInputElement>(null);

    // ---------------------------------------------------------------------------
    // DATA FETCH
    // ---------------------------------------------------------------------------

    const { jobData } = useJobWorkspace();

    const fetchMilestoneDetails = useCallback(async () => {
        // 1. Check Session Storage First
        const storageKey = `trustbridge_milestone_${milestoneId}`;
        const storedData = sessionStorage.getItem(storageKey);

        if (storedData) {
            try {
                const parsedData = JSON.parse(storedData);

                setMilestoneDetails(parsedData);

                // Map the scope checklist if it exists
                if (parsedData.scopeItems?.length) {
                    setScopeItems(
                        parsedData.scopeItems.map((text: string, i: number) => ({
                            id: `storage-scope-${i}`,
                            text,
                            checked: false,
                        }))
                    );
                }

                setIsLoading(false);
                return;
            } catch (error) {
                console.error("Failed to parse stored milestone data:", error);
            }
        }

        console.log("No valid storage found. Falling back to API fetch...");

        try {
            setIsLoading(true);
            setError(null);

            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL || ''}/api/milestone/details?milestoneId=${milestoneId}`,
                { credentials: "include" }
            );

            if (response.status === 401 || response.status === 403) {
                throw new Error("Unauthorized. Please log in again.");
            }
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}`);
            }

            const data: MilestoneDetailsDto = await response.json();
            setMilestoneDetails(data);

            if (data.scopeItems?.length) {
                setScopeItems(
                    data.scopeItems.map((text, i) => ({
                        id: `api-scope-${i}`,
                        text,
                        checked: false,
                    }))
                );
            }
        } catch (err: any) {
            setError(err.message || "An unexpected error occurred.");
        } finally {
            setIsLoading(false);
        }
    }, [milestoneId]);

// Keep the useEffect exactly the same, it will now run our updated callback
    useEffect(() => {
        fetchMilestoneDetails();
    }, [fetchMilestoneDetails]);

    // ---------------------------------------------------------------------------
    // FILE HANDLING
    // ---------------------------------------------------------------------------

    const processFiles = (fileList: FileList | null) => {
        if (!fileList) return;
        const newFiles: UploadedFile[] = Array.from(fileList).map((f) => ({
            id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
            file: f,
        }));
        setUploadedFiles((prev) => [...prev, ...newFiles]);
    };

    const handleDrag = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(e.type === "dragenter" || e.type === "dragover");
    };

    const handleDrop = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);
        processFiles(e.dataTransfer.files);
    };

    const addScopeItem = () => {
        if (!newScopeItemText.trim()) return;
        setScopeItems((prev) => [
            ...prev,
            { id: `manual-${Date.now()}`, text: newScopeItemText.trim(), checked: false },
        ]);
        setNewScopeItemText("");
    };

    const toggleScopeItem = (id: string) => {
        setScopeItems((prev) =>
            prev.map((item) => (item.id === id ? { ...item, checked: !item.checked } : item))
        );
    };

    const removeScopeItem = (id: string) => {
        setScopeItems((prev) => prev.filter((item) => item.id !== id));
    };

    // ---------------------------------------------------------------------------
    // SUBMISSION
    // ---------------------------------------------------------------------------

    const hasDeliverable =
        uploadedFiles.length > 0 || deliverableLink.trim().length > 0;
    const canSubmit = hasDeliverable && notes.trim().length >= 10 && declarationAccepted;

    const handleSubmit = async () => {
        if (!canSubmit || isSubmitting) return;
        setIsSubmitting(true);
        try {
            const formData = new FormData();
            formData.append("milestoneId", milestoneId);
            formData.append("deliverableLink", deliverableLink);
            formData.append("notes", notes);
            formData.append("declarationAccepted", "true");
            formData.append("scopeItems", JSON.stringify(scopeItems));
            uploadedFiles.forEach(({ file }) => formData.append("files", file));

            const response = await fetch(
                `http://localhost:8080/api/milestone/request-release/freelancer`,
                { method: "POST", credentials: "include", body: formData }
            );

            if (!response.ok) throw new Error(`Submission failed: ${response.status}`);
            setSubmitSuccess(true);
        } catch (err: any) {
            setError(err.message || "Submission failed. Please try again.");
        } finally {
            setIsSubmitting(false);
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

    const detectLinkPlatform = (url: string): string => {
        if (!url) return "";
        if (url.includes("github.com")) return "GitHub";
        if (url.includes("figma.com")) return "Figma";
        if (url.includes("drive.google.com") || url.includes("docs.google.com"))
            return "Google Drive";
        if (url.includes("notion.so")) return "Notion";
        if (url.includes("vercel.app") || url.includes("vercel.com")) return "Vercel";
        if (url.includes("loom.com")) return "Loom";
        if (url.includes("staging.") || url.includes(".dev") || url.includes(".preview"))
            return "Staging";
        return "External Link";
    };

    // ---------------------------------------------------------------------------
    // LOADING STATE
    // ---------------------------------------------------------------------------

    if (isLoading) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex flex-col items-center justify-center text-[#0A3D1A] dark:text-[#3FCD6B]">
                <div className="w-14 h-14 bg-white dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-2xl flex items-center justify-center shadow-lg dark:shadow-none animate-pulse mb-6">
                    <ShieldCheck className="text-[#3FCD6B]" size={28} />
                </div>
                <div className="flex gap-2">
                    {[0, 1, 2].map((i) => (
                        <div
                            key={i}
                            className="w-2 h-2 bg-[#3FCD6B] rounded-full animate-bounce"
                            style={{ animationDelay: `${i * 150}ms` }}
                        />
                    ))}
                </div>
            </div>
        );
    }

    // ---------------------------------------------------------------------------
    // ERROR STATE
    // ---------------------------------------------------------------------------

    if (error && !milestoneDetails) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex items-center justify-center p-6">
                <div className="bg-white dark:bg-neutral-900 text-red-600 dark:text-red-400 p-8 rounded-3xl border border-red-100 dark:border-red-900/30 max-w-md w-full text-center shadow-sm">
                    <AlertCircle size={48} className="mx-auto mb-4 opacity-80" />
                    <p className="text-xl font-bold mb-2 tracking-tight">Could Not Load Milestone</p>
                    <p className="text-sm text-slate-500 dark:text-neutral-400 mb-8">{error}</p>
                    <button
                        onClick={() => router.back()}
                        className="w-full bg-[#0A3D1A] dark:bg-white text-white dark:text-neutral-950 py-3 rounded-xl font-bold hover:opacity-90 transition-opacity"
                    >
                        Go Back
                    </button>
                </div>
            </div>
        );
    }

    // ---------------------------------------------------------------------------
    // SUCCESS STATE
    // ---------------------------------------------------------------------------

    if (submitSuccess && milestoneDetails) {
        return (
            <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex items-center justify-center p-6">
                <div className="max-w-lg w-full text-center">
                    <div className="w-20 h-20 bg-[#0A3D1A] dark:bg-[#3FCD6B]/10 dark:border dark:border-[#3FCD6B]/30 rounded-3xl flex items-center justify-center mx-auto mb-8 shadow-[0_0_40px_rgba(63,205,107,0.25)]">
                        <CheckCircle2 size={36} className="text-[#3FCD6B]" />
                    </div>
                    <h1
                        className="text-3xl font-black tracking-tight text-slate-900 dark:text-white mb-3"
                        style={{ fontFamily: "Syne, sans-serif" }}
                    >
                        Submission Received
                    </h1>
                    <p className="text-slate-500 dark:text-neutral-400 mb-2">
                        Your deliverables for{" "}
                        <span className="font-semibold text-slate-700 dark:text-neutral-200">
                            {milestoneDetails.title}
                        </span>{" "}
                        have been logged and timestamped.
                    </p>
                    <p className="text-slate-500 dark:text-neutral-400 mb-10 text-sm leading-relaxed">
                        <span className="font-medium text-[#0A3D1A] dark:text-[#3FCD6B]">
                            {milestoneDetails.clientName}
                        </span>{" "}
                        has been notified and has 7 days to review. Escrow funds remain held until they approve or the dispute window closes.
                    </p>
                    <button
                        onClick={() => router.push(`/dashboard/freelancer/jobs/${jobId}`)}
                        className="bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] px-8 py-3.5 rounded-xl font-bold transition-transform active:scale-[0.98] inline-flex items-center gap-2 shadow-lg shadow-[#3FCD6B]/20"
                    >
                        Return to Job <ArrowRight size={16} />
                    </button>
                </div>
            </div>
        );
    }

    // ---------------------------------------------------------------------------
    // MAIN RENDER
    // ---------------------------------------------------------------------------

    const milestone = milestoneDetails!;
    const platform = detectLinkPlatform(deliverableLink);
    const checkedScopeCount = scopeItems.filter((i) => i.checked).length;
    const today = new Date().toLocaleDateString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric",
    });

    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 font-sans text-slate-900 dark:text-neutral-100 selection:bg-[#3FCD6B]/30 pb-24">

            {/* ------------------------------------------------------------------ */}
            {/* TOP NAVIGATION                                                       */}
            {/* ------------------------------------------------------------------ */}
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
                        <Briefcase size={14} /> {milestoneId}
                    </div>
                </div>
            </div>

            <div className="max-w-6xl mx-auto px-6 pt-10 w-full">

                {/* ------------------------------------------------------------------ */}
                {/* HERO                                                                */}
                {/* ------------------------------------------------------------------ */}
                <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-8 mb-16">
                    <div className="max-w-2xl">
                        <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-[#0F6E56] bg-[#E1F5EE] dark:bg-[#3FCD6B]/10 dark:text-[#3FCD6B] px-3 py-1.5 rounded-full uppercase tracking-wider mb-4">
                            <Lock size={12} /> Funds Secured in Escrow
                        </span>
                        <h1
                            className="text-4xl md:text-5xl font-black tracking-tight text-slate-900 dark:text-white mb-4 leading-tight"
                            style={{ fontFamily: "Syne, sans-serif" }}
                        >
                            Submit Deliverables
                        </h1>
                        <p className="text-slate-500 dark:text-neutral-400 font-medium">
                            Milestone:{" "}
                            <span className="text-slate-700 dark:text-neutral-200 font-semibold">
                                {milestone.title}
                            </span>
                            {" · "}
                            Client:{" "}
                            <span className="text-slate-700 dark:text-neutral-200 font-semibold">
                                {milestone.clientName}
                            </span>
                        </p>
                    </div>
                    <div className="text-left lg:text-right shrink-0">
                        <p className="text-xs font-bold text-slate-400 dark:text-neutral-500 uppercase tracking-widest mb-2">
                            Release Amount
                        </p>
                        <p className="text-4xl font-black text-[#0A3D1A] dark:text-[#3FCD6B] tracking-tighter">
                            {formatCurrency(milestone.milestoneAmount)}
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-12">

                    {/* ============================================================ */}
                    {/* LEFT COLUMN: SUBMISSION FORM                                  */}
                    {/* ============================================================ */}
                    <div className="space-y-6">

                        {/* -------------------------------------------------------- */}
                        {/* SECTION 1: FILES & ATTACHMENTS                           */}
                        {/* -------------------------------------------------------- */}
                        <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                            <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                <Upload size={16} className="text-[#3FCD6B]" />
                                Files &amp; Attachments
                            </h2>
                            <p className="text-sm text-slate-500 dark:text-neutral-500 mb-5">
                                Upload your final deliverables, reference documents, or supporting assets.
                            </p>

                            {/* Drop Zone */}
                            <div
                                onDragEnter={handleDrag}
                                onDragOver={handleDrag}
                                onDragLeave={handleDrag}
                                onDrop={handleDrop}
                                onClick={() => fileInputRef.current?.click()}
                                className={`relative cursor-pointer rounded-2xl border-2 border-dashed p-10 text-center transition-all duration-200 ${
                                    dragActive
                                        ? "border-[#3FCD6B] bg-[#3FCD6B]/5 dark:bg-[#3FCD6B]/10 scale-[1.01]"
                                        : "border-slate-200 dark:border-neutral-700 hover:border-[#3FCD6B]/60 hover:bg-[#3FCD6B]/5 dark:hover:bg-[#3FCD6B]/5"
                                }`}
                            >
                                <div
                                    className={`w-12 h-12 rounded-2xl flex items-center justify-center mx-auto mb-4 transition-colors duration-200 ${
                                        dragActive
                                            ? "bg-[#3FCD6B]/20"
                                            : "bg-slate-100 dark:bg-neutral-800"
                                    }`}
                                >
                                    <Upload
                                        size={20}
                                        className={dragActive ? "text-[#3FCD6B]" : "text-slate-400"}
                                    />
                                </div>
                                <p className="font-bold text-slate-700 dark:text-neutral-200 mb-1">
                                    {dragActive ? "Release to attach files" : "Drop files or click to upload"}
                                </p>
                                <p className="text-xs text-slate-400 dark:text-neutral-500">
                                    PDF, PNG, JPG, ZIP, MP4 · Max 100 MB per file
                                </p>
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    multiple
                                    className="hidden"
                                    onChange={(e) => processFiles(e.target.files)}
                                />
                            </div>

                            {/* File List */}
                            {uploadedFiles.length > 0 && (
                                <div className="mt-4 space-y-2">
                                    {uploadedFiles.map(({ id, file }) => (
                                        <div
                                            key={id}
                                            className="flex items-center gap-3 bg-slate-50 dark:bg-neutral-800/50 rounded-xl px-4 py-3 border border-slate-100 dark:border-neutral-700/50"
                                        >
                                            <div className="w-8 h-8 bg-white dark:bg-neutral-900 rounded-lg flex items-center justify-center border border-slate-200 dark:border-neutral-700 shrink-0">
                                                {file.type === "application/pdf" ? (
                                                    <FileText size={14} className="text-red-400" />
                                                ) : file.type.startsWith("image/") ? (
                                                    <File size={14} className="text-purple-400" />
                                                ) : (
                                                    <File size={14} className="text-slate-400" />
                                                )}
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <p className="text-sm font-semibold text-slate-800 dark:text-neutral-200 truncate">
                                                    {file.name}
                                                </p>
                                                <p className="text-xs text-slate-400 dark:text-neutral-500 flex items-center gap-2 mt-0.5">
                                                    {formatFileSize(file.size)}
                                                    <span className="inline-flex items-center gap-1 text-[#3FCD6B]">
                                                        <Fingerprint size={10} />
                                                        SHA-256 verified
                                                    </span>
                                                </p>
                                            </div>
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setUploadedFiles((prev) =>
                                                        prev.filter((f) => f.id !== id)
                                                    );
                                                }}
                                                className="w-7 h-7 flex items-center justify-center rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                            >
                                                <X size={14} />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* -------------------------------------------------------- */}
                        {/* SECTION 2: DELIVERABLE LINK                              */}
                        {/* -------------------------------------------------------- */}
                        <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                            <h2 className="text-base font-bold text-slate-900 dark:text-white mb-1 flex items-center gap-2">
                                <Link2 size={16} className="text-[#3FCD6B]" />
                                Deliverable Link
                            </h2>
                            <p className="text-sm text-slate-500 dark:text-neutral-500 mb-5">
                                GitHub repo, Figma board, Vercel preview, Notion doc, Google Drive, or any URL.
                            </p>
                            <div className="flex items-center bg-slate-50 dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 rounded-xl focus-within:ring-2 focus-within:ring-[#3FCD6B]/50 focus-within:border-[#3FCD6B] transition-all overflow-hidden">
                                <div className="pl-4 flex items-center gap-2 shrink-0">
                                    <Globe size={16} className="text-slate-400" />
                                    {platform && (
                                        <span className="text-xs font-bold text-[#3FCD6B] bg-[#3FCD6B]/10 px-2 py-0.5 rounded-full whitespace-nowrap">
                                            {platform}
                                        </span>
                                    )}
                                </div>
                                <input
                                    type="url"
                                    value={deliverableLink}
                                    onChange={(e) => setDeliverableLink(e.target.value)}
                                    placeholder="https://github.com/your-org/project-repo"
                                    className="flex-1 bg-transparent py-3.5 px-3 text-sm text-slate-800 dark:text-neutral-100 placeholder:text-slate-400 dark:placeholder:text-neutral-600 focus:outline-none"
                                />
                            </div>
                        </div>

                        {/* -------------------------------------------------------- */}
                        {/* SECTION 3: SUBMISSION NOTES                              */}
                        {/* -------------------------------------------------------- */}
                        <div className="bg-white dark:bg-neutral-900/50 rounded-3xl p-6 border border-slate-100 dark:border-neutral-800">
                            <div className="flex justify-between items-start mb-1">
                                <h2 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
                                    <FileText size={16} className="text-[#3FCD6B]" />
                                    Submission Notes
                                </h2>
                                <span
                                    className={`text-xs font-bold transition-colors ${
                                        notes.length === 0
                                            ? "text-slate-300 dark:text-neutral-700"
                                            : notes.length < 10
                                                ? "text-amber-500"
                                                : "text-[#3FCD6B]"
                                    }`}
                                >
                                    {notes.length} chars
                                </span>
                            </div>
                            <p className="text-sm text-slate-500 dark:text-neutral-500 mb-5">
                                Explain what was built, decisions made, how to review, and any known caveats.
                            </p>
                            <textarea
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                rows={7}
                                placeholder={`What was delivered:\n— Feature X implemented as per spec, including edge case Y...\n\nHow to review:\n— Log in at staging.example.com with test@example.com / password123\n\nNotes:\n— Minor scope addition: added dark mode per client verbal request`}
                                className="w-full bg-slate-50 dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 rounded-xl p-4 text-sm text-slate-800 dark:text-neutral-100 placeholder:text-slate-400 dark:placeholder:text-neutral-600 focus:outline-none focus:ring-2 focus:ring-[#3FCD6B]/50 focus:border-[#3FCD6B] transition-all resize-none font-mono leading-relaxed"
                            />
                            {notes.length > 0 && notes.length < 10 && (
                                <p className="text-xs text-amber-600 dark:text-amber-500 mt-2 font-medium">
                                    Minimum 10 characters required.
                                </p>
                            )}
                        </div>

                        {/* -------------------------------------------------------- */}
                        {/* SECTION 4: SCOPE CHECKLIST                               */}
                        {/* -------------------------------------------------------- */}
                        <div className="bg-white dark:bg-neutral-900/50 rounded-3xl border border-slate-100 dark:border-neutral-800 overflow-hidden">
                            <button
                                onClick={() => setScopeExpanded((p) => !p)}
                                className="w-full flex items-center justify-between p-6 text-left hover:bg-slate-50/50 dark:hover:bg-neutral-800/30 transition-colors"
                            >
                                <div>
                                    <h2 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
                                        <CheckSquare size={16} className="text-[#3FCD6B]" />
                                        Scope Checklist
                                        <span className="text-xs font-medium text-slate-400 dark:text-neutral-500 normal-case tracking-normal">
                                            — Optional
                                        </span>
                                    </h2>
                                    {scopeItems.length > 0 && (
                                        <p className="text-xs text-slate-400 dark:text-neutral-500 mt-0.5">
                                            {checkedScopeCount} of {scopeItems.length} items confirmed
                                        </p>
                                    )}
                                </div>
                                {scopeExpanded ? (
                                    <ChevronUp size={18} className="text-slate-400 shrink-0" />
                                ) : (
                                    <ChevronDown size={18} className="text-slate-400 shrink-0" />
                                )}
                            </button>

                            {scopeExpanded && (
                                <div className="px-6 pb-6 border-t border-slate-100 dark:border-neutral-800 pt-5">
                                    {scopeItems.length === 0 ? (
                                        <p className="text-sm text-slate-400 dark:text-neutral-500 mb-5">
                                            Map agreed deliverables from the brief. Tick items off to confirm each requirement was met before submitting.
                                        </p>
                                    ) : (
                                        <div className="space-y-2 mb-5">
                                            {scopeItems.map((item) => (
                                                <div
                                                    key={item.id}
                                                    className={`flex items-center gap-3 p-3 rounded-xl border transition-all cursor-pointer group select-none ${
                                                        item.checked
                                                            ? "bg-[#3FCD6B]/5 border-[#3FCD6B]/20 dark:border-[#3FCD6B]/20"
                                                            : "bg-slate-50 dark:bg-neutral-800/50 border-slate-100 dark:border-neutral-700/50 hover:border-slate-200 dark:hover:border-neutral-600"
                                                    }`}
                                                    onClick={() => toggleScopeItem(item.id)}
                                                >
                                                    {item.checked ? (
                                                        <CheckSquare size={16} className="text-[#3FCD6B] shrink-0" />
                                                    ) : (
                                                        <Square size={16} className="text-slate-300 dark:text-neutral-600 shrink-0" />
                                                    )}
                                                    <span
                                                        className={`text-sm flex-1 transition-colors leading-relaxed ${
                                                            item.checked
                                                                ? "text-slate-400 dark:text-neutral-600 line-through"
                                                                : "text-slate-700 dark:text-neutral-200"
                                                        }`}
                                                    >
                                                        {item.text}
                                                    </span>
                                                    <button
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            removeScopeItem(item.id);
                                                        }}
                                                        className="opacity-0 group-hover:opacity-100 w-6 h-6 flex items-center justify-center rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all"
                                                    >
                                                        <X size={12} />
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    )}

                                    {/* Add Item Input */}
                                    <div className="flex gap-2">
                                        <input
                                            type="text"
                                            value={newScopeItemText}
                                            onChange={(e) => setNewScopeItemText(e.target.value)}
                                            onKeyDown={(e) => {
                                                if (e.key === "Enter") {
                                                    e.preventDefault();
                                                    addScopeItem();
                                                }
                                            }}
                                            placeholder="e.g. Responsive layout across all breakpoints"
                                            className="flex-1 bg-slate-50 dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 rounded-xl px-4 py-2.5 text-sm text-slate-800 dark:text-neutral-100 placeholder:text-slate-400 dark:placeholder:text-neutral-600 focus:outline-none focus:ring-2 focus:ring-[#3FCD6B]/50 focus:border-[#3FCD6B] transition-all"
                                        />
                                        <button
                                            onClick={addScopeItem}
                                            disabled={!newScopeItemText.trim()}
                                            className="w-10 h-10 bg-[#0A3D1A] dark:bg-[#3FCD6B] text-[#3FCD6B] dark:text-[#0A3D1A] rounded-xl flex items-center justify-center disabled:opacity-30 hover:opacity-90 transition-opacity shrink-0"
                                        >
                                            <Plus size={18} />
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* -------------------------------------------------------- */}
                        {/* SECTION 5: DECLARATION OF HANDOVER                       */}
                        {/* Signature dark card — visually anchors the legal weight. */}
                        {/* -------------------------------------------------------- */}
                        <div className="bg-[#0A3D1A] rounded-3xl p-6 border border-[#0A3D1A] dark:bg-neutral-900 dark:border-neutral-800 relative overflow-hidden">
                            {/* Glassmorphic orbs */}
                            <div className="absolute -top-12 -right-12 w-40 h-40 bg-[#3FCD6B]/15 blur-3xl rounded-full pointer-events-none" />
                            <div className="absolute -bottom-8 -left-8 w-32 h-32 bg-[#3FCD6B]/10 blur-2xl rounded-full pointer-events-none" />

                            <div className="relative z-10">
                                <div className="flex items-center gap-2 mb-5">
                                    <Fingerprint size={18} className="text-[#3FCD6B]" />
                                    <h2 className="text-base font-bold text-white">
                                        Declaration of Handover
                                    </h2>
                                </div>

                                <div className="bg-white/5 dark:bg-neutral-800/60 rounded-2xl p-5 mb-5 border border-white/10 dark:border-neutral-700/50">
                                    <p className="text-sm text-white/70 dark:text-neutral-400 mb-3 leading-relaxed">
                                        By checking the box below, I, the service provider, formally declare that:
                                    </p>
                                    <ol className="space-y-2.5">
                                        {[
                                            "The work submitted constitutes complete and satisfactory fulfilment of the agreed milestone specifications.",
                                            "All deliverables are original or appropriately licensed, and free from third-party intellectual property claims.",
                                            "I am formally requesting the release of escrowed funds in accordance with the TrustBridge escrow agreement.",
                                            "I understand that submitting a false or misleading declaration may constitute a breach of contract and applicable UK law.",
                                        ].map((clause, i) => (
                                            <li key={i} className="flex gap-3 text-sm text-white/60 dark:text-neutral-400 leading-relaxed">
                                                <span className="text-[#3FCD6B] font-black shrink-0 mt-0.5 text-xs">
                                                    {i + 1}.
                                                </span>
                                                {clause}
                                            </li>
                                        ))}
                                    </ol>
                                </div>

                                <label className="flex items-start gap-4 cursor-pointer group">
                                    <div
                                        onClick={() => setDeclarationAccepted((p) => !p)}
                                        className={`mt-0.5 w-6 h-6 rounded-md border-2 flex items-center justify-center shrink-0 transition-all duration-200 ${
                                            declarationAccepted
                                                ? "bg-[#3FCD6B] border-[#3FCD6B]"
                                                : "bg-transparent border-white/30 dark:border-neutral-600 group-hover:border-[#3FCD6B]/70"
                                        }`}
                                    >
                                        {declarationAccepted && (
                                            <CheckCircle2 size={14} className="text-[#0A3D1A]" />
                                        )}
                                    </div>
                                    <span className="text-sm text-white/80 dark:text-neutral-300 leading-relaxed font-medium">
                                        I declare this work complete according to the agreed specifications, and I am formally requesting the release of funds from escrow.
                                    </span>
                                </label>
                            </div>
                        </div>

                        {/* -------------------------------------------------------- */}
                        {/* CTA ROW                                                   */}
                        {/* -------------------------------------------------------- */}
                        <div className="space-y-4 pt-2">
                            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                                <button
                                    onClick={handleSubmit}
                                    disabled={!canSubmit || isSubmitting}
                                    className="flex-1 bg-[#3FCD6B] hover:bg-[#34b35c] disabled:bg-slate-200 dark:disabled:bg-neutral-800 disabled:text-slate-400 dark:disabled:text-neutral-600 disabled:cursor-not-allowed text-[#0A3D1A] py-4 rounded-xl text-sm font-bold transition-all active:scale-[0.98] flex items-center justify-center gap-2 shadow-lg shadow-[#3FCD6B]/20 disabled:shadow-none"
                                >
                                    {isSubmitting ? (
                                        <>
                                            <Loader2 size={16} className="animate-spin" />
                                            Submitting Deliverables...
                                        </>
                                    ) : (
                                        <>
                                            Submit &amp; Request Release
                                            <ArrowRight size={16} />
                                        </>
                                    )}
                                </button>
                                <button
                                    onClick={() => router.back()}
                                    className="px-6 py-4 rounded-xl text-sm font-bold text-slate-500 dark:text-neutral-400 hover:text-slate-700 dark:hover:text-neutral-200 border border-slate-200 dark:border-neutral-800 hover:border-slate-300 dark:hover:border-neutral-700 transition-all"
                                >
                                    Cancel
                                </button>
                            </div>

                            {/* Validation hint — only shown when form is incomplete */}
                            {!canSubmit && (
                                <div className="flex items-start gap-3 bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 rounded-2xl px-4 py-3">
                                    <AlertCircle size={15} className="text-amber-500 mt-0.5 shrink-0" />
                                    <p className="text-xs text-amber-700 dark:text-amber-400 font-medium leading-relaxed">
                                        To submit: provide at least one file or deliverable link, write submission notes (min. 10 characters), and check the declaration.
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* ============================================================ */}
                    {/* RIGHT COLUMN: VAULT + STATUS WIDGETS                         */}
                    {/* ============================================================ */}
                    <div className="space-y-6 lg:sticky lg:top-24 h-fit">

                        {/* Vault Card */}
                        <div className="bg-[#0A3D1A] dark:bg-neutral-900 rounded-3xl p-8 text-white shadow-xl relative overflow-hidden border border-[#0A3D1A] dark:border-neutral-800">
                            <div className="absolute -top-12 -right-12 w-40 h-40 bg-[#3FCD6B]/20 blur-3xl rounded-full pointer-events-none" />
                            <div className="absolute -bottom-8 -left-8 w-32 h-32 bg-[#3FCD6B]/10 blur-2xl rounded-full pointer-events-none" />

                            <h3 className="text-xs font-bold text-white/60 dark:text-neutral-500 uppercase tracking-widest mb-6 relative z-10 flex items-center gap-2">
                                <ShieldCheck size={16} /> TrustBridge Vault
                            </h3>

                            <div className="relative z-10 mb-8">
                                <p className="text-sm text-white/80 dark:text-neutral-400 mb-2 font-medium">
                                    Awaiting Your Submission
                                </p>
                                <p className="text-5xl font-black text-[#3FCD6B] tracking-tighter">
                                    {formatCurrency(milestone.milestoneAmount)}
                                </p>
                            </div>

                            <div className="pt-6 border-t border-white/10 dark:border-neutral-800 relative z-10 space-y-3">
                                <p className="text-[10px] font-bold text-white/40 dark:text-neutral-600 uppercase tracking-widest mb-4">
                                    What Happens Next
                                </p>
                                {[
                                    { step: "1", text: "Submission logged &amp; timestamped on TrustBridge." },
                                    { step: "2", text: `${milestone.clientName} is notified to review.` },
                                    { step: "3", text: "Client approves → funds released to your account." },
                                    { step: "4", text: "No response in 7 days → auto-approval or dispute window." },
                                ].map(({ step, text }) => (
                                    <div key={step} className="flex gap-3 items-start">
                                        <span className="w-5 h-5 bg-[#3FCD6B]/20 text-[#3FCD6B] rounded-full text-[10px] font-black flex items-center justify-center shrink-0 mt-0.5">
                                            {step}
                                        </span>
                                        <p
                                            className="text-sm text-white/60 dark:text-neutral-400 leading-relaxed"
                                            dangerouslySetInnerHTML={{ __html: text }}
                                        />
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Submission Protection Card */}
                        <div className="bg-white dark:bg-neutral-900 border border-slate-200 dark:border-neutral-800 rounded-3xl p-6 shadow-sm">
                            <h3 className="font-bold text-slate-900 dark:text-white mb-2 flex items-center gap-2">
                                <Lock size={16} className="text-slate-400" /> Submission Protection
                            </h3>
                            <p className="text-sm text-slate-500 dark:text-neutral-400 leading-relaxed mb-4">
                                Every file is SHA-256 hashed and timestamped at upload. This creates an immutable proof-of-delivery record recognised under UK contract law.
                            </p>
                            <div className="space-y-2.5">
                                {[
                                    { label: "Delivery Date", value: today },
                                    { label: "Review Window", value: "7 days" },
                                    { label: "Dispute Available", value: "Yes — via TrustBridge" },
                                    { label: "Escrow Provider", value: "FCA-regulated" },
                                ].map(({ label, value }) => (
                                    <div key={label} className="flex justify-between items-center text-sm">
                                        <span className="text-slate-400 dark:text-neutral-500">{label}</span>
                                        <span className="font-semibold text-slate-700 dark:text-neutral-300">
                                            {value}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Scope Progress — only shown when there are scope items */}
                        {scopeItems.length > 0 && (
                            <div
                                className={`rounded-3xl p-5 border transition-all duration-300 ${
                                    checkedScopeCount === scopeItems.length
                                        ? "bg-[#3FCD6B]/5 border-[#3FCD6B]/20 dark:border-[#3FCD6B]/20"
                                        : "bg-white dark:bg-neutral-900 border-slate-200 dark:border-neutral-800"
                                }`}
                            >
                                <div className="flex items-center justify-between mb-3">
                                    <p className="text-sm font-bold text-slate-700 dark:text-neutral-200">
                                        Scope Coverage
                                    </p>
                                    <span
                                        className={`text-sm font-black transition-colors ${
                                            checkedScopeCount === scopeItems.length
                                                ? "text-[#3FCD6B]"
                                                : "text-slate-400"
                                        }`}
                                    >
                                        {checkedScopeCount}/{scopeItems.length}
                                    </span>
                                </div>
                                <div className="w-full bg-slate-100 dark:bg-neutral-800 rounded-full h-1.5 mb-2">
                                    <div
                                        className="h-1.5 bg-[#3FCD6B] rounded-full transition-all duration-500"
                                        style={{
                                            width: `${scopeItems.length ? (checkedScopeCount / scopeItems.length) * 100 : 0}%`,
                                        }}
                                    />
                                </div>
                                {checkedScopeCount < scopeItems.length ? (
                                    <p className="text-xs text-slate-400 dark:text-neutral-500">
                                        {scopeItems.length - checkedScopeCount} items still unchecked.
                                    </p>
                                ) : (
                                    <p className="text-xs text-[#3FCD6B] font-semibold flex items-center gap-1">
                                        <CheckCircle2 size={12} /> All scope items confirmed.
                                    </p>
                                )}
                            </div>
                        )}
                    </div>

                </div>
            </div>
        </div>
    );
}