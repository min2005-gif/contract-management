package vn.vatm.contract.config;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import vn.vatm.contract.config.ApiExceptions.BadRequestException;
import vn.vatm.contract.config.ApiExceptions.ConflictException;
import vn.vatm.contract.config.ApiExceptions.ForbiddenException;
import vn.vatm.contract.config.ApiExceptions.NotFoundException;

/**
 * Translates exceptions into RFC 7807 {@code application/problem+json} responses with a stable
 * machine {@code code} and a human-readable, Vietnamese-first {@code detail} (FR-019/UX principle).
 */
@RestControllerAdvice
public class ProblemHandler {

  private ProblemDetail problem(HttpStatus status, String code, String detail) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setProperty("code", code);
    return pd;
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail notFound(NotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
  public ProblemDetail forbidden(Exception ex) {
    return problem(
        HttpStatus.FORBIDDEN,
        "FORBIDDEN",
        "Bạn không có quyền truy cập tài nguyên này. / You are not allowed to access this resource.");
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail conflict(ConflictException ex) {
    return problem(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ProblemDetail optimisticLock(OptimisticLockingFailureException ex) {
    return problem(
        HttpStatus.CONFLICT,
        "CONCURRENT_MODIFICATION",
        "Hợp đồng đã được người khác cập nhật. Vui lòng tải lại và thử lại. /"
            + " The contract was modified by someone else; reload and try again.");
  }

  @ExceptionHandler(BadRequestException.class)
  public ProblemDetail badRequest(BadRequestException ex) {
    return problem(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validation(MethodArgumentNotValidException ex) {
    var field = ex.getBindingResult().getFieldError();
    String detail =
        field != null
            ? "Trường '" + field.getField() + "' không hợp lệ: " + field.getDefaultMessage()
            : "Dữ liệu không hợp lệ. / Invalid data.";
    ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detail);
    pd.setProperty(
        "errors",
        ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .toList());
    return pd;
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ProblemDetail uploadTooLarge(MaxUploadSizeExceededException ex) {
    return problem(
        HttpStatus.PAYLOAD_TOO_LARGE,
        "ATTACHMENT_TOO_LARGE",
        "Tệp đính kèm vượt quá kích thước cho phép. / Attachment exceeds the allowed size.");
  }
}
