package cn.sh.wy;

public class EasyUserBean {
    public String username;
    public String password;
    public String role;
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
