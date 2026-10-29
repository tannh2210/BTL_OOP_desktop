package com.nhom5.eventticket.service;
import com.nhom5.eventticket.model.*;
import com.nhom5.eventticket.repository.InMemoryStore;
import java.util.List;
/** Local notification listing. Observer subscriptions are not part of this version. */
public class NotificationService {
    private final InMemoryStore store;
    public NotificationService(InMemoryStore store) {this.store=store;}
    public List<Notification> forUser(String userId) {
        return store.allNotifications().stream().filter(n->n.getRecipient().getUserId().equals(userId)).toList();
    }
}
