package com.example.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class GroupTest {
    @Test
    void testGroupGettersSettersAndEquals() {
        Group group1 = new Group();
        group1.setId("g1");
        group1.setName("Test Group");
        group1.setCode("CODE1234");
        group1.setCreatedBy("u1");
        group1.setMemberIds(Arrays.asList("u1", "u2"));

        assertThat(group1.getId()).isEqualTo("g1");
        assertThat(group1.getName()).isEqualTo("Test Group");
        assertThat(group1.getCode()).isEqualTo("CODE1234");
        assertThat(group1.getCreatedBy()).isEqualTo("u1");
        assertThat(group1.getMemberIds()).containsExactly("u1", "u2");

        Group group2 = new Group();
        group2.setId("g1");
        group2.setName("Test Group");
        group2.setCode("CODE1234");
        group2.setCreatedBy("u1");
        group2.setMemberIds(Arrays.asList("u1", "u2"));

        assertThat(group1).isEqualTo(group2);
        assertThat(group1.hashCode()).isEqualTo(group2.hashCode());
    }
}
