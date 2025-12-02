package com.iit.booking.service;

import com.iit.booking.model.*;
import com.iit.booking.model.enums.BookingStatus;
import com.iit.booking.model.enums.UserType;
import com.iit.booking.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired private BookingRepository bookingRepo;
    @Autowired private RoomRepository roomRepo;

    // Priority Logic Implementation
    @Transactional
    public String createBooking(User user, Long roomId, LocalDateTime start, LocalDateTime end, String purpose, String resources) {
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        
        // 1. Check for Clashes
        List<Booking> clashes = bookingRepo.findOverlappingBookings(roomId, start, end);

        if (!clashes.isEmpty()) {
            for (Booking existing : clashes) {
                User existingUser = existing.getBookedBy();

                // Logic for Student
                if (user.getRole() == UserType.STUDENT) {
                    throw new RuntimeException("Slot Clash! This slot is already booked by " + existingUser.getName());
                }

                // Logic for Faculty
                if (user.getRole() == UserType.FACULTY) {
                    if (existingUser.getRole() == UserType.STUDENT) {
                        // Override Student
                        existing.setStatus(BookingStatus.OVERRIDDEN);
                        bookingRepo.save(existing);
                        // Notification would go here
                    } else {
                        // Clash with Faculty/Admin
                        throw new RuntimeException("Slot Clash with another Faculty/Admin!");
                    }
                }

                // Logic for Admin
                if (user.getRole() == UserType.ADMIN) {
                    // Admin overrides everyone
                    existing.setStatus(BookingStatus.OVERRIDDEN);
                    bookingRepo.save(existing);
                }
            }
        }

        // 2. Create Booking
        Booking newBooking = new Booking();
        newBooking.setBookedBy(user);
        newBooking.setRoom(room);
        newBooking.setStartTime(start);
        newBooking.setEndTime(end);
        newBooking.setPurpose(purpose);
        newBooking.setRequestedResources(resources);

        // 3. Status determination
        if (user.getRole() == UserType.ADMIN) {
            newBooking.setStatus(BookingStatus.CONFIRMED);
        } else {
            // Prompt 4 says "Admin approves any booking", but faculty override takes effect immediately via clash removal.
            // We set it to PENDING for final approval.
            newBooking.setStatus(BookingStatus.PENDING);
        }

        bookingRepo.save(newBooking);
        return "Booking Request Created with status: " + newBooking.getStatus();
    }
}
