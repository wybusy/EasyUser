package cn.sh.wy;

import java.util.Date;

public class EasySessionBean {
    public String session;
    public String username;
    public Long loginTime;
    public Long lastVisitTime;

    public EasySessionBean(String username, String session) {
        this.session = session;
        this.username = username;
        this.loginTime = new Date().getTime();
        this.lastVisitTime = this.loginTime;
    }
}
