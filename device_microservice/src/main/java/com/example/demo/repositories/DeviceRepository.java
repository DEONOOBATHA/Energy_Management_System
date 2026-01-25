package com.example.demo.repositories;

import com.example.demo.entities.Device;
import com.example.demo.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<Device> findByName(String name);

    /**
     * Example: Custom query
     */
    @Query(value = "SELECT p " +
            "FROM Device p " +
            "WHERE p.name = :name " +
            "AND p.maxConsValue >= 60  ")
    Optional<Device> findDeviceByName(@Param("name") String name);

}
