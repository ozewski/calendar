import java.sql.*;
import java.util.*;

public class Client {
    private EventCalendar calendar;
    private Connection connection;
    private Scanner scanner;

    private static final List<String> MONTHS = Arrays.asList(
            "january", "february", "march", "april", "may", "june", "july", "august", "september", "october",
            "november", "december"
    );  // static list for input parsing

    public Client(Connection conn, Scanner sc) throws SQLException {
        this.calendar = new EventCalendar(conn);  // create blank EventCalendar
        this.connection = conn;
        this.scanner = sc;
    }

    /**
     * Runs the main application. This method is blocking.
     */
    public void run() throws SQLException {
        changeCalendarDate();  // initially prompt user to pick month
        boolean running = true;
        while (running) {
            for (int i = 0; i < 50; i++)
                System.out.println();  // clear screen
            calendar.printCalendar();  // print calendar UI
            System.out.println();
            printCalendarOptions();  // prompt user with options
            int opt = promptIntRange(">> ", 1, 5);
            System.out.println();
            switch (opt) {  // delegate user choice
                case 1 -> viewEvent();
                case 2 -> addEvent();
                case 3 -> deleteEvent();
                case 4 -> changeCalendarDate();
                case 5 -> running = false;
            }
        }
    }

    /**
     * Prints out the user's main set of options to interact with the calendar.
     */
    public static void printCalendarOptions() {
        System.out.println("OPTIONS:");
        System.out.println("  1) View an event");
        System.out.println("  2) Add an event");
        System.out.println("  3) Delete an event");
        System.out.println("  4) Change the month");
        System.out.println("  5) Quit\n");
    }

    /**
     * Holds the program until the user presses "enter" (blocking).
     */
    public void waitForEnter() {
        System.out.print("\n(press ENTER to continue) ");
        scanner.nextLine();
    }

    /**
     * Safely prompt the user for a boolean value, through a y/n response
     * @param prompt The initial prompt
     * @return The user's boolean choice
     */
    public boolean promptBoolean(String prompt) {
        Boolean res = null;
        while (true) {
            System.out.print(prompt);
            char c = scanner.nextLine().charAt(0);
            if (c == 'y')
                res = true;
            else if (c == 'n')
                res = false;
            if (res != null)
                return res;  // return if input was valid, otherwise loop
        }
    }

    /**
     * Safely prompt the user to input a valid integer.
     * @param prompt The initial prompt
     * @return The user's integer choice
     */
    public int promptInt(String prompt) {
        int res;
        while (true) {
            System.out.print(prompt);
            try {
                res = scanner.nextInt();
                break;
            } catch (InputMismatchException e) {
                scanner.next();  // consume newline to avoid infinite loop
            }
        }
        scanner.nextLine();  // consume newline for sake of future scans
        return res;
    }

    /**
     * Safely prompt the user for an integer, limited to a specific range.
     * @param prompt The initial prompt
     * @param low The input's lower bound
     * @param high The input's higher bound
     * @return The user's integer choice
     */
    public int promptIntRange(String prompt, int low, int high) {
        int option = low - 1;
        while (option < low || option > high) {  // loop until input is within given range
            option = promptInt(prompt);
        }
        return option;
    }

    /**
     * Safely prompt the user to pick a valid month.
     * @param prompt The initial prompt
     * @return An integer (0-11) representing the user's choice
     */
    public int promptMonth(String prompt) {
        int res = -1;
        while (res < 0) {  // loop until month name is valid
            System.out.print(prompt);
            res = MONTHS.indexOf(scanner.nextLine().toLowerCase());
        }
        return res;
    }

    /**
     * Prompt the user to choose an event from a list of events within the month.
     * @return An Event object representing the user's choice
     */
    public Event promptEvent() {
        ArrayList<Event> events = calendar.getAllEvents();
        int i = 1;
        System.out.println("Events in " + calendar.getMonthAndYearName() + ": ");
        for (Event e : events) {  // list all events in the month
            System.out.println("  " + i + ") " + e.getName() + " (" + e.getDateAndTime() + ")");
            i++;
        }
        System.out.print("\nPick which event ");
        return events.get(promptIntRange(">> ", 1, i - 1) - 1);  // allow user to choose from list
    }

    /**
     * User option screen: allows the user to change the month and year of the calendar.
     */
    public void changeCalendarDate() throws SQLException {
        System.out.print("Enter a month ");
        int month = promptMonth(">> ");

        System.out.print("Enter a year ");
        int year = promptInt(">> ");

        calendar.changeMonth(year, month);
    }

    /**
     * User option screen: allows the user to show details about a specific event in the month.
     */
    public void viewEvent() {
        Set<Integer> days = calendar.getEvents().keySet();
        if (days.isEmpty()) {  // no events in the month
            System.out.println(" - No events found for this month.");
        } else {
            Event target = promptEvent();
            System.out.println("\n-----\n");
            target.printInformation();  // print target details
        }
        waitForEnter();
    }

    /**
     * User option screen: allows the user to add a new event to the calendar.
     */
    public void addEvent() throws SQLException {
        System.out.print("Name of event >> ");
        String name = scanner.nextLine();
        System.out.print("Set a note (optional) >> ");
        String note = scanner.nextLine();
        System.out.print("Enter the day of month ");
        int day = promptIntRange(">> ", 1, 31);
        System.out.print("Enter the hour of day (0-23) ");
        int hour = promptIntRange(">> ", 1, 23);
        Timestamp date = Timestamp.valueOf(String.format(  // create SQL timestamp from ISO format
                "%d-%02d-%02d %02d:00:00", calendar.getYear(), calendar.getMonth() + 1, day, hour
        ));
        calendar.addEvent(name, note, date);  // add to calendar
        System.out.println("\n - Added event to calendar.");
        waitForEnter();
    }

    /**
     * User option screen: allows the user to delete an event from the calendar.
     */
    public void deleteEvent() throws SQLException {
        Set<Integer> days = calendar.getEvents().keySet();
        if (days.isEmpty()) {
            System.out.println(" - No events found for this month.");
        } else {
            Event target = promptEvent();
            System.out.print("Confirm deletion of event \"" + target.getName() + "\" (y/n) ");
            boolean confirmation = promptBoolean(">> ");  // ask user to confirm deletion
            if (confirmation) {
                calendar.deleteEvent(target);  // delete event
                System.out.println("\n - Event deleted from calendar.");
            } else {
                System.out.println("\n - Event was not deleted.");
            }
        }
        waitForEnter();
    }
}
