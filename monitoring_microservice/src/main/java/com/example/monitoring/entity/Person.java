package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String address;

    private Integer age;
}
