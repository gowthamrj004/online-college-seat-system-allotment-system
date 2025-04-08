/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SignUp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author GOWTHAM RJ
 */
public class AutoAllocation {
   
public static void main(String[] args) {
        System.out.println("Starting auto-allocation...");
        startAllocation();
    }
public static boolean isAllocationDeadlinePassed() {
    String surl = "jdbc:MySQL://localhost:3306/list_of_colleges";
    String suse = "root";
    String spass = "Prema@1977";
    String query = "SELECT deadline FROM deadlines";
    try (Connection con = java.sql.DriverManager.getConnection(surl, suse, spass);
         PreparedStatement pst = con.prepareStatement(query);
         ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
            Timestamp deadline = rs.getTimestamp("deadline");
            return new Date().after(deadline); 
        }
    } catch (SQLException ex) {
        System.out.println("Database error while checking allocation deadline: " + ex.getMessage());
        ex.printStackTrace();
    }
    return false;
}

public static void startAllocation() {
if (!isAllocationDeadlinePassed()) {
        System.out.println("The allocation deadline has not yet passed. Allocation will not proceed.");
        return; 
    }
    try {
        // Database connection setup
        String SUrl = "jdbc:MySQL://localhost:3306/list_of_colleges";
        String SUser = "root";
        String SPass = "Prema@1977";
        Connection con = java.sql.DriverManager.getConnection(SUrl, SUser, SPass);
        con.setAutoCommit(false);
        String query = "SELECT * FROM users ORDER BY rank1"; 
        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            String UID = rs.getString("uid");
            String rank = rs.getString("rank1");
            String category = rs.getString("cateogory");
            String tableName = "user_choiceUID_" + UID;
            String userChoicesQuery = "SELECT * FROM " + tableName + " ORDER BY choice_id"; 
            PreparedStatement pstChoices = con.prepareStatement(userChoicesQuery);
            ResultSet rsChoices = pstChoices.executeQuery();
            boolean allocated = false;
            while (rsChoices.next() && !allocated) {
                String collegeName = rsChoices.getString("college_name");
                String branchName = rsChoices.getString("branch_name");
                String checkSeatsQuery = "SELECT * FROM list_of_college WHERE college_name = ? AND branch_name = ?";
                PreparedStatement pstSeats = con.prepareStatement(checkSeatsQuery);
                pstSeats.setString(1, collegeName);
                pstSeats.setString(2, branchName);
                ResultSet rsSeats = pstSeats.executeQuery();
                if (rsSeats.next()) {
                    int total_seats = rsSeats.getInt("total_seats");
                    int genseat = rsSeats.getInt("genseat");
                    int obcseat = rsSeats.getInt("obcseat");
                    int scstseat = rsSeats.getInt("scstseat");
                    int gencutoff = rsSeats.getInt("gencutoff");
                    int obccutoff = rsSeats.getInt("obccutoff");
                    int scstcutoff = rsSeats.getInt("scstcutoff");
                    int userRank = Integer.parseInt(rank);
                    boolean eligibleForAllocation = false;
                    if (category.equals("GM") && userRank <= gencutoff && genseat > 0) {
                        eligibleForAllocation = true;
                        String updateSeatsQuery = "UPDATE list_of_college SET genseat = genseat - 1,total_seats=total_seats - 1 WHERE college_name = ? AND branch_name = ?";
                        PreparedStatement updateSeats = con.prepareStatement(updateSeatsQuery);
                        updateSeats.setString(1, collegeName);
                        updateSeats.setString(2, branchName);
                        updateSeats.executeUpdate();
                    } else if (category.equals("OBC") && userRank <= obccutoff && obcseat > 0) {
                        eligibleForAllocation = true;
                        String updateSeatsQuery = "UPDATE list_of_college SET obcseat = obcseat - 1,total_seats=total_seats - 1 WHERE college_name = ? AND branch_name = ?";
                        PreparedStatement updateSeats = con.prepareStatement(updateSeatsQuery);
                        updateSeats.setString(1, collegeName);
                        updateSeats.setString(2, branchName);
                        updateSeats.executeUpdate();
                    } else if (category.equals("SC/ST") && userRank <= scstcutoff && scstseat > 0) {
                        eligibleForAllocation = true;
                        String updateSeatsQuery = "UPDATE list_of_college SET scstseat = scstseat - 1,total_seats=total_seats - 1 WHERE college_name = ? AND branch_name = ?";
                        PreparedStatement updateSeats = con.prepareStatement(updateSeatsQuery);
                        updateSeats.setString(1, collegeName);
                        updateSeats.setString(2, branchName);
                        updateSeats.executeUpdate();
                    }
                    if (eligibleForAllocation) {
                        String updateUserQuery = "UPDATE users SET allocated_college = ?, allocated_branch = ? WHERE uid = ?";
                        PreparedStatement updateUser = con.prepareStatement(updateUserQuery);
                        updateUser.setString(1, collegeName);
                        updateUser.setString(2, branchName);
                        updateUser.setString(3, UID);
                        updateUser.executeUpdate();
                        JOptionPane.showMessageDialog(null, "User " + UID + " allocated to " + collegeName + " - " + branchName);
                        allocated = true;  
                    }
                }
            }

            if (!allocated) {
                JOptionPane.showMessageDialog(null, "User " + UID + " could not be allocated a college.");
            }
        }
        con.commit();
        con.close();  
    } catch (SQLException ex) {
        System.out.println("Error occurred during allocation: " + ex.getMessage());
    }
}
}