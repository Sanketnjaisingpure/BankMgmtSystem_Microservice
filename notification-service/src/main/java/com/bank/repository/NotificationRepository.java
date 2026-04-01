package com.bank.repository;

import com.bank.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification , UUID> {

    @Query("SELECT n FROM Notification n WHERE n.status = :status")
    List<Notification> findByStatus(String status);
}
