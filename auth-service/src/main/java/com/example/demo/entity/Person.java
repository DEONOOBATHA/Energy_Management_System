package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;
    @Column(unique = true, nullable = false, name = "username")
    private String username;
    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "person_roles", 
                     joinColumns = @JoinColumn(name = "person_id", nullable = false))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();
}
