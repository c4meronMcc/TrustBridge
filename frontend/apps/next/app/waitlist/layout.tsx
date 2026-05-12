import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Join the Waitlist",
  description: "Sign up for early access to TrustBridge — the escrow payment platform built for freelancers, agencies and contractors. Be first to know when we launch.",

  alternates: {
    canonical: "/waitlist",
  },

  openGraph: {
    title: "Join the TrustBridge Waitlist",
    description: "Secure your spot. TrustBridge guarantees your payments before you start work — built for freelancers and agencies in the UK.",
    url: "https://trustbridge.co.uk/waitlist",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "TrustBridge — Join the Waitlist",
      },
    ],
  },

  twitter: {
    card: "summary_large_image",
    title: "Join the TrustBridge Waitlist",
    description: "Secure your spot. Escrow payments built for freelancers — funds secured before you start, released when you finish.",
    images: ["/og-image.png"],
  },

  robots: {
    index: true,
    follow: true,
  },
};

export default function WaitlistLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}