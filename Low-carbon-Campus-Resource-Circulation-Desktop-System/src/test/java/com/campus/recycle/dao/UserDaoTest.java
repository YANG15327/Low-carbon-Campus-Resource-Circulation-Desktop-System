package com.campus.recycle.dao;

import com.campus.recycle.TestDatabaseSupport;
import com.campus.recycle.model.User;
import com.campus.recycle.model.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDaoTest extends TestDatabaseSupport {

    private final UserDao userDao = new UserDao();

    private User newUser(String username) {
        return new User(null, username, "hash", "salt", UserRole.STUDENT, 100,
                "昵称", "a@b.com", null);
    }

    @Test
    void insertAndFindById() {
        int id = userDao.insert(newUser("alice"));
        assertTrue(id > 0);
        User found = userDao.findById(id).orElseThrow();
        assertEquals("alice", found.getUsername());
        assertEquals(UserRole.STUDENT, found.getRole());
        assertEquals(100, found.getCreditScore());
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        assertTrue(userDao.findByUsername("nobody").isEmpty());
    }

    @Test
    void existsByUsername() {
        assertFalse(userDao.existsByUsername("bob"));
        userDao.insert(newUser("bob"));
        assertTrue(userDao.existsByUsername("bob"));
    }

    @Test
    void findAllReturnsAllUsers() {
        userDao.insert(newUser("u1"));
        userDao.insert(newUser("u2"));
        List<User> users = userDao.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void updateProfileChangesNicknameAndEmail() {
        int id = userDao.insert(newUser("carol"));
        userDao.updateProfile(id, "新昵称", "new@b.com");
        User updated = userDao.findById(id).orElseThrow();
        assertEquals("新昵称", updated.getNickname());
        assertEquals("new@b.com", updated.getEmail());
    }

    @Test
    void deleteRemovesUser() {
        int id = userDao.insert(newUser("dave"));
        assertTrue(userDao.delete(id));
        assertTrue(userDao.findById(id).isEmpty());
    }
}
