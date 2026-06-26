import Link from "next/link";

export default function OnboardingRefresh() {
    return (
        <div className="min-h-screen bg-[#f4f6f1] dark:bg-neutral-950 flex flex-col items-center justify-center p-4">
            <div className="bg-white dark:bg-neutral-900 border border-slate-200 dark:border-neutral-800 rounded-2xl shadow-sm p-8 max-w-md w-full text-center">

                {/* Warning Icon */}
                <div className="w-16 h-16 bg-[#FAEEDA] dark:bg-transparent dark:border dark:border-amber-500/30 rounded-2xl flex items-center justify-center mx-auto mb-6">
                    <i className="ti ti-clock text-[#854F0B] dark:text-amber-500 text-3xl"></i>
                </div>

                <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight mb-2">
                    Session Expired
                </h1>

                <p className="text-slate-500 dark:text-neutral-400 text-sm mb-8 leading-relaxed">
                    For your security, identity verification sessions expire after a few minutes of inactivity. Please generate a new secure link from your dashboard to continue.
                </p>

                <Link
                    href="/dashboard/freelancer"
                    className="w-full inline-block bg-white dark:bg-neutral-800 border border-slate-200 dark:border-neutral-700 text-slate-700 dark:text-neutral-300 font-semibold py-3 px-4 rounded-xl hover:bg-slate-50 dark:hover:bg-neutral-700 transition-colors"
                >
                    Return to Dashboard
                </Link>
            </div>
        </div>
    );
}