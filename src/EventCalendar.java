import java.sql.*;
import java.text.*;
import java.util.*;

/**
 * Represents a calendar of events, loaded from a data source.
 */
public class EventCalendar {
    private final Calendar calendar;  // used for date logic to format calendar UI
    private int year;
    private int month;
    private final Connection connection;
    private HashMap<Integer, ArrayList<Event>> events;  // each day gets an array to hold more than one event

    public EventCalendar(Connection conn) {
        // initialize calendar given connection
        this.calendar = new GregorianCalendar();
        this.connection = conn;
    }

    /**
     * Changes the calendar to operate on a specific month and year.
     * Will force re-loading of events for the new month.
     * @param year A year
     * @param month A month (0-11)
     */
    public void changeMonth(int year, int month) throws SQLException {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);  // used later to find beginning day of week
        this.year = year;
        this.month = month;
        loadEvents();  // reload events
    }

    /**
     * Registers a new event to the internal event cache.
     * @param e Event to be registered
     */
    private void registerEvent(Event e) {
        int day = e.getDateTime().getDayOfMonth();
        if (!events.containsKey(day))
            // create new array because no events are loaded for that day
            events.put(day, new ArrayList<>());
        events.get(day).add(e);  // add event to internal array
    }

    /**
     * Removes an event from the internal event cache.
     * @param e Event to deregister
     */
    private void deregisterEvent(Event e) {
        int day = e.getDateTime().getDayOfMonth();
        ArrayList<Event> dayEvents = events.get(day);
        dayEvents.remove(e);  // remove from cache
        if (dayEvents.size() == 0)
            // remove day array if it has no events left
            events.remove(day);
    }

    /**
     * Loads events from a database source and registers them into the internal cache.
     * Will only load events for the object's month.
     */
    private void loadEvents() throws SQLException {
        this.events = new HashMap<>();  // reset events cache
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM events WHERE EXTRACT(YEAR FROM time) = ? AND EXTRACT(MONTH FROM time) = ?");
        stmt.setInt(1, year);
        stmt.setInt(2, month + 1);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Event e = new Event(rs);  // Event object parses data from ResultSet
            registerEvent(e);  // registered into cache
        }
    }

    /**
     * Adds a new event to the database and registers it to the cache.
     * @param name Name of the event
     * @param note Short note (optional)
     * @param time Timestamp representing start time of event
     */
    public void addEvent(String name, String note, Timestamp time) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO events VALUES (DEFAULT, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, name);
        if (note == null || note.isEmpty()) {  // handle optionality
            stmt.setNull(2, 0);
        } else {
            stmt.setString(2, note);
        }
        stmt.setTimestamp(3, time);
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();  // fetch generated ID to pass into Event object
        rs.next();
        Event e = new Event(rs.getInt(1), name, note, time.toLocalDateTime());  // build fresh Event
        registerEvent(e);  // cache register
    }

    /**
     * Deletes an event from the database and the cache.
     * @param e Event to be deleted
     */
    public void deleteEvent(Event e) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM events WHERE id = ?");
        stmt.setInt(1, e.getId());
        stmt.executeUpdate();  // delete from database
        deregisterEvent(e);  // delete from cache
    }

    /**
     * @return The currently set year
     */
    public int getYear() {
        return year;
    }

    /**
     * @return The currently set month
     */
    public int getMonth() {
        return month;
    }

    /**
     *
     * @return A simple date format for the current month and year (MONTH year)
     */
    public String getMonthAndYearName() {
        return new SimpleDateFormat("MMMM yyyy").format(calendar.getTime());
    }

    /**
     * @return The internal event cache (Map)
     */
    public HashMap<Integer, ArrayList<Event>> getEvents() {
        return events;
    }

    /**
     * @return ArrayList of all currently cached events
     */
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> res = new ArrayList<>();
        for (ArrayList<Event> events : events.values())
            res.addAll(events);
        return res;
    }

    /**
     * Returns a formatted string of the total count of events in the cache
     * @return A string ("no events" or "1 event" or "n events")
     */
    private String formatEventCount() {
        int size = getAllEvents().size();
        String res = (size == 0 ? "no" : size) + " event";
        if (size != 1)
            res += "s";
        return res;
    }

    /**
     * Prints a graphical representation of the calendar.
     * The calendar is displayed in a text grid and all dates are aligned properly with their days of the week.
     */
    public void printCalendar() {
        System.out.println("+----------------------------------+");
        System.out.printf("| %-32s |\n", getMonthAndYearName() + " (" + formatEventCount() + ")");
        System.out.println("+----+----+----+----+----+----+----+");
        System.out.println("| Su | Mo | Tu | We | Th | Fr | Sa |");
        System.out.println("+----+----+----+----+----+----+----+");

        int startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;  // day of the week of the first of the month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);  // total days in month
        int extraDays = (7 - (startDay + daysInMonth) % 7) % 7;
        // extra spaces required to visually fill the remainder of the final calendar row
        int posInWeek = 0;

        // iteration begins negative to skip initial spaces before the first weekday
        for (int day = -(startDay - 1); day <= daysInMonth + extraDays; day++) {
            System.out.print("| ");
            if (day > 0 && day <= daysInMonth) {
                if (day < 10)  // extra space for number justification when < 10
                    System.out.print(" ");
                System.out.print(day);
            } else {
                // current num is outside the bounds of the calendar; print blank space
                System.out.print("  ");
            }
            if (events.containsKey(day))  // add asterisk to note event on the day
                System.out.print("*");
            else
                System.out.print(" ");
            if (++posInWeek == 7) {  // reached the end of the row; cycle back
                System.out.println("|");
                posInWeek = 0;
            }
        }
        System.out.println("+----+----+----+----+----+----+----+");
    }
}
