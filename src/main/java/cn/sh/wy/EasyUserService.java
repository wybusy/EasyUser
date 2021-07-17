package cn.sh.wy;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EasyUserService {
    public static String path = "/publicdata/easyuser";
    /**
     * 最长在线时间为30天
     */
    public static Long maxOnlineTime = 30L * 24 * 60 * 60 * 1000;
    private static final String salt = "user.wy.sh.cn";
    /**
     * 默认管理员 admin/EasyUser
     */
    private static final Map<String, EasyUserBean> userData = new HashMap<>();
    /**
     * 默认两种角色，必然存在：easyAdmin管理员，easyUser用户
     */
    private static final Map<String, EasyRoleBean> roleData = new HashMap<>();
    private static final Map<String, EasySessionBean> sessionData = new HashMap<>();

    private static void mkDirs(File file) {
        File fileParent = file.getParentFile();
        if (!(fileParent.exists() && fileParent.isDirectory())) {
            mkDirs(fileParent);
            fileParent.mkdir();
            try {
                if (!System.getProperty("os.name").toLowerCase().startsWith("win"))
                    Runtime.getRuntime().exec("chmod 755 " + fileParent.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveFile(String path, String fileName, Boolean append, String content) {
        File file = new File(path, fileName);
        if (!(file.exists() && file.isFile())) {
            try {
                mkDirs(file);
                file.createNewFile();
                if (!System.getProperty("os.name").toLowerCase().startsWith("win"))
                    Runtime.getRuntime().exec("chmod 644 " + file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file, append);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            Writer writer = new BufferedWriter(osw);
            writer.write(content);
            writer.flush();
            writer.close();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String path, String fileName) {
        String result = "";
        File file = new File(path, fileName);
        if (file.exists() && file.isFile()) { //判断文件是否存在
            Long fileLength = file.length();
            byte[] fileContent = new byte[fileLength.intValue()];
            try {
                FileInputStream in = new FileInputStream(file);
                in.read(fileContent);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                result = new String(fileContent, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static void saveUser() {
        String content = JSON.toJSONString(userData.values());
        saveFile(path, "user.json", false, content);
        saveFile(path + "/user", (new Date().getTime()) + ".json", false, content);
    }

    private static void saveRole() {
        String content = JSON.toJSONString(roleData.values());
        saveFile(path, "role.json", false, content);
        saveFile(path + "/role", (new Date().getTime()) + ".json", false, content);
    }

    private static void saveSession() {
        String content = JSON.toJSONString(sessionData.values());
        saveFile(path, "session.json", false, content);
    }

    private static String md5(String plainText) {
        byte[] bytes = null;
        try {
            bytes = plainText.getBytes("UTF-8");
        } catch  (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] digesta = null;
        try {
            // 得到一个md5的消息摘要
            MessageDigest alga = MessageDigest.getInstance("MD5");
            // 添加要进行计算摘要的信息
            alga.update(bytes);
            // 得到该摘要
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 将摘要转为字符串
        String hs = "";
        String stmp = "";
        for (int n = 0; n < digesta.length; n++) {
            stmp = (Integer.toHexString(digesta[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    private static String dealPw(String password) {
        return md5(password + salt);
    }

    private static void loadData() {
        if (userData.isEmpty()) {
            String userDataString = readFile(path, "user.json");
            if (userDataString.equals("")) {
                userData.put("administrator", new EasyUserBean("administrator", dealPw("EasyUser"), "easyAdmin", "系统管理员", "{}"));
                saveUser();
            } else {
                List<EasyUserBean> userDataList = JSON.parseArray(userDataString, EasyUserBean.class);
                for (EasyUserBean userBean : userDataList)
                    userData.put(userBean.username, userBean);
            }
        }
        if (roleData.isEmpty()) {
            String roleDataString = readFile(path, "role.json");
            if (roleDataString.equals("")) {
                roleData.put("easyAdmin", new EasyRoleBean("easyAdmin", "默认管理员", "{}"));
                roleData.put("easyUser", new EasyRoleBean("easyUser", "默认用户", "{}"));
                saveRole();
            } else {
                List<EasyRoleBean> roleDataList = JSON.parseArray(roleDataString, EasyRoleBean.class);
                for (EasyRoleBean roleBean : roleDataList)
                    roleData.put(roleBean.roleName, roleBean);
            }
        }
        if (sessionData.isEmpty()) {
            String sessionDataString = readFile(path, "session.json");
            if(!sessionDataString.equals("")) {
                List<EasySessionBean> sessionDataList = JSON.parseArray(sessionDataString, EasySessionBean.class);
                for(EasySessionBean sessionBean: sessionDataList)
                    sessionData.put(sessionBean.session, sessionBean);
            }
        }
    }

    private static Map<String, EasyUserBean> getUserData() {
        loadData();
        return userData;
    }

    private static Map<String, EasyRoleBean> getRoleData() {
        loadData();
        return roleData;
    }

    private static Map<String, EasySessionBean> getSessionData() {
        loadData();
        return sessionData;
    }

    public static EasyUserBean login(String username, String password) {
        EasyUserBean result = null;
        Map<String, EasyUserBean> users = getUserData();
        if (users.containsKey(username) && users.get(username).password.equals(dealPw(password))) {
            Map<String, EasySessionBean> sessionBeanMap = getSessionData();
            for (String key : sessionBeanMap.keySet())
                if (new Date().getTime() - sessionBeanMap.get(key).loginTime > maxOnlineTime) sessionBeanMap.remove(key);
            result = users.get(username);
            result.session = md5(username + System.currentTimeMillis());
            sessionBeanMap.put(result.session, new EasySessionBean(username, result.session));
            saveSession();
        }
        return result;
    }

    /**
     * 根据session取得userBean。当用户被删除后，用户即被登录失效
     */
    public static EasyUserBean info(String session) {
        EasyUserBean result = null;
        Map<String, EasyUserBean> users = getUserData();
        Map<String, EasySessionBean> sessionBeanMap = getSessionData();
        if (sessionBeanMap.containsKey(session) // session仍存在，没有被强制下线删除
                && new Date().getTime() - sessionBeanMap.get(session).loginTime < maxOnlineTime // 自登录起最大天数内
                && users.containsKey(sessionBeanMap.get(session).username)) { // 防止用户已经被删掉
            result = users.get(sessionBeanMap.get(session).username);
        }
        return result;
    }

    public static boolean logout(String session) {
        boolean result = false;
        Map<String, EasySessionBean> sessionBeanMap = getSessionData();
        if (sessionBeanMap.containsKey(session)) { // 防止用户已经被删掉
            sessionBeanMap.remove(session);
            result = true;
        }
        return result;
    }

    public static boolean changePassWord(String session, String oldPassword, String newPassword){
        boolean result = false;
        EasyUserBean userBean = info(session);
        if(!(userBean == null || newPassword == null || newPassword.equals("")) && userBean.password.equals(dealPw(oldPassword))){
            userBean.password = dealPw(newPassword);
            saveUser();
            result = true;
        }
        return result;
    }

    private static boolean isAdmin(String session) {
        boolean result = false;
        EasyUserBean userBean = info(session);
        if (userBean != null && userBean.role.equals("easyAdmin")) {
            result = true;
        }
        return result;
    }

    public static Map<String, EasyUserBean> getUserList(String session) {
        Map<String, EasyUserBean> result = null;
        if (isAdmin(session)) {
            result = getUserData();
        }
        return result;
    }

    public static Map<String, EasyRoleBean> getRoleList(String session) {
        Map<String, EasyRoleBean> result = null;
        if (isAdmin(session)) {
            result = getRoleData();
        }
        return result;
    }

    public static boolean addUser(String session, String username, String password, String role, String realname, String moreInfoJson) {
        boolean result = false;
        if (isAdmin(session) && !getUserData().containsKey(username) && getRoleData().containsKey(role)) {
            getUserData().put(username, new EasyUserBean(username, dealPw(password), role, realname, moreInfoJson));
            saveUser();
            result = true;
        }
        return result;
    }

    public static boolean modifyUser(String session, String username, String password, String role, String realname, String moreInfoJson) {
        boolean result = false;
        if (isAdmin(session)
                && !info(session).username.equals(username) //自己不能通过用户列表修改自己的信息
                && getUserData().containsKey(username)
                && (role == null || getRoleData().containsKey(role))) {
            EasyUserBean userBean = getUserData().get(username);
            if (!(password == null || password.equals(""))) userBean.password = dealPw(password);
            if (role != null && !username.equals("administrator")) userBean.role = role;
            if (realname != null) userBean.realname = realname;
            if (moreInfoJson != null) userBean.moreInfoJson = moreInfoJson;
            saveUser();
            result = true;
        }
        return result;
    }

    public static boolean deleteUser(String session, String username) {
        boolean result = false;
        if (isAdmin(session)
                && !info(session).username.equals(username) //自己不能通过用户列表删除自己
                && getUserData().containsKey(username) && !username.equals("administrator")) {
            getUserData().remove(username);
            saveUser();
            result = true;
        }
        return result;
    }

    /**
     * 批量添加用户,返回因无权限或用户重复而未能成功添加的用户
     */
    public static List<EasyUserBean> addUsers(String session, List<EasyUserBean> userList) {
        List<EasyUserBean> result = new ArrayList<>();
        boolean save = false;
        if (isAdmin(session)) {
            Map<String, EasyUserBean> users = getUserData();
            for (EasyUserBean userBean : userList) {
                if (users.containsKey(userBean.username) || userBean.password == null || userBean.password.equals("")) {
                    result.add(userBean);
                } else {
                    save = true;
                    userBean.password = dealPw(userBean.password);
                    getUserData().put(userBean.username, userBean);
                }
            }
            if (save) saveUser();
        } else {
            result = userList;
        }
        return result;
    }
}
