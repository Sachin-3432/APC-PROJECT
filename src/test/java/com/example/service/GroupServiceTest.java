package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.example.dto.GroupRequest;
import com.example.model.Group;
import com.example.model.User;
import com.example.repository.GroupRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class GroupServiceTest {
    private GroupRepository groupRepository;
    private UserService userService;
    private GroupService groupService;

    @BeforeEach
    void setup() {
        groupRepository = Mockito.mock(GroupRepository.class);
        userService = Mockito.mock(UserService.class);
        groupService = new GroupService();
        org.springframework.test.util.ReflectionTestUtils.setField(groupService, "groupRepository", groupRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(groupService, "userService", userService);
    }

    @Test
    void testCreateGroupPersistsAndReturnsResponse() {
        GroupRequest req = new GroupRequest();
        req.setName("Test Group");
        User user = new User();
        user.setId("u1");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        Group savedGroup = new Group();
        savedGroup.setId("g1");
        savedGroup.setName("Test Group");
        savedGroup.setCreatedBy("u1");
        savedGroup.setMemberIds(Collections.singletonList("u1"));
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        var resp = groupService.createGroup("test@example.com", req);
        assertThat(resp.getName()).isEqualTo("Test Group");
        assertThat(resp.getCreatedBy()).isEqualTo("u1");
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(captor.capture());
        Group groupSaved = captor.getValue();
        assertThat(groupSaved.getName()).isEqualTo("Test Group");
        assertThat(groupSaved.getMemberIds()).contains("u1");
    }
}
