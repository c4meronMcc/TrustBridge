"use client";

import React, { createContext, useContext, useState, ReactNode } from "react";

// You can expand this interface based on your actual job data structure
interface JobWorkspaceState {
    jobData: any | null;
    setJobData: (data: any) => void;
}

const JobWorkspaceContext = createContext<JobWorkspaceState | undefined>(undefined);

export function JobWorkspaceProvider({ children, initialData = null }: { children: ReactNode, initialData?: any }) {
    const [jobData, setJobData] = useState(initialData);

    return (
        <JobWorkspaceContext.Provider value={{ jobData, setJobData }}>
            {children}
        </JobWorkspaceContext.Provider>
    );
}

export const useJobWorkspace = () => {
    const context = useContext(JobWorkspaceContext);
    if (context === undefined) {
        throw new Error("useJobWorkspace must be used within a JobWorkspaceProvider");
    }
    return context;
};