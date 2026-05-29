"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';

// ─── TYPES ─────────────────────────────────────────────────────────────────
interface Milestone {
    id: string;
    deliverable: string;
    amount: number;
    dueDate: string;
    duration: string;
}

interface JobDraft {
    title: string;
    description: string;
    value: string;
    currency: string;
    clientFirstName: string;
    clientLastName: string;
    clientEmail: string;
    clientCompany: string;
    clientNumber: string;
    clientAddress: string;
    milestones: Milestone[];
}

// ─── DTO TYPES (mirrors backend JobCreationDto) ────────────────────────────
interface MilestoneCreationDto {
    title: string;
    amount: number;
    sequence_amount: number;
}

interface JobCreationDto {
    clientEmail: string | null;
    clientPhoneNumber: string | null;
    clientFirstName: string | null;
    clientLastName: string | null;
    title: string;
    description: string;
    amount: number;
    currency: string;
    countryCode: string;
    milestones: MilestoneCreationDto[] | null;
}

// ─── HELPERS ───────────────────────────────────────────────────────────────

/** Strips display label from currency selector value, e.g. "GBP (£)" → "GBP" */
function parseCurrencyCode(value: string): string {
    return value.split(' ')[0].trim();
}

/**
 * Derives a country code from the currency as a reasonable default.
 * In production you'd add a dedicated country-code field to the form.
 */
function deriveCountryCode(currencyCode: string): string {
    const map: Record<string, string> = {
        GBP: 'GB',
        USD: 'US',
        EUR: 'EU',
    };
    return map[currencyCode] ?? 'GB';
}

/**
 * Maps the frontend JobDraft to the backend JobCreationDto shape.
 * `freelancerEmail` is resolved from the authenticated session on the
 * backend in production; here we pass an empty string as a placeholder —
 * swap this for `session.user.email` when auth context is wired up.
 */
function buildJobCreationDto(draft: JobDraft, freelancerEmail: string): JobCreationDto {
    const currencyCode = parseCurrencyCode(draft.currency);
    const countryCode  = deriveCountryCode(currencyCode);

    const milestones: MilestoneCreationDto[] = draft.milestones.map((ms, index) => ({
        title:           ms.deliverable,
        amount:          ms.amount,
        sequence_amount: index + 1,
    }));

    return {
        clientEmail:      draft.clientEmail.trim()       || null,
        clientPhoneNumber: draft.clientNumber.trim()     || null,
        clientFirstName:  draft.clientFirstName.trim()   || null,
        clientLastName:   draft.clientLastName.trim()    || null,
        title:            draft.title.trim(),
        description:      draft.description.trim(),
        amount:           parseFloat(draft.value),
        currency:         currencyCode,
        countryCode:      countryCode,
        milestones:       milestones.length > 0 ? milestones : null,
    };
}

// ─── VALIDATION TYPES ──────────────────────────────────────────────────────
type FieldErrors = Partial<Record<string, string>>;

// ─── ANIMATIONS ────────────────────────────────────────────────────────────
const slideInStyle: React.CSSProperties = {
    animation: 'slideInFromRight 0.28s cubic-bezier(0.22, 0.61, 0.36, 1) both',
};

const GLOBAL_STYLES = `
@keyframes slideInFromRight {
  from { opacity: 0; transform: translateX(18px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}
.drawer-enter {
  animation: drawerIn 0.25s cubic-bezier(0.22, 0.61, 0.36, 1) both;
}
@keyframes drawerIn {
  from { transform: translateX(-100%); }
  to   { transform: translateX(0); }
}
.error-msg {
  animation: fadeInUp 0.18s ease both;
}
`;

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateStep(step: number, draft: JobDraft, confirmed: boolean): FieldErrors {
    const errors: FieldErrors = {};

    if (step === 1) {
        if (!draft.title.trim())       errors.title       = 'Job title is required.';
        if (!draft.description.trim()) errors.description = 'Description is required.';
        if (!draft.value.trim())       errors.value       = 'Total job value is required.';
        else if (isNaN(parseFloat(draft.value)) || parseFloat(draft.value) <= 0)
            errors.value = 'Enter a valid positive value.';
    }

    if (step === 2) {
        if (!draft.clientFirstName.trim()) errors.clientFirstName = 'First name is required.';
        if (!draft.clientLastName.trim())  errors.clientLastName  = 'Last name is required.';
        if (!draft.clientEmail.trim())     errors.clientEmail     = 'Email address is required.';
        else if (!emailRegex.test(draft.clientEmail)) errors.clientEmail = 'Enter a valid email address.';
        if (!draft.clientAddress.trim())   errors.clientAddress   = 'Client address is required.';
    }

    if (step === 3) {
        draft.milestones.forEach((ms, i) => {
            if (!ms.deliverable.trim())       errors[`ms_deliverable_${i}`] = 'Deliverable is required.';
            if (!ms.amount || ms.amount <= 0) errors[`ms_amount_${i}`]      = 'Enter a valid amount.';
            if (!ms.duration.trim())          errors[`ms_duration_${i}`]    = 'Duration is required.';
        });
    }

    if (step === 4) {
        if (!confirmed) errors.confirmed = 'You must confirm the details before submitting.';
    }

    return errors;
}

// ─── FIELD WRAPPER ─────────────────────────────────────────────────────────
function Field({ label, hint, error, children }: {
    label: string;
    hint?: string;
    error?: string;
    children: React.ReactNode;
}) {
    return (
        <div>
            <label className="block text-[13px] font-semibold text-slate-900 dark:text-neutral-200 mb-1">{label}</label>
            {hint && <p className="text-[12px] text-slate-500 dark:text-neutral-400 mb-2">{hint}</p>}
            {children}
            {error && (
                <p className="error-msg mt-1.5 text-[11.5px] font-semibold text-red-500 dark:text-red-400 flex items-center gap-1">
                    <i className="ti ti-alert-circle text-[13px]"></i> {error}
                </p>
            )}
        </div>
    );
}

function inputCls(error?: string, extra = '') {
    return `w-full text-sm text-slate-900 dark:text-white placeholder:text-slate-400 dark:placeholder:text-neutral-600 px-4 py-3 border rounded-xl bg-white dark:bg-neutral-950 shadow-sm dark:shadow-none focus:outline-none focus:ring-1 transition-shadow ${
        error
            ? 'border-red-400 dark:border-red-500 focus:border-red-400 focus:ring-red-200 dark:focus:ring-red-900/50'
            : 'border-slate-200 dark:border-neutral-800 focus:border-[#3FCD6B] focus:ring-[#3FCD6B] dark:focus:border-[#3FCD6B] dark:focus:ring-transparent'
    } ${extra}`;
}

function milestoneCls(error?: string) {
    return `w-full text-sm text-slate-900 dark:text-white placeholder:text-slate-400 dark:placeholder:text-neutral-600 px-3 py-2 border rounded-lg bg-white dark:bg-neutral-950 focus:outline-none transition-colors ${
        error
            ? 'border-red-400 dark:border-red-500 focus:border-red-400'
            : 'border-slate-200 dark:border-neutral-800 focus:border-[#3FCD6B] dark:focus:border-[#3FCD6B]'
    }`;
}

// ─── COMPONENT ─────────────────────────────────────────────────────────────
export default function NewJobWizard() {
    const router = useRouter();
    const [step, setStep] = useState<number>(1);
    const [errors, setErrors] = useState<FieldErrors>({});
    const [confirmed, setConfirmed] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const [draft, setDraft] = useState<JobDraft>({
        title: '',
        description: '',
        value: '',
        currency: 'GBP (£)',
        clientFirstName: '',
        clientLastName: '',
        clientEmail: '',
        clientCompany: '',
        clientNumber: '',
        clientAddress: '',
        milestones: [
            { id: crypto.randomUUID(), deliverable: '', amount: 0, dueDate: '', duration: '' }
        ]
    });

    const handleInput = (field: keyof JobDraft, value: any) => {
        setDraft(prev => ({ ...prev, [field]: value }));
        setErrors(prev => {
            const next = { ...prev };
            delete next[field as string];
            return next;
        });
    };

    const updateMilestone = (id: string, field: keyof Milestone, value: any) => {
        const idx = draft.milestones.findIndex(ms => ms.id === id);
        setDraft(prev => ({
            ...prev,
            milestones: prev.milestones.map(ms =>
                ms.id === id
                    ? { ...ms, [field]: field === 'amount' ? (parseFloat(value) || 0) : value }
                    : ms
            )
        }));
        setErrors(prev => {
            const next = { ...prev };
            delete next[`ms_${field}_${idx}`];
            return next;
        });
    };

    const addMilestone = () => {
        setDraft(prev => ({
            ...prev,
            milestones: [...prev.milestones, { id: crypto.randomUUID(), deliverable: '', amount: 0, dueDate: '', duration: '' }]
        }));
    };

    const removeMilestone = (id: string) => {
        if (draft.milestones.length <= 1) return;
        setDraft(prev => ({
            ...prev,
            milestones: prev.milestones.filter(ms => ms.id !== id)
        }));
    };

    const calculateTotal = () =>
        draft.milestones.reduce((sum, ms) => sum + (ms.amount || 0), 0);

    const tryAdvance = () => {
        const errs = validateStep(step, draft, confirmed);
        if (Object.keys(errs).length > 0) {
            setErrors(errs);
            return;
        }
        setErrors({});
        setStep(s => s + 1);
        setSidebarOpen(false);
    };

    // ─── SUBMIT ────────────────────────────────────────────────────────────
    const handleSubmit = async () => {
        const errs = validateStep(4, draft, confirmed);
        if (Object.keys(errs).length > 0) {
            setErrors(errs);
            return;
        }

        setIsSubmitting(true);
        setSubmitError(null);

        try {
            // TODO: Replace empty string with the authenticated freelancer's email,
            // e.g. from useSession() once auth context is wired up.
            const freelancerEmail = '';

            const dto = buildJobCreationDto(draft, freelancerEmail);

            const response = await fetch('http://localhost:8080/api/job/creation', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include', // sends HttpOnly session cookie
                body: JSON.stringify(dto),
            });

            if (!response.ok) {
                // Try to surface a meaningful error message from the server
                let message = `Request failed with status ${response.status}`;
                try {
                    const text = await response.text();
                    if (text) message = text;
                } catch {
                    // ignore parse failure, use status-based message
                }
                throw new Error(message);
            }

            setIsSuccess(true);
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : 'Something went wrong. Please try again.';
            setSubmitError(message);
        } finally {
            setIsSubmitting(false);
        }
    };

    // ─── SIDEBAR STEP ──────────────────────────────────────────────────────
    const SidebarStep = ({ num, icon, title, sub }: { num: number; icon: string; title: string; sub: string }) => {
        const isActive = step === num;
        const isDone   = step > num;
        const isLocked = num > step;

        const handleClick = () => {
            if (isLocked) return;
            setStep(num);
            setSidebarOpen(false);
        };

        return (
            <div
                className={`flex items-start gap-2.5 mb-1 ${isLocked ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'}`}
                onClick={handleClick}
            >
                <div className="flex flex-col items-center">
                    <div className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-medium shrink-0 border-2 transition-colors ${
                        isDone   ? 'bg-[#3FCD6B] border-[#3FCD6B] text-neutral-900'    :
                            isActive ? 'border-[#3FCD6B] text-[#3FCD6B] bg-transparent' :
                                'border-white/20 dark:border-neutral-700 text-white/40 dark:text-neutral-600'
                    }`}>
                        {isDone
                            ? <i className="ti ti-check text-[11px]"></i>
                            : isLocked
                                ? <i className="ti ti-lock text-[10px]"></i>
                                : <i className={`ti ${icon} text-[11px]`}></i>}
                    </div>
                    {num < 4 && <div className="w-[1.5px] h-7 bg-white/10 dark:bg-neutral-800 my-1"></div>}
                </div>
                <div className="pt-0.5">
                    <div className={`text-xs font-medium ${isActive ? 'text-white' : isDone ? 'text-white/70 dark:text-neutral-300' : 'text-white/45 dark:text-neutral-500'}`}>{title}</div>
                    <div className={`text-[10px] mt-[1px] ${isActive ? 'text-white/50 dark:text-neutral-400' : 'text-white/30 dark:text-neutral-600'}`}>{sub}</div>
                </div>
            </div>
        );
    };

    const SidebarContent = () => (
        <>
            <div className="p-5 border-b border-white/10 dark:border-neutral-800 flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 bg-[#3FCD6B] rounded-lg flex items-center justify-center shrink-0 shadow-sm dark:shadow-none">
                        <i className="ti ti-shield-check text-[#0F5525] dark:text-neutral-950 text-lg"></i>
                    </div>
                    <span className="text-white text-base font-bold tracking-tight">TrustBridge</span>
                </div>
                <button
                    className="md:hidden text-white/60 hover:text-white transition-colors p-1"
                    onClick={() => setSidebarOpen(false)}
                >
                    <i className="ti ti-x text-lg"></i>
                </button>
            </div>

            <div className="p-6 px-4 flex-1 overflow-y-auto">
                <div className="text-[10px] text-white/40 dark:text-neutral-500 uppercase tracking-widest mb-4 pl-1 font-bold">New job setup</div>
                <SidebarStep num={1} icon="ti-file-description" title="Job details"      sub="Title, description, value" />
                <SidebarStep num={2} icon="ti-user"             title="Client info"      sub="Who you're working with"   />
                <SidebarStep num={3} icon="ti-list-check"       title="Milestones"       sub="Deliverables & splits"     />
                <SidebarStep num={4} icon="ti-writing"          title="Contract & terms" sub="Review & confirm"          />
            </div>

            <div className="p-4 border-t border-white/10 dark:border-neutral-800">
                <button
                    onClick={() => router.push('/dashboard/freelancer')}
                    className="text-xs font-medium text-white/50 dark:text-neutral-400 hover:text-white dark:hover:text-white transition-colors flex items-center gap-2 w-full px-2 py-2 rounded-lg hover:bg-white/5 dark:hover:bg-neutral-800"
                >
                    <i className="ti ti-arrow-left text-sm"></i> Cancel & return
                </button>
            </div>
        </>
    );

    const stepTitles = ['Job details', 'Client info', 'Milestones', 'Contract & terms'];
    const stepSubs   = [
        'Give this job a clear title and description so both parties are aligned from the start.',
        "Enter your client's details. They'll receive an invitation to review and sign the contract.",
        'Break the job into milestones. Funds are released when each milestone is approved.',
        'Review the auto-generated contract. Both parties must agree before funds can be deposited.',
    ];

    return (
        <>
            <style>{GLOBAL_STYLES}</style>

            <div className="h-screen w-full flex font-sans overflow-hidden bg-white dark:bg-neutral-950">

                {/* ─── DESKTOP SIDEBAR ─── */}
                <div className="hidden md:flex w-[260px] bg-[#0F5525] dark:bg-neutral-900 flex-col shrink-0 h-full border-r border-transparent dark:border-neutral-800">
                    <SidebarContent />
                </div>

                {/* ─── MOBILE SIDEBAR DRAWER ─── */}
                {sidebarOpen && (
                    <>
                        <div
                            className="md:hidden fixed inset-0 bg-black/40 dark:bg-black/80 z-30"
                            onClick={() => setSidebarOpen(false)}
                        />
                        <div className="md:hidden drawer-enter fixed top-0 left-0 h-full w-[280px] bg-[#0F5525] dark:bg-neutral-900 flex flex-col z-40 shadow-2xl dark:border-r dark:border-neutral-800">
                            <SidebarContent />
                        </div>
                    </>
                )}

                {/* ─── MAIN CONTENT ─── */}
                <div className="flex-1 flex flex-col relative h-full bg-[#f9fbf8] dark:bg-neutral-950 min-w-0">

                    {!isSuccess && (
                        <div className="absolute top-0 left-0 right-0 h-[3px] bg-slate-100 dark:bg-neutral-900 z-10">
                            <div
                                className="h-full bg-[#3FCD6B] transition-all duration-500 ease-out"
                                style={{ width: `${(step / 4) * 100}%` }}
                            />
                        </div>
                    )}

                    {/* ── Success Panel ── */}
                    {isSuccess ? (
                        <div className="flex-1 flex flex-col items-center justify-center p-8 text-center bg-white dark:bg-neutral-950">
                            <div className="w-16 h-16 rounded-full bg-[#E1F5EE] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 flex items-center justify-center mb-5 shadow-sm dark:shadow-none">
                                <i className="ti ti-check text-3xl text-[#0F5525] dark:text-[#3FCD6B]"></i>
                            </div>
                            <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">Job created successfully</h2>
                            <p className="text-[15px] text-slate-500 dark:text-neutral-400 max-w-md mb-8 leading-relaxed">
                                The contract has been sent to{' '}
                                <span className="font-semibold text-slate-700 dark:text-neutral-200">{draft.clientFirstName}</span> at{' '}
                                <span className="font-semibold text-slate-700 dark:text-neutral-200">{draft.clientEmail}</span>. Once they sign and deposit{' '}
                                <span className="font-semibold text-[#0F5525] dark:text-[#3FCD6B]">£{calculateTotal().toLocaleString('en-GB')}</span> into escrow, work can begin.
                            </p>
                            <div className="flex flex-col sm:flex-row gap-3">
                                <button
                                    onClick={() => router.push('/dashboard/freelancer')}
                                    className="px-5 py-2.5 border border-slate-200 dark:border-neutral-800 text-slate-600 dark:text-neutral-300 rounded-xl text-sm font-semibold hover:bg-slate-50 dark:hover:bg-neutral-900 transition-colors"
                                >
                                    Back to dashboard
                                </button>
                                <button className="px-5 py-2.5 bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 rounded-xl text-sm font-semibold hover:bg-[#157132] dark:hover:opacity-90 transition-colors shadow-sm dark:shadow-none">
                                    View job details
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            {/* ── Header ── */}
                            <div className="shrink-0 px-5 sm:px-8 md:px-10 py-5 md:py-8 bg-white dark:bg-neutral-950 border-b border-slate-200 dark:border-neutral-800">
                                <div className="max-w-3xl mx-auto w-full flex items-start gap-4">

                                    <div className="flex items-center gap-3 md:hidden">
                                        <button
                                            onClick={() => setSidebarOpen(true)}
                                            className="p-2 rounded-lg border border-slate-200 dark:border-neutral-800 text-slate-600 dark:text-neutral-400 hover:bg-slate-50 dark:hover:bg-neutral-900 transition-colors shrink-0"
                                        >
                                            <i className="ti ti-menu-2 text-lg"></i>
                                        </button>
                                    </div>

                                    <div className="flex-1 min-w-0">
                                        <div className="md:hidden mb-1">
                                            <span className="text-[10px] font-bold text-[#3FCD6B] uppercase tracking-wider">
                                                Step {step} of 4
                                            </span>
                                        </div>
                                        <h2 className="text-lg md:text-xl font-bold text-slate-900 dark:text-white tracking-tight">
                                            {stepTitles[step - 1]}
                                        </h2>
                                        <p className="text-[13px] md:text-[13.5px] text-slate-500 dark:text-neutral-400 mt-1 leading-snug">
                                            {stepSubs[step - 1]}
                                        </p>
                                    </div>

                                    <button
                                        onClick={() => router.push('/dashboard/freelancer')}
                                        className="hidden md:flex shrink-0 items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 dark:border-neutral-800 bg-white dark:bg-transparent text-slate-600 dark:text-neutral-300 text-sm font-semibold hover:bg-[#0F5525] dark:hover:bg-neutral-900 hover:text-white dark:hover:text-white transition-colors shadow-sm dark:shadow-none"
                                    >
                                        <i className="ti ti-arrow-left text-sm"></i>
                                        Back to dashboard
                                    </button>
                                </div>
                            </div>

                            {/* ── Scrollable Body ── */}
                            <div className="flex-1 overflow-y-auto px-5 sm:px-8 md:px-10 py-7 md:py-10">
                                <div className="max-w-3xl mx-auto w-full">

                                    {/* ── Step 1: Job Details ── */}
                                    {step === 1 && (
                                        <div className="space-y-5" style={slideInStyle}>
                                            <Field label="Job title" error={errors.title}>
                                                <input
                                                    type="text"
                                                    value={draft.title}
                                                    onChange={e => handleInput('title', e.target.value)}
                                                    className={inputCls(errors.title)}
                                                    placeholder="e.g. Brand identity redesign"
                                                />
                                            </Field>
                                            <Field
                                                label="Description"
                                                hint="Describe the scope of work. This will appear in the signed contract."
                                                error={errors.description}
                                            >
                                                <textarea
                                                    value={draft.description}
                                                    onChange={e => handleInput('description', e.target.value)}
                                                    className={inputCls(errors.description, 'min-h-[120px]')}
                                                    placeholder="Describe the work to be done..."
                                                />
                                            </Field>
                                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                                <Field label="Total job value" error={errors.value}>
                                                    <input
                                                        type="text"
                                                        value={draft.value}
                                                        onChange={e => handleInput('value', e.target.value)}
                                                        className={inputCls(errors.value)}
                                                        placeholder="£0.00"
                                                    />
                                                </Field>
                                                <Field label="Currency">
                                                    <select
                                                        value={draft.currency}
                                                        onChange={e => handleInput('currency', e.target.value)}
                                                        className={inputCls()}
                                                    >
                                                        <option>GBP (£)</option>
                                                        <option>USD ($)</option>
                                                        <option>EUR (€)</option>
                                                    </select>
                                                </Field>
                                            </div>
                                        </div>
                                    )}

                                    {/* ── Step 2: Client Info ── */}
                                    {step === 2 && (
                                        <div className="space-y-5" style={slideInStyle}>
                                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                                <Field label="First name" error={errors.clientFirstName}>
                                                    <input
                                                        type="text"
                                                        value={draft.clientFirstName}
                                                        onChange={e => handleInput('clientFirstName', e.target.value)}
                                                        className={inputCls(errors.clientFirstName)}
                                                        placeholder="First name"
                                                    />
                                                </Field>
                                                <Field label="Last name" error={errors.clientLastName}>
                                                    <input
                                                        type="text"
                                                        value={draft.clientLastName}
                                                        onChange={e => handleInput('clientLastName', e.target.value)}
                                                        className={inputCls(errors.clientLastName)}
                                                        placeholder="Last name"
                                                    />
                                                </Field>
                                            </div>
                                            <Field
                                                label="Email address"
                                                hint="An invitation and contract link will be sent here."
                                                error={errors.clientEmail}
                                            >
                                                <input
                                                    type="email"
                                                    value={draft.clientEmail}
                                                    onChange={e => handleInput('clientEmail', e.target.value)}
                                                    className={inputCls(errors.clientEmail)}
                                                    placeholder="client@company.com"
                                                />
                                            </Field>
                                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                                <Field label="Company name">
                                                    <input
                                                        type="text"
                                                        value={draft.clientCompany}
                                                        onChange={e => handleInput('clientCompany', e.target.value)}
                                                        className={inputCls()}
                                                        placeholder="Acme Ltd (optional)"
                                                    />
                                                </Field>
                                                <Field label="Phone number">
                                                    <input
                                                        type="tel"
                                                        value={draft.clientNumber}
                                                        onChange={e => handleInput('clientNumber', e.target.value)}
                                                        className={inputCls()}
                                                        placeholder="+44 7700 000000 (optional)"
                                                    />
                                                </Field>
                                            </div>
                                            <Field label="Client address" error={errors.clientAddress}>
                                                <input
                                                    type="text"
                                                    value={draft.clientAddress}
                                                    onChange={e => handleInput('clientAddress', e.target.value)}
                                                    className={inputCls(errors.clientAddress)}
                                                    placeholder="Street address, city, postcode"
                                                />
                                            </Field>
                                            <div className="bg-[#E6F1FB] dark:bg-transparent border border-[#d1e6f9] dark:border-neutral-800 rounded-xl p-4 flex gap-3 items-start shadow-sm dark:shadow-none">
                                                <i className="ti ti-info-circle text-[#185FA5] dark:text-neutral-400 text-lg mt-[1px] shrink-0"></i>
                                                <p className="text-xs text-[#0C447C] dark:text-neutral-400 leading-relaxed font-medium">
                                                    Client details are encrypted at rest and only shared with relevant parties as required by the contract. TrustBridge is PSD3 compliant for domestic transactions.
                                                </p>
                                            </div>
                                        </div>
                                    )}

                                    {/* ── Step 3: Milestones ── */}
                                    {step === 3 && (
                                        <div className="space-y-4" style={slideInStyle}>
                                            {draft.milestones.map((ms, index) => (
                                                <div key={ms.id} className="bg-white dark:bg-transparent border border-slate-200 dark:border-neutral-800 rounded-xl p-4 md:p-5 shadow-sm dark:shadow-none">
                                                    <div className="flex justify-between items-center mb-4">
                                                        <span className="text-xs font-bold text-[#0F5525] dark:text-[#3FCD6B] bg-[#E1F5EE] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 px-3 py-1 rounded-lg">
                                                            Milestone {index + 1}
                                                        </span>
                                                        {draft.milestones.length > 1 && (
                                                            <button
                                                                onClick={() => removeMilestone(ms.id)}
                                                                className="text-xs font-medium text-slate-400 hover:text-red-600 dark:hover:text-red-400 flex items-center gap-1.5 transition-colors"
                                                            >
                                                                <i className="ti ti-trash"></i>
                                                                <span className="hidden sm:inline">Remove</span>
                                                            </button>
                                                        )}
                                                    </div>

                                                    <div className="grid grid-cols-1 sm:grid-cols-[1fr_140px] gap-4 mb-4">
                                                        <div>
                                                            <label className="block text-xs font-semibold text-slate-900 dark:text-neutral-200 mb-1.5">Deliverable</label>
                                                            <input
                                                                type="text"
                                                                value={ms.deliverable}
                                                                onChange={e => updateMilestone(ms.id, 'deliverable', e.target.value)}
                                                                className={milestoneCls(errors[`ms_deliverable_${index}`])}
                                                                placeholder="e.g. High-fidelity mockups"
                                                            />
                                                            {errors[`ms_deliverable_${index}`] && (
                                                                <p className="error-msg mt-1 text-[11px] font-semibold text-red-500 dark:text-red-400 flex items-center gap-1">
                                                                    <i className="ti ti-alert-circle"></i> {errors[`ms_deliverable_${index}`]}
                                                                </p>
                                                            )}
                                                        </div>
                                                        <div>
                                                            <label className="block text-xs font-semibold text-slate-900 dark:text-neutral-200 mb-1.5">Amount (£)</label>
                                                            <input
                                                                type="number"
                                                                min="0"
                                                                step="0.01"
                                                                value={ms.amount || ''}
                                                                onChange={e => updateMilestone(ms.id, 'amount', e.target.value)}
                                                                className={milestoneCls(errors[`ms_amount_${index}`])}
                                                                placeholder="0"
                                                            />
                                                            {errors[`ms_amount_${index}`] && (
                                                                <p className="error-msg mt-1 text-[11px] font-semibold text-red-500 dark:text-red-400 flex items-center gap-1">
                                                                    <i className="ti ti-alert-circle"></i> {errors[`ms_amount_${index}`]}
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>

                                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                                        <div>
                                                            <label className="block text-xs font-semibold text-slate-900 dark:text-neutral-200 mb-1.5">
                                                                Due date <span className="text-slate-400 dark:text-neutral-500 font-normal">(optional)</span>
                                                            </label>
                                                            <input
                                                                type="date"
                                                                value={ms.dueDate}
                                                                onChange={e => updateMilestone(ms.id, 'dueDate', e.target.value)}
                                                                className={milestoneCls()}
                                                            />
                                                        </div>
                                                        <div>
                                                            <label className="block text-xs font-semibold text-slate-900 dark:text-neutral-200 mb-1.5">Duration</label>
                                                            <input
                                                                type="text"
                                                                value={ms.duration}
                                                                onChange={e => updateMilestone(ms.id, 'duration', e.target.value)}
                                                                className={milestoneCls(errors[`ms_duration_${index}`])}
                                                                placeholder="e.g. 2 weeks"
                                                            />
                                                            {errors[`ms_duration_${index}`] && (
                                                                <p className="error-msg mt-1 text-[11px] font-semibold text-red-500 dark:text-red-400 flex items-center gap-1">
                                                                    <i className="ti ti-alert-circle"></i> {errors[`ms_duration_${index}`]}
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}

                                            <button
                                                onClick={addMilestone}
                                                className="text-sm font-bold text-[#0F5525] dark:text-[#3FCD6B] py-2 flex items-center gap-2 hover:opacity-80 transition-opacity"
                                            >
                                                <i className="ti ti-plus text-lg"></i> Add another milestone
                                            </button>

                                            <div className="bg-[#E1F5EE] dark:bg-transparent border border-[#c4ebd8] dark:border-[#3FCD6B]/30 rounded-xl p-4 md:p-5 flex justify-between items-center mt-2 shadow-sm dark:shadow-none">
                                                <span className="text-sm font-bold text-[#0F6E56] dark:text-white flex items-center gap-2">
                                                    <i className="ti ti-shield-lock text-lg text-[#3FCD6B]"></i> Total held in escrow
                                                </span>
                                                <span className="text-xl font-black text-[#0F5525] dark:text-[#3FCD6B]">
                                                    £{calculateTotal().toLocaleString('en-GB')}
                                                </span>
                                            </div>
                                        </div>
                                    )}

                                    {/* ── Step 4: Review & Confirm ── */}
                                    {step === 4 && (
                                        <div className="space-y-7" style={slideInStyle}>
                                            <div>
                                                <h3 className="text-xs font-bold text-slate-500 dark:text-neutral-400 uppercase tracking-wider mb-3">Job summary</h3>
                                                <div className="bg-white dark:bg-transparent border border-slate-200 dark:border-neutral-800 rounded-xl p-1 shadow-sm dark:shadow-none">
                                                    <div className="flex justify-between py-3 px-4 border-b border-slate-100 dark:border-neutral-800 text-[13px]">
                                                        <span className="text-slate-500 dark:text-neutral-400 font-medium">Title</span>
                                                        <span className="font-bold text-slate-900 dark:text-white text-right ml-4 truncate max-w-[60%]">{draft.title || 'Untitled'}</span>
                                                    </div>
                                                    <div className="flex justify-between py-3 px-4 border-b border-slate-100 dark:border-neutral-800 text-[13px]">
                                                        <span className="text-slate-500 dark:text-neutral-400 font-medium">Client</span>
                                                        <span className="font-bold text-slate-900 dark:text-white">{draft.clientFirstName} {draft.clientLastName}</span>
                                                    </div>
                                                    {draft.clientCompany && (
                                                        <div className="flex justify-between py-3 px-4 border-b border-slate-100 dark:border-neutral-800 text-[13px]">
                                                            <span className="text-slate-500 dark:text-neutral-400 font-medium">Company</span>
                                                            <span className="font-bold text-slate-900 dark:text-white">{draft.clientCompany}</span>
                                                        </div>
                                                    )}
                                                    <div className="flex justify-between py-3 px-4 border-b border-slate-100 dark:border-neutral-800 text-[13px]">
                                                        <span className="text-slate-500 dark:text-neutral-400 font-medium">Milestones</span>
                                                        <span className="font-bold text-slate-900 dark:text-white">{draft.milestones.length}</span>
                                                    </div>
                                                    <div className="flex justify-between py-3 px-4 text-[13px]">
                                                        <span className="text-slate-500 dark:text-neutral-400 font-medium">Total value</span>
                                                        <span className="font-bold text-[#0F5525] dark:text-[#3FCD6B]">£{calculateTotal().toLocaleString('en-GB')}</span>
                                                    </div>
                                                </div>
                                            </div>

                                            <div>
                                                <h3 className="text-xs font-bold text-slate-500 dark:text-neutral-400 uppercase tracking-wider mb-3">Contract terms</h3>
                                                <div className="bg-white dark:bg-transparent border border-slate-200 dark:border-neutral-800 rounded-xl p-5 md:p-6 text-[13px] text-slate-600 dark:text-neutral-300 h-48 overflow-y-auto leading-relaxed shadow-sm dark:shadow-none">
                                                    <strong className="text-slate-900 dark:text-white block mb-3 text-sm">TrustBridge Escrow Agreement</strong>
                                                    This agreement is entered into between <strong>You</strong> (&#34;Freelancer&#34;) and{' '}
                                                    <strong>{draft.clientFirstName || 'Client'}</strong> (&#34;Client&#34;).
                                                    <br /><br />
                                                    <strong>1. Payment terms.</strong> The full contract value of £{calculateTotal().toLocaleString('en-GB')} will be deposited into a TrustBridge-managed escrow account prior to work commencing.
                                                    <br /><br />
                                                    <strong>2. Dispute resolution.</strong> In the event of a dispute, TrustBridge will mediate as per our domestic UK policies.
                                                </div>
                                            </div>

                                            <div className="space-y-3 pt-1">
                                                <label className={`flex items-start gap-3 cursor-pointer group rounded-xl p-3 border transition-colors ${
                                                    errors.confirmed
                                                        ? 'border-red-300 dark:border-red-900/50 bg-red-50 dark:bg-transparent'
                                                        : 'border-transparent hover:bg-slate-50 dark:hover:bg-neutral-900'
                                                }`}>
                                                    <input
                                                        type="checkbox"
                                                        checked={confirmed}
                                                        onChange={e => {
                                                            setConfirmed(e.target.checked);
                                                            if (e.target.checked) setErrors(prev => { const n = {...prev}; delete n.confirmed; return n; });
                                                        }}
                                                        className="mt-1 w-4 h-4 rounded border-slate-300 dark:border-neutral-700 text-[#3FCD6B] bg-white dark:bg-transparent focus:ring-[#3FCD6B] transition-shadow shrink-0"
                                                    />
                                                    <span className="text-[13px] text-slate-700 dark:text-neutral-300 leading-relaxed font-medium group-hover:text-slate-900 dark:group-hover:text-white transition-colors">
                                                        I confirm the job details and milestones above are accurate.
                                                        <span className="text-slate-500 dark:text-neutral-500 font-normal block mt-0.5">An immutable audit record will be created upon submission.</span>
                                                    </span>
                                                </label>
                                                {errors.confirmed && (
                                                    <p className="error-msg text-[12px] font-semibold text-red-500 dark:text-red-400 flex items-center gap-1.5 px-1">
                                                        <i className="ti ti-alert-circle text-[14px]"></i> {errors.confirmed}
                                                    </p>
                                                )}
                                            </div>

                                            {/* ── API error banner ── */}
                                            {submitError && (
                                                <div className="bg-red-50 dark:bg-transparent border border-red-200 dark:border-red-900/50 rounded-xl p-4 flex gap-3 items-start">
                                                    <i className="ti ti-alert-triangle text-red-500 dark:text-red-400 text-lg mt-[1px] shrink-0"></i>
                                                    <div>
                                                        <p className="text-[13px] font-bold text-red-700 dark:text-red-400">Submission failed</p>
                                                        <p className="text-[12px] text-red-600 dark:text-red-500 mt-0.5">{submitError}</p>
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    )}

                                </div>
                            </div>

                            {/* ── Footer ── */}
                            <div className="shrink-0 px-5 sm:px-8 md:px-10 py-4 md:py-5 border-t border-slate-200 dark:border-neutral-800 bg-white dark:bg-neutral-950">
                                <div className="max-w-3xl mx-auto w-full flex items-center justify-between">
                                    {step === 1 ? (
                                        <span className="text-[13px] font-semibold text-slate-400 dark:text-neutral-600">Step 1 of 4</span>
                                    ) : (
                                        <button
                                            onClick={() => { setErrors({}); setSubmitError(null); setStep(s => s - 1); }}
                                            className="px-4 sm:px-5 py-2.5 border border-slate-200 dark:border-neutral-800 text-slate-600 dark:text-neutral-300 rounded-xl text-sm font-semibold hover:bg-slate-50 dark:hover:bg-neutral-900 transition-colors flex items-center gap-2"
                                        >
                                            <i className="ti ti-arrow-left"></i>
                                            <span className="hidden sm:inline">Back</span>
                                        </button>
                                    )}

                                    {step < 4 ? (
                                        <button
                                            onClick={tryAdvance}
                                            className="px-5 sm:px-6 py-2.5 bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 rounded-xl text-sm font-bold hover:bg-[#157132] dark:hover:opacity-90 transition-opacity flex items-center gap-2 shadow-sm dark:shadow-none ml-auto"
                                        >
                                            Continue <i className="ti ti-arrow-right"></i>
                                        </button>
                                    ) : (
                                        <button
                                            onClick={handleSubmit}
                                            disabled={isSubmitting}
                                            className="px-5 sm:px-6 py-2.5 bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 rounded-xl text-sm font-bold hover:bg-[#157132] dark:hover:opacity-90 transition-opacity flex items-center gap-2 shadow-sm dark:shadow-none ml-auto disabled:opacity-70 disabled:cursor-not-allowed"
                                        >
                                            {isSubmitting
                                                ? <><i className="ti ti-loader-2 animate-spin"></i> Processing...</>
                                                : <><i className="ti ti-send"></i> <span>Create &amp; send contract</span></>
                                            }
                                        </button>
                                    )}
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </>
    );
}