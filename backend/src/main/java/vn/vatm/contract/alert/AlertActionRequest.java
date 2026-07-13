package vn.vatm.contract.alert;

import jakarta.validation.constraints.NotNull;

/** Request body to acknowledge or resolve an alert. */
public record AlertActionRequest(@NotNull AlertStatus status) {}
