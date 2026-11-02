package com.campus.recycle.service;

import com.campus.recycle.TestDatabaseSupport;
import com.campus.recycle.dao.UserDao;
import com.campus.recycle.exception.DuplicateResourceException;
import com.campus.recycle.exception.ValidationException;
import com.campus.recycle.model.User;
import com.campus.recycle.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest extends TestDatabaseSupport {

    private final UserService userService = new UserService(new UserDao());

    @Test
    void registerStoresHashedPassword() {
        User user = userService.register("alice", "pass123", "爱丽丝", "alice@x.com");
        assertEquals("alice", user.getUsername());
        // 密码不得明文存储
        assertNotEquals("pass123", user.getPasswordHash());
        assertTrue(PasswordUtil.verify("pass123", user.getSalt(), user.getPasswordHash()));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        userService.register("bob", "pass123", null, null);
        assertThrows(DuplicateResourceException.class,
                () -> userService.register("bob", "other123", null, null));
    }

    @Test
    void registerRejectsInvalidUsername() {
        assertThrows(ValidationException.class,
                () -> userService.register("ab", "pass123", null, null));
    }

    @Test
    void registerRejectsWeakPassword() {
        assertThrows(ValidationException.class,
                () -> userService.register("carol", "123456", null, null));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        userService.register("dave", "pass123", null, null);
        User loggedIn = userService.login("dave", "pass123");
        assertEquals("dave", loggedIn.getUsername());
    }

    @Test
    void loginRejectsWrongPassword() {
        userService.register("eve", "pass123", null, null);
        assertThrows(ValidationException.class, () -> userService.login("eve", "wrong123"));
    }

    @Test
    void loginRejectsUnknownUsername() {
        assertThrows(ValidationException.class, () -> userService.login("ghost", "pass123"));
    }
}
