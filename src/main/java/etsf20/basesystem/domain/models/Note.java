package etsf20.basesystem.domain.models;

import java.time.Instant;
import java.util.UUID;

public class Note {
    private UUID uuid;
    private final Instant timestamp;
    private final String userName;
    private String displayName;
    private String title;
    private String body;

    public Note(String title, String body, String userName) {
        this.title = title;
        this.body = body;
        this.userName = userName;
        this.timestamp = Instant.now();
    }

    public Note(UUID uuid, Instant timestamp, String userName, String displayName, String title, String body) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.userName = userName;
        this.displayName = displayName;
        this.title = title;
        this.body = body;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
