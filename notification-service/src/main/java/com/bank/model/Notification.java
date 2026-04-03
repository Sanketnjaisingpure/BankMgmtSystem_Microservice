package com.bank.model;

import com.bank.ENUM.ChannelType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private ChannelType channelType;

    private String message;

    private LocalDateTime sentAt;

    @Column(name = "retry_count")
    private int retryCount=0;

    private String status;
}
