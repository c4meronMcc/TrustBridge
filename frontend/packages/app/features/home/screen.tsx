"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { ArrowRight, ShieldCheck, Landmark, Scale, Split, CheckCircle2, Plug, Twitter, Linkedin, Menu, XIcon, Briefcase, Building2, Users, CreditCard } from "lucide-react";

export default function App() {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const onScroll = () => setScrolled(el.scrollTop > 40);
    el.addEventListener("scroll", onScroll, { passive: true });
    return () => el.removeEventListener("scroll", onScroll);
  }, []);

  // Close menu when user scrolls
  useEffect(() => {
    if (scrolled) setMenuOpen(false);
  }, [scrolled]);

  const navLinks = ["Businesses", "Pricing", "Solution"];

  return (
    <div ref={ref} style={{ height: "100vh", overflowY: "scroll", background: "#E6F4EA", fontFamily: "Inter, sans-serif" }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap');
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        .font-serif { font-family: 'Playfair Display', serif; }
        .bg-noise { background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E"); }

        /* Mobile menu slides down from top */
        .mobile-menu {
          position: fixed;
          top: 0; left: 0; right: 0;
          z-index: 98;
          background: rgba(85, 85, 85, 0.99);
          backdrop-filter: blur(16px);
          -webkit-backdrop-filter: blur(16px);
          padding: 5.5rem 2rem 2.5rem;
          border-bottom-left-radius: 1.5rem;
          border-bottom-right-radius: 1.5rem;
          border-bottom: 1px solid rgba(0,0,0,0.06);
          box-shadow: 0 16px 48px rgba(0,0,0,0.12);
          transform: translateY(-110%);
          opacity: 0;
          transition: transform 0.4s cubic-bezier(0.16,1,0.3,1), opacity 0.3s ease;
          pointer-events: none;
        }
        .mobile-menu.open {
          transform: translateY(0);
          opacity: 1;
          pointer-events: all;
        }
        .mobile-nav-link {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 1.1rem 0;
          font-size: 1.125rem;
          font-weight: 600;
          color: #0B2117;
          text-decoration: none;
          border-bottom: 1px solid rgba(0,0,0,0.06);
          transition: color 0.2s;
        }
        .mobile-nav-link:hover { color: #1ED760; }
        .mobile-nav-link:last-of-type { border-bottom: none; }

        /* Hide/show based on breakpoint */
        .desktop-only { display: flex; }
        .mobile-only  { display: none; }

        @media (max-width: 768px) {
          .desktop-only { display: none !important; }
          .mobile-only  { display: flex !important; }
        }
      `}</style>

      {/* ── MOBILE SLIDE-DOWN MENU ── */}
      <div className={`mobile-menu ${menuOpen ? "open" : ""}`}>
        {navLinks.map(l => (
          <a key={l} href={`#${l.toLowerCase()}`} className="mobile-nav-link" onClick={() => setMenuOpen(false)}>
            {l}
            <ArrowRight size={16} style={{ opacity: 0.3 }} />
          </a>
        ))}
        <Link
          href="/waitlist"
          onClick={() => setMenuOpen(false)}
          style={{ display: "inline-flex", alignItems: "center", gap: "0.5rem", marginTop: "1.75rem", background: "#1ED760", color: "#0B2117", fontWeight: 700, fontSize: "1rem", padding: "0.9rem 2rem", borderRadius: "999px", textDecoration: "none", width: "100%", justifyContent: "center" }}
        >
          Join Waitlist <ArrowRight size={16} />
        </Link>
      </div>

      {/* ── NAVBAR ── */}
      <div style={{
        position: "fixed", top: 0, left: 0, right: 0, zIndex: 99,
        display: "flex", justifyContent: "center",
        padding: scrolled ? "12px 24px" : "24px 24px",
        transition: "padding 2.5s cubic-bezier(0.16,1,0.3,1)",
        pointerEvents: "none",
      }}>
        <div style={{
          pointerEvents: "all",
          width: "100%",
          maxWidth: scrolled ? "900px" : "1100px",
          display: "flex", alignItems: "center", justifyContent: "space-between",
          background: scrolled || menuOpen ? "rgba(255,255,255,0.95)" : "transparent",
          borderRadius: scrolled ? "999px" : "8px",
          padding: scrolled ? "10px 24px" : menuOpen ? "10px 16px" : "0",
          boxShadow: scrolled ? "0 4px 30px rgba(0,0,0,0.12)" : "none",
          border: scrolled ? "1px solid rgba(0,0,0,0.08)" : "1px solid transparent",
          backdropFilter: scrolled ? "blur(12px)" : "none",
          transition: "all 2.5s cubic-bezier(0.16,1,0.3,1)",
        }}>

          {/* Logo — swaps light/dark */}
          <div style={{ position: "relative", height: "1.5rem", width: "9rem" }}>
            <img src="/TrustBridgeName.png" alt="TrustBridge"
              style={{ position: "absolute", top: "-4px", left: 0, opacity: scrolled || menuOpen ? 0 : 1, transition: "opacity 0.4s" }} />
            <img src="/TrustBridgeNameBlack.png" alt="TrustBridge"
              style={{ position: "absolute", top: "-4px", left: 0, opacity: scrolled || menuOpen ? 1 : 0, transition: "opacity 0.4s" }} />
          </div>

          {/* Desktop nav links */}
          <div className="desktop-only" style={{ gap: 55, marginRight: 55 }}>
            {navLinks.map(l => (
              <a key={l} href={`#${l.toLowerCase()}`} style={{ textDecoration: "none", fontSize: 14, fontWeight: 500, color: scrolled ? "rgba(11,33,23,0.6)" : "rgba(255,255,255,0.75)", transition: "color 0.3s" }}>{l}</a>
            ))}
          </div>

          {/* Desktop CTA */}
          <Link href="/waitlist" className="desktop-only"
            style={{ background: "#1ED760", color: "#0B2117", fontWeight: 600, fontSize: 14, padding: "8px 20px", borderRadius: 999, textDecoration: "none", alignItems: "center", gap: "0.4rem" }}>
            Waitlist <ArrowRight size={14} />
          </Link>

          {/* Mobile burger/close button */}
          <button
            className="mobile-only"
            onClick={() => setMenuOpen(o => !o)}
            style={{
              background: "none", border: "none", cursor: "pointer",
              padding: "6px", borderRadius: "8px",
              alignItems: "center", justifyContent: "center",
              color: scrolled || menuOpen ? "#0B2117" : "white",
              transition: "color 0.3s",
            }}
            aria-label={menuOpen ? "Close menu" : "Open menu"}
          >
            {menuOpen
              ? <XIcon size={24} strokeWidth={2.5} />
              : <Menu size={24} strokeWidth={2.5} />
            }
          </button>
        </div>
      </div>

      {/* ── HERO ── */}
      <section className="bg-noise" style={{ background: "#0B2117", color: "white", paddingTop: "8rem", paddingBottom: "6rem", borderBottomLeftRadius: "2rem", borderBottomRightRadius: "2rem", boxShadow: "0 8px 40px rgba(0,0,0,0.2)" }}>
        <div style={{ maxWidth: "56rem", margin: "0 auto", padding: "0 1.5rem", textAlign: "center" }}>
          <h1 className="font-serif" style={{ fontSize: "clamp(2.5rem, 6vw, 4.5rem)", fontWeight: 500, lineHeight: 1.1, marginBottom: "1.5rem" }}>
            Never Wait for<br />a Payment Again
          </h1>
          <p style={{ fontSize: "1.125rem", color: "rgba(255,255,255,0.65)", maxWidth: "36rem", margin: "0 auto 2.5rem", lineHeight: 1.7, fontWeight: 300 }}>
            The smart bridge between your hard work and your bank account. We secure the funds before you start, so you get paid the moment you finish.
          </p>
          <div style={{ display: "flex", gap: "1rem", justifyContent: "center", flexWrap: "wrap", marginBottom: "4rem" }}>
            <a href="#solution" style={{ padding: "0.875rem 2rem", borderRadius: "9999px", background: "#1ED760", color: "#0B2117", fontWeight: 600, fontSize: "1rem", textDecoration: "none" }}>Solution</a>
            <Link href="/waitlist" style={{ padding: "0.875rem 2rem", borderRadius: "9999px", background: "white", color: "#0B2117", fontWeight: 600, fontSize: "1rem", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "0.5rem" }}>
              Waitlist <ArrowRight size={16} />
            </Link>
          </div>
          <div style={{ borderRadius: "1.5rem", overflow: "hidden", border: "1px solid rgba(255,255,255,0.1)" }}>
            <img src="/LadyOnLaptop.avif" alt="Freelancer working on laptop" style={{ width: "100%", height: "clamp(220px, 40vw, 450px)", objectFit: "cover", display: "block" }} />
          </div>
        </div>
      </section>

      {/* ── PARTNERS ── */}
      <section style={{ background: "#E6F4EA", padding: "3rem 1.5rem" }}>
        <div style={{ maxWidth: "80rem", margin: "0 auto", textAlign: "center" }}>
          <span style={{ display: "inline-block", background: "#0B2117", color: "white", fontSize: "0.75rem", fontWeight: 600, letterSpacing: "0.08em", padding: "0.5rem 1.25rem", borderRadius: "9999px", marginBottom: "2rem" }}>
            Built on the same infrastructure used by
          </span>
          <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", alignItems: "center", gap: "2rem", opacity: 0.55, color: "#0B2117" }}>
            <span style={{ fontWeight: 700, fontSize: "1.25rem" }}>stripe</span>
            <span style={{ fontWeight: 700, display: "flex", alignItems: "center", gap: "0.4rem" }}><ShieldCheck size={18} /> Escrow.com</span>
            <span style={{ fontWeight: 700, display: "flex", alignItems: "center", gap: "0.4rem" }}><Landmark size={18} /> Open Banking</span>
            <span style={{ fontWeight: 600 }}>Pay</span>
          </div>
        </div>
      </section>

      {/* ── SOLUTION ── */}
      <section id="solution" style={{ background: "#E6F4EA", padding: "0rem 1.5rem 2.5rem" }}>
        <div style={{ maxWidth: "80rem", margin: "0 auto", display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: "3rem", alignItems: "center" }}>
          <div style={{ background: "#0B2117", color: "white", borderRadius: "2rem", padding: "clamp(2rem, 5vw, 3.5rem)", position: "relative", overflow: "hidden" }}>
            <div style={{ position: "absolute", top: 0, right: 0, width: "16rem", height: "16rem", background: "#1ED760", opacity: 0.05, filter: "blur(80px)", borderRadius: "9999px" }} />
            <h2 className="font-serif" style={{ fontSize: "clamp(1.75rem, 4vw, 2.25rem)", fontWeight: 500, marginBottom: "1.25rem", lineHeight: 1.2 }}>
              Escrow Payments<br />For Freelancers
            </h2>
            <p style={{ color: "rgba(255,255,255,0.65)", lineHeight: 1.7, marginBottom: "2rem" }}>
              Join us, accommodating all types of freelancers, builders to agencies and everything in between. Use our secure Email or API link and we solve the rest.
            </p>
            <Link href="/waitlist" style={{ display: "inline-flex", alignItems: "center", gap: "0.4rem", background: "#1ED760", color: "#0B2117", fontWeight: 600, padding: "0.75rem 1.5rem", borderRadius: "9999px", textDecoration: "none" }}>
              Join Waitlist <ArrowRight size={15} />
            </Link>
          </div>
          <img src="/Group 11.png" alt="Product diagram" style={{ width: "100%", borderRadius: "1rem" }} />
        </div>
      </section>

      {/* ── HOW IT WORKS (3 SIMPLE STEPS) ── */}
      <section id="how-it-works" style={{ background: "#E6F4EA", padding: "4rem 1.5rem 6rem" }}>
        <div style={{ maxWidth: "64rem", margin: "0 auto", textAlign: "center" }}>
          <h2 className="font-serif" style={{ fontSize: "clamp(1.75rem, 4vw, 2.5rem)", fontWeight: 500, color: "#0B2117", marginBottom: "1rem" }}>
            Guaranteed payments in three simple steps.
          </h2>
          <p style={{ color: "rgba(11,33,23,0.55)", fontSize: "1rem", maxWidth: "36rem", margin: "0 auto 4rem", lineHeight: 1.6 }}>
            TrustBridge sits between you and your client, ensuring the money is there before the tools come out.
          </p>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "2rem", position: "relative" }}>
            {/* Desktop connecting line */}
            <div className="desktop-only" style={{ position: "absolute", top: "50%", left: "10%", right: "10%", height: "1px", background: "rgba(11,33,23,0.1)", zIndex: 0 }}></div>

            <div style={{ background: "white", padding: "2.5rem 1.5rem", borderRadius: "1.5rem", border: "1px solid rgba(0,0,0,0.05)", boxShadow: "0 4px 20px rgba(0,0,0,0.02)", position: "relative", zIndex: 1 }}>
              <div style={{ width: "3.5rem", height: "3.5rem", background: "#0B2117", color: "#1ED760", borderRadius: "0.75rem", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "1.25rem", fontWeight: 700, margin: "0 auto 1.5rem" }}>1</div>
              <h3 style={{ fontSize: "1.25rem", fontWeight: 700, color: "#0B2117", marginBottom: "0.75rem" }}>Agree & Lock</h3>
              <p style={{ color: "rgba(11,33,23,0.55)", fontSize: "0.875rem", lineHeight: 1.6 }}>You and your client agree on project milestones. The client funds the secure TrustBridge vault upfront.</p>
            </div>

            <div style={{ background: "white", padding: "2.5rem 1.5rem", borderRadius: "1.5rem", border: "1px solid rgba(0,0,0,0.05)", boxShadow: "0 4px 20px rgba(0,0,0,0.02)", position: "relative", zIndex: 1 }}>
              <div style={{ width: "3.5rem", height: "3.5rem", background: "#0B2117", color: "#1ED760", borderRadius: "0.75rem", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "1.25rem", fontWeight: 700, margin: "0 auto 1.5rem" }}>2</div>
              <h3 style={{ fontSize: "1.25rem", fontWeight: 700, color: "#0B2117", marginBottom: "0.75rem" }}>Do the Work</h3>
              <p style={{ color: "rgba(11,33,23,0.55)", fontSize: "0.875rem", lineHeight: 1.6 }}>Hit your milestones with complete peace of mind, knowing the money is already secured and waiting for you.</p>
            </div>

            <div style={{ background: "#0B2117", color: "white", padding: "2.5rem 1.5rem", borderRadius: "1.5rem", boxShadow: "0 10px 30px rgba(0,0,0,0.1)", position: "relative", zIndex: 1 }}>
              <div style={{ width: "3.5rem", height: "3.5rem", background: "white", color: "#0B2117", borderRadius: "0.75rem", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "1.25rem", fontWeight: 700, margin: "0 auto 1.5rem" }}>3</div>
              <h3 style={{ fontSize: "1.25rem", fontWeight: 700, marginBottom: "0.75rem" }}>Get Paid Instantly</h3>
              <p style={{ color: "rgba(255,255,255,0.7)", fontSize: "0.875rem", lineHeight: 1.6 }}>Submit your work. Once the milestone is approved, the system releases your funds automatically. No chasing.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ── WHO IT'S FOR (BUSINESSES) ── */}
      <section id="businesses" style={{ background: "white", padding: "7rem 1.5rem", borderTop: "1px solid rgba(0,0,0,0.05)" }}>
        <div style={{ maxWidth: "80rem", margin: "0 auto" }}>
          <div style={{ textAlign: "center", marginBottom: "5rem" }}>
            <h2 className="font-serif" style={{ fontSize: "clamp(2rem, 4vw, 2.75rem)", fontWeight: 500, color: "#0B2117", marginBottom: "1rem" }}>Built for every builder.</h2>
            <p style={{ color: "rgba(11,33,23,0.6)", fontSize: "1.125rem", maxWidth: "42rem", margin: "0 auto", lineHeight: 1.6 }}>From solo developers to scaling digital agencies, TrustBridge provides the financial infrastructure to guarantee your payments.</p>
          </div>
          
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))", gap: "2rem" }}>
            <div style={{ padding: "3rem 2.5rem", borderRadius: "1.5rem", background: "#E6F4EA", border: "1px solid rgba(0,0,0,0.03)", transition: "transform 0.3s" }} className="hover:-translate-y-1">
              <Briefcase size={36} color="#0B2117" style={{ marginBottom: "1.5rem" }} />
              <h3 style={{ fontSize: "1.5rem", fontWeight: 700, color: "#0B2117", marginBottom: "1rem" }}>Freelancers</h3>
              <p style={{ color: "rgba(11,33,23,0.65)", lineHeight: 1.6 }}>Tradespeople, developers, designers, and consultants. Anyone who wants deposit protection and guaranteed payment on completion.</p>
            </div>
            
            <div style={{ padding: "3rem 2.5rem", borderRadius: "1.5rem", background: "#E6F4EA", border: "1px solid rgba(0,0,0,0.03)", transition: "transform 0.3s" }} className="hover:-translate-y-1">
              <Building2 size={36} color="#0B2117" style={{ marginBottom: "1.5rem" }} />
              <h3 style={{ fontSize: "1.5rem", fontWeight: 700, color: "#0B2117", marginBottom: "1rem" }}>Agencies</h3>
              <p style={{ color: "rgba(11,33,23,0.65)", lineHeight: 1.6 }}>Manage multi-milestone projects across multiple contractors with a clean, auditable payment flow. No more spreadsheet chaos.</p>
            </div>
            
            <div style={{ padding: "3rem 2.5rem", borderRadius: "1.5rem", background: "#E6F4EA", border: "1px solid rgba(0,0,0,0.03)", transition: "transform 0.3s" }} className="hover:-translate-y-1">
              <Users size={36} color="#0B2117" style={{ marginBottom: "1.5rem" }} />
              <h3 style={{ fontSize: "1.5rem", fontWeight: 700, color: "#0B2117", marginBottom: "1rem" }}>Clients</h3>
              <p style={{ color: "rgba(11,33,23,0.65)", lineHeight: 1.6 }}>Individuals and businesses who want absolute assurance that their funds are only released when the agreed work is genuinely delivered.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ── FEATURES ── */}
      <section style={{ background: "white", padding: "6rem 1.5rem", borderTop: "1px solid rgba(0,0,0,0.05)", borderBottom: "1px solid rgba(0,0,0,0.05)" }}>
        <div style={{ maxWidth: "80rem", margin: "0 auto", display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "1.5rem" }}>
          {[
            { Icon: Scale, title: "Neutral Arbitration", desc: "Disputes force compromise. If no agreement is reached, we provide neutral third-party arbitration." },
            { Icon: Split, title: "Smart Routing", desc: "Automatically selects the cheapest payment rail based on size. Frictionless cards or Open Banking." },
            { Icon: CheckCircle2, title: "State-Machine", desc: "Financial transitions are enforced by code. Money cannot move unless conditions are met. No exceptions." },
          ].map(({ Icon, title, desc }) => (
            <div key={title} style={{ background: "rgba(230,244,234,0.5)", border: "1px solid rgba(0,0,0,0.05)", borderRadius: "1rem", padding: "2rem" }}>
              <Icon size={28} color="#0B2117" style={{ marginBottom: "1.5rem" }} />
              <h3 style={{ fontWeight: 700, marginBottom: "0.5rem", color: "#0B2117" }}>{title}</h3>
              <p style={{ color: "rgba(11,33,23,0.55)", fontSize: "0.875rem", lineHeight: 1.6 }}>{desc}</p>
            </div>
          ))}
          <div style={{ background: "#0B2117", borderRadius: "1rem", padding: "2rem", color: "white", position: "relative", overflow: "hidden" }}>
            <span style={{ position: "absolute", top: "1rem", right: "1rem", fontSize: "0.625rem", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", background: "rgba(255,255,255,0.1)", padding: "0.25rem 0.5rem", borderRadius: "4px" }}>Soon</span>
            <Plug size={28} color="#1ED760" style={{ marginBottom: "1.5rem" }} />
            <h3 style={{ fontWeight: 700, marginBottom: "0.5rem" }}>Stack Triggers</h3>
            <p style={{ color: "rgba(255,255,255,0.55)", fontSize: "0.875rem", lineHeight: 1.6 }}>Link GitHub or Trello. Merge a PR or move a card to "Done" to release payments automatically.</p>
          </div>
        </div>
      </section>

      {/* ── PRICING ── */}
      <section id="pricing" style={{ background: "#E6F4EA", padding: "7rem 1.5rem 8rem", borderTopLeftRadius: "2rem", borderTopRightRadius: "2rem" }}>
        <div style={{ maxWidth: "80rem", margin: "0 auto" }}>
          <div style={{ textAlign: "center", marginBottom: "5rem" }}>
            <h2 className="font-serif" style={{ fontSize: "clamp(2rem, 4vw, 2.75rem)", fontWeight: 500, color: "#0B2117", marginBottom: "1rem" }}>Transparent, optimized pricing.</h2>
            <p style={{ color: "rgba(11,33,23,0.6)", fontSize: "1.125rem", maxWidth: "42rem", margin: "0 auto", lineHeight: 1.6 }}>
              No monthly subscriptions. We automatically route your transactions through the cheapest, most secure rail—passing the savings to you.
            </p>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))", gap: "2rem", alignItems: "center" }}>
            {/* Stripe Card */}
            <div style={{ background: "white", padding: "3rem 2rem", borderRadius: "1.5rem", border: "1px solid rgba(0,0,0,0.05)", boxShadow: "0 4px 20px rgba(0,0,0,0.03)" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
                <CreditCard size={24} color="#0B2117" />
                <h3 style={{ fontSize: "1.25rem", fontWeight: 700, color: "#0B2117" }}>Under £1,000</h3>
              </div>
              <p style={{ color: "rgba(11,33,23,0.55)", marginBottom: "2rem", minHeight: "3rem", fontSize: "0.9rem", lineHeight: 1.5 }}>Frictionless payments for smaller transactions where convenience matters most.</p>
              <div style={{ fontSize: "2.25rem", fontWeight: 700, color: "#0B2117", marginBottom: "0.25rem", letterSpacing: "-0.03em" }}>Stripe</div>
              <p style={{ color: "rgba(11,33,23,0.45)", fontWeight: 500, marginBottom: "2.5rem", fontSize: "0.875rem" }}>Includes Apple & Google Pay</p>
              <ul style={{ listStyle: "none", padding: 0, margin: 0, color: "rgba(11,33,23,0.7)", display: "flex", flexDirection: "column", gap: "1rem", fontSize: "0.95rem" }}>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Standard processing fees</li>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Two-tap mobile checkout</li>
              </ul>
            </div>

            {/* TrueLayer Card (Highlighted) */}
            <div style={{ background: "#0B2117", padding: "3.5rem 2rem", borderRadius: "1.5rem", border: "1px solid #1ED760", boxShadow: "0 20px 40px rgba(30,215,96,0.15)", position: "relative", zIndex: 10, transform: "scale(1.02)" }}>
              <div style={{ position: "absolute", top: 0, left: "50%", transform: "translate(-50%, -50%)", background: "#1ED760", color: "#0B2117", padding: "0.4rem 1.25rem", borderRadius: "999px", fontSize: "0.75rem", fontWeight: 700, letterSpacing: "0.05em", textTransform: "uppercase" }}>Most Popular</div>
              <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
                <Landmark size={24} color="#1ED760" />
                <h3 style={{ fontSize: "1.25rem", fontWeight: 700, color: "white" }}>£1,000 - £20,000</h3>
              </div>
              <p style={{ color: "rgba(255,255,255,0.65)", marginBottom: "2rem", minHeight: "3rem", fontSize: "0.9rem", lineHeight: 1.5 }}>Instant bank transfers that save you hundreds in processing fees on a mid-value job.</p>
              <div style={{ fontSize: "2.25rem", fontWeight: 700, color: "white", marginBottom: "0.25rem", letterSpacing: "-0.03em" }}>Open Banking</div>
              <p style={{ color: "#1ED760", fontWeight: 500, marginBottom: "2.5rem", fontSize: "0.875rem" }}>via Pay by Bank</p>
              <ul style={{ listStyle: "none", padding: 0, margin: 0, color: "rgba(255,255,255,0.8)", display: "flex", flexDirection: "column", gap: "1rem", fontSize: "0.95rem" }}>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Drastically reduced flat fees</li>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Instant settlement to account</li>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Zero chargeback risk</li>
              </ul>
            </div>

            {/* Escrow.com Card */}
            <div style={{ background: "white", padding: "3rem 2rem", borderRadius: "1.5rem", border: "1px solid rgba(0,0,0,0.05)", boxShadow: "0 4px 20px rgba(0,0,0,0.03)" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
                <ShieldCheck size={24} color="#0B2117" />
                <h3 style={{ fontSize: "1.25rem", fontWeight: 700, color: "#0B2117" }}>Over £20,000</h3>
              </div>
              <p style={{ color: "rgba(11,33,23,0.55)", marginBottom: "2rem", minHeight: "3rem", fontSize: "0.9rem", lineHeight: 1.5 }}>Fully regulated escrow handling for high-value transactions and enterprise agencies.</p>
              <div style={{ fontSize: "2.25rem", fontWeight: 700, color: "#0B2117", marginBottom: "0.25rem", letterSpacing: "-0.03em" }}>Escrow.com</div>
              <p style={{ color: "rgba(11,33,23,0.45)", fontWeight: 500, marginBottom: "2.5rem", fontSize: "0.875rem" }}>Regulated Fund Holder</p>
              <ul style={{ listStyle: "none", padding: 0, margin: 0, color: "rgba(11,33,23,0.7)", display: "flex", flexDirection: "column", gap: "1rem", fontSize: "0.95rem" }}>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Institutional grade protection</li>
                <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}><CheckCircle2 size={18} color="#1ED760" /> Secure wire transfer support</li>
              </ul>
            </div>
          </div>
        </div>
      </section>


      {/* ── FOOTER ── */}
      <div style={{ background: "white" }}>
        <footer className="bg-noise" style={{ background: "#0B2117", color: "white", borderTopLeftRadius: "2rem", borderTopRightRadius: "2rem", padding: "6rem 1.5rem 3rem" }}>
          <div style={{ maxWidth: "48rem", margin: "0 auto", textAlign: "center", marginBottom: "5rem" }}>
            <h2 className="font-serif" style={{ fontSize: "clamp(1.75rem, 4vw, 3rem)", fontWeight: 500, marginBottom: "1.25rem" }}>Stop working on a handshake.</h2>
            <p style={{ color: "rgba(255,255,255,0.55)", marginBottom: "2.5rem", lineHeight: 1.7 }}>
              Sign up for early access and be first to know when we launch.
            </p>
            <Link href="/waitlist" style={{ display: "inline-flex", alignItems: "center", gap: "0.5rem", background: "#1ED760", color: "#0B2117", fontWeight: 700, padding: "1rem 2.5rem", borderRadius: "0.75rem", textDecoration: "none", fontSize: "1rem" }}>
              Join the Waitlist <ArrowRight size={18} />
            </Link>
          </div>
          <div style={{ maxWidth: "80rem", margin: "0 auto", paddingTop: "2rem", borderTop: "1px solid rgba(255,255,255,0.1)", display: "flex", flexWrap: "wrap", alignItems: "center", justifyContent: "space-between", gap: "1.5rem" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <img src="/TrustBridgeName.png" alt="TrustBridge" style={{ height: "1.25rem" }} />
              <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "0.875rem", marginLeft: "0.5rem" }}>© 2026 All rights reserved.</span>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "1.25rem" }}>
              <a href="https://x.com/TrusttBridge" aria-label="Twitter" style={{ color: "rgba(255,255,255,0.5)" }}>
                <svg viewBox="0 0 24 24" className="w-5 h-5 fill-current" aria-hidden="true">
                    <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 22.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
                </svg>
              </a>
              {/* <a href="#" aria-label="LinkedIn" style={{ color: "rgba(255,255,255,0.5)" }}><Linkedin size={18} /></a> */}
              <span style={{ width: 1, height: 16, background: "rgba(255,255,255,0.2)", display: "inline-block" }} />
              <a href="/privacy" style={{ color: "rgba(255,255,255,0.5)", fontSize: "0.875rem", textDecoration: "none" }}>Privacy Policy</a>
              <a href="/terms" style={{ color: "rgba(255,255,255,0.5)", fontSize: "0.875rem", textDecoration: "none" }}>Terms of Service</a>
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}