import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Privacy Policy",
  description: "Learn how TrustBridge collects, uses, and protects your personal data. We are fully compliant with UK GDPR and committed to keeping your information secure.",

  alternates: {
    canonical: "/privacy",
  },

  openGraph: {
    title: "Privacy Policy — TrustBridge",
    description: "How TrustBridge handles your personal data. UK GDPR compliant. We never sell your data.",
    url: "https://trustbridge.co.uk/privacy",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "TrustBridge Privacy Policy",
      },
    ],
  },

  twitter: {
    card: "summary_large_image",
    title: "Privacy Policy — TrustBridge",
    description: "How TrustBridge handles your personal data. UK GDPR compliant.",
    images: ["/og-image.png"],
  },

  robots: {
    index: true,
    follow: true,
  },
};

export default function PrivacyLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}