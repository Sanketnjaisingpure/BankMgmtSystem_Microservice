package com.bank.feign;

import com.bank.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "NOTIFICATION-SERVICE",url = "http://localhost:8082")
public interface NotificationFeignService {

    @PostMapping("/api/v1/notifications/account-create-notification")
    void sendNotification(@RequestBody NotificationRequestDTO notificationRequestDTO);

}
