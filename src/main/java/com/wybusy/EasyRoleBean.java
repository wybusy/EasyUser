package com.wybusy;

public class EasyRoleBean {
    public String roleName;
    public String description;
    public String authority; //可以是多个权限，用逗号分隔
    public String moreInfoJson;

    public EasyRoleBean(String roleName, String description, String authority, String moreInfoJson) {
        this.roleName = roleName;
        this.description = description;
        this.authority = authority;
        this.moreInfoJson = moreInfoJson;
    }
}
