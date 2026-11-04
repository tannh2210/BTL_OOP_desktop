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
        String normalizedEmail = email.trim();
        if (!validEmail(normalizedEmail)) throw new IllegalArgumentException("Email không hợp lệ.");
        synchronized (store) {
            String normalizedUsername = username.trim();
            boolean duplicate = store.allUsers().stream()
                    .anyMatch(u -> u.getUsername().equalsIgnoreCase(normalizedUsername));
            if (duplicate) throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
            String id = UUID.randomUUID().toString();
            Customer customer = new Customer(id, id, normalizedUsername, password, fullName.trim(), normalizedEmail);
            store.saveUser(customer);
            return customer;
        }
    }
    public User login(String username, String password) {
        if (blank(username) || blank(password))
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        String normalizedUsername = username.trim();
        return store.allUsers().stream()
          .filter(u -> u.getUsername().equalsIgnoreCase(normalizedUsername) && u.matchesPassword(password))
          .findFirst().orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng."));
    }
    public void logout(User user) { /* Session state is held by AppContext. */ }
    public void updateProfile(User user, String name, String email) {
        if (blank(name) || blank(email)) throw new IllegalArgumentException("Thông tin không được để trống.");
        if (user == null) throw new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập.");
        String normalizedEmail = email.trim();
        if (!validEmail(normalizedEmail)) throw new IllegalArgumentException("Email không hợp lệ.");
        user.setFullName(name.trim());
        user.setEmail(normalizedEmail);
    }
    private static boolean validEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
