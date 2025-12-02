package com.iit.booking.controller;

import com.iit.booking.model.*;
import com.iit.booking.model.enums.*;
import com.iit.booking.repo.*;
import com.iit.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class APIController {

    @Autowired private UserRepository userRepo;
    @Autowired private BuildingRepository buildingRepo;
    @Autowired private FloorRepository floorRepo;
    @Autowired private RoomRepository roomRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private BookingService bookingService;

    // --- AUTH ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
        String roleStr = data.get("role");
        String email = data.get("email");
        if(userRepo.findByEmail(email).isPresent()) return ResponseEntity.badRequest().body("Email exists");

        User user;
        if(roleStr.equals("STUDENT")) {
            Student s = new Student();
            s.setStudentId(data.get("specificId"));
            s.setBranch(data.get("branch"));
            s.setProgram(data.get("program"));
            user = s;
        } else if(roleStr.equals("FACULTY")) {
            if(!"iit_fac_2025".equals(data.get("secretKey"))) return ResponseEntity.badRequest().body("Invalid Faculty Secret");
            Faculty f = new Faculty();
            f.setEmployeeId(data.get("specificId"));
            f.setDepartment(data.get("branch"));
            user = f;
        } else if(roleStr.equals("ADMIN")) {
            if(!"iit_admin_2025".equals(data.get("secretKey"))) return ResponseEntity.badRequest().body("Invalid Admin Secret");
            Admin a = new Admin();
            a.setEmployeeId(data.get("specificId"));
            user = a;
        } else {
            return ResponseEntity.badRequest().body("Invalid Role");
        }

        user.setName(data.get("name"));
        user.setEmail(email);
        user.setPassword(data.get("password"));
        userRepo.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {
        Optional<User> u = userRepo.findByEmail(data.get("email"));
        if(u.isPresent() && u.get().getPassword().equals(data.get("password"))) {
            return ResponseEntity.ok(u.get());
        }
        return ResponseEntity.status(401).body("Invalid Credentials");
    }

    // --- INFRASTRUCTURE ---
    @PostMapping("/building")
    public ResponseEntity<?> addBuilding(@RequestBody Map<String, Object> data) {
        String name = data.get("name").toString();
        int floors = Integer.parseInt(data.get("floors").toString());
        Building b = new Building();
        b.setName(name);
        Building saved = buildingRepo.save(b);
        for(int i = 1; i <= floors; i++) {
            Floor f = new Floor();
            f.setFloorNumber(i);
            f.setBuilding(saved);
            floorRepo.save(f);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/building/{id}")
    public ResponseEntity<?> deleteBuilding(@PathVariable Long id) {
        buildingRepo.deleteById(id);
        return ResponseEntity.ok("Deleted Building");
    }

    @DeleteMapping("/floor/{id}")
    public ResponseEntity<?> deleteFloor(@PathVariable Long id) {
        floorRepo.deleteById(id);
        return ResponseEntity.ok("Deleted Floor");
    }

    @DeleteMapping("/room/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        roomRepo.deleteById(id);
        return ResponseEntity.ok("Deleted Room");
    }

    @PostMapping("/room")
    public ResponseEntity<?> addRoom(@RequestBody Map<String, Object> data) {
        try {
            Long fId = Long.parseLong(data.get("floorId").toString());
            Floor f = floorRepo.findById(fId).orElseThrow(() -> new RuntimeException("Floor not found"));
            String type = data.get("type").toString();
            
            Room r;
            if(type.equals("CLASSROOM")) {
                Classroom c = new Classroom();
                Object sb = data.get("hasSmartBoard");
                c.setHasSmartBoard(sb != null && Boolean.parseBoolean(sb.toString()));
                r = c;
            } else {
                Lab l = new Lab();
                Object lt = data.get("labType");
                l.setLabType(lt != null ? lt.toString() : "General Lab");
                r = l;
            }
            
            r.setName(data.get("name").toString());
            r.setCapacity(Integer.parseInt(data.get("capacity").toString()));
            // Handle optional resources field
            Object res = data.get("resources");
            r.setResources(res != null ? res.toString() : "");
            r.setFloor(f);
            
            return ResponseEntity.ok(roomRepo.save(r));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating room: " + e.getMessage());
        }
    }

    @GetMapping("/campus")
    public ResponseEntity<?> getCampus() { return ResponseEntity.ok(buildingRepo.findAll()); }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rooms", roomRepo.count());
        stats.put("pending", bookingRepo.countByStatus(BookingStatus.PENDING));
        stats.put("buildings", buildingRepo.count());
        return ResponseEntity.ok(stats);
    }

    // --- BOOKING ---
    @GetMapping("/slots")
    public ResponseEntity<?> getSlots(@RequestParam Long roomId, @RequestParam String date) {
        List<Map<String, Object>> slots = new ArrayList<>();
        for (int i = 8; i < 24; i++) {
            LocalDateTime start = LocalDateTime.parse(date + "T" + (i < 10 ? "0"+i : i) + ":00:00");
            LocalDateTime end = start.plusHours(1);
            List<Booking> clashes = bookingRepo.findOverlappingBookings(roomId, start, end);
            
            Map<String, Object> slot = new HashMap<>();
            slot.put("start", start.toString());
            slot.put("end", end.toString());
            slot.put("label", String.format("%02d:00 - %02d:00", i, i+1));
            
            if(clashes.isEmpty()) {
                slot.put("status", "AVAILABLE");
            } else {
                Booking b = clashes.get(0);
                slot.put("status", "BOOKED");
                slot.put("bookedBy", b.getBookedBy().getName());
                slot.put("userRole", b.getBookedBy().getRole());
            }
            slots.add(slot);
        }
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/book")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, String> data) {
        try {
            Long userId = Long.parseLong(data.get("userId"));
            Long roomId = Long.parseLong(data.get("roomId"));
            LocalDateTime start = LocalDateTime.parse(data.get("startTime"));
            LocalDateTime end = LocalDateTime.parse(data.get("endTime"));
            String purpose = data.get("purpose");
            String resources = data.get("resources");

            User currentUser = userRepo.findById(userId).orElseThrow();
            Room room = roomRepo.findById(roomId).orElseThrow();

            List<Booking> clashes = bookingRepo.findOverlappingBookings(roomId, start, end);
            
            if (!clashes.isEmpty()) {
                Booking existing = clashes.get(0);
                User existingUser = existing.getBookedBy();

                if (existingUser.getRole() == UserType.ADMIN) {
                    return ResponseEntity.status(400).body("Slot booked by ADMIN. Cannot Override.");
                }

                if (currentUser.getRole() == UserType.FACULTY) {
                    if (existingUser.getRole() == UserType.STUDENT) {
                        existing.setStatus(BookingStatus.OVERRIDDEN);
                        bookingRepo.save(existing);
                    } else {
                        return ResponseEntity.status(400).body("Slot booked by another Faculty/Admin.");
                    }
                } else if (currentUser.getRole() == UserType.ADMIN) {
                    existing.setStatus(BookingStatus.OVERRIDDEN);
                    bookingRepo.save(existing);
                } else if (currentUser.getRole() == UserType.STUDENT) {
                    return ResponseEntity.status(400).body("Slot already booked.");
                }
            }

            Booking newBooking = new Booking();
            newBooking.setBookedBy(currentUser);
            newBooking.setRoom(room);
            newBooking.setStartTime(start);
            newBooking.setEndTime(end);
            newBooking.setPurpose(purpose);
            newBooking.setRequestedResources(resources);
            
            if(currentUser.getRole() == UserType.ADMIN) newBooking.setStatus(BookingStatus.CONFIRMED);
            else newBooking.setStatus(BookingStatus.PENDING);

            bookingRepo.save(newBooking);
            return ResponseEntity.ok("Booking Request Placed!");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/cancel-booking/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, @RequestBody Map<String, Long> data) {
        Long userId = data.get("userId");
        Booking b = bookingRepo.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!b.getBookedBy().getId().equals(userId)) return ResponseEntity.status(403).body("Unauthorized");
        b.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(b);
        return ResponseEntity.ok("Booking Cancelled");
    }

    @GetMapping("/my-bookings/{userId}")
    public ResponseEntity<?> getMyBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingRepo.findByBookedById(userId));
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(bookingRepo.findByStatus(BookingStatus.PENDING));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        Booking b = bookingRepo.findById(id).orElseThrow();
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepo.save(b);
        return ResponseEntity.ok("Approved");
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        Booking b = bookingRepo.findById(id).orElseThrow();
        b.setStatus(BookingStatus.REJECTED);
        bookingRepo.save(b);
        return ResponseEntity.ok("Rejected");
    }
    
    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> data) {
        Long uid = Long.parseLong(data.get("id"));
        User u = userRepo.findById(uid).orElseThrow();
        u.updateProfile(data.get("name"), data.get("password"));
        userRepo.save(u);
        return ResponseEntity.ok(u);
    }
}
