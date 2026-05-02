package com.ecoshop.notification.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.notification.service.dto.NotificationDtos.*;
import com.ecoshop.notification.service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Internal — other services can call this directly to dispatch a notification. */
    @PostMapping("/send")
    public ApiResponse<NotificationResponse> send(@Valid @RequestBody SendRequest req) {
        return ApiResponse.ok(notificationService.send(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.getById(id));
    }
}
