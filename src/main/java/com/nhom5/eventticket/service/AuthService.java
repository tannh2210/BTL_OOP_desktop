package com.nhom5.eventticket.service;

import com.nhom5.eventticket.model.Customer;
import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.util.UUID;

/** Simple local sign-in/register flow. Not production authentication. */
public class AuthService {
    private final InMemoryStore store;

    public AuthService(InMemoryStore store) { this.store = store; }

    public Customer registerCustomer(String username, String password, String fullName, String email) {
        if (blank(username) || blank(fullName) || blank(email) || password == null || password.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập đủ thông tin.");
        validateNewPassword(password);
        String normalizedEmail = email.trim();
        if (!validEmail(normalizedEmail)) throw new IllegalArgumentException("Email không hợp lệ.");
        synchronized (store) {
            String normalizedUsername = username.trim();
            boolean duplicateUsername = store.allUsers().stream()
                    .anyMatch(u -> u.getUsername().equalsIgnoreCase(normalizedUsername));
            if (duplicateUsername) throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
            ensureEmailAvailable(normalizedEmail, null);
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
        // Password is deliberately compared exactly; never trim legacy credentials at sign-in.
        return store.allUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(normalizedUsername) && u.matchesPassword(password))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng."));
    }

    public void logout(User user) { /* Session state is held by AppContext. */ }

    public void updateProfile(User user, String name, String email) {
        if (user == null) throw new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập.");
        if (blank(name) || blank(email)) throw new IllegalArgumentException("Thông tin không được để trống.");
        String normalizedEmail = email.trim();
        if (!validEmail(normalizedEmail)) throw new IllegalArgumentException("Email không hợp lệ.");
        synchronized (store) {
            ensureEmailAvailable(normalizedEmail, user);
            user.setFullName(name.trim());
            user.setEmail(normalizedEmail);
        }
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (user == null) throw new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập.");
        if (currentPassword == null || currentPassword.isEmpty())
            throw new IllegalArgumentException("Nhập mật khẩu hiện tại.");
        if (!user.matchesPassword(currentPassword))
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        validateNewPassword(newPassword);
        if (user.matchesPassword(newPassword))
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        synchronized (store) {
            // Recheck while holding the shared store lock before mutating account state.
            if (!user.matchesPassword(currentPassword))
                throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
            user.changePassword(currentPassword, newPassword);
        }
    }

    private void ensureEmailAvailable(String email, User excludedUser) {
        boolean duplicate = store.allUsers().stream()
                .filter(user -> excludedUser == null || !user.getUserId().equals(excludedUser.getUserId()))
                .anyMatch(user -> user.getEmail().trim().equalsIgnoreCase(email));
        if (duplicate) throw new IllegalArgumentException("Email đã được sử dụng.");
    }

    private static void validateNewPassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu.");
        if (hasBoundaryWhitespace(password))
            throw new IllegalArgumentException("Mật khẩu không được có khoảng trắng ở đầu hoặc cuối.");
        if (password.length() < 6)
            throw new IllegalArgumentException("Mật khẩu cần ít nhất 6 ký tự.");
    }

    private static boolean hasBoundaryWhitespace(String value) {
        return !value.isEmpty() && (isWhitespace(value.charAt(0)) || isWhitespace(value.charAt(value.length() - 1)));
    }

    private static boolean isWhitespace(char value) {
        return Character.isWhitespace(value) || Character.isSpaceChar(value);
    }

    private static boolean validEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
