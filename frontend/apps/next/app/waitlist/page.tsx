"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, ArrowRight, CheckCircle } from "lucide-react";

const professions = [
  "Freelance Developer",
  "Freelance Designer",
  "Creative Agency",
  "Copywriter / Content Creator",
  "Marketing Consultant",
  "Video Editor / Animator",
  "Photographer / Videographer",
  "Web / Digital Agency",
  "Accountant / Finance Consultant",
  "Other",
];

export default function Waitlist() {
  const [email, setEmail] = useState("");
  const [profession, setProfession] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    setError("Please enter a valid email address.");
    return;
  }
  if (!profession) {
    setError("Please select your profession.");
    return;
  }
  setError("");
  setLoading(true); // add: const [loading, setLoading] = useState(false);

  try {
    const res = await fetch("/api/waitlist", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, profession }),
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.error);
    setSubmitted(true);
  } catch (err: any) {
    setError(err.message || "Something went wrong. Please try again.");
  } finally {
    setLoading(false);
  }
    };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap');
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        :root {
          --dark: #0B2117;
          --green: #1ED760;
          --light: #E6F4EA;
        }

        body { font-family: 'Inter', sans-serif; }
        .font-serif { font-family: 'Playfair Display', serif; }

        /* ── PAGE ── */
        .page {
          min-height: 100vh;
          background: var(--dark);
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 5rem 1.5rem 3rem;
          position: relative;
          overflow: hidden;
        }

        /* ── NOISE ── */
        .noise {
          position: absolute; inset: 0; pointer-events: none; z-index: 0;
          background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.05'/%3E%3C/svg%3E");
        }

        /* ── BACKGROUND DECORATION ── */
        /* Ghost UI cards — static, low opacity, purely atmospheric */
        .bg-scene {
          position: absolute; inset: 0;
          pointer-events: none; z-index: 0;
        }

        .ghost-card {
          position: absolute;
          border: 1px solid rgba(255,255,255,0.07);
          border-radius: 1rem;
          background: rgba(255,255,255,0.025);
          padding: 1.125rem 1.25rem;
          color: rgba(255,255,255,0.35);
        }

        /* Top-left card — escrow amount */
        .ghost-1 {
          width: 220px;
          top: 12%; left: 6%;
          opacity: 0.55;
          transform: rotate(-3deg);
        }
        /* Bottom-right card — payment released */
        .ghost-2 {
          width: 200px;
          bottom: 12%; right: 7%;
          opacity: 0.5;
          transform: rotate(2deg);
        }
        /* Top-right card — timeline */
        .ghost-3 {
          width: 190px;
          top: 15%; right: 8%;
          opacity: 0.45;
          transform: rotate(2.5deg);
        }
        /* Bottom-left card — invoice line */
        .ghost-4 {
          width: 210px;
          bottom: 14%; left: 5%;
          opacity: 0.45;
          transform: rotate(-2deg);
        }

        .ghost-label {
          font-size: 0.5625rem;
          font-weight: 700;
          letter-spacing: 0.12em;
          text-transform: uppercase;
          color: rgba(255,255,255,0.25);
          margin-bottom: 0.375rem;
        }
        .ghost-amount {
          font-family: 'Playfair Display', serif;
          font-size: 1.5rem;
          font-weight: 500;
          color: rgba(255,255,255,0.35);
          margin-bottom: 0.625rem;
          line-height: 1;
        }
        .ghost-divider {
          height: 1px;
          background: rgba(255,255,255,0.06);
          margin: 0.5rem 0;
        }
        .ghost-row {
          display: flex;
          justify-content: space-between;
          font-size: 0.625rem;
          color: rgba(255,255,255,0.2);
          margin-bottom: 0.25rem;
        }
        .ghost-row span:last-child { color: rgba(255,255,255,0.3); }

        .ghost-dot {
          display: inline-block;
          width: 5px; height: 5px;
          border-radius: 9999px;
          margin-right: 0.3rem;
          vertical-align: middle;
        }
        .ghost-status {
          font-size: 0.6875rem;
          font-weight: 600;
          color: rgba(30,215,96,0.4);
          display: flex;
          align-items: center;
        }

        .ghost-timeline-step {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.2rem 0;
          font-size: 0.625rem;
        }
        .ghost-tl-dot {
          width: 6px; height: 6px;
          border-radius: 9999px;
          flex-shrink: 0;
        }
        .ghost-tl-line {
          width: 1px; height: 10px;
          background: rgba(255,255,255,0.06);
          margin-left: 2.5px;
        }

        /* Subtle radial gradients for warmth */
        .glow-top-left {
          position: absolute;
          width: 500px; height: 500px;
          border-radius: 9999px;
          background: radial-gradient(circle, rgba(30,215,96,0.05) 0%, transparent 70%);
          top: -100px; left: -100px;
          pointer-events: none; z-index: 0;
        }
        .glow-bottom-right {
          position: absolute;
          width: 500px; height: 500px;
          border-radius: 9999px;
          background: radial-gradient(circle, rgba(30,215,96,0.04) 0%, transparent 70%);
          bottom: -120px; right: -80px;
          pointer-events: none; z-index: 0;
        }

        /* ── FORM CARD ── */
        .card {
          background: white;
          border-radius: 1.25rem;
          padding: 2.25rem 2rem 2rem;
          width: 100%;
          max-width: 360px;
          box-shadow: 0 32px 80px rgba(0,0,0,0.45), 0 2px 8px rgba(0,0,0,0.2);
          position: relative;
          z-index: 1;
        }

        .input {
          width: 100%;
          padding: 0.7rem 0.875rem;
          border: 1.5px solid rgba(11,33,23,0.1);
          border-radius: 0.625rem;
          font-size: 0.875rem;
          font-family: 'Inter', sans-serif;
          color: var(--dark);
          background: #f9fafb;
          outline: none;
          transition: border-color 0.2s, box-shadow 0.2s;
          appearance: none;
          -webkit-appearance: none;
        }
        .input:focus {
          border-color: var(--green);
          box-shadow: 0 0 0 3px rgba(30,215,96,0.12);
          background: white;
        }
        .input::placeholder { color: rgba(11,33,23,0.28); }

        select.input {
          background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='14' height='14' viewBox='0 0 24 24' fill='none' stroke='%230B2117' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
          background-repeat: no-repeat;
          background-position: right 0.875rem center;
          background-color: #f9fafb;
          padding-right: 2.5rem;
          cursor: pointer;
        }
        select.input:focus { background-color: white; }

        .submit-btn {
          width: 100%;
          padding: 0.8rem;
          border-radius: 0.625rem;
          background: var(--dark);
          color: white;
          font-weight: 600;
          font-size: 0.9rem;
          font-family: 'Inter', sans-serif;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.4rem;
          transition: background 0.2s, transform 0.15s;
          margin-top: 0.25rem;
        }
        .submit-btn:hover { background: #1a3d2b; transform: translateY(-1px); }
        .submit-btn:active { transform: translateY(0); }

        /* ── RESPONSIVE — hide outer ghost cards on small screens ── */
        @media (max-width: 900px) {
          .ghost-3, .ghost-4 { display: none; }
          .ghost-1 { left: 2%; top: 8%; }
          .ghost-2 { right: 2%; bottom: 8%; }
        }
        @media (max-width: 600px) {
          .ghost-1, .ghost-2 { display: none; }
          .page { padding: 4.5rem 1.25rem 2.5rem; }
        }
      `}</style>

      <div className="page">
        <div className="noise" aria-hidden="true" />
        <div className="glow-top-left" aria-hidden="true" />
        <div className="glow-bottom-right" aria-hidden="true" />

        {/* ── BACKGROUND GHOST CARDS ── */}
        <div className="bg-scene" aria-hidden="true">

          {/* Top-left: escrow balance */}
          <div className="ghost-card ghost-1">
            <p className="ghost-label">Funds in escrow</p>
            <p className="ghost-amount">£2,400.00</p>
            <div className="ghost-status">
              <span className="ghost-dot" style={{ background: "rgba(30,215,96,0.5)" }} />
              Secured &amp; waiting
            </div>
            <div className="ghost-divider" />
            <div className="ghost-row"><span>Client</span><span>Acme Studio</span></div>
            <div className="ghost-row"><span>Project</span><span>Brand Refresh</span></div>
            <div className="ghost-row"><span>Due</span><span>14 Mar 2026</span></div>
          </div>

          {/* Bottom-right: payment notification */}
          <div className="ghost-card ghost-2">
            <p className="ghost-label">Just now</p>
            <p style={{ fontSize: "0.75rem", fontWeight: 700, color: "rgba(255,255,255,0.3)", marginBottom: "0.25rem" }}>Payment released ✓</p>
            <p style={{ fontSize: "0.6875rem", color: "rgba(255,255,255,0.2)", lineHeight: 1.5 }}>£1,200 has landed in your account.</p>
            <div className="ghost-divider" />
            <div className="ghost-row"><span>From</span><span>Acme Studio</span></div>
            <div className="ghost-row"><span>Ref</span><span>Milestone 1/2</span></div>
          </div>

          {/* Top-right: timeline */}
          <div className="ghost-card ghost-3">
            <p className="ghost-label" style={{ marginBottom: "0.625rem" }}>Status</p>
            {[
              { label: "Funds deposited", done: true },
              { label: "Work in progress", done: true },
              { label: "Awaiting approval", active: true },
              { label: "Payment released", done: false },
            ].map((s, i, arr) => (
              <div key={s.label}>
                <div className="ghost-timeline-step">
                  <div className="ghost-tl-dot" style={{
                    background: s.done ? "rgba(30,215,96,0.5)" : s.active ? "rgba(30,215,96,0.25)" : "rgba(255,255,255,0.1)"
                  }} />
                  <span style={{ color: s.done ? "rgba(255,255,255,0.3)" : s.active ? "rgba(255,255,255,0.25)" : "rgba(255,255,255,0.12)" }}>
                    {s.label}
                  </span>
                </div>
                {i < arr.length - 1 && <div className="ghost-tl-line" />}
              </div>
            ))}
          </div>

          {/* Bottom-left: invoice rows */}
          <div className="ghost-card ghost-4">
            <p className="ghost-label">Invoice #0042</p>
            <div className="ghost-divider" />
            <div className="ghost-row"><span>Design sprint</span><span>£800</span></div>
            <div className="ghost-row"><span>Revisions</span><span>£200</span></div>
            <div className="ghost-row"><span>Delivery</span><span>£400</span></div>
            <div className="ghost-divider" />
            <div className="ghost-row" style={{ fontWeight: 600 }}>
              <span style={{ color: "rgba(255,255,255,0.3)" }}>Total</span>
              <span style={{ color: "rgba(255,255,255,0.35)" }}>£1,400.00</span>
            </div>
          </div>
        </div>

        {/* ══════════════════════
            BACK TO HOME — top left
        ══════════════════════ */}
        <header style={{
          position: "fixed", top: 0, left: 0,
          zIndex: 50, padding: "1.25rem 1.75rem",
        }}>
          <Link
            href="/"
            style={{
              display: "inline-flex", alignItems: "center", gap: "0.4rem",
              color: "rgba(255,255,255,0.45)", fontSize: "0.8125rem", fontWeight: 500,
              textDecoration: "none", transition: "color 0.2s",
            }}
            onMouseEnter={e => (e.currentTarget.style.color = "rgba(255,255,255,0.85)")}
            onMouseLeave={e => (e.currentTarget.style.color = "rgba(255,255,255,0.45)")}
          >
            <ArrowLeft size={13} /> Back to home
          </Link>
        </header>

        {/* ══════════════════════
            FORM CARD
        ══════════════════════ */}
        <main>
          <div className="card">
            {!submitted ? (
              <>
                

                <h1 className="font-serif" style={{ fontSize: "1.5rem", fontWeight: 500, color: "#0B2117", lineHeight: 1.2, marginBottom: "0.375rem" }}>
                  Join the Waitlist
                </h1>
                <p style={{ color: "rgba(11,33,23,0.4)", fontSize: "0.8125rem", lineHeight: 1.55, marginBottom: "1.625rem" }}>
                  Sign up for early access and be first to know when we launch.
                </p>

                <div style={{ display: "flex", flexDirection: "column", gap: "0.875rem" }}>
                  <div>
                    <label htmlFor="email" style={{ display: "block", fontSize: "0.6875rem", fontWeight: 600, color: "rgba(11,33,23,0.45)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "0.35rem" }}>
                      Email address
                    </label>
                    <input
                      id="email"
                      className="input"
                      type="email"
                      placeholder="you@example.com"
                      autoComplete="email"
                      value={email}
                      onChange={e => { setEmail(e.target.value); setError(""); }}
                      onKeyDown={e => e.key === "Enter" && handleSubmit()}
                    />
                  </div>

                  <div>
                    <label htmlFor="profession" style={{ display: "block", fontSize: "0.6875rem", fontWeight: 600, color: "rgba(11,33,23,0.45)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "0.35rem" }}>
                      I am a…
                    </label>
                    <select
                      id="profession"
                      className="input"
                      value={profession}
                      onChange={e => { setProfession(e.target.value); setError(""); }}
                    >
                      <option value="" disabled>Select profession…</option>
                      {professions.map(p => (
                        <option key={p} value={p}>{p}</option>
                      ))}
                    </select>
                  </div>

                  {error && (
                    <p role="alert" style={{ fontSize: "0.75rem", color: "#dc2626", paddingLeft: "0.125rem", marginTop: "-0.25rem" }}>
                      {error}
                    </p>
                  )}

                  <button className="submit-btn" onClick={handleSubmit} disabled={loading}>
                    {loading ? "Submitting…" : <> Reserve my spot <ArrowRight size={14} /> </>}
                  </button>
                </div>

                <p style={{ textAlign: "center", fontSize: "0.6875rem", color: "rgba(11,33,23,0.28)", marginTop: "1rem" }}>
                  No spam. Unsubscribe anytime.
                </p>
              </>
            ) : (
              <section aria-live="polite" style={{ textAlign: "center", padding: "0.5rem 0" }}>
                <div style={{ width: 48, height: 48, borderRadius: "9999px", background: "#E6F4EA", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 1.125rem" }}>
                  <CheckCircle size={22} color="#1ED760" strokeWidth={2.5} />
                </div>
                <h2 className="font-serif" style={{ fontSize: "1.375rem", fontWeight: 500, color: "#0B2117", marginBottom: "0.4rem" }}>
                  You're on the list!
                </h2>
                <p style={{ color: "rgba(11,33,23,0.45)", fontSize: "0.8125rem", lineHeight: 1.6, marginBottom: "1.625rem" }}>
                  We'll reach out to <strong style={{ color: "#0B2117" }}>{email}</strong> when your spot is ready.
                </p>
                <Link href="/" style={{ display: "inline-flex", alignItems: "center", gap: "0.4rem", background: "#0B2117", color: "white", fontWeight: 600, fontSize: "0.875rem", padding: "0.7rem 1.5rem", borderRadius: "9999px", textDecoration: "none" }}>
                  <ArrowLeft size={13} /> Back to home
                </Link>
              </section>
            )}
          </div>
        </main>
      </div>
    </>
  );
}