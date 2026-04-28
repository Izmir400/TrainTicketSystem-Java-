package application;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainDAO {

    /**
     * Main flexible method used by Dashboard and TrainScheduleController
     * Supports partial search (from/to can be empty)
     */
    public List<Train> getTrainsForDashboard(String from, String to, String date) {
        List<Train> trains = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT ts.schedule_id, t.train_id, t.train_name, " +
            "s1.station_name AS departure_station, " +
            "s2.station_name AS arrival_station, " +
            "ts.departure_time, ts.arrival_time, ts.available_seats, ts.price " +
            "FROM trains t " +
            "JOIN train_schedule ts ON t.train_id = ts.train_id " +
            "JOIN stations s1 ON ts.departure_station_id = s1.station_id " +
            "JOIN stations s2 ON ts.arrival_station_id = s2.station_id " +
            "WHERE DATE(ts.departure_time) = ? "
        );

        boolean hasFrom = from != null && !from.trim().isEmpty();
        boolean hasTo   = to != null && !to.trim().isEmpty();

        if (hasFrom) sql.append("AND s1.station_name = ? ");
        if (hasTo)   sql.append("AND s2.station_name = ? ");

        sql.append("ORDER BY ts.departure_time ASC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setString(index++, date);

            if (hasFrom) {
                stmt.setString(index++, from.trim());
            }
            if (hasTo) {
                stmt.setString(index++, to.trim());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Train train = new Train();

                    train.setTrainId(rs.getInt("train_id"));
                    train.setTrainName(rs.getString("train_name"));
                    train.setDepartureStation(rs.getString("departure_station"));
                    train.setArrivalStation(rs.getString("arrival_station"));
                    train.setDepartureTime(rs.getTimestamp("departure_time"));
                    train.setArrivalTime(rs.getTimestamp("arrival_time"));
                    train.setAvailableSeats(rs.getInt("available_seats"));
                    train.setPrice(rs.getDouble("price"));

                    // Fallback price if price is 0 or null
                    if (train.getPrice() <= 0) {
                        train.setPrice(25.0);
                    }

                    // Important: Store schedule_id for booking
                    // We'll add this field to Train class later if needed
                    // For now, we'll use train_id as fallback

                    trains.add(train);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error in getTrainsForDashboard: " + e.getMessage());
        }

        return trains;
    }

    /**
     * Get ALL train schedules (used by TrainScheduleController)
     */
    public List<Train> getAllTrainSchedules() {
        List<Train> trains = new ArrayList<>();

        String sql = """
            SELECT ts.schedule_id, t.train_id, t.train_name,
                   s1.station_name AS departure_station,
                   s2.station_name AS arrival_station,
                   ts.departure_time, ts.arrival_time,
                   ts.available_seats, ts.price
            FROM train_schedule ts
            JOIN trains t ON ts.train_id = t.train_id
            JOIN stations s1 ON ts.departure_station_id = s1.station_id
            JOIN stations s2 ON ts.arrival_station_id = s2.station_id
            ORDER BY ts.departure_time ASC
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Train train = new Train();

                train.setTrainId(rs.getInt("train_id"));
                train.setTrainName(rs.getString("train_name"));
                train.setDepartureStation(rs.getString("departure_station"));
                train.setArrivalStation(rs.getString("arrival_station"));
                train.setDepartureTime(rs.getTimestamp("departure_time"));
                train.setArrivalTime(rs.getTimestamp("arrival_time"));
                train.setAvailableSeats(rs.getInt("available_seats"));
                train.setPrice(rs.getDouble("price"));

                if (train.getPrice() <= 0) {
                    train.setPrice(25.0);
                }

                trains.add(train);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error in getAllTrainSchedules: " + e.getMessage());
        }

        return trains;
    }

    // Backward compatibility
    public List<Train> searchTrains(String from, String to, String date) {
        return getTrainsForDashboard(from, to, date);
    }

    // Get only today's trains
    public List<Train> getTodaysTrains() {
        String today = LocalDate.now().toString();
        return getTrainsForDashboard("", "", today);
    }
}