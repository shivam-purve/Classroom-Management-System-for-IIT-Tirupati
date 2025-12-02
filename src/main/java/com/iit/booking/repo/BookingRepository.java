package com.iit.booking.repo;

import com.iit.booking.model.Booking;
import com.iit.booking.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Get all bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.bookedBy.id = :uid ORDER BY b.startTime DESC")
    List<Booking> findByBookedById(@Param("uid") Long uid);

    List<Booking> findByStatus(BookingStatus status);

    // Find overlaps excluding Rejected/Overridden
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((b.startTime < :end) AND (b.endTime > :start))")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId, 
                                          @Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
    
    // Count for Admin Dashboard
    long countByStatus(BookingStatus status);
}
