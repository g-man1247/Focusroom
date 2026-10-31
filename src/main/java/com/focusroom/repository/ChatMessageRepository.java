package com.focusroom.repository;

import com.focusroom.entity.ChatMessage;
import com.focusroom.entity.StudyRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomOrderBySentAtAsc(StudyRoom room);

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.sender WHERE m.room.id = :roomId ORDER BY m.sentAt ASC")
    List<ChatMessage> findRecentMessagesByRoomId(@Param("roomId") Long roomId, Pageable pageable);
}
