import Link from "next/link";

export default function OnboardingSuccess() {
    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex flex-col items-center justify-center p-4">
            <div className="bg-white dark:bg-neutral-900 border border-slate-200 dark:border-neutral-800 rounded-2xl shadow-sm p-8 max-w-md w-full text-center">

                {/* Success Icon */}
                <div className="w-16 h-16 bg-[#EAF3DE] dark:bg-transparent dark:border dark:border-[#3FCD6B]/30 rounded-2xl flex items-center justify-center mx-auto mb-6">
                    <i className="ti ti-shield-check text-[#3B6D11] dark:text-[#3FCD6B] text-3xl"></i>
                </div>

                <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight mb-2">
                    Verification Submitted!
                </h1>

                <p className="text-slate-500 dark:text-neutral-400 text-sm mb-8 leading-relaxed">
                    Stripe has successfully received your identity documents. Your TrustBridge dashboard will automatically unlock the moment your account is approved.
                </p>

                <Link
                    href="/dashboard/freelancer"
                    className="w-full inline-block bg-[#0F5525] dark:bg-[#3FCD6B] text-white dark:text-neutral-950 font-semibold py-3 px-4 rounded-xl hover:bg-[#1a7a38] dark:hover:opacity-90 transition-colors"
                >
                    Return to Dashboard
                </Link>
            </div>
        </div>
    );
}