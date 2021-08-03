package com.wybusy;

public class EasyUserBean {
    public String uuid;
    public String username;
    public String password;
    public String role; //可以是多个角色，用逗号分隔
    public String authority; //可以是多个权限，用逗号分隔
    public String realname;
    public String moreInfoJson;
    public String session;

    public EasyUserBean(String username, String password, String role, String realname, String moreInfoJson){
        this.username = username;
        this.password = password;
        this.role = role;
        this.realname = realname;
        this.moreInfoJson = moreInfoJson;
    }
}
