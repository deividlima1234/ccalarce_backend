package com.ccalarce.siglof.dto;

import com.ccalarce.siglof.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private Boolean active;
    private String profilePictureUrl;
}
