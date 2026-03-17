package com.soen345.meow.dto;

public class SignupRequest {
    private String email;
    private String phone;
    private String password;

    public SignupRequest() {}

    public SignupRequest(String email, String phone, String password) {
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
