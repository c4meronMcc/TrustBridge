import { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    { url: "https://trustbridge.uk", lastModified: new Date(), priority: 1 },
    { url: "https://trustbridge.uk/waitlist", lastModified: new Date(), priority: 0.8 },
    { url: "https://trustbridge.uk/privacy", lastModified: new Date(), priority: 0.5 },
    { url: "https://trustbridge.uk/terms", lastModified: new Date(), priority: 0.5 },
  ];
}