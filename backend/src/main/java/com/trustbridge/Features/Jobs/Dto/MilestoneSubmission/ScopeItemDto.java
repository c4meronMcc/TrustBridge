package com.trustbridge.Features.Jobs.Dto.MilestoneSubmission;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ScopeItemDto(
        String id,

        @JsonAlias({"description", "text"})
        String description,

        @JsonAlias({"isCompleted", "checked"})
        boolean isCompleted

) { }
