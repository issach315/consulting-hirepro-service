package com.hirepro.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class UpdateAuthUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    private String password;

    @Pattern(regexp = "SUPERADMIN|CLIENT_ADMIN|BDM|SALES_EXECUTIVE|HR_MANAGER|ACCOUNT_MANAGER|PAYROLL_SPECIALIST|RECRUITER|SR_RECRUITER|USIT_RECRUITER|USIT_COORDINATOR|TEAM_LEAD|EMPLOYEE_MANAGER|HR|EMPLOYEE|IT_SUPPORT|TRAINER|ONBOARDING_SPECIALIST",
            message = "Invalid role")
    private String role;

    @Pattern(regexp = "DOMESTIC|USIT", message = "Employee type must be DOMESTIC or USIT")
    private String employeeType;

    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private String status;

    private String clientId;

    // Constructors
    public UpdateAuthUserRequest() {
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}