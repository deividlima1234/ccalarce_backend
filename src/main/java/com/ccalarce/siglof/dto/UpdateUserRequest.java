package com.ccalarce.siglof.dto;

import com.ccalarce.siglof.model.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private Role role;
    private Boolean active;
    private String password; // Optional
}
