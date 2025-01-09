package ru.amatemeow.vks.exception;

public class AppExternalRequestError extends AppError {

  public AppExternalRequestError() {
    super();
  }

  public AppExternalRequestError(String message) {
    super(message);
  }

  public AppExternalRequestError(String message, Throwable cause) {
    super(message, cause);
  }

  public AppExternalRequestError(Throwable cause) {
    super(cause);
  }
}
