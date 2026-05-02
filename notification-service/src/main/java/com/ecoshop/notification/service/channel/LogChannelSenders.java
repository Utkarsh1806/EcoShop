package com.ecoshop.notification.service.channel;

import com.ecoshop.notification.service.domain.Channel;
import com.ecoshop.notification.service.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Dev default: logs notifications to stdout instead of actually sending. Replace with real
 * implementations for each channel before going to production.
 */
@Configuration
public class LogChannelSenders {

    private static final Logger log = LoggerFactory.getLogger("NOTIFICATION");

    @Component
    public static class EmailLogSender implements ChannelSender {
        @Override public Channel channel() { return Channel.EMAIL; }
        @Override public String send(Notification n) {
            log.info("[EMAIL] to={} subject='{}' body='{}'", n.getRecipient(), n.getSubject(),
                     truncate(n.getBody()));
            return "log-" + UUID.randomUUID();
        }
    }

    @Component
    public static class SmsLogSender implements ChannelSender {
        @Override public Channel channel() { return Channel.SMS; }
        @Override public String send(Notification n) {
            log.info("[SMS] to={} body='{}'", n.getRecipient(), truncate(n.getBody()));
            return "log-" + UUID.randomUUID();
        }
    }

    @Component
    public static class PushLogSender implements ChannelSender {
        @Override public Channel channel() { return Channel.PUSH; }
        @Override public String send(Notification n) {
            log.info("[PUSH] to={} title='{}' body='{}'", n.getRecipient(), n.getSubject(),
                     truncate(n.getBody()));
            return "log-" + UUID.randomUUID();
        }
    }

    @Component
    public static class WhatsAppLogSender implements ChannelSender {
        @Override public Channel channel() { return Channel.WHATSAPP; }
        @Override public String send(Notification n) {
            log.info("[WHATSAPP] to={} body='{}'", n.getRecipient(), truncate(n.getBody()));
            return "log-" + UUID.randomUUID();
        }
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
