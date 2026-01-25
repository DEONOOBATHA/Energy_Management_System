package com.example.demo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.apache.catalina.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_device")
public class UserDevice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Person user;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    public UserDevice() {}

    public UserDevice(Person user, Device device) {
        this.user = user;
        this.device = device;
    }

    // Getters și setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Person getUser() { return user; }
    public void setUser(Person user) { this.user = user; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
}
