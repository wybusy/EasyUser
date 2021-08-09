package com.wybusy;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * # EasyUser
 * 
 * > 一个极简的用户管理，建议管理用户不超过1千。不使用数据库，用文本文件保存数据。username是唯一标识，建议使用手机号。
 * 
 * ## 更新日志
 * 
 * ##### - V2.0.0
 * 
 * - 规范路径cn.sh.wy->com.wybusy
 * - 规范类名EasyUserService->EasyUser
 * - 增加logo
 * - 增加权限管理
 * 
 * ## spring boot使用
 * 
 * ##### - V2.0.0
 * 
 * ```java
 * <dependency>
 * <groupId>com.wybusy</groupId>
 * <artifactId>EasyUser</artifactId>
 * <version>2.0.0</version>
 * </dependency>
 * ```
 * 
 * ## TODO
 * 
 * - 指定天数后，强制修改密码
 * - 找回密码
 * - 日志
 * - 管理员可迫使某个用户全部登录失效
 * - 记录lastvisit
 * - 根据lastvisit显示在线用户
 * 
 * ## 策略
 * 
 * - 登录失效
 * - 自登录起，指定时间（30天）失效
 * - (plan)自最后活动，30分钟失效
 * - 互踢
 * - 多次登录互不影响 人数=0
 * - (plan)指定同时登录人数 人数>0
 * - 文本数据库，JSON文件
 * - 数据备份方案，同时保存两份
 * - session也持久保存，重启程序不影响用户登录状态
 * - 管理员不能通过用户列表修改自己的密码，信息，不能删除自己
 * - 一个角色可以包含多个权限，用逗号分隔
 * - 一个用户可以包含多个角色，用逗号分隔
 */


public class EasyUser {
    /**
     * - 默认数据存储路径：/publicdata/easyuser
     */
    public static String path = "/publicdata/easyuser";
    public static Long maxOnlineTime = 30L * 24 * 60 * 60 * 1000;
    private static final String salt = "user.wybusy.com";
    /**
     * - 默认管理员 administrator/EasyUser，不可删除，身份不可修改
     */
    private static final Map<String, EasyUserBean> userData = new HashMap<>();
    /**
     * - 默认三种角色，必然存在：easyStaff内部员工，easyAdmin管理员，easyUser用户，不可删除
     * - easyStaff内部员工，只有administrator对他有“增删改”权限，其他人员无法更改
     */
    private static final Map<String, EasyRoleBean> roleData = new HashMap<>();
    /**
     * - 默认三种权限，必然存在：easyAdmin用户管理权限，easyStaff员工权限，easyUser注册用户权限，不可删除
     */
    private static final Map<String, EasyAuthorityBean> authorityData = new HashMap<>();
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

    private static void saveAuthority() {
        String content = JSON.toJSONString(authorityData.values());
        saveFile(path, "authority.json", false, content);
        saveFile(path + "/authority", (new Date().getTime()) + ".json", false, content);
    }

    private static void saveSession() {
        String content = JSON.toJSONString(sessionData.values());
        saveFile(path, "session.json", false, content);
    }

    private static String md5(String plainText) {
        byte[] bytes = null;
        try {
            bytes = plainText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
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
                userData.put("administrator", new EasyUserBean("administrator", dealPw("EasyUser"), "", "系统管理员", "{}"));
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
                roleData.put("easyStaff", new EasyRoleBean("easyStaff", "默认角色:员工", "easyStaff", "{}"));
                roleData.put("easyAdmin", new EasyRoleBean("easyAdmin", "默认角色:管理员", "easyAdmin", "{}"));
                roleData.put("easyUser", new EasyRoleBean("easyUser", "默认角色:用户", "easyUser", "{}"));
                saveRole();
            } else {
                List<EasyRoleBean> roleDataList = JSON.parseArray(roleDataString, EasyRoleBean.class);
                for (EasyRoleBean roleBean : roleDataList)
                    roleData.put(roleBean.roleName, roleBean);
            }
        }
        if (authorityData.isEmpty()) {
            String authorityDataString = readFile(path, "authority.json");
            if (authorityDataString.equals("")) {
                authorityData.put("easyStaff", new EasyAuthorityBean("easyStaff", "默认权限:员工", "{}"));
                authorityData.put("easyAdmin", new EasyAuthorityBean("easyAdmin", "默认权限:用户管理", "{}"));
                authorityData.put("easyUser", new EasyAuthorityBean("easyUser", "默认权限:注册用户", "{}"));
                saveAuthority();
            } else {
                List<EasyAuthorityBean> authorityDataList = JSON.parseArray(authorityDataString, EasyAuthorityBean.class);
                for (EasyAuthorityBean authorityBean : authorityDataList)
                    authorityData.put(authorityBean.authorityName, authorityBean);
            }
        }
        if (sessionData.isEmpty()) {
            String sessionDataString = readFile(path, "session.json");
            if (!sessionDataString.equals("")) {
                List<EasySessionBean> sessionDataList = JSON.parseArray(sessionDataString, EasySessionBean.class);
                for (EasySessionBean sessionBean : sessionDataList)
                    sessionData.put(sessionBean.session, sessionBean);
            }
        }
    }

    private static Map<String, EasySessionBean> getSessionData() {
        loadData();
        return sessionData;
    }

    /**
     * ## 用户自我管理使用功能
     *
     */

    /**
     * ##### - login
     * 用户登录
     *
     * @param username
     * @param password
     * @return EasyUserBean
     */
    public static EasyUserBean login(String username, String password) {
        EasyUserBean result = null;
        Map<String, EasyUserBean> users = getUserData();
        if (users.containsKey(username) && users.get(username).password.equals(dealPw(password))) {
            Map<String, EasySessionBean> sessionBeanMap = getSessionData();
            for (String key : sessionBeanMap.keySet())
                if (new Date().getTime() - sessionBeanMap.get(key).loginTime > maxOnlineTime)
                    sessionBeanMap.remove(key);
            result = users.get(username);
            result.session = md5(username + System.currentTimeMillis());
            sessionBeanMap.put(result.session, new EasySessionBean(username, result.session));
            saveSession();
            getUserAuthorities(result);
        }
        return result;
    }

    /**
     * ##### - info
     * 根据session取得userBean。当用户被删除后，用户即被登录失效
     *
     * @param session
     * @return EasyUserBean
     */
    public static EasyUserBean info(String session) {
        EasyUserBean result = null;
        Map<String, EasyUserBean> users = getUserData();
        Map<String, EasySessionBean> sessionBeanMap = getSessionData();
        if (sessionBeanMap.containsKey(session) // session仍存在，没有被强制下线删除
                && new Date().getTime() - sessionBeanMap.get(session).loginTime < maxOnlineTime // 自登录起最大天数内
                && users.containsKey(sessionBeanMap.get(session).username)) { // 防止用户已经被删掉
            result = users.get(sessionBeanMap.get(session).username);
            getUserAuthorities(result);
        }
        return result;
    }

    /**
     * ##### - getUserAuthorities
     * 获得用户的全部权限
     *
     * @param userBean
     * @return Set<EasyAuthorityBean>
     */
    public static String getUserAuthorities(EasyUserBean userBean) {
        Set<String> authoritySet = new HashSet<>();
        if (userBean != null) {
            Map<String, EasyRoleBean> roleBeans = getRoleData();
            Map<String, EasyAuthorityBean> authorityBeans = getAuthorityData();
            if (userBean.username.equals("administrator")) {
                authoritySet.addAll(authorityBeans.keySet());
            } else {
                String[] roles = userBean.role.split(",");
                for (String role : roles) {
                    if (roleBeans.containsKey(role)) {
                        String[] authorities = roleBeans.get(role).authority.split(",");
                        for (String authority : authorities) {
                            if (authorityBeans.containsKey(authority)) {
                                authoritySet.add(authority);
                            }
                        }
                    }
                }
            }
        }
        boolean dot = false;
        userBean.authority = "";
        for(String authority: authoritySet) {
            userBean.authority += ((dot) ? "," : "") + authority;
            dot = true;
        }
        return userBean.authority;
    }

    /**
     * ##### - haveAuthority
     * 用户是否具有某种特定权限
     *
     * @param userBean
     * @param authorityName
     * @return
     */
    public static boolean haveAuthority(EasyUserBean userBean, String authorityName) {
        boolean result = false;
        if (userBean.username.equals("administrator")) {
            result = true;
        } else {
            List<String> authorityBeans = new ArrayList<>(Arrays.asList(getUserAuthorities(userBean).split(",")));
            if (authorityBeans.contains(authorityName)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * ##### - logout
     * 用户登出
     *
     * @param session
     * @return
     */
    public static boolean logout(String session) {
        boolean result = false;
        Map<String, EasySessionBean> sessionBeanMap = getSessionData();
        if (sessionBeanMap.containsKey(session)) { // 防止用户已经被删掉
            sessionBeanMap.remove(session);
            saveSession();
            result = true;
        }
        return result;
    }

    /**
     * ##### - changePassWord
     * 用户修改密码
     *
     * @param userBean
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public static boolean changePassWord(EasyUserBean userBean, String oldPassword, String newPassword) {
        boolean result = false;
        if (!(userBean == null || newPassword == null || newPassword.equals("")) && userBean.password.equals(dealPw(oldPassword))) {
            userBean.password = dealPw(newPassword);
            saveUser();
            result = true;
        }
        return result;
    }

    /**
     * ##### - getUserData
     * 管理员获得用户列表
     *
     * @return
     */
    public static Map<String, EasyUserBean> getUserData() {
        loadData();
        return userData;
    }

    /**
     * ##### - getRoleData
     * 管理员获得角色列表
     *
     * @return
     */
    public static Map<String, EasyRoleBean> getRoleData() {
        loadData();
        return roleData;
    }

    /**
     * ##### - getAuthorityData
     * 管理员获得权限列表
     *
     * @return Map<String, EasyAuthorityBean>
     */
    public static Map<String, EasyAuthorityBean> getAuthorityData() {
        loadData();
        return authorityData;
    }

    /**
     * ##### - register(plan)
     *
     * ##### - poke(plan)
     *
     */

    /**
     * ## 管理员用户管理功能
     *
     */

    /**
     * ##### - addUser
     * 管理员添加用户，不是administrator不能添加有easyStaff权限的人
     *
     * @param userBean
     * @param username
     * @param password
     * @param role
     * @param realname
     * @param moreInfoJson
     * @return boolean
     */
    public static EasyUserBean addUser(EasyUserBean userBean, String username, String password, String role, String realname, String moreInfoJson) {
        EasyUserBean result = null;
        if (haveAuthority(userBean, "easyAdmin") && !getUserData().containsKey(username)) {
            EasyUserBean newUserBean = new EasyUserBean(username, dealPw(password), role, realname, moreInfoJson);
            if (userBean.username.equals("administrator") || !haveAuthority(newUserBean, "easyStaff")) {
                getUserData().put(username, newUserBean);
                saveUser();
                result = newUserBean;
            }
        }
        return result;
    }

    /**
     * ##### - modifyUser
     * 管理员修改用户信息。不是administrator不能更改有easyStaff权限的人
     *
     * @param userBean
     * @param username
     * @param password
     * @param role
     * @param realname
     * @param moreInfoJson
     * @return boolean
     */
    public static EasyUserBean modifyUser(EasyUserBean userBean, String username, String password, String role, String realname, String moreInfoJson) {
        EasyUserBean result = null;
        if (haveAuthority(userBean, "easyAdmin")
                && !userBean.username.equals(username) //自己不能通过用户列表修改自己的信息
                && getUserData().containsKey(username)
                && (userBean.username.equals("administrator") || !haveAuthority(getUserData().get(username), "easyStaff")) // 不是administrator不能更改有easyStaff权限的人
            ) {
            result = getUserData().get(username);
            if (!(password == null || password.equals(""))) result.password = dealPw(password);
            if (role != null && !username.equals("administrator")){
                result.role = role;
                getUserAuthorities(result);
            }
            if (realname != null) result.realname = realname;
            if (moreInfoJson != null) result.moreInfoJson = moreInfoJson;
            saveUser();
        }
        return result;
    }

    /**
     * ##### - delUser
     * 管理员删除用户，不是administrator不能删除有easyStaff权限的人
     *
     * @param userBean
     * @param username
     * @return boolean
     */
    public static boolean delUser(EasyUserBean userBean, String username) {
        boolean result = false;
        if (haveAuthority(userBean, "easyAdmin")
                && !userBean.username.equals(username) //自己不能通过用户列表删除自己
                && getUserData().containsKey(username)
                && (userBean.username.equals("administrator") || !haveAuthority(getUserData().get(username), "easyStaff"))
                && !username.equals("administrator")) {
            getUserData().remove(username);
            saveUser();
            result = true;
        }
        return result;
    }

    /**
     * ##### - addUsers
     * 批量添加用户，不是administrator不能添加有easyStaff权限的人。返回因无权限或用户重复而未能成功添加的用户
     *
     * @param userBean
     * @param userList
     * @return List<EasyUserBean>
     */
    public static List<EasyUserBean> addUsers(EasyUserBean userBean, List<EasyUserBean> userList) {
        List<EasyUserBean> result = new ArrayList<>();
        boolean save = false;
        if (haveAuthority(userBean, "easyAdmin")) {
            Map<String, EasyUserBean> users = getUserData();
            for (EasyUserBean user : userList) {
                if (users.containsKey(user.username) || user.password == null || user.password.equals("")
                || (!userBean.username.equals("administrator") && haveAuthority(user, "easyStaff"))) {
                    result.add(user);
                } else {
                    save = true;
                    user.password = dealPw(user.password);
                    getUserData().put(user.username, user);
                }
            }
            if (save) saveUser();
        } else {
            result = userList;
        }
        return result;
    }

    /**
     * ## 管理员角色管理
     *
     */

    /**
     * ##### - addRole
     * 增加角色
     *
     * @param userBean
     * @param roleName
     * @param description
     * @param authority
     * @param moreInfoJson
     * @return EasyRoleBean
     */
    public static EasyRoleBean addRole(EasyUserBean userBean, String roleName, String description, String authority, String moreInfoJson) {
        EasyRoleBean result = null;
        if (haveAuthority(userBean, "easyAdmin")) {
            Map<String, EasyRoleBean> roleBeans = getRoleData();
            if (!roleBeans.containsKey(roleName)) {
                result = new EasyRoleBean(roleName, description, authority, moreInfoJson);
                roleBeans.put(roleName, result);
                saveRole();
            }
        }
        return result;
    }

    /**
     * ##### - delRole
     * 删除角色
     *
     * @param userBean
     * @param roleName
     * @return boolean
     */
    public static boolean delRole(EasyUserBean userBean, String roleName) {
        boolean result = false;
        if (haveAuthority(userBean, "easyAdmin")
                && !roleName.equals("easyStaff")
                && !roleName.equals("easyAdmin")
                && !roleName.equals("easyUser")) {
            Map<String, EasyRoleBean> roleBeans = getRoleData();
            if (roleBeans.containsKey(roleName)) {
                roleBeans.remove(roleName);
                saveRole();
                result = true;
            }
        }
        return result;
    }

    /**
     * ##### modifyRole
     * 修改角色信息
     * @param userBean
     * @param roleName
     * @param description
     * @param authority
     * @param moreInfoJson
     * @return EasyRoleBean
     */
    public static EasyRoleBean modifyRole(EasyUserBean userBean, String roleName, String description, String authority, String moreInfoJson) {
        EasyRoleBean result = null;
        if (haveAuthority(userBean, "easyAdmin")) {
            Map<String, EasyRoleBean> roleBeans = getRoleData();
            if (roleBeans.containsKey(roleName)) {
                result = roleBeans.get(roleName);
                result.description = description;
                result.authority = authority;
                result.moreInfoJson = moreInfoJson;
                saveRole();
            }
        }
        return result;
    }

    /**
     * ## 管理员权限管理
     *
     */

    /**
     * ##### - addAuthority
     * 增加权限
     *
     * @param userBean
     * @param authorityName
     * @param description
     * @param moreInfoJson
     * @return boolean
     */
    public static boolean addAuthority(EasyUserBean userBean, String authorityName, String description, String moreInfoJson) {
        boolean result = false;
        if (haveAuthority(userBean, "easyAdmin")) {
            Map<String, EasyAuthorityBean> authorityBeans = getAuthorityData();
            if (!authorityBeans.containsKey(authorityName)) {
                authorityBeans.put(authorityName, new EasyAuthorityBean(authorityName, description, moreInfoJson));
                saveAuthority();
                result = true;
            }
        }
        return result;
    }

    /**
     * ##### - delAuthority
     * 删除角色
     *
     * @param userBean
     * @param authorityName
     * @return boolean
     */
    public static boolean delAuthority(EasyUserBean userBean, String authorityName) {
        boolean result = false;
        if (haveAuthority(userBean, "easyAdmin")
                && !authorityName.equals("easyStaff")
                && !authorityName.equals("easyAdmin")
                && !authorityName.equals("easyUser")) {
            Map<String, EasyAuthorityBean> authorityBeans = getAuthorityData();
            if (authorityBeans.containsKey(authorityName)) {
                authorityBeans.remove(authorityName);
                saveAuthority();
                result = true;
            }
        }
        return result;
    }

    /**
     * 替换规则
     * ^ *
     * ^[^\*].*\n
     * \* ?/?
     * @(.*)\n -> > \1    \n
     */
}
