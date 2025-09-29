package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class GroupResponse {
    private String id;
    private String name;
    private String code;
    private String createdBy;
    private List<String> memberNames;
    private List<String> memberIds;
}
