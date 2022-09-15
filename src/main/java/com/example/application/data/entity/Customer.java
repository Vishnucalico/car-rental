package com.example.application.data.entity;

import javax.persistence.Entity;

@Entity
public class Customer extends AbstractEntity {

    private String customerName;
    private String customerType;

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public String getCustomerType() {
        return customerType;
    }
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

}
