package ru.amatemeow.vks.exception;

public class AppError extends RuntimeException {

  public AppError() {
    super();
  }

  public AppError(String message) {
    super(message);
  }

  public AppError(String message, Throwable cause) {
    super(message, cause);
  }

  public AppError(Throwable cause) {
    super(cause);
  }
}
