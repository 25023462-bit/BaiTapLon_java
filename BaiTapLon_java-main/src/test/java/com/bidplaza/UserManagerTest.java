package com.bidplaza;

import com.bidplaza.exception.AuthenticationException;
import com.bidplaza.manager.UserManager;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.Seller;
import com.bidplaza.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests cho UserManager.
 *
 * Dùng reflection để reset singleton instance trước mỗi test,
 * đảm bảo mỗi test chạy độc lập với trạng thái sạch.
 */
class UserManagerTest {

    /**
     * Reset UserManager singleton trước mỗi test.
     * Vì UserManager dùng Singleton pattern, cần dùng reflection
     * để set field `instance` về null, buộc getInstance() tạo mới.
     */
    @BeforeEach
    void resetUserManagerInstance() throws Exception {
        Field instanceField = UserManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    // -------------------------------------------------------------------------
    // PHẦN 1: Đăng ký user
    // -------------------------------------------------------------------------

    /**
     * Test 1: Đăng ký user mới với thông tin hợp lệ → thành công, trả về User.
     */
    @Test
    void register_newUser_shouldSucceed() throws AuthenticationException {
        UserManager manager = UserManager.getInstance();

        User user = manager.register("alice", "password123", "BIDDER");

        assertNotNull(user);
        assertEquals("alice", user.getUsername());
    }

    /**
     * Test 2: Đăng ký trùng username → ném AuthenticationException.
     */
    @Test
    void register_duplicateUsername_shouldThrowException() throws AuthenticationException {
        UserManager manager = UserManager.getInstance();
        manager.register("alice", "password123", "BIDDER");

        assertThrows(AuthenticationException.class, () ->
                manager.register("alice", "otherpass", "SELLER")
        );
    }

    // -------------------------------------------------------------------------
    // PHẦN 2: Đăng nhập
    // -------------------------------------------------------------------------

    /**
     * Test 3: Đăng nhập đúng username + password → trả về User.
     */
    @Test
    void login_correctPassword_shouldReturnUser() throws AuthenticationException {
        UserManager manager = UserManager.getInstance();
        manager.register("bob", "securePass", "BIDDER");

        User loggedIn = manager.login("bob", "securePass");

        assertNotNull(loggedIn);
        assertEquals("bob", loggedIn.getUsername());
    }

    /**
     * Test 4: Đăng nhập sai password → ném AuthenticationException.
     */
    @Test
    void login_wrongPassword_shouldThrowException() throws AuthenticationException {
        UserManager manager = UserManager.getInstance();
        manager.register("bob", "securePass", "BIDDER");

        assertThrows(AuthenticationException.class, () ->
                manager.login("bob", "wrongPass")
        );
    }

    /**
     * Test 5: Đăng nhập với username không tồn tại → ném AuthenticationException.
     */
    @Test
    void login_nonExistentUser_shouldThrowException() {
        UserManager manager = UserManager.getInstance();

        assertThrows(AuthenticationException.class, () ->
                manager.login("ghost", "anyPass")
        );
    }

    // -------------------------------------------------------------------------
    // PHẦN 3: Kiểm tra role trả về
    // -------------------------------------------------------------------------

    /**
     * Test 6: Đăng ký với role BIDDER → trả về instance của Bidder.
     */
    @Test
    void register_bidderRole_shouldReturnBidder() throws AuthenticationException {
        UserManager manager = UserManager.getInstance();

        User user = manager.register("carol", "pass", "BIDDER");

        assertInstanceOf(Bidder.class, user);
        assertEquals("BIDDER", user.getRole());
    }

    /**
     * Test 7: Đăng ký với role SELLER → trả về instance của Seller.
     */
    @Test
    void register_sellerRole_shouldReturnSeller() throws AuthenticationException {
        UserManager manager = UserManager.getInstance();

        User user = manager.register("dave", "pass", "SELLER");

        assertInstanceOf(Seller.class, user);
        assertEquals("SELLER", user.getRole());
    }
}
