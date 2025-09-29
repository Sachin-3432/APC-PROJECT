package com.example.controller;

import java.util.List;

import com.example.dto.GroupRequest;
import com.example.dto.GroupResponse;
import com.example.service.GroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    @GetMapping("/{id}/balances")
    public ResponseEntity<?> getGroupBalances(@PathVariable String id) {
        try {
            return ResponseEntity.ok(groupService.getGroupBalances(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<GroupResponse> groups = groupService.getUserGroups(email);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            Authentication authentication,
            @RequestBody GroupRequest request) {
        try {
            String email = authentication.getName();
            GroupResponse response = groupService.createGroup(email, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<?> joinGroup(
            Authentication authentication,
            @PathVariable String code) {
        try {
            String email = authentication.getName();
            com.example.dto.JoinResponse response = groupService.joinGroup(email, code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.example.dto.ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable String id) {
        try {
            GroupResponse response = groupService.getGroupById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable String id,
            @RequestBody GroupRequest request) {
        try {
            GroupResponse response = groupService.updateGroup(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
