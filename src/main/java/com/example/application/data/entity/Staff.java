package com.example.application.data.entity;

import javax.persistence.Entity;

@Entity
public class Staff extends AbstractEntity {

    private String staffName;

    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

}
