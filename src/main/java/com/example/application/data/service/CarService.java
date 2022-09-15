package com.example.application.data.service;

import com.example.application.data.entity.Car;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CarService {

    private final CarRepository repository;

    @Autowired
    public CarService(CarRepository repository) {
        this.repository = repository;
    }

    public Optional<Car> get(UUID id) {
        return repository.findById(id);
    }

    public Car update(Car entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Car> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
