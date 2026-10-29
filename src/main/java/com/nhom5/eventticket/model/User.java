package com.nhom5.eventticket.model;

/** UML M3: User has Customer and Admin subclasses.*/
public class User {
    private final String userId;
    private final String username;
    private String password; // M3 field; NEVER print or commit real passwords. Prototype only: password hashing is not implemented.
    private String fullName;
    private String email;

    public User(String userId, String username, String password, String fullName, String email) {
        this.userId = java.util.Objects.requireNonNull(userId);
        this.username = java.util.Objects.requireNonNull(username);
        this.password = java.util.Objects.requireNonNull(password);
        this.fullName = java.util.Objects.requireNonNull(fullName);
        this.email = java.util.Objects.requireNonNull(email);
    }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean matchesPassword(String input) { return password.equals(input); }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public void setFullName(String fullName) { this.fullName = java.util.Objects.requireNonNull(fullName); }
    public void setEmail(String email) { this.email = java.util.Objects.requireNonNull(email); }

    // Preserve M3 no-argument methods. Real operations will be orchestrated by AuthService.
    public boolean login() { throw new UnsupportedOperationException("Chưa hỗ trợ: User.login / AuthService"); }
    public void logout() { throw new UnsupportedOperationException("Chưa hỗ trợ: User.logout / AuthService"); }
    public void updateProfile() { throw new UnsupportedOperationException("Chưa hỗ trợ: User.updateProfile / AuthService"); }
}
