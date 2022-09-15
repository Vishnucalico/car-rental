package com.example.application.data.service;

import com.example.application.data.entity.Staff;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, UUID> {

}