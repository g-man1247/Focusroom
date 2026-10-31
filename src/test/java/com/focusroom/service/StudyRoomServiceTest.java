package com.focusroom.service;

import com.focusroom.dto.CreateRoomDto;
import com.focusroom.entity.RoomMembership;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.MembershipRole;
import com.focusroom.entity.enums.RoomStatus;
import com.focusroom.entity.enums.RoomVisibility;
import com.focusroom.exception.RoomFullException;
import com.focusroom.exception.UnauthorizedAccessException;
import com.focusroom.repository.RoomMembershipRepository;
import com.focusroom.repository.StudyRoomRepository;
import com.focusroom.service.impl.StudyRoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyRoomServiceTest {

    @Mock
    private StudyRoomRepository roomRepository;

    @Mock
    private RoomMembershipRepository membershipRepository;

    @InjectMocks
    private StudyRoomServiceImpl roomService;

    private User owner;
    private User student;
    private StudyRoom publicRoom;
    private StudyRoom privateRoom;

    @BeforeEach
    void setUp() {
        owner = new User("prof_smith", "smith@univ.edu", "hash", "Professor Smith");
        owner.setId(1L);

        student = new User("bob", "bob@univ.edu", "hash", "Bob Jones");
        student.setId(2L);

        publicRoom = new StudyRoom("DBMS Group", "Database study", "DBMS", owner, RoomVisibility.PUBLIC, 5);
        publicRoom.setId(10L);

        privateRoom = new StudyRoom("Private Research", "Secret research", "AI", owner, RoomVisibility.PRIVATE, 5);
        privateRoom.setId(20L);
        privateRoom.setAccessCode("pass123");
    }

    @Test
    @DisplayName("Should create room and register owner membership")
    void testCreateRoom() {
        CreateRoomDto dto = new CreateRoomDto();
        dto.setName("Algorithms");
        dto.setSubject("CS");
        dto.setVisibility(RoomVisibility.PUBLIC);
        dto.setMaxParticipants(10);

        when(roomRepository.save(any(StudyRoom.class))).thenAnswer(inv -> inv.getArgument(0));
        when(membershipRepository.save(any(RoomMembership.class))).thenAnswer(inv -> inv.getArgument(0));

        StudyRoom created = roomService.createRoom(dto, owner);

        assertNotNull(created);
        assertEquals("Algorithms", created.getName());
        verify(membershipRepository, times(1)).save(any(RoomMembership.class));
    }

    @Test
    @DisplayName("Should throw RoomFullException when joining room at maximum capacity")
    void testJoinFullRoom() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(publicRoom));
        when(membershipRepository.findByUserAndRoom(student, publicRoom)).thenReturn(Optional.empty());
        when(membershipRepository.countByRoomAndActiveTrue(publicRoom)).thenReturn(5L); // max is 5

        assertThrows(RoomFullException.class, () -> roomService.joinRoom(10L, student, null));
    }

    @Test
    @DisplayName("Should reject joining private room with invalid access code")
    void testJoinPrivateRoomInvalidCode() {
        when(roomRepository.findById(20L)).thenReturn(Optional.of(privateRoom));

        assertThrows(UnauthorizedAccessException.class, () -> roomService.joinRoom(20L, student, "wrongCode"));
    }

    @Test
    @DisplayName("Should join private room with valid access code")
    void testJoinPrivateRoomValidCode() {
        when(roomRepository.findById(20L)).thenReturn(Optional.of(privateRoom));
        when(membershipRepository.findByUserAndRoom(student, privateRoom)).thenReturn(Optional.empty());
        when(membershipRepository.countByRoomAndActiveTrue(privateRoom)).thenReturn(1L);
        when(membershipRepository.save(any(RoomMembership.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomMembership membership = roomService.joinRoom(20L, student, "pass123");

        assertNotNull(membership);
        assertTrue(membership.isActive());
        assertEquals(MembershipRole.MEMBER, membership.getRole());
    }
}
