"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import {
  ArrowRight, ShieldCheck, Landmark, Scale, Split,
  CheckCircle2, Plug, Menu, XIcon, Briefcase, Building2,
  Users, CreditCard,
} from "lucide-react";

const NAV_LINKS = ["Businesses", "Pricing", "Solution"];

const STEPS = [
  {
    step: "1",
    title: "Agree & Lock",
    desc: "You and your client agree on project milestones. The client funds the secure TrustBridge vault upfront.",
    dark: false,
  },
  {
    step: "2",
    title: "Do the Work",
    desc: "Hit your milestones with complete peace of mind, knowing the money is already secured and waiting for you.",
    dark: false,
  },
  {
    step: "3",
    title: "Get Paid Instantly",
    desc: "Submit your work. Once the milestone is approved, the system releases your funds automatically. No chasing.",
    dark: true,
  },
];

const AUDIENCES = [
  {
    Icon: Briefcase,
    title: "Freelancers",
    desc: "Tradespeople, developers, designers, and consultants. Anyone who wants deposit protection and guaranteed payment on completion.",
  },
  {
    Icon: Building2,
    title: "Agencies",
    desc: "Manage multi-milestone projects across multiple contractors with a clean, auditable payment flow. No more spreadsheet chaos.",
  },
  {
    Icon: Users,
    title: "Clients",
    desc: "Individuals and businesses who want absolute assurance that their funds are only released when the agreed work is genuinely delivered.",
  },
];

const FEATURES = [
  {
    Icon: Scale,
    title: "Neutral Arbitration",
    desc: "Disputes force compromise. If no agreement is reached, we provide neutral third-party arbitration.",
  },
  {
    Icon: Split,
    title: "Smart Routing",
    desc: "Automatically selects the cheapest payment rail based on size. Frictionless cards or Open Banking.",
  },
  {
    Icon: CheckCircle2,
    title: "State-Machine",
    desc: "Financial transitions are enforced by code. Money cannot move unless conditions are met. No exceptions.",
  },
];

export default function HomePage() {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const pageRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = pageRef.current;
    if (!el) return;
    const onScroll = () => setScrolled(el.scrollTop > 40);
    el.addEventListener("scroll", onScroll, { passive: true });
    return () => el.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    if (scrolled) setMenuOpen(false);
  }, [scrolled]);

  return (
    <div
      ref={pageRef}
      className="h-screen overflow-y-scroll bg-[#E6F4EA]"
      style={{ fontFamily: "Inter, sans-serif" }}
    >
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap');
        .font-serif { font-family: 'Playfair Display', serif; }
        .bg-noise { background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E"); }
      `}</style>

      {/* Mobile slide-down menu */}
      <div
        aria-hidden={!menuOpen}
        className={`
          fixed top-0 left-0 right-0 z-[98]
          bg-white backdrop-blur-xl
          px-8 pt-[5.5rem] pb-10
          rounded-b-3xl border-b border-black/[0.06]
          shadow-[0_16px_48px_rgba(0,0,0,0.12)]
          transition-all duration-[400ms] ease-[cubic-bezier(0.16,1,0.3,1)]
          ${menuOpen ? "translate-y-0 opacity-100 pointer-events-auto" : "-translate-y-[110%] opacity-0 pointer-events-none"}
        `}
      >
        <nav>
          {NAV_LINKS.map((link) => (
            <a
              key={link}
              href={`#${link.toLowerCase()}`}
              onClick={() => setMenuOpen(false)}
              className="flex items-center justify-between py-[1.1rem] text-lg font-semibold text-[#0B2117] border-b border-black/[0.06] last:border-0 hover:text-[#1ED760] transition-colors no-underline"
            >
              {link}
              <ArrowRight size={16} className="opacity-30" />
            </a>
          ))}
          <Link
            href="/waitlist"
            onClick={() => setMenuOpen(false)}
            className="flex items-center justify-center gap-2 mt-7 bg-[#1ED760] text-[#0B2117] font-bold text-base px-8 py-[0.9rem] rounded-full no-underline"
          >
            Join Waitlist <ArrowRight size={16} />
          </Link>
        </nav>
      </div>

      {/* Navbar — inline styles kept only for scroll-animated values */}
      <header
        className="fixed top-0 left-0 right-0 z-[99] flex justify-center pointer-events-none transition-[padding] duration-[2500ms] ease-[cubic-bezier(0.16,1,0.3,1)]"
        style={{ padding: scrolled ? "12px 24px" : "24px 24px" }}
      >
        <div
          className="pointer-events-auto w-full flex items-center justify-between transition-all duration-[2500ms] ease-[cubic-bezier(0.16,1,0.3,1)]"
          style={{
            maxWidth: scrolled ? "900px" : "1100px",
            background: scrolled || menuOpen ? "rgba(255,255,255,0.95)" : "transparent",
            borderRadius: scrolled ? "9999px" : "8px",
            padding: scrolled ? "10px 24px" : menuOpen ? "10px 16px" : "0",
            boxShadow: scrolled ? "0 4px 30px rgba(0,0,0,0.12)" : "none",
            border: scrolled ? "1px solid rgba(0,0,0,0.08)" : "1px solid transparent",
            backdropFilter: scrolled ? "blur(12px)" : "none",
          }}
        >
          <div className="relative h-6 w-36">
            <img
              src="/TrustBridgeName.png"
              alt="TrustBridge"
              className="absolute top-[-4px] left-0 transition-opacity duration-[400ms]"
              style={{ opacity: scrolled || menuOpen ? 0 : 1 }}
            />
            <img
              src="/TrustBridgeNameBlack.png"
              alt="TrustBridge"
              className="absolute top-[-4px] left-0 transition-opacity duration-[400ms]"
              style={{ opacity: scrolled || menuOpen ? 1 : 0 }}
            />
          </div>

          <nav className="hidden md:flex items-center gap-14 mr-14">
            {NAV_LINKS.map((link) => (
              <a
                key={link}
                href={`#${link.toLowerCase()}`}
                className="no-underline text-sm font-medium transition-colors duration-300"
                style={{ color: scrolled ? "rgba(11,33,23,0.6)" : "rgba(255,255,255,0.75)" }}
              >
                {link}
              </a>
            ))}
          </nav>

          <Link
            href="/waitlist"
            className="hidden md:flex items-center gap-1.5 bg-[#1ED760] text-[#0B2117] font-semibold text-sm px-5 py-2 rounded-full no-underline"
          >
            Waitlist <ArrowRight size={14} />
          </Link>

          <button
            className="flex md:hidden items-center justify-center p-1.5 rounded-lg bg-transparent border-0 cursor-pointer transition-colors duration-300"
            onClick={() => setMenuOpen((prev) => !prev)}
            aria-label={menuOpen ? "Close menu" : "Open menu"}
            style={{ color: scrolled || menuOpen ? "#0B2117" : "white" }}
          >
            {menuOpen
              ? <XIcon size={24} strokeWidth={2.5} />
              : <Menu size={24} strokeWidth={2.5} />
            }
          </button>
        </div>
      </header>

      <main>

        {/* Hero */}
        <section className="bg-noise bg-[#0B2117] text-white pt-32 pb-24 rounded-b-[2rem] shadow-[0_8px_40px_rgba(0,0,0,0.2)]">
          <div className="max-w-[56rem] mx-auto px-6 text-center">
            <h1 className="font-serif text-[clamp(2.5rem,6vw,4.5rem)] font-medium leading-[1.1] mb-6">
              Never Wait for<br />a Payment Again
            </h1>
            <p className="text-lg text-white/65 max-w-[36rem] mx-auto mb-10 leading-[1.7] font-light">
              The smart bridge between your hard work and your bank account. We secure the funds before you start, so you get paid the moment you finish.
            </p>
            <div className="flex gap-4 justify-center flex-wrap mb-16">
              <a
                href="#solution"
                className="px-8 py-[0.875rem] rounded-full bg-[#1ED760] text-[#0B2117] font-semibold text-base no-underline"
              >
                Solution
              </a>
              <Link
                href="/waitlist"
                className="flex items-center gap-2 px-8 py-[0.875rem] rounded-full bg-white text-[#0B2117] font-semibold text-base no-underline"
              >
                Waitlist <ArrowRight size={16} />
              </Link>
            </div>
            <div className="rounded-[1.5rem] overflow-hidden border border-white/10">
              <img
                src="/LadyOnLaptop.avif"
                alt="Freelancer working on laptop"
                className="w-full object-cover block"
                style={{ height: "clamp(220px, 40vw, 450px)" }}
              />
            </div>
          </div>
        </section>

        {/* Partners */}
        <section className="bg-[#E6F4EA] py-12 px-6">
          <div className="max-w-[80rem] mx-auto text-center">
            <span className="inline-block bg-[#0B2117] text-white text-xs font-semibold tracking-[0.08em] px-5 py-2 rounded-full mb-8">
              Built on the same infrastructure used by
            </span>
            <div className="flex flex-wrap justify-center items-center gap-8 opacity-55 text-[#0B2117]">
              <span className="font-bold text-xl">stripe</span>
              <span className="font-bold flex items-center gap-1.5"><ShieldCheck size={18} /> Escrow.com</span>
              <span className="font-bold flex items-center gap-1.5"><Landmark size={18} /> Open Banking</span>
              <span className="font-semibold">Pay</span>
            </div>
          </div>
        </section>

        {/* Solution */}
        <section id="solution" className="bg-[#E6F4EA] px-6 pb-10">
          <div className="max-w-[80rem] mx-auto grid grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-12 items-center">
            <div className="bg-[#0B2117] text-white rounded-[2rem] p-[clamp(2rem,5vw,3.5rem)] relative overflow-hidden">
              <div className="absolute top-0 right-0 w-64 h-64 bg-[#1ED760] opacity-5 blur-[80px] rounded-full pointer-events-none" />
              <h2 className="font-serif text-[clamp(1.75rem,4vw,2.25rem)] font-medium mb-5 leading-[1.2]">
                Escrow Payments<br />For Freelancers
              </h2>
              <p className="text-white/65 leading-[1.7] mb-8">
                Join us, accommodating all types of freelancers, builders to agencies and everything in between. Use our secure Email or API link and we solve the rest.
              </p>
              <Link
                href="/waitlist"
                className="inline-flex items-center gap-1.5 bg-[#1ED760] text-[#0B2117] font-semibold px-6 py-3 rounded-full no-underline"
              >
                Join Waitlist <ArrowRight size={15} />
              </Link>
            </div>
            <img src="/Group 11.png" alt="TrustBridge product diagram" className="w-full rounded-[1rem]" />
          </div>
        </section>

        {/* How it works */}
        <section id="how-it-works" className="bg-[#E6F4EA] px-6 py-16 pb-24">
          <div className="max-w-[64rem] mx-auto text-center">
            <h2 className="font-serif text-[clamp(1.75rem,4vw,2.5rem)] font-medium text-[#0B2117] mb-4">
              Guaranteed payments in three simple steps.
            </h2>
            <p className="text-[#0B2117]/55 text-base max-w-[36rem] mx-auto mb-16 leading-[1.6]">
              TrustBridge sits between you and your client, ensuring the money is there before the tools come out.
            </p>

            <div className="grid grid-cols-[repeat(auto-fit,minmax(220px,1fr))] gap-8 relative">
              <div className="hidden md:block absolute top-1/2 left-[10%] right-[10%] h-px bg-[#0B2117]/10 z-0 pointer-events-none" />

              {STEPS.map(({ step, title, desc, dark }) => (
                <article
                  key={step}
                  className={`relative z-10 px-6 py-10 rounded-[1.5rem] ${
                    dark
                      ? "bg-[#0B2117] text-white shadow-[0_10px_30px_rgba(0,0,0,0.1)]"
                      : "bg-white border border-black/5 shadow-[0_4px_20px_rgba(0,0,0,0.02)]"
                  }`}
                >
                  <div
                    className={`w-14 h-14 rounded-xl flex items-center justify-center text-xl font-bold mx-auto mb-6 ${
                      dark ? "bg-white text-[#0B2117]" : "bg-[#0B2117] text-[#1ED760]"
                    }`}
                  >
                    {step}
                  </div>
                  <h3 className={`text-xl font-bold mb-3 ${dark ? "text-white" : "text-[#0B2117]"}`}>{title}</h3>
                  <p className={`text-sm leading-[1.6] ${dark ? "text-white/70" : "text-[#0B2117]/55"}`}>{desc}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* Who it's for */}
        <section id="businesses" className="bg-white px-6 py-28 border-t border-black/5">
          <div className="max-w-[80rem] mx-auto">
            <div className="text-center mb-20">
              <h2 className="font-serif text-[clamp(2rem,4vw,2.75rem)] font-medium text-[#0B2117] mb-4">
                Built for every builder.
              </h2>
              <p className="text-[#0B2117]/60 text-lg max-w-[42rem] mx-auto leading-[1.6]">
                From solo developers to scaling digital agencies, TrustBridge provides the financial infrastructure to guarantee your payments.
              </p>
            </div>

            <div className="grid grid-cols-[repeat(auto-fit,minmax(300px,1fr))] gap-8">
              {AUDIENCES.map(({ Icon, title, desc }) => (
                <article key={title} className="p-10 rounded-[1.5rem] bg-[#E6F4EA] border border-black/[0.03]">
                  <Icon size={36} color="#0B2117" className="mb-6" />
                  <h3 className="text-2xl font-bold text-[#0B2117] mb-4">{title}</h3>
                  <p className="text-[#0B2117]/65 leading-[1.6]">{desc}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* Features */}
        <section className="bg-white px-6 py-24 border-t border-b border-black/5">
          <div className="max-w-[80rem] mx-auto grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-6">
            {FEATURES.map(({ Icon, title, desc }) => (
              <article key={title} className="bg-[#E6F4EA]/50 border border-black/5 rounded-2xl p-8">
                <Icon size={28} color="#0B2117" className="mb-6" />
                <h3 className="font-bold mb-2 text-[#0B2117]">{title}</h3>
                <p className="text-[#0B2117]/55 text-sm leading-[1.6]">{desc}</p>
              </article>
            ))}

            <article className="bg-[#0B2117] rounded-2xl p-8 text-white relative overflow-hidden">
              <span className="absolute top-4 right-4 text-[0.625rem] font-bold uppercase tracking-[0.1em] bg-white/10 px-2 py-1 rounded">
                Soon
              </span>
              <Plug size={28} color="#1ED760" className="mb-6" />
              <h3 className="font-bold mb-2">Stack Triggers</h3>
              <p className="text-white/55 text-sm leading-[1.6]">
                Link GitHub or Trello. Merge a PR or move a card to "Done" to release payments automatically.
              </p>
            </article>
          </div>
        </section>

        {/* Pricing */}
        <section id="pricing" className="bg-[#E6F4EA] px-6 py-28 pb-32 rounded-t-[2rem]">
          <div className="max-w-[80rem] mx-auto">
            <div className="text-center mb-20">
              <h2 className="font-serif text-[clamp(2rem,4vw,2.75rem)] font-medium text-[#0B2117] mb-4">
                Transparent, optimised pricing.
              </h2>
              <p className="text-[#0B2117]/60 text-lg max-w-[42rem] mx-auto leading-[1.6]">
                No monthly subscriptions. We automatically route your transactions through the cheapest, most secure rail, passing the savings to you.
              </p>
            </div>

            <div className="grid grid-cols-[repeat(auto-fit,minmax(300px,1fr))] gap-8 items-center">

              <article className="bg-white p-12 rounded-[1.5rem] border border-black/5 shadow-[0_4px_20px_rgba(0,0,0,0.03)]">
                <div className="flex items-center gap-3 mb-4">
                  <CreditCard size={24} color="#0B2117" />
                  <h3 className="text-xl font-bold text-[#0B2117]">Under £1,000</h3>
                </div>
                <p className="text-[#0B2117]/55 mb-8 min-h-[3rem] text-[0.9rem] leading-[1.5]">
                  Frictionless payments for smaller transactions where convenience matters most.
                </p>
                <p className="text-[2.25rem] font-bold text-[#0B2117] mb-1 tracking-[-0.03em]">Stripe</p>
                <p className="text-[#0B2117]/45 font-medium mb-10 text-sm">Includes Apple & Google Pay</p>
                <ul className="space-y-4 text-[#0B2117]/70 text-[0.95rem] list-none p-0 m-0">
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Standard processing fees</li>
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Two-tap mobile checkout</li>
                </ul>
              </article>

              <article className="bg-[#0B2117] p-14 rounded-[1.5rem] border border-[#1ED760] shadow-[0_20px_40px_rgba(30,215,96,0.15)] relative z-10 scale-[1.02]">
                <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[#1ED760] text-[#0B2117] px-5 py-1.5 rounded-full text-xs font-bold tracking-[0.05em] uppercase whitespace-nowrap">
                  Most Popular
                </div>
                <div className="flex items-center gap-3 mb-4">
                  <Landmark size={24} color="#1ED760" />
                  <h3 className="text-xl font-bold text-white">£1,000 – £20,000</h3>
                </div>
                <p className="text-white/65 mb-8 min-h-[3rem] text-[0.9rem] leading-[1.5]">
                  Instant bank transfers that save you hundreds in processing fees on a mid-value job.
                </p>
                <p className="text-[2.25rem] font-bold text-white mb-1 tracking-[-0.03em]">Open Banking</p>
                <p className="text-[#1ED760] font-medium mb-10 text-sm">via Pay by Bank</p>
                <ul className="space-y-4 text-white/80 text-[0.95rem] list-none p-0 m-0">
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Drastically reduced flat fees</li>
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Instant settlement to account</li>
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Zero chargeback risk</li>
                </ul>
              </article>

              <article className="bg-white p-12 rounded-[1.5rem] border border-black/5 shadow-[0_4px_20px_rgba(0,0,0,0.03)]">
                <div className="flex items-center gap-3 mb-4">
                  <ShieldCheck size={24} color="#0B2117" />
                  <h3 className="text-xl font-bold text-[#0B2117]">Over £20,000</h3>
                </div>
                <p className="text-[#0B2117]/55 mb-8 min-h-[3rem] text-[0.9rem] leading-[1.5]">
                  Fully regulated escrow handling for high-value transactions and enterprise agencies.
                </p>
                <p className="text-[2.25rem] font-bold text-[#0B2117] mb-1 tracking-[-0.03em]">Escrow.com</p>
                <p className="text-[#0B2117]/45 font-medium mb-10 text-sm">Regulated Fund Holder</p>
                <ul className="space-y-4 text-[#0B2117]/70 text-[0.95rem] list-none p-0 m-0">
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Institutional grade protection</li>
                  <li className="flex items-center gap-3"><CheckCircle2 size={18} color="#1ED760" /> Secure wire transfer support</li>
                </ul>
              </article>

            </div>
          </div>
        </section>

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