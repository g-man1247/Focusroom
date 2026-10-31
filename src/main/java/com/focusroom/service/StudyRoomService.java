package com.focusroom.service;

import com.focusroom.dto.CreateRoomDto;
import com.focusroom.entity.RoomMembership;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyRoomService {

    StudyRoom createRoom(CreateRoomDto dto, User owner);

    List<StudyRoom> getPublicActiveRooms();

    List<Object[]> getPopularRoomsWithCounts();

    List<StudyRoom> searchRooms(String query);

    StudyRoom getRoomById(Long id);

    RoomMembership joinRoom(Long roomId, User user, String accessCode);

    void leaveRoom(Long roomId, User user);

    boolean isUserInRoom(Long roomId, User user);

    List<RoomMembership> getActiveRoomMemberships(Long roomId);

    void archiveRoom(Long roomId, User requester);

    void deleteRoom(Long roomId, User requester);

    Page<StudyRoom> getAllRoomsForAdmin(Pageable pageable);

    long countActiveRooms();

    long countTotalRooms();
}
