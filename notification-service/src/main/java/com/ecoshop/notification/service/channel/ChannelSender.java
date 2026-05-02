package com.ecoshop.notification.service.channel;

import com.ecoshop.notification.service.domain.Channel;
import com.ecoshop.notification.service.domain.Notification;

/**
 * Strategy for delivering a notification on a particular channel.
 * Concrete implementations: SES (email), Twilio/MSG91 (SMS), FCM (push), WhatsApp Business API.
 */
public interface ChannelSender {

    Channel channel();

    /** Send the notification. Returns provider message id on success; throws on failure. */
    String send(Notification notification);
}
