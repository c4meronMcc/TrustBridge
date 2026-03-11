import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Terms of Service",
  description: "Read the TrustBridge Terms of Service. Understand how our milestone-based escrow platform works, how funds are protected, and how disputes are resolved.",

  alternates: {
    canonical: "/terms",
  },

  openGraph: {
    title: "Terms of Service — TrustBridge",
    description: "How TrustBridge's escrow platform works, how your funds are protected, and our dispute resolution process.",
    url: "https://trustbridge.co.uk/terms",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "TrustBridge Terms of Service",
      },
    ],
  },

  twitter: {
    card: "summary_large_image",
    title: "Terms of Service — TrustBridge",
    description: "How TrustBridge's escrow platform works and how your funds are protected.",
    images: ["/og-image.png"],
  },

  robots: {
    index: true,
    follow: true,
  },
};

export default function TermsLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}