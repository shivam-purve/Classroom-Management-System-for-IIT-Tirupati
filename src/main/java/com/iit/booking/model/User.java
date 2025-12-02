package com.iit.booking.model;

import com.iit.booking.model.enums.UserType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_class", discriminatorType = DiscriminatorType.STRING)
@Table(name = "users")
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    @Enumerated(EnumType.STRING)
    private UserType role;

    // Encapsulation of profile editing logic
    public void updateProfile(String name, String password) {
        if(name != null && !name.isEmpty()) this.name = name;
        if(password != null && !password.isEmpty()) this.password = password;
    }
}
