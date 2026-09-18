package com.karthik.incidentmanagement.dto;

import com.karthik.incidentmanagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Minimal, safe-to-expose view of a User — no password, ever. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDto {

    private Long id;
    private String name;
    private String email;
    private Role role;
}
