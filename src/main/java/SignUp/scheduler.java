/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SignUp;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author GOWTHAM RJ
 */
public class scheduler {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delay = calculateDelay();
        if (delay > 0) {
            scheduler.schedule(() -> {
                try {
                    AutoAllocation.startAllocation(); 
                    System.out.println("Allocation executed successfully.");
                } catch (Exception ex) {
                    System.out.println("Error during allocation execution: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }, delay, TimeUnit.MILLISECONDS);
            System.out.println("Allocation scheduled to run after " + delay + " ms.");
        } else {
            System.out.println("The deadline has already passed or is invalid. No scheduling will occur.");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            System.out.println("Scheduler shutdown.");
        }));
    }
   private static long calculateDelay() {
    String dbUrl = "jdbc:mysql://localhost:3306/list_of_colleges";
    String dbUser = "root";
    String dbPass = "Prema@1977";
    String query = "SELECT MAX(deadline) AS latest_deadline FROM deadlines";
    try (Connection con = java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPass);
         PreparedStatement pst = con.prepareStatement(query);
         ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
            Timestamp deadline = rs.getTimestamp("latest_deadline");
            if (deadline != null) {
                long currentTime = System.currentTimeMillis();
                long deadlineTime = deadline.getTime();

                if (deadlineTime > currentTime) {
                    return deadlineTime - currentTime;
                } else {
                    System.out.println("Deadline has already passed.");
                    return 0;
                }
            } else {
                System.out.println("No valid deadline found in the database.");
            }
        }
    } catch (SQLException ex) {
        System.out.println("Database error while calculating delay: " + ex.getMessage());
        ex.printStackTrace();
    }
    return 0; 
}
}

