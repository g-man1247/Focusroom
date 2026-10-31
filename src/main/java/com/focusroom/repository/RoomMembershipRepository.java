package com.focusroom.repository;

import com.focusroom.entity.RoomMembership;
import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {

    Optional<RoomMembership> findByUserAndRoom(User user, StudyRoom room);

    Optional<RoomMembership> findByUserIdAndRoomId(Long userId, Long roomId);

    List<RoomMembership> findByRoomAndActiveTrue(StudyRoom room);

    List<RoomMembership> findByUserAndActiveTrue(User user);

    long countByRoomAndActiveTrue(StudyRoom room);

    boolean existsByUserAndRoomAndActiveTrue(User user, StudyRoom room);

    /**
     * Demonstrates: DML UPDATE via JPQL with parameterized criteria
     */
    @Modifying
    @Query("UPDATE RoomMembership m SET m.active = false, m.leftAt = CURRENT_TIMESTAMP WHERE m.user.id = :userId")
    void deactivateAllMembershipsForUser(@Param("userId") Long userId);
}
