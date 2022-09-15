package com.example.application.data.entity;

import javax.persistence.Entity;

@Entity
public class Car extends AbstractEntity {

    private String carName;
    private String carType;

    public String getCarName() {
        return carName;
    }
    public void setCarName(String carName) {
        this.carName = carName;
    }
    public String getCarType() {
        return carType;
    }
    public void setCarType(String carType) {
        this.carType = carType;
    }

}
