package com.wybusy;

import org.junit.Test;

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
        assertTrue("administrator获得authority，应该是全部（目前只有1个）;", EasyUser.getUserAuthorities(userBean).equals("easyAdmin"));
        assertTrue("administrator任意权限都应该是true", EasyUser.haveAuthority(userBean, "any"));
        assertTrue("administrator加一个权限", EasyUser.addAuthority(userBean, "staff", "员工权限", "{}"));
        assertTrue("administrator加一个角色", EasyUser.addRole(userBean, "staff", "员工角色", "easyAdmin,staff", "{}").roleName.equals("staff"));
        assertTrue("administrator加一个用户", EasyUser.addUser(userBean, "staff", "staff", "easyUser,staff", "张三", "{}").realname.equals("张三"));
        assertTrue("administrator改密码", EasyUser.changePassWord(userBean, "EasyUser", "easyUser"));
        assertTrue("adminstrator登出", EasyUser.logout(session));
        // staff
        userBean = EasyUser.login("staff", "staff");
        assertTrue("staff登录，返回EasyUserBean;", userBean.username.equals("staff"));
        session = userBean.session;
        assertSame("通过session获得EasyUserBean;", EasyUser.info(session), userBean);
        assertEquals("staff获得authority，应该是全部（目前有2个）;", EasyUser.getUserAuthorities(userBean).split(",").length, 2);
        assertTrue("staff的easyUser权限都应该是true", EasyUser.haveAuthority(userBean, "easyAdmin"));
        assertTrue("staff删一个权限", EasyUser.delAuthority(userBean, "staff"));
        assertTrue("staff删一个角色", EasyUser.delRole(userBean, "staff"));
        assertTrue("staff登出", EasyUser.logout(session));
        // administrators
        userBean = EasyUser.login("administrator", "easyUser");
        assertTrue("administrator用正确的密码登录，返回EasyUserBean;", userBean.username.equals("administrator"));
        session = userBean.session;
        assertTrue("administrator删staff用户", EasyUser.delUser(userBean, "staff"));
        assertTrue("administrator改密码", EasyUser.changePassWord(userBean, "easyUser", "EasyUser"));
        assertTrue("adminstrator登出", EasyUser.logout(session));
    }
}
