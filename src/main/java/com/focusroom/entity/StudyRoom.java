package com.focusroom.entity;

import com.focusroom.entity.enums.RoomStatus;
import com.focusroom.entity.enums.RoomVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
    name = "study_rooms",
    indexes = {
        @Index(name = "idx_room_subject", columnList = "subject"),
        @Index(name = "idx_room_visibility", columnList = "visibility"),
        @Index(name = "idx_room_status", columnList = "status"),
        @Index(name = "idx_room_owner", columnList = "owner_id")
    }
)
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room name cannot be blank")
    @Size(min = 3, max = 100, message = "Room name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 50, message = "Subject cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_owner"))
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomVisibility visibility = RoomVisibility.PUBLIC;

    @Column(name = "access_code", length = 50)
    private String accessCode;

    @Min(value = 2, message = "Room must allow at least 2 participants")
    @Max(value = 100, message = "Room participant limit cannot exceed 100")
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants = 25;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "room")
    private List<StudySession> studySessions = new ArrayList<>();

    public StudyRoom() {
    }

    public StudyRoom(String name, String description, String subject, User owner, RoomVisibility visibility, Integer maxParticipants) {
        this.name = name;
        this.description = description;
        this.subject = subject;
        this.owner = owner;
        this.visibility = visibility;
        this.maxParticipants = maxParticipants != null ? maxParticipants : 25;
        this.status = RoomStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public RoomVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(RoomVisibility visibility) {
        this.visibility = visibility;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RoomMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<RoomMembership> memberships) {
        this.memberships = memberships;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<StudySession> getStudySessions() {
        return studySessions;
    }

    public void setStudySessions(List<StudySession> studySessions) {
        this.studySessions = studySessions;
    }

    public int getActiveParticipantCount() {
        return (int) memberships.stream()
                .filter(RoomMembership::isActive)
                .count();
    }

    public boolean isPrivate() {
        return this.visibility == RoomVisibility.PRIVATE;
    }

    public boolean isFull() {
        return getActiveParticipantCount() >= maxParticipants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyRoom studyRoom = (StudyRoom) o;
        return Objects.equals(id, studyRoom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
