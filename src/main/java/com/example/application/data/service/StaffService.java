package com.example.application.data.service;

import com.example.application.data.entity.Staff;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StaffService {

    private final StaffRepository repository;

    @Autowired
    public StaffService(StaffRepository repository) {
        this.repository = repository;
    }

    public Optional<Staff> get(UUID id) {
        return repository.findById(id);
    }

    public Staff update(Staff entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Staff> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
