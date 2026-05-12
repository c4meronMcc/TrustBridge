import Link from "next/link";
import { ArrowLeft, ArrowRight } from "lucide-react";

export default function NotFound() {
  return (
    <div className="font-sans min-h-screen bg-[#0B2117] text-white flex flex-col overflow-hidden relative">

      <style dangerouslySetInnerHTML={{__html: `
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap');
        .font-sans { font-family: 'Inter', sans-serif; }
        .font-serif { font-family: 'Playfair Display', serif; }
        .bg-noise {
          background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
        }
      `}} />

      <div className="bg-noise absolute inset-0 pointer-events-none z-0" aria-hidden="true" />

      <div
        className="absolute top-[-120px] left-1/2 -translate-x-1/2 w-[500px] h-[500px] rounded-full pointer-events-none z-0"
        style={{ background: "radial-gradient(circle, rgba(30,215,96,0.06) 0%, transparent 70%)" }}
        aria-hidden="true"
      />

      <main className="relative z-10 flex-1 flex flex-col items-center justify-center text-center px-6 py-24">

        <div className="w-10 h-px bg-[#1ED760]/40 mb-8" aria-hidden="true" />

        <h1 className="font-serif text-4xl sm:text-6xl md:text-7xl font-medium mb-4 text-white">
          Page not found.
        </h1>

        <p className="text-white/45 text-sm sm:text-base max-w-sm leading-relaxed mb-10">
          We couldn't find what you were looking for. Head back to the homepage or join the waitlist while you're here.
        </p>

        <div className="flex flex-col sm:flex-row items-center gap-3 w-full max-w-xs sm:max-w-none sm:w-auto">
          <Link
            href="/waitlist"
            className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-3 rounded-full bg-[#1ED760] text-[#0B2117] font-bold text-sm hover:bg-[#18c454] transition-all duration-200"
          >
            Join the Waitlist <ArrowRight className="w-4 h-4" />
          </Link>
          <Link
            href="/"
            className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-3 rounded-full border border-white/15 text-white/60 font-semibold text-sm hover:bg-white/5 transition-all duration-200"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Home
          </Link>
        </div>

      </main>

      <footer className="relative z-10 w-full px-6 pb-8">
        <div className="max-w-7xl mx-auto pt-6 border-t border-white/[0.06] flex flex-wrap items-center justify-center sm:justify-between gap-4 text-white/25 text-xs sm:text-sm">
          <span>© 2026 TrustBridge. All rights reserved.</span>
          <nav className="flex items-center gap-4" aria-label="Footer navigation">
            <Link href="/privacy" className="hover:text-white/50 transition-colors">Privacy Policy</Link>
            <Link href="/terms" className="hover:text-white/50 transition-colors">Terms of Service</Link>
          </nav>
        </div>
      </footer>

    </div>
  );
}