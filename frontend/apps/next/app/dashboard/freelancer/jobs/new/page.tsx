"use client";

import React, { useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Plus, Trash2, ShieldCheck, ArrowRight, Briefcase, AlertCircle } from "lucide-react";

const useRouter = () => ({
    push: (path: string) => alert(`Navigating to: ${path}`)
});

const milestoneSchema = z.object({
    title: z.string().min(3, "Title must be at least 3 characters"),
    amount: z.number({ invalid_type_error: "Must be a valid number" }).min(50, "Minimum amount is £50"),
});

const jobCreationSchema = z.object({
    clientFirstName: z.string().min(1, "First name is required"),
    clientLastName: z.string().min(1, "Last name is required"),
    clientEmail: z.string().email("Please enter a valid client email"),
    clientPhoneNumber: z.string().optional(),
    jobTitle: z.string().min(5, "Job title is required"),
    description: z.string().min(10, "Please provide a brief description"),
    milestones: z.array(milestoneSchema).min(1, "You must add at least one milestone"),
});

type JobCreationFormValues = z.infer<typeof jobCreationSchema>;

export default function NewJobWizard() {
    const router = useRouter();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const [isSuccess, setIsSuccess] = useState(false);

    const {
        register,
        control,
        handleSubmit,
        watch,
        formState: { errors },
    } = useForm<JobCreationFormValues>({
        resolver: zodResolver(jobCreationSchema),
        defaultValues: {
            clientFirstName: "",
            clientLastName: "",
            clientEmail: "",
            clientPhoneNumber: "",
            jobTitle: "",
            description: "",
            milestones: [{ title: "Initial Deposit / Phase 1", amount: 500 }],
        },
    });

    const { fields, append, remove } = useFieldArray({
        control,
        name: "milestones",
    });

    // Dynamically calculate the total value and fees
    const watchedMilestones = watch("milestones") || [];

    const totalAmount = watchedMilestones.reduce(
        (sum: number, m: { title: string; amount: number }) => sum + (Number(m.amount) || 0),
        0
    );

    const trustBridgeFee = totalAmount * 0.01;

    const totalProcessingFees = watchedMilestones.reduce(
        (sum: number, m: { title: string; amount: number }) => {
            const amt = Number(m.amount) || 0;
            if (amt >= 10000) {
                return sum + (amt * 0.0325); // Escrow.com max fee
            } else if (amt > 0) {
                return sum + (amt * 0.034) + 0.25; // Stripe max fee
            }
            return sum;
        },
        0
    );

    const estimatedPayout = totalAmount - trustBridgeFee - totalProcessingFees;

    const onSubmit = async (data: JobCreationFormValues) => {
        setIsSubmitting(true);
        setSubmitError(null);

        try {
            const dto = {
                clientEmail: data.clientEmail.trim(),
                clientPhoneNumber: data.clientPhoneNumber?.trim() || null,
                clientFirstName: data.clientFirstName.trim(),
                clientLastName: data.clientLastName.trim(),
                title: data.jobTitle.trim(),
                description: data.description.trim(),
                amount: totalAmount,
                currency: "GBP",
                countryCode: "GB",
                milestones: data.milestones.map((ms: { title: string; amount: number }, index: number) => ({
                    title: ms.title,
                    amount: ms.amount,
                    sequence_amount: index + 1,
                })),
            };

            const response = await fetch("http://localhost:8080/api/job/creation", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include", // sends HttpOnly session cookie
                body: JSON.stringify(dto),
            });

            if (!response.ok) {
                let message = `Request failed with status ${response.status}`;
                try {
                    const text = await response.text();
                    if (text) message = text;
                } catch {
                    // ignore parse failure
                }
                throw new Error(message);
            }

            setIsSuccess(true);
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : "Something went wrong. Please try again.";
            setSubmitError(message);
        } finally {
            setIsSubmitting(false);
        }
    };

    // ─── SUCCESS VIEW ─────────────────────────────────────────────────────────
    if (isSuccess) {
        return (
            <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4 font-sans">
                <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 max-w-md w-full text-center">
                    <div className="w-16 h-16 bg-[#E1F5EE] rounded-full flex items-center justify-center mx-auto mb-6">
                        <ShieldCheck className="text-[#3FCD6B]" size={32} />
                    </div>
                    <h2 className="text-2xl font-bold text-slate-900 mb-2">Job Created!</h2>
                    <p className="text-slate-500 mb-8">
                        The contract and escrow deposit request has been drafted. Once the client approves, work can begin.
                    </p>
                    <button
                        onClick={() => router.push("/dashboard/freelancer")}
                        className="w-full bg-[#0F5525] hover:bg-[#157132] text-white font-bold py-3 px-6 rounded-xl transition-all"
                    >
                        Return to Dashboard
                    </button>
                </div>
            </div>
        );
    }

    // ─── WIZARD VIEW ──────────────────────────────────────────────────────────
    return (
        <div className="min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8 font-sans text-slate-900">
            <div className="max-w-3xl mx-auto">
                <div className="mb-8 text-center">
                    <div className="inline-flex items-center justify-center w-12 h-12 bg-[#0A3D1A] text-white rounded-xl mb-4 shadow-lg">
                        <Briefcase size={24} />
                    </div>
                    <h1 className="text-3xl font-bold text-slate-900">Draft a New Job</h1>
                    <p className="text-slate-500 mt-2">Define the scope, set your milestones, and invite your client to fund the escrow.</p>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">

                    {/* STEP 1: Client & Job Details */}
                    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                        <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
                            <span className="bg-slate-100 text-slate-600 px-2 py-1 rounded-md text-sm">1</span>
                            Job Details
                        </h2>

                        <div className="space-y-4">
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">First Name</label>
                                    <input
                                        {...register("clientFirstName")}
                                        className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.clientFirstName ? 'border-red-500' : 'border-slate-200'}`}
                                        placeholder="Client's First Name"
                                    />
                                    {errors.clientFirstName && <p className="text-red-500 text-xs mt-1">{errors.clientFirstName.message}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Last Name</label>
                                    <input
                                        {...register("clientLastName")}
                                        className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.clientLastName ? 'border-red-500' : 'border-slate-200'}`}
                                        placeholder="Client's Last Name"
                                    />
                                    {errors.clientLastName && <p className="text-red-500 text-xs mt-1">{errors.clientLastName.message}</p>}
                                </div>
                            </div>

                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Client Email</label>
                                    <input
                                        {...register("clientEmail")}
                                        type="email"
                                        className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.clientEmail ? 'border-red-500' : 'border-slate-200'}`}
                                        placeholder="client@company.com"
                                    />
                                    {errors.clientEmail && <p className="text-red-500 text-xs mt-1">{errors.clientEmail.message}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Phone Number <span className="text-slate-400 font-normal">(Optional)</span></label>
                                    <input
                                        {...register("clientPhoneNumber")}
                                        className="w-full p-3 border border-slate-200 rounded-lg focus:ring-2 focus:ring-[#3FCD6B] outline-none"
                                        placeholder="+44 7700 000000"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Job Title</label>
                                <input
                                    {...register("jobTitle")}
                                    className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.jobTitle ? 'border-red-500' : 'border-slate-200'}`}
                                    placeholder="e.g. E-commerce Website Development"
                                />
                                {errors.jobTitle && <p className="text-red-500 text-xs mt-1">{errors.jobTitle.message}</p>}
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Scope of Work</label>
                                <textarea
                                    {...register("description")}
                                    rows={3}
                                    className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.description ? 'border-red-500' : 'border-slate-200'}`}
                                    placeholder="Briefly describe what will be delivered..."
                                />
                                {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>}
                            </div>
                        </div>
                    </div>

                    {/* STEP 2: Milestone Builder */}
                    <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold flex items-center gap-2">
                                <span className="bg-slate-100 text-slate-600 px-2 py-1 rounded-md text-sm">2</span>
                                Payment Milestones
                            </h2>
                        </div>

                        <div className="space-y-4">
                            {fields.map((field: { id: string }, index: number) => (
                                <div key={field.id} className="flex gap-4 items-start p-4 bg-slate-50 border border-slate-100 rounded-xl relative group">
                                    <div className="flex-1">
                                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Milestone {index + 1}</label>
                                        <input
                                            {...register(`milestones.${index}.title` as const)}
                                            className={`w-full p-2.5 bg-white border rounded-md focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.milestones?.[index]?.title ? 'border-red-500' : 'border-slate-200'}`}
                                            placeholder="e.g. Wireframes Approved"
                                        />
                                        {errors.milestones?.[index]?.title && (
                                            <p className="text-red-500 text-xs mt-1">{errors.milestones[index]?.title?.message}</p>
                                        )}
                                    </div>

                                    <div className="w-1/3">
                                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Amount (£)</label>
                                        <div className="relative">
                                            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 font-medium">£</span>
                                            <input
                                                type="number"
                                                step="0.01"
                                                {...register(`milestones.${index}.amount` as const, { valueAsNumber: true })}
                                                className={`w-full pl-8 p-2.5 bg-white border rounded-md focus:ring-2 focus:ring-[#3FCD6B] outline-none ${errors.milestones?.[index]?.amount ? 'border-red-500' : 'border-slate-200'}`}
                                                placeholder="0.00"
                                            />
                                        </div>
                                        {errors.milestones?.[index]?.amount && (
                                            <p className="text-red-500 text-xs mt-1">{errors.milestones[index]?.amount?.message}</p>
                                        )}
                                    </div>

                                    {fields.length > 1 && (
                                        <button
                                            type="button"
                                            onClick={() => remove(index)}
                                            className="mt-6 p-2.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-md transition-colors"
                                            title="Remove Milestone"
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>

                        <button
                            type="button"
                            onClick={() => append({ title: "", amount: 0 })}
                            className="mt-4 flex items-center gap-2 text-sm font-semibold text-[#0A3D1A] bg-[#0A3D1A]/10 px-4 py-2 rounded-lg hover:bg-[#0A3D1A]/20 transition-colors"
                        >
                            <Plus size={16} /> Add Another Milestone
                        </button>
                        {errors.milestones?.root && <p className="text-red-500 text-xs mt-2">{errors.milestones.root.message}</p>}
                    </div>

                    {/* STEP 3: Review & Dispatch */}
                    <div className="bg-[#0A3D1A] text-white p-6 rounded-2xl shadow-md">
                        <h2 className="text-lg font-bold mb-4 border-b border-white/20 pb-2">Escrow Summary</h2>

                        <div className="space-y-2 mb-6 text-sm">
                            <div className="flex justify-between items-center text-white/80">
                                <span>Total Project Value:</span>
                                <span>£{totalAmount.toLocaleString('en-GB', { minimumFractionDigits: 2 })}</span>
                            </div>
                            <div className="flex justify-between items-center text-white/80">
                                <span>TrustBridge Fee (1% deduction):</span>
                                <span>- £{trustBridgeFee.toLocaleString('en-GB', { minimumFractionDigits: 2 })}</span>
                            </div>
                            <div className="flex justify-between items-center text-white/80">
                                <span>Est. Processing Fees (Stripe / Escrow.com):</span>
                                <span>- £{totalProcessingFees.toLocaleString('en-GB', { minimumFractionDigits: 2 })}</span>
                            </div>
                            <div className="flex justify-between items-center font-bold text-lg pt-2 border-t border-white/20 mt-2">
                                <span>Your Estimated Payout:</span>
                                <span className="text-[#3FCD6B]">£{estimatedPayout.toLocaleString('en-GB', { minimumFractionDigits: 2 })}</span>
                            </div>
                        </div>

                        {submitError && (
                            <div className="mb-4 bg-red-500/10 border border-red-500/50 rounded-lg p-3 flex gap-2 items-start">
                                <AlertCircle size={16} className="text-red-400 mt-0.5 shrink-0" />
                                <p className="text-sm text-red-200">{submitError}</p>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full flex items-center justify-center gap-2 bg-[#3FCD6B] hover:bg-[#34b35c] text-[#0A3D1A] font-bold py-3.5 px-6 rounded-xl transition-all disabled:opacity-70 disabled:cursor-not-allowed"
                        >
                            {isSubmitting ? "Processing..." : "Create Job & Invite Client"}
                            {!isSubmitting && <ArrowRight size={18} />}
                        </button>

                        <p className="text-xs text-center mt-4 text-white/60 flex items-center justify-center gap-1.5">
                            <ShieldCheck size={14} />
                            Client funds are held securely in escrow until milestones are approved.
                        </p>
                    </div>

                </form>
            </div>
        </div>
    );
}