package com.gestaofinanceira.modules.auth;

public class RegisterResponse {

    private String message;

    public RegisterResponse() {}

    public RegisterResponse(String message) {
        this.message = message;
    }

    public static RegisterResponse of(String message) {
        return new RegisterResponse(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}