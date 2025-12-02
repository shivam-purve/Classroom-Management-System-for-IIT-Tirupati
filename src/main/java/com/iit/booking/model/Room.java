package com.iit.booking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "room_type")
public abstract class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int capacity;
    
    // Resources (JSON stored as String for simplicity in this demo, typically a separate entity)
    private String resources; // e.g. "Projector, Smartboard"

    @ManyToOne
    @JoinColumn(name = "floor_id")
    @JsonBackReference
    private Floor floor;
}
