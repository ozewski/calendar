import java.sql.*;
import java.time.*;
import java.time.format.*;

public class Event {
    // configure various format templates for dates
    private static final DateTimeFormatter DATE_FORMAT_LONG = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("MMMM dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    private int id;
    private String name;
    private String note;
    private LocalDateTime dateTime;  // LocalDateTime is more versatile than sql.Timestamp

    public Event(ResultSet rs) throws SQLException {  // parse from ResultSet
        id = rs.getInt("id");
        name = rs.getString("name");
        note = rs.getString("note");
        dateTime = rs.getTimestamp("time").toLocalDateTime();
    }

    public Event(int id, String name, String note, LocalDateTime time) {  // manual creation
        this.id = id;
        this.name = name;
        this.note = note;
        this.dateTime = time;
    }

    /**
     * @return The event ID
     */
    public int getId() {
        return id;
    }

    /**
     * @return The event name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The event's LocalDateTime
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * @return A short string describing the event by its date and time without a year
     */
    public String getDateAndTime() {
        return dateTime.format(DATE_FORMAT_SHORT) + " at " + dateTime.format(TIME_FORMAT);
    }

    /**
     * Prints a short description of the event.
     */
    public void printInformation() {
        System.out.println("EVENT NAME: " + name);
        if (note != null && !note.isEmpty())
            System.out.println(" -- NOTE: \"" + note + "\"");
        System.out.println(" -- DATE: " + dateTime.format(DATE_FORMAT_LONG));
        System.out.println(" -- TIME: " + dateTime.format(TIME_FORMAT));
    }
}
