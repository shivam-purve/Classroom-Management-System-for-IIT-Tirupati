package com.iit.booking.model;

import com.iit.booking.model.enums.UserType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("FACULTY")
public class Faculty extends User {
    private String employeeId;
    private String department;

    public Faculty() {
        this.setRole(UserType.FACULTY);
    }
}
