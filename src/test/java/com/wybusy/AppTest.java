package com.wybusy;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Set;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testUser()
    {
        EasyUser.path = "/test";

        // administrator
        // login
        EasyUserBean userBean = EasyUser.login("administrator", "easyUser");
        assertTrue( "Login administrator wrong password;", userBean==null );
        userBean = EasyUser.login("administrator", "EasyUser");
        assertTrue( "Login administrator right password;", userBean.username.equals("administrator") );
        String session = userBean.session;;
        // info
        EasyUserBean user = EasyUser.info(session);
        assertTrue( "Get administrator info;", user == userBean);
        // authority
        assertTrue("administrator authority;", EasyUser.getUserAuthorities(session).size()==1);
        // have authority
        assertTrue("任意权限都应该是true", EasyUser.haveAuthority(session, "any"));
    }
}
