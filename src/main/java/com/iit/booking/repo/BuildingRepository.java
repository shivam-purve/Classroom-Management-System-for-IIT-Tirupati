package com.iit.booking.repo;
import com.iit.booking.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> { }
