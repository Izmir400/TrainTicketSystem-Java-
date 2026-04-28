package application;

import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private String trainName;
    private String route;
    private Timestamp departureTime;
    private int seatsBooked;
    private String status;

    // Default constructor
    public Booking() {}

    public Booking(int bookingId, String trainName, String route, 
                   Timestamp departureTime, int seatsBooked, String status) {
        this.bookingId = bookingId;
        this.trainName = trainName;
        this.route = route;
        this.departureTime = departureTime;
        this.seatsBooked = seatsBooked;
        this.status = status;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public Timestamp getDepartureTime() { return departureTime; }
    public void setDepartureTime(Timestamp departureTime) { this.departureTime = departureTime; }

    public int getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(int seatsBooked) { this.seatsBooked = seatsBooked; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}