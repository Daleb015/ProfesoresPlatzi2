package com.platzi.profesoresplatzi.util;

public class CustomErrorType {

  public CustomErrorType(String errorMessage) {
    super();
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  private String errorMessage;
}
