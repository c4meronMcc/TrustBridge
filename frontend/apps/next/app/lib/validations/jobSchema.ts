import { z } from "zod";

// 1. Define the schema for a single milestone
const milestoneSchema = z.object({
    title: z.string().min(3, "Title must be at least 3 characters"),
    amount: z.number().min(50, "Minimum milestone amount is £50"),
    sequenceOrder: z.number(),
});

// 2. Define the schema for the entire job
export const jobCreationSchema = z.object({
    clientEmail: z.string().email("Please enter a valid client email"),
    jobTitle: z.string().min(5, "Job title is required"),
    description: z.string().min(10, "Please provide a brief description of the work"),
    // An array of milestones that must contain at least one item
    milestones: z.array(milestoneSchema).min(1, "You must add at least one milestone"),
});

export type JobCreationFormValues = z.infer<typeof jobCreationSchema>;