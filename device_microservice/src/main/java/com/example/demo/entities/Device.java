package com.example.demo.entities;

import jakarta.persistence.*;
import org.apache.catalina.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Device  implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "maxConsValue", nullable = false)
    private int maxConsValue;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDevice> userDevices = new ArrayList<>();

    public Device() {

    }

    public Device(String name, int maxConsValue) {
        this.name = name;
        this.maxConsValue = maxConsValue;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxConsValue() {
        return maxConsValue;
    }

    public void setMaxConsValue(int maxConsValue) {
        this.maxConsValue = maxConsValue;
    }
}


