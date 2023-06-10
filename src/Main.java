import java.sql.*;
import java.util.*;
import io.github.cdimascio.dotenv.*;

public class Main {
    public static void main(String[] args) {
        Dotenv config = Dotenv.load();  // load config from .env file
        Scanner sc = new Scanner(System.in);  // create scanner for input

        Properties props = new Properties();  // set up props object to connect to database
        props.setProperty("user", config.get("USERNAME"));
        props.setProperty("password", config.get("PASSWORD"));

        // connect to database
        try (Connection conn = DriverManager.getConnection("jdbc:" + config.get("URL"), props)) {
            Client runner = new Client(conn, sc);  // create client
            runner.run();  // run application (blocking)
        } catch (SQLException e) {
            // could not connect (server is off?)
            System.out.println("Error: could not connect to the database.");
            System.out.println("Check that the database is running and that the configuration details are correct");
        }

        sc.close();  // prevent memory leak
    }
}