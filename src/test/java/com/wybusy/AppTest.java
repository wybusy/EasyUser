package com.wybusy;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testUser() {
        EasyUser.path = "/test";

        // administrator
        EasyUserBean userBean = EasyUser.login("administrator", "easyUser");
        assertNull("administrator用错误的密码登录，返回null;", userBean);
        userBean = EasyUser.login("administrator", "EasyUser");
        assertTrue("administrator用正确的密码登录，返回EasyUserBean;", userBean.username.equals("administrator"));
        String session = userBean.session;
        assertSame("通过session获得EasyUserBean;", EasyUser.info(session), userBean);
        assertEquals("administrator获得authority，应该是全部（目前只有1个）;", EasyUser.getUserAuthorities(session).size(), 1);
        assertTrue("administrator任意权限都应该是true", EasyUser.haveAuthority(session, "any"));
        assertTrue("administrator加一个权限", EasyUser.addAuthority(session, "staff", "员工权限", "{}"));
        assertTrue("administrator加一个角色", EasyUser.addRole(session, "staff", "员工角色", "easyAdmin,staff", "{}"));
        assertTrue("administrator加一个用户", EasyUser.addUser(session, "staff", "staff", "easyUser,staff", "张三", "{}"));
        assertTrue("administrator改密码", EasyUser.changePassWord(session, "EasyUser", "easyUser"));
        assertTrue("adminstrator登出", EasyUser.logout(session));
        // staff
        userBean = EasyUser.login("staff", "staff");
        assertTrue("staff登录，返回EasyUserBean;", userBean.username.equals("staff"));
        session = userBean.session;
        assertSame("通过session获得EasyUserBean;", EasyUser.info(session), userBean);
        assertEquals("staff获得authority，应该是全部（目前有2个）;", EasyUser.getUserAuthorities(session).size(), 2);
        assertTrue("staff的easyUser权限都应该是true", EasyUser.haveAuthority(session, "easyAdmin"));
        assertTrue("staff删一个权限", EasyUser.delAuthority(session, "staff"));
        assertTrue("staff删一个角色", EasyUser.delRole(session, "staff"));
        assertTrue("staff登出", EasyUser.logout(session));
        // administrators
        userBean = EasyUser.login("administrator", "easyUser");
        assertTrue("administrator用正确的密码登录，返回EasyUserBean;", userBean.username.equals("administrator"));
        session = userBean.session;
        assertTrue("administrator删staff用户", EasyUser.delUser(session, "staff"));
        assertTrue("administrator改密码", EasyUser.changePassWord(session, "easyUser", "EasyUser"));
        assertTrue("adminstrator登出", EasyUser.logout(session));
    }
}
