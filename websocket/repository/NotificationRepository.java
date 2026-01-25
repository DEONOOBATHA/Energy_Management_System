package com.sscl.websocket_service.repository;

import com.sscl.websocket_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

        @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.timestamp DESC")
        List<Notification> findByUserIdOrderByTimestampDesc(@Param("userId") UUID userId);

}
