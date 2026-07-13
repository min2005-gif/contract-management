package vn.vatm.contract.config;

/** Application exceptions mapped to HTTP problem responses by {@link ProblemHandler}. */
public final class ApiExceptions {

  private ApiExceptions() {}

  /** 404 - resource not found. */
  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
      super(message);
    }
  }

  /** 403 - caller lacks permission or is out of unit scope. */
  public static class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
      super(message);
    }
  }

  /** 409 - conflicting state (e.g., duplicate contract number, concurrent edit). */
  public static class ConflictException extends RuntimeException {
    public ConflictException(String message) {
      super(message);
    }
  }

  /** 400 - invalid request the bean-validation layer cannot express. */
  public static class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
      super(message);
    }
  }

  /** 502 - an internal integration (signature, e-document, accounting) is unavailable. */
  public static class IntegrationUnavailableException extends RuntimeException {
    public IntegrationUnavailableException(String message) {
      super(message);
    }

    public IntegrationUnavailableException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
