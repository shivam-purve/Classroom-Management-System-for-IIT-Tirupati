package com.iit.booking.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CLASSROOM")
public class Classroom extends Room {
    private boolean hasSmartBoard;
}
