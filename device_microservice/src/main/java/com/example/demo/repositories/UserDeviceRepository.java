package com.example.demo.repositories;

import com.example.demo.entities.Device;
import com.example.demo.entities.Person;
import com.example.demo.entities.UserDevice;
import org.hibernate.sql.exec.spi.AbstractJdbcOperationQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    Optional<UserDevice> findFirstByUserId(UUID userid);

   UserDevice findById(Long id);

    Optional<UserDevice> findByDeviceId(UUID deviceid);


    /**
     * Example: JPA generate query by existing field
     */
    List<UserDevice> findByUserId(UUID userid);


}