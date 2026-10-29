package com.nhom5.eventticket.service;
import com.nhom5.eventticket.model.*;
import com.nhom5.eventticket.repository.InMemoryStore;
import java.util.UUID;
/** Simple local sign-in/register flow. Not production authentication. */
public class AuthService {
    private final InMemoryStore store;
    public AuthService(InMemoryStore store) { this.store = store; }
    public Customer registerCustomer(String username, String password, String fullName, String email) {
        if (blank(username) || blank(fullName) || blank(email) || blank(password))
            throw new IllegalArgumentException("Vui lòng nhập đủ thông tin.");
        if (password.length() < 6) throw new IllegalArgumentException("Mật khẩu cần ít nhất 6 ký tự.");
        if (!email.contains("@")) throw new IllegalArgumentException("Email không hợp lệ.");
        synchronized (store) {
            boolean duplicate = store.allUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
            if (duplicate) throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
            String id = UUID.randomUUID().toString();
            Customer customer = new Customer(id, id, username.trim(), password, fullName.trim(), email.trim());
            store.saveUser(customer);
            return customer;
        }
    }
    public User login(String username, String password) {
        return store.allUsers().stream()
          .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()) && u.matchesPassword(password))
          .findFirst().orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng."));
    }
    public void logout(User user) { /* Session state is held by AppContext. */ }
    public void updateProfile(User user, String name, String email) {
        if (blank(name) || blank(email)) throw new IllegalArgumentException("Thông tin không được để trống.");
        user.setFullName(name.trim()); user.setEmail(email.trim());
    }
    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
