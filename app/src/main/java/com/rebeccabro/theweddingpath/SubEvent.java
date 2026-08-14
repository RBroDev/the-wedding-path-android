package com.rebeccabro.theweddingpath;

/**
 * Data model representing a specific vendor sub-event or milestone.
 * Encapsulates the event details along with its database primary key
 * to facilitate precise update and delete operations.
 */
public class SubEvent {

    private final int id;
    private final String title;
    private final long timestamp;

    /**
     * Constructs a new SubEvent.
     *
     * @param id        The unique database primary key for this event.
     * @param title     The title or description of the sub-event.
     * @param timestamp The date and time of the event, represented in milliseconds.
     */
    public SubEvent(int id, String title, long timestamp) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
    }

    /**
     * Retrieves the database primary key.
     *
     * @return The unique identifier for this event.
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the title of the sub-event.
     *
     * @return The event title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the timestamp of the sub-event.
     *
     * @return The event timestamp in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }
}