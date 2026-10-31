package com.focusroom.repository;

import com.focusroom.entity.StudyRoom;
import com.focusroom.entity.enums.RoomStatus;
import com.focusroom.entity.enums.RoomVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    List<StudyRoom> findByStatusAndVisibilityOrderByCreatedAtDesc(RoomStatus status, RoomVisibility visibility);

    List<StudyRoom> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    long countByStatus(RoomStatus status);

    /**
     * Demonstrates: INNER JOIN, WHERE, ORDER BY
     */
    @Query("SELECT r FROM StudyRoom r WHERE r.status = :status AND r.visibility = :visibility AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.subject) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<StudyRoom> searchRooms(@Param("query") String query, 
                                @Param("status") RoomStatus status, 
                                @Param("visibility") RoomVisibility visibility);

    /**
     * Demonstrates: LEFT JOIN, GROUP BY, Aggregate COUNT, ORDER BY
     * Returns rooms with their active participant count
     */
    @Query("SELECT r, COUNT(m) AS activeCount " +
           "FROM StudyRoom r " +
           "LEFT JOIN r.memberships m ON m.active = true " +
           "WHERE r.status = 'ACTIVE' AND r.visibility = 'PUBLIC' " +
           "GROUP BY r " +
           "ORDER BY COUNT(m) DESC, r.createdAt DESC")
    List<Object[]> findPopularActiveRooms();

    Page<StudyRoom> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
