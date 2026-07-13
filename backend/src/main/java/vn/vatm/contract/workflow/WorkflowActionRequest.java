package vn.vatm.contract.workflow;

import jakarta.validation.constraints.NotNull;

/** Request body for a workflow action; {@code reason} is required for REJECT. */
public record WorkflowActionRequest(@NotNull WorkflowAction action, String reason) {}
