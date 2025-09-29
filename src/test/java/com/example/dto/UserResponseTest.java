package com.example.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserResponseTest {
    @Test
    void testUserResponseGettersSettersAndEquals() {
        UserResponse user1 = new UserResponse();
        user1.setId("u1");
        user1.setFullName("Test User");
        user1.setEmail("test@example.com");
        user1.setProfileImage("img.png");
        user1.setRoles(Arrays.asList("USER", "ADMIN"));
        user1.setActive(true);

        assertThat(user1.getId()).isEqualTo("u1");
        assertThat(user1.getFullName()).isEqualTo("Test User");
        assertThat(user1.getEmail()).isEqualTo("test@example.com");
        assertThat(user1.getProfileImage()).isEqualTo("img.png");
        assertThat(user1.getRoles()).containsExactly("USER", "ADMIN");
        assertThat(user1.isActive()).isTrue();

        UserResponse user2 = new UserResponse();
        user2.setId("u1");
        user2.setFullName("Test User");
        user2.setEmail("test@example.com");
        user2.setProfileImage("img.png");
        user2.setRoles(Arrays.asList("USER", "ADMIN"));
        user2.setActive(true);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
}
