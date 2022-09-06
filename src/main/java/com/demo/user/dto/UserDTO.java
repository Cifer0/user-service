package com.demo.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO implements Serializable {
    private final String username;

    private String fullName;

    private String firstName;

    private String lastName;

    public UserDTO(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    public UserDTO(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @JsonCreator
    public UserDTO(String username, String fullName, String firstName, String lastName) {
        this.username = username;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }
}
