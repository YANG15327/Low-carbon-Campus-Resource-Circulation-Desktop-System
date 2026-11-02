package com.campus.recycle.service;

import com.campus.recycle.dao.UserDao;
import com.campus.recycle.exception.DuplicateResourceException;
import com.campus.recycle.exception.NotFoundException;
import com.campus.recycle.exception.ValidationException;
import com.campus.recycle.model.User;
import com.campus.recycle.model.UserRole;
import com.campus.recycle.util.PasswordUtil;
import com.campus.recycle.util.Validator;

import java.util.Optional;

/**
 * 用户业务逻辑：注册、登录、资料维护与密码修改。
 *
 * <p>负责输入合法性校验、密码加密、重复用户名检测等业务规则，保证写入持久层
 * 的数据合法且安全。</p>
 */
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /** 注册普通学生用户。 */
    public User register(String username, String password, String nickname, String email) {
        return register(username, password, nickname, email, UserRole.STUDENT);
    }

    /** 注册管理员账号（用于系统初始化）。 */
    public User registerAdmin(String username, String password) {
        return register(username, password, null, null, UserRole.ADMIN);
    }

    private User register(String username, String password, String nickname, String email, UserRole role) {
        Validator.requireUsername(username);
        Validator.requirePassword(password);
        if (userDao.existsByUsername(username)) {
            throw new DuplicateResourceException("用户名已存在: " + username);
        }
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);
        User user = new User(null, username, hash, salt, role, 100, nickname, email, null);
        int id = userDao.insert(user);
        return userDao.findById(id).orElseThrow(() -> new NotFoundException("用户创建失败"));
    }

    /** 登录：校验用户名与密码，返回用户实体。 */
    public User login(String username, String password) {
        Validator.requireNotBlank(username, "用户名");
        Validator.requireNotBlank(password, "密码");
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ValidationException("用户名或密码错误"));
        if (!PasswordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {
            throw new ValidationException("用户名或密码错误");
        }
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public Optional<User> findById(int id) {
        return userDao.findById(id);
    }

    /** 更新资料（昵称、邮箱）。 */
    public User updateProfile(int userId, String nickname, String email) {
        requireExist(userId);
        userDao.updateProfile(userId, nickname, email);
        return userDao.findById(userId).orElseThrow();
    }

    /** 修改密码：校验旧密码后更新。 */
    public void changePassword(int userId, String oldPassword, String newPassword) {
        User user = requireExist(userId);
        if (!PasswordUtil.verify(oldPassword, user.getSalt(), user.getPasswordHash())) {
            throw new ValidationException("旧密码不正确");
        }
        Validator.requirePassword(newPassword);
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPassword, salt);
        userDao.updatePassword(userId, hash, salt);
    }

    private User requireExist(int userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在: id=" + userId));
    }
}
