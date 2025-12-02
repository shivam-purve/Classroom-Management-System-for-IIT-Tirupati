package com.iit.booking.model;

import com.iit.booking.model.enums.UserType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    private String employeeId;

    public Admin() {
        this.setRole(UserType.ADMIN);
    }
}
