package com.rebeccabro.theweddingpath;

/**
 * Name: Rebecca Scranton
 * Date: August 13, 2026
 * Description: Data model representing a specific vendor sub-event or milestone.
 * Holds the database primary key (id) required for strict Update and Delete operations.
 */
public class SubEvent {

    private final int id;
    private final String title;
    private final long timestamp;

    public SubEvent(int id, String title, long timestamp) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public long getTimestamp() { return timestamp; }
}
