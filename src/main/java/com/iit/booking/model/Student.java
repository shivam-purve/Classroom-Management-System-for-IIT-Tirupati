package com.iit.booking.model;

import com.iit.booking.model.enums.UserType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("STUDENT")
public class Student extends User {
    private String studentId; // Roll No
    private String program;   // B.Tech, M.Tech
    private String branch;

    public Student() {
        this.setRole(UserType.STUDENT);
    }
}
