"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowRight, ArrowLeft, CheckCircle } from "lucide-react";

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

  const handleSubmit = () => {
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("Please enter a valid email address.");
      return;
    }
    if (!profession) {
      setError("Please select your profession.");
      return;
    }
    setError("");
    // TODO: wire up to your backend / email provider here
    setSubmitted(true);
  };

  return (
    <div style={{
      minHeight: "100vh",
      background: "#1ED760",
      fontFamily: "Inter, sans-serif",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      padding: "2rem 1.5rem",
      position: "relative",
      overflow: "hidden",
    }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap');
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        .font-serif { font-family: 'Playfair Display', serif; }

        /* Subtle background circles for depth */
        .bg-circle-1 {
          position: absolute; border-radius: 9999px;
          background: rgba(255,255,255,0.15);
          width: 500px; height: 500px;
          top: -180px; right: -120px;
          pointer-events: none;
        }
        .bg-circle-2 {
          position: absolute; border-radius: 9999px;
          background: rgba(11,33,23,0.07);
          width: 400px; height: 400px;
          bottom: -150px; left: -100px;
          pointer-events: none;
        }

        .card {
          background: white;
          border-radius: 2rem;
          padding: clamp(2rem, 5vw, 3.5rem);
          width: 100%;
          max-width: 480px;
          box-shadow: 0 24px 80px rgba(11,33,23,0.15), 0 4px 16px rgba(11,33,23,0.08);
          position: relative;
          z-index: 1;
        }

        .input {
          width: 100%;
          padding: 0.875rem 1.125rem;
          border: 1.5px solid rgba(0,0,0,0.1);
          border-radius: 0.875rem;
          font-size: 0.9375rem;
          font-family: inherit;
          color: #0B2117;
          background: #fafafa;
          outline: none;
          transition: border-color 0.2s, box-shadow 0.2s;
          appearance: none;
          -webkit-appearance: none;
        }
        .input:focus {
          border-color: #1ED760;
          box-shadow: 0 0 0 3px rgba(30,215,96,0.15);
          background: white;
        }
        .input::placeholder { color: rgba(11,33,23,0.35); }

        select.input {
          background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%230B2117' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
          background-repeat: no-repeat;
          background-position: right 1rem center;
          padding-right: 2.75rem;
          cursor: pointer;
        }
        select.input:invalid { color: rgba(11,33,23,0.35); }

        .submit-btn {
          width: 100%;
          padding: 1rem;
          border-radius: 0.875rem;
          background: #0B2117;
          color: white;
          font-weight: 700;
          font-size: 1rem;
          font-family: inherit;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          transition: background 0.2s, transform 0.15s;
        }
        .submit-btn:hover { background: #1a3d2b; transform: translateY(-1px); }
        .submit-btn:active { transform: translateY(0); }

        .error-msg {
          font-size: 0.8125rem;
          color: #dc2626;
          margin-top: 0.4rem;
          padding-left: 0.25rem;
        }

        .back-link {
          display: inline-flex;
          align-items: center;
          gap: 0.4rem;
          color: #0B2117;
          font-size: 0.875rem;
          font-weight: 600;
          text-decoration: none;
          opacity: 0.7;
          transition: opacity 0.2s;
          margin-bottom: 2rem;
          position: relative;
          z-index: 1;
        }
        .back-link:hover { opacity: 1; }

        .badge {
          display: inline-flex;
          align-items: center;
          gap: 0.4rem;
          background: rgba(11,33,23,0.08);
          color: #0B2117;
          font-size: 0.75rem;
          font-weight: 600;
          letter-spacing: 0.06em;
          padding: 0.35rem 0.875rem;
          border-radius: 9999px;
          margin-bottom: 1.25rem;
        }
      `}</style>

      {/* Background decoration */}
      <div className="bg-circle-1" />
      <div className="bg-circle-2" />

      {/* Back to home */}
      <Link href="/" className="back-link">
        <ArrowLeft size={15} /> Back to home
      </Link>

      <div className="card">
        {!submitted ? (
          <>
            {/* Header */}
            <div className="badge">
              <span style={{ width: 6, height: 6, borderRadius: "9999px", background: "#1ED760", display: "inline-block" }} />
              Early Access
            </div>

            <h1 className="font-serif" style={{ fontSize: "clamp(1.6rem, 4vw, 2rem)", fontWeight: 500, color: "#0B2117", lineHeight: 1.2, marginBottom: "0.625rem" }}>
              Join the Waitlist
            </h1>
            <p style={{ color: "rgba(11,33,23,0.55)", fontSize: "0.9375rem", lineHeight: 1.6, marginBottom: "2rem" }}>
              Secure your spot and get your first <strong style={{ color: "#0B2117" }}>£5,000 in transactions fee-free</strong> when we launch.
            </p>

            {/* Form */}
            <div style={{ display: "flex", flexDirection: "column", gap: "1.125rem" }}>

              {/* Email */}
              <div>
                <label style={{ display: "block", fontSize: "0.8125rem", fontWeight: 600, color: "#0B2117", marginBottom: "0.4rem" }}>
                  Email address
                </label>
                <input
                  className="input"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={e => { setEmail(e.target.value); setError(""); }}
                  onKeyDown={e => e.key === "Enter" && handleSubmit()}
                />
              </div>

              {/* Profession */}
              <div>
                <label style={{ display: "block", fontSize: "0.8125rem", fontWeight: 600, color: "#0B2117", marginBottom: "0.4rem" }}>
                  What best describes you?
                </label>
                <select
                  className="input"
                  value={profession}
                  onChange={e => { setProfession(e.target.value); setError(""); }}
                  required
                >
                  <option value="" disabled>Select your profession…</option>
                  {professions.map(p => (
                    <option key={p} value={p}>{p}</option>
                  ))}
                </select>
              </div>

              {/* Error */}
              {error && <p className="error-msg">{error}</p>}

              {/* Submit */}
              <button className="submit-btn" onClick={handleSubmit} style={{ marginTop: "0.5rem" }}>
                Reserve my spot <ArrowRight size={16} />
              </button>
            </div>

            {/* Footer note */}
            <p style={{ textAlign: "center", fontSize: "0.75rem", color: "rgba(11,33,23,0.4)", marginTop: "1.25rem" }}>
              No spam, ever. Unsubscribe at any time.
            </p>
          </>
        ) : (
          /* ── SUCCESS STATE ── */
          <div style={{ textAlign: "center", padding: "1rem 0" }}>
            <div style={{ width: 64, height: 64, borderRadius: "9999px", background: "#E6F4EA", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 1.5rem" }}>
              <CheckCircle size={32} color="#1ED760" strokeWidth={2.5} />
            </div>
            <h2 className="font-serif" style={{ fontSize: "1.75rem", fontWeight: 500, color: "#0B2117", marginBottom: "0.75rem" }}>
              You're on the list!
            </h2>
            <p style={{ color: "rgba(11,33,23,0.55)", fontSize: "0.9375rem", lineHeight: 1.6, marginBottom: "2rem" }}>
              We'll be in touch at <strong style={{ color: "#0B2117" }}>{email}</strong> when it's your turn. Your first £5,000 is on us.
            </p>
            <Link href="/" style={{ display: "inline-flex", alignItems: "center", gap: "0.4rem", background: "#0B2117", color: "white", fontWeight: 600, fontSize: "0.9375rem", padding: "0.75rem 1.75rem", borderRadius: "9999px", textDecoration: "none" }}>
              <ArrowLeft size={15} /> Back to home
            </Link>
          </div>
        )}
      </div>

      {/* Brand wordmark below card */}
      <div style={{ marginTop: "2rem", position: "relative", zIndex: 1 }}>
        <img src="/TrustBridgeNameBlack.png" alt="TrustBridge" style={{ height: "1.125rem", opacity: 0.4 }} />
      </div>
    </div>
  );
}