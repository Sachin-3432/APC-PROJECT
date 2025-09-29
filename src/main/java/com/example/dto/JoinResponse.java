package com.example.dto;

import lombok.Data;

@Data
public class JoinResponse {
    private GroupResponse group;
    private boolean alreadyMember;
}
