package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

	/**
	 * Books a ticket - FIXED for your database schema
	 */
	public boolean bookTicket(int scheduleId, String username, int seats, double totalPrice) {
	    String sql = """
	        INSERT INTO bookings (user_id, schedule_id, seats_booked, total_price, booking_date)
	        SELECT u.id, ?, ?, ?, NOW()
	        FROM users u 
	        WHERE u.username = ?
	        """;

	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, scheduleId);
	        stmt.setInt(2, seats);
	        stmt.setDouble(3, totalPrice);
	        stmt.setString(4, username);

	        int rows = stmt.executeUpdate();
	        System.out.println("Booking insert rows affected: " + rows);
	        return rows > 0;

	    } catch (SQLException e) {
	        System.err.println("=== BOOKING INSERT ERROR ===");
	        System.err.println("Message: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
    /**
     * Get all bookings for a specific user (by username)
     */
    public List<Booking> getBookingsByUsername(String username) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.booking_id, t.train_name, 
                   CONCAT(s1.station_name, ' → ', s2.station_name) AS route,
                   ts.departure_time, b.seats_booked, 
                   'Confirmed' AS status
            FROM bookings b
            JOIN users u ON b.user_id = u.id
            JOIN train_schedule ts ON b.schedule_id = ts.schedule_id
            JOIN trains t ON ts.train_id = t.train_id
            JOIN stations s1 ON ts.departure_station_id = s1.station_id
            JOIN stations s2 ON ts.arrival_station_id = s2.station_id
            WHERE u.username = ?
            ORDER BY ts.departure_time DESC
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking(
                    rs.getInt("booking_id"),
                    rs.getString("train_name"),
                    rs.getString("route"),
                    rs.getTimestamp("departure_time"),
                    rs.getInt("seats_booked"),
                    rs.getString("status")
                );
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Cancel a booking
     */
    public boolean cancelBooking(int bookingId, String username) {
        String sql = """
            DELETE b FROM bookings b
            JOIN users u ON b.user_id = u.id
            WHERE b.booking_id = ? AND u.username = ?
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            stmt.setString(2, username);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}