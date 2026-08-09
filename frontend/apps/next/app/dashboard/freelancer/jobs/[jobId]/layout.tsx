import {JobWorkspaceProvider} from "../../../../components/context/JobWorkSpaceContext";

export default function JobLayout({ children }: { children: React.ReactNode }) {
    return (
        // The Job Focus page inside this layout will set the data,
        // and the Request Release page will consume it.
        <JobWorkspaceProvider>
            {children}
        </JobWorkspaceProvider>
    );
}