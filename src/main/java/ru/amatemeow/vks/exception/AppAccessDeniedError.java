package ru.amatemeow.vks.exception;

public class AppAccessDeniedError extends AppError {

  public AppAccessDeniedError() {
    super();
  }

  public AppAccessDeniedError(String message) {
    super(message);
  }

  public AppAccessDeniedError(String message, Throwable cause) {
    super(message, cause);
  }

  public AppAccessDeniedError(Throwable cause) {
    super(cause);
  }
}
