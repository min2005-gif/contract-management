package vn.vatm.contract.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** Request body for creating/updating a contract (validates FR-011 required fields). */
public record ContractInput(
    @NotBlank String contractNumber,
    @NotBlank String name,
    @NotNull ContractType type,
    @NotBlank String partyA,
    @NotBlank String partyB,
    @NotNull @PositiveOrZero BigDecimal value,
    @NotNull LocalDate signDate,
    @NotNull LocalDate termEnd,
    @NotNull UUID personInChargeId,
    Boolean official,
    Map<String, Object> extraFields) {}
