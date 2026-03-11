import React from 'react';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function PrivacyPolicy() {
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
            Privacy Policy
          </h1>
          <p className="text-[#0B2117]/60 font-medium tracking-wide text-xs sm:text-sm uppercase">
            Last Updated: March 2026
          </p>
        </div>

        <div className="space-y-8 sm:space-y-10 text-sm sm:text-base leading-relaxed text-[#0B2117]/80">

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">1. Introduction</h2>
            <p>
              TrustBridge ("we", "our", or "us") respects your privacy and is committed to protecting your personal data. This Privacy Policy outlines how we collect, use, and safeguard your information when you visit trustbridge.co.uk or use our escrow orchestration platform.
            </p>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">2. Data We Collect</h2>
            <p className="mb-4">During our Early Access and Waitlist phase, we primarily collect:</p>
            <div className="mb-6 bg-white/50 p-4 sm:p-6 rounded-2xl border border-black/5">
              <ul className="list-disc pl-4 sm:pl-5 space-y-2 sm:space-y-3">
                <li><strong className="text-[#0B2117]">Identity Data:</strong> First name, last name, and professional title.</li>
                <li><strong className="text-[#0B2117]">Contact Data:</strong> Email address.</li>
                <li><strong className="text-[#0B2117]">Technical Data:</strong> IP address, browser type, and operating system (collected via standard analytics).</li>
              </ul>
            </div>
            <p>
              <strong className="text-[#0B2117]">Future Platform Data:</strong> Upon full launch, to comply with KYC/AML regulations, we will securely collect identity verification documents, bank details, and transaction histories. This sensitive financial data will be passed directly to our regulated partners (Stripe, Escrow.com) and is not stored in plain text on TrustBridge servers.
            </p>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">3. How We Use Your Data</h2>
            <p className="mb-4">We use your Waitlist data exclusively to:</p>
            <ul className="list-disc pl-4 sm:pl-5 space-y-2">
              <li>Notify you about your cohort status and provide early access codes.</li>
              <li>Send critical updates regarding the platform's launch schedule.</li>
              <li>Prevent fraud and ensure the integrity of our waitlist system.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">4. Third-Party Disclosures</h2>
            <p className="mb-4">We do not sell your personal data. We may share your data with trusted third parties strictly for operational purposes:</p>
            <ul className="list-disc pl-4 sm:pl-5 space-y-2">
              <li><strong className="text-[#0B2117]">Financial Infrastructure:</strong> Stripe, Escrow.com, and TrueLayer (only when executing a transaction).</li>
              <li><strong className="text-[#0B2117]">Analytics & Hosting:</strong> Vercel, AWS, and standard privacy-compliant analytics providers.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-lg sm:text-xl font-bold text-[#0B2117] mb-3 sm:mb-4">5. Your GDPR Rights</h2>
            <p className="mb-4">As a UK-based entity, we operate in full compliance with UK GDPR. You have the right to:</p>
            <ul className="list-disc pl-4 sm:pl-5 space-y-2">
              <li>Request access to your personal data.</li>
              <li>Request correction or deletion of your data (e.g., removing yourself from the waitlist).</li>
              <li>Object to the processing of your data.</li>
            </ul>
            <div className="mt-6 sm:mt-8 p-5 sm:p-6 bg-white rounded-2xl border border-black/5 shadow-sm text-center">
              <p className="text-sm sm:text-base mb-2">To exercise any of these rights, please contact us at</p>
              <a
                href="mailto:hello@trustbridge.co.uk"
                className="font-bold text-[#1ED760] hover:text-[#18B852] transition-colors text-base sm:text-lg inline-block break-all"
              >
                hello@trustbridge.co.uk
              </a>
            </div>
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