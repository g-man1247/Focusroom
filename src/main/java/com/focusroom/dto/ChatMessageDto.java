package com.focusroom.dto;

import com.focusroom.entity.enums.MessageType;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long id;
    private Long roomId;
    private String senderUsername;
    private String senderFullName;
    private String message;
    private MessageType messageType;
    private LocalDateTime sentAt;

    public ChatMessageDto() {
    }

    public ChatMessageDto(Long id, Long roomId, String senderUsername, String senderFullName, String message, MessageType messageType, LocalDateTime sentAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderUsername = senderUsername;
        this.senderFullName = senderFullName;
        this.message = message;
        this.messageType = messageType;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderFullName() {
        return senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
