import React from 'react';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function TermsOfService() {
  return (
    <div className="font-sans text-[#1A2E24] antialiased selection:bg-[#1ED760] selection:text-[#0B2117] overflow-x-hidden bg-[#E6F4EA] min-h-screen flex flex-col">

      <style dangerouslySetInnerHTML={{__html: `
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap');
        .font-sans { font-family: 'Inter', sans-serif; }
        .font-serif { font-family: 'Playfair Display', serif; }
        .bg-noise {
          background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.04'/%3E%3C/svg%3E");
        }
      `}} />

      {/* Navbar */}
      <nav className="w-full z-50 pt-4 px-4 sm:pt-6 sm:px-6 relative">
        <div className="max-w-7xl mx-auto flex items-center justify-center sm:justify-between sm:bg-white/50 sm:backdrop-blur-md sm:shadow-sm rounded-full px-4 sm:px-6 py-3 sm:border sm:border-black/5">
          <Link href="/" className="hidden sm:flex items-center gap-2">
            <img className="w-36" src="/TrustBridgeNameBlack.png" alt="TrustBridge" />
          </Link>
          <Link
            href="/"
            className="px-5 py-2 rounded-full bg-[#0B2117] text-white hover:bg-[#1A2E24] transition-colors font-semibold text-sm flex items-center gap-2"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Home
          </Link>
        </div>
      </nav>

      {/* Main Content */}
      <main className="flex-1 w-full max-w-3xl mx-auto px-4 sm:px-6 py-12 sm:py-16 md:py-24">

        <div className="mb-8 sm:mb-12 border-b border-[#0B2117]/10 pb-6 sm:pb-8">
          <h1 className="font-serif text-3xl sm:text-4xl md:text-5xl font-medium mb-3 sm:mb-4 text-[#0B2117]">
            Terms of Service
          </h1>
          <p className="text-[#0B2117]/60 font-medium tracking-wide text-xs sm:text-sm uppercase">
            Last Updated: March 2026
          </p>
        </div>

        <div className="space-y-8 sm:space-y-10 text-sm sm:text-base leading-relaxed text-[#0B2117]/80">

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">1. Introduction and Platform Role</h2>
            <p className="mb-4">
              Welcome to TrustBridge. We provide a milestone-based escrow orchestration platform designed to facilitate secure transactions between independent contractors ("Freelancers") and their clients ("Clients").
            </p>
            <div className="bg-white/60 border border-black/5 p-4 sm:p-6 rounded-2xl shadow-sm">
              <p className="text-[#0B2117]">
                <strong className="block mb-2 text-[#0B2117] text-base sm:text-lg">Crucial Distinction:</strong>
                TrustBridge acts strictly as the orchestration and intelligence layer operating under the UK Commercial Agent framework. We are <strong>not</strong> a bank, nor are we the regulated fund holder. All regulated fund holding and payment processing is handled by licensed third-party financial institutions (e.g., Stripe, Escrow.com, and TrueLayer).
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">2. Smart Payment Routing and Custody</h2>
            <p className="mb-4">When a Client funds a milestone, TrustBridge automatically routes the transaction via the most appropriate regulated rail:</p>
            <ul className="list-disc pl-4 sm:pl-5 space-y-2 sm:space-y-3 mb-6">
              <li><strong className="text-[#0B2117]">Under £1,000:</strong> Processed via Stripe (including Apple Pay/Google Pay).</li>
              <li><strong className="text-[#0B2117]">£1,000 to £20,000:</strong> Processed via Open Banking (TrueLayer/Pay by Bank) to optimise processing fees.</li>
              <li><strong className="text-[#0B2117]">Over £20,000:</strong> Handled entirely via the Escrow.com API, where Escrow.com acts as the regulated fund custodian.</li>
            </ul>
            <p>By using TrustBridge, you additionally agree to the respective Terms of Service of these integrated payment partners based on your transaction size.</p>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">3. State Machine Enforcement (The "Code is Law" Clause)</h2>
            <p className="mb-4">
              TrustBridge enforces financial transitions via a strict digital state machine. Funds locked in connection with a specific milestone <strong>cannot</strong> be moved, withdrawn, or released until the pre-agreed conditions for that milestone are digitally verified as met.
            </p>
            <p>
              Neither the Client, the Freelancer, nor TrustBridge administrators can manually override this flow outside of the predefined Dispute Resolution process.
            </p>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">4. Dispute Resolution and Arbitration</h2>
            <p className="mb-4">If a dispute arises regarding whether a milestone has been met, TrustBridge initiates a structured resolution process:</p>
            <div className="mb-6 bg-white/50 p-4 sm:p-6 rounded-2xl border border-black/5">
              <ol className="list-decimal pl-4 sm:pl-5 space-y-3 sm:space-y-4">
                <li><strong className="text-[#0B2117]">Proposal Rounds:</strong> Both parties submit a proposed payment split. If the Client outright accepts the Freelancer's proposal, it is automatically selected to protect the more vulnerable party. This repeats for a maximum of three (3) rounds.</li>
                <li><strong className="text-[#0B2117]">Neutral Arbitration:</strong> If no agreement is reached after three rounds, the dispute escalates to neutral arbitration. During the Early Access phase, this is handled by TrustBridge administration; at scale, it is outsourced to an independent third-party arbitration service.</li>
              </ol>
            </div>
            <p>The decision reached via the final arbitration step is binding, and funds will be released according to that decision.</p>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">5. Beta Access and Waitlist</h2>
            <p>
              TrustBridge is currently offering Early Access via a controlled cohort waitlist. Acceptance into the beta program is at the sole discretion of TrustBridge. Promotional offers (such as "first £5,000 fee-free") are limited to approved early-access users and may be revoked if abuse or fraudulent activity is detected.
            </p>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">6. Governing Law</h2>
            <p>
              These Terms are governed by and construed in accordance with the laws of England and Wales. Any disputes arising from these Terms will be subject to the exclusive jurisdiction of the courts of England and Wales.
            </p>
          </section>

        </div>
      </main>

      {/* Footer */}
      <div className="bg-white mt-auto">
        <footer className="bg-[#0B2117] bg-noise text-white pt-12 sm:pt-16 pb-8 sm:pb-12 rounded-t-[2rem]">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 flex flex-col items-center gap-6 sm:flex-row sm:justify-between">

            <div className="flex items-center gap-2 text-white/60 text-sm">
              <img className="h-4 sm:h-5" src="/TrustBridgeName.png" alt="TrustBridge" />
              <span className="ml-2">© 2026 All rights reserved.</span>
            </div>

            <nav className="flex flex-wrap items-center justify-center gap-4 sm:gap-5" aria-label="Footer navigation">
              <a
                href="https://x.com/TrusttBridge"
                aria-label="TrustBridge on X"
                className="text-white/50 hover:text-white/80 transition-colors"
              >
                <svg viewBox="0 0 24 24" className="w-5 h-5 fill-current" aria-hidden="true">
                  <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 22.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
                </svg>
              </a>
              <span className="w-px h-4 bg-white/20" aria-hidden="true" />
              <Link href="/privacy" className="text-sm text-white/60 hover:text-white transition-colors">Privacy Policy</Link>
              <Link href="/terms" className="text-sm text-white/60 hover:text-white transition-colors">Terms of Service</Link>
            </nav>

          </div>
        </footer>
      </div>

    </div>
  );
}