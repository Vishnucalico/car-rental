package com.example.application.data.service;

import com.example.application.data.entity.Car;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, UUID> {

}