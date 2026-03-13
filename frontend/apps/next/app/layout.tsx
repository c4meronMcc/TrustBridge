import { StylesProvider } from './styles-provider'
import './globals.css'
import type { Metadata } from 'next'
import { Analytics } from "@vercel/analytics/next"
import { SpeedInsights } from "@vercel/speed-insights/next"

export const metadata: Metadata = {
  // ── TITLES ──
  title: {
    template: "%s — TrustBridge",
    default: "TrustBridge — Escrow Payments for Freelancers",
  },

  // ── DESCRIPTION ──
  description: "TrustBridge secures client funds before you start work and releases payment the moment you finish. Escrow payments built for freelancers, agencies and contractors in the UK.",

  // ── CANONICAL URL ──
  metadataBase: new URL("https://trustbridge.co.uk"),
  alternates: {
    canonical: "/",
  },

  // ── KEYWORDS ──
  keywords: [
    "escrow payments",
    "freelancer payment protection",
    "secure payments for freelancers",
    "get paid on time",
    "freelance invoice protection",
    "UK escrow service",
    "payment guarantee freelancers",
    "client payment security",
    "milestone payments",
    "TrustBridge",
  ],

  // ── OPEN GRAPH (Facebook, LinkedIn previews) ──
  openGraph: {
    type: "website",
    url: "https://trustbridge.co.uk",
    title: "TrustBridge — Escrow Payments for Freelancers",
    description: "Funds secured before you start. Payment released the moment you finish. Never wait for an invoice again.",
    siteName: "TrustBridge",
    images: [
      {
        url: "/og-image.png", // create a 1200x630px image and put it in /public
        width: 1200,
        height: 630,
        alt: "TrustBridge — Escrow Payments for Freelancers",
      },
    ],
  },

  // ── TWITTER / X CARD ──
  twitter: {
    card: "summary_large_image",
    site: "@TrusttBridge",
    creator: "@TrusttBridge",
    title: "TrustBridge — Escrow Payments for Freelancers",
    description: "Funds secured before you start. Payment released the moment you finish.",
    images: ["/og-image.png"],
  },

  // ── FAVICON / ICONS ──
  icons: {
    icon: "/favicon.png",
    apple: "/apple-icon.png",
  },

  // ── ROBOTS ──
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },

  // ── VERIFICATION (add these when you set up Google Search Console) ──
  // verification: {
  //   google: "your-google-verification-code",
  // },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>
        <StylesProvider>{children}</StylesProvider>
        <Analytics />
      </body>
    </html>
  )
}