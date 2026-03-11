import { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    { url: "https://trustbridge.co.uk", lastModified: new Date(), priority: 1 },
    { url: "https://trustbridge.co.uk/waitlist", lastModified: new Date(), priority: 0.8 },
    { url: "https://trustbridge.co.uk/privacy", lastModified: new Date(), priority: 0.5 },
  ];
}