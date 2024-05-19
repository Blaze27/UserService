package com.example.userservice.securities.models;

import com.example.userservice.models.Role;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.core.GrantedAuthority;

@JsonDeserialize
public class CustomGrantedAuthority implements GrantedAuthority {
    private String authority;
    public CustomGrantedAuthority(Role role) {
        this.authority = role.getValue();
    }

    public CustomGrantedAuthority() {}

    @Override
    public String getAuthority() {
        return authority;
    }
}
