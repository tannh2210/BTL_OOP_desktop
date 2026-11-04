package com.nhom5.eventticket;

import com.nhom5.eventticket.model.Customer;
import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.repository.DemoData;
import com.nhom5.eventticket.repository.InMemoryStore;
import com.nhom5.eventticket.service.AuthService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @Test
    void registrationRejectsDuplicateUsernameAndEmailCaseInsensitively() {
        AuthService auth = new AuthService(DemoData.create());
        assertThrows(IllegalArgumentException.class,
                () -> auth.registerCustomer(" CUSTOMER ", "abcdef", "Test", "new@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> auth.registerCustomer("new-user", "abcdef", "Test", " CUSTOMER@EXAMPLE.LOCAL "));

        Customer created = auth.registerCustomer("new-user", "abcdef", "Test", "  New@Example.com  ");
        assertEquals("New@Example.com", created.getEmail());
    }

    @Test
    void concurrentRegistrationsCannotClaimTheSameEmail() throws Exception {
        AuthService auth = new AuthService(new InMemoryStore());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = pool.submit(() -> tryRegister(auth, "first", start));
            Future<Boolean> second = pool.submit(() -> tryRegister(auth, "second", start));
            start.countDown();
            assertNotEquals(first.get(), second.get(), "Exactly one registration should claim the email");
        } finally {
            pool.shutdownNow();
        }
    }

    private static boolean tryRegister(AuthService auth, String username, CountDownLatch start) throws Exception {
        start.await();
        try {
            auth.registerCustomer(username, "abcdef", "Test", "shared@example.com");
            return true;
        } catch (IllegalArgumentException duplicate) {
            return false;
        }
    }

    @Test
    void profileEmailIsUniqueButCurrentEmailCanBeSaved() {
        AuthService auth = new AuthService(DemoData.create());
        User customer = auth.login("customer", "123456");
        auth.updateProfile(customer, "Tên mới", " CUSTOMER@example.local ");
        assertEquals("CUSTOMER@example.local", customer.getEmail());
        assertThrows(IllegalArgumentException.class,
                () -> auth.updateProfile(customer, "Tên mới", " ADMIN@EXAMPLE.LOCAL "));
    }

    @Test
    void registrationRejectsBoundaryWhitespaceAndShortPasswordsButLegacyPasswordStillWorks() {
        AuthService auth = new AuthService(DemoData.create());
        assertThrows(IllegalArgumentException.class,
                () -> auth.registerCustomer("one", " abcdef", "Test", "one@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> auth.registerCustomer("two", "abcde", "Test", "two@example.com"));
        assertNotNull(auth.login("customer", "123456"));
    }

    @Test
    void passwordChangeValidatesAndKeepsExistingSessionUser() {
        InMemoryStore store = DemoData.create();
        AuthService auth = new AuthService(store);
        User user = auth.login("customer", "123456");

        assertThrows(IllegalArgumentException.class, () -> auth.changePassword(user, "wrong", "abcdef"));
        assertThrows(IllegalArgumentException.class, () -> auth.changePassword(user, "123456", "abcdef "));
        assertThrows(IllegalArgumentException.class, () -> auth.changePassword(user, "123456", "abcde"));
        assertThrows(IllegalArgumentException.class, () -> auth.changePassword(user, "123456", "123456"));
        auth.changePassword(user, "123456", "newpass");
        assertSame(user, auth.login("customer", "newpass"));
        assertThrows(IllegalArgumentException.class, () -> auth.login("customer", "123456"));
    }

    @Test
    void legacyPasswordWithBoundaryWhitespaceRemainsAnExactMatch() {
        InMemoryStore store = new InMemoryStore();
        Customer legacy = new Customer("legacy", "legacy", "legacy-user", " oldpass ", "Legacy", "legacy@example.com");
        store.saveUser(legacy);
        AuthService auth = new AuthService(store);
        assertSame(legacy, auth.login("legacy-user", " oldpass "));
        assertThrows(IllegalArgumentException.class, () -> auth.login("legacy-user", "oldpass"));
    }
}
