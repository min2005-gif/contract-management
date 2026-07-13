package vn.vatm.contract.contract;

import java.util.List;
import org.springframework.data.domain.Page;

/** Minimal pagination envelope matching contracts/openapi.yaml ContractPage. */
public record PageResponse<T>(List<T> content, int page, int size, long total) {

  public static <E, T> PageResponse<T> of(Page<E> page, List<T> content) {
    return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
  }
}
