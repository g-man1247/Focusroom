package com.focusroom.service.impl;

import com.focusroom.dto.CreateRoomDto;
import com.focusroom.entity.RoomMembership;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import com.focusroom.entity.enums.MembershipRole;
import com.focusroom.entity.enums.Role;
import com.focusroom.entity.enums.RoomStatus;
import com.focusroom.entity.enums.RoomVisibility;
import com.focusroom.exception.InvalidOperationException;
import com.focusroom.exception.ResourceNotFoundException;
import com.focusroom.exception.RoomFullException;
import com.focusroom.exception.UnauthorizedAccessException;
import com.focusroom.repository.RoomMembershipRepository;
import com.focusroom.repository.StudyRoomRepository;
import com.focusroom.service.StudyRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudyRoomServiceImpl implements StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final RoomMembershipRepository membershipRepository;

    public StudyRoomServiceImpl(StudyRoomRepository studyRoomRepository, RoomMembershipRepository membershipRepository) {
        this.studyRoomRepository = studyRoomRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public StudyRoom createRoom(CreateRoomDto dto, User owner) {
        StudyRoom room = new StudyRoom();
        room.setName(dto.getName().trim());
        room.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        room.setSubject(dto.getSubject().trim());
        room.setOwner(owner);
        room.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : RoomVisibility.PUBLIC);
        
        if (room.getVisibility() == RoomVisibility.PRIVATE) {
            if (dto.getAccessCode() == null || dto.getAccessCode().trim().isEmpty()) {
                throw new InvalidOperationException("Private study rooms require an access code");
            }
            room.setAccessCode(dto.getAccessCode().trim());
        }

        room.setMaxParticipants(dto.getMaxParticipants() != null ? dto.getMaxParticipants() : 25);
        room.setStatus(RoomStatus.ACTIVE);

        StudyRoom savedRoom = studyRoomRepository.save(room);

        // Automatically assign owner as active room member
        RoomMembership ownerMembership = new RoomMembership(owner, savedRoom, MembershipRole.OWNER);
        membershipRepository.save(ownerMembership);

        return savedRoom;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> getPublicActiveRooms() {
        return studyRoomRepository.findByStatusAndVisibilityOrderByCreatedAtDesc(RoomStatus.ACTIVE, RoomVisibility.PUBLIC);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPopularRoomsWithCounts() {
        return studyRoomRepository.findPopularActiveRooms();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> searchRooms(String query) {
        if (query == null || query.isBlank()) {
            return getPublicActiveRooms();
        }
        return studyRoomRepository.searchRooms(query.trim(), RoomStatus.ACTIVE, RoomVisibility.PUBLIC);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyRoom getRoomById(Long id) {
        return studyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study room not found with id: " + id));
    }

    @Override
    @Transactional
    public RoomMembership joinRoom(Long roomId, User user, String accessCode) {
        StudyRoom room = getRoomById(roomId);

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot join an inactive or archived study room");
        }

        // Check private room access code
        if (room.isPrivate() && !room.getOwner().equals(user)) {
            if (accessCode == null || !accessCode.trim().equals(room.getAccessCode())) {
                throw new UnauthorizedAccessException("Invalid access code for private study room");
            }
        }

        // Check capacity
        long currentCount = membershipRepository.countByRoomAndActiveTrue(room);
        Optional<RoomMembership> existingMembership = membershipRepository.findByUserAndRoom(user, room);

        if (existingMembership.isPresent() && existingMembership.get().isActive()) {
            return existingMembership.get();
        }

        if (currentCount >= room.getMaxParticipants()) {
            throw new RoomFullException("Study room '" + room.getName() + "' is at full capacity (" + room.getMaxParticipants() + " members)");
        }

        if (existingMembership.isPresent()) {
            RoomMembership membership = existingMembership.get();
            membership.setActive(true);
            return membershipRepository.save(membership);
        } else {
            MembershipRole role = room.getOwner().equals(user) ? MembershipRole.OWNER : MembershipRole.MEMBER;
            RoomMembership newMembership = new RoomMembership(user, room, role);
            return membershipRepository.save(newMembership);
        }
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, User user) {
        StudyRoom room = getRoomById(roomId);
        Optional<RoomMembership> membership = membershipRepository.findByUserAndRoom(user, room);
        membership.ifPresent(m -> {
            m.setActive(false);
            membershipRepository.save(m);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInRoom(Long roomId, User user) {
        StudyRoom room = getRoomById(roomId);
        return membershipRepository.existsByUserAndRoomAndActiveTrue(user, room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomMembership> getActiveRoomMemberships(Long roomId) {
        StudyRoom room = getRoomById(roomId);
        return membershipRepository.findByRoomAndActiveTrue(room);
    }

    @Override
    @Transactional
    public void archiveRoom(Long roomId, User requester) {
        StudyRoom room = getRoomById(roomId);
        if (!room.getOwner().equals(requester) && requester.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("Only the room owner or an administrator can archive this room");
        }
        room.setStatus(RoomStatus.ARCHIVED);
        studyRoomRepository.save(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId, User requester) {
        StudyRoom room = getRoomById(roomId);
        if (!room.getOwner().equals(requester) && requester.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("Only the room owner or an administrator can delete this room");
        }
        studyRoomRepository.delete(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudyRoom> getAllRoomsForAdmin(Pageable pageable) {
        return studyRoomRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveRooms() {
        return studyRoomRepository.countByStatus(RoomStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalRooms() {
        return studyRoomRepository.count();
    }
}
