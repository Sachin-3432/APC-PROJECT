package com.example.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    
    @GetMapping({ "/admin", "/admin/" })
    public String adminPage(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login.html";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin) {
            return "redirect:/login.html";
        }

        // Forward to the static admin.html resource
        return "forward:/admin.html";
    }
}
