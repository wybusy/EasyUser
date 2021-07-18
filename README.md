# EasyUser

一个极简的用户管理。

默认administrator/EasyUser，不可删除，身份不可修改。

默认两种角色，必然存在：easyAdmin管理员，easyUser用户，不可删除

## 更新日志

##### V2.0.0

- 规范路径cn.sh.wy->com.wybusy 
- 规范类名EasyUserService->EasyUser
- 增加logo

## spring boot使用

##### V1.0.0

```java
        <dependency>
            <groupId>com.wybusy</groupId>
            <artifactId>EasyUser</artifactId>
            <version>1.0.0</version>
        </dependency>
```

## 策略

1. 登录失效
   - 自登录起，指定时间（30天）失效
   - (plan)自最后活动，30分钟失效
2. 互踢
   - 多次登录互不影响 人数=0
   - (plan)指定同时登录人数 人数>0
3. 文本数据库，JSON文件
4. 数据备份方案，同时保存两份
5. session也持久保存，重启程序不影响用户登录状态
6. 管理员不能通过用户列表修改自己的密码，信息，不能删除自己

## TODO

1. 指定天数后，强制修改密码
2. 找回密码
3. 日志
4. 管理员可迫使某个用户全部登录失效
5. 记录lastvisit
6. 根据lastvisit显示在线用户


## Bean

##### - EasyUserBean

用户信息

```java
    public String username;
    public String password;
    public String role;
    public String realname;
    public String moreInfoJson;
    public String session;
```

##### - EasyRoleBean

角色信息

```java
    public String roleName;
    public String description;
    public String moreInfoJson;
```

##### - EasySessionBean

Session信息

```java
    public String session;
    public String username;
    public Long loginTime; //据此判断是否需要重新登录
    public Long lastVisitTime;
```

## Service

### 变量
##### - path

数据存放目录，默认"/publicdata/easyuser"

##### - maxOnlineTime

登录后，多久需要重新登录，默认30天

### 函数

#### 用户登录相关

##### - login
登录

```java
EasyUserBean login(String username, String password)
```

##### - logout

登出

```java
boolean logout(String session)
```
##### - info

根据session取得userBean。当用户被删除后，用户即被登录失效

```java
EasyUserBean info(String session)
```

##### - register(plan)

##### - poke(plan)

##### - changePassWord
用户修改密码

```java
boolean changePassWord(String session, String oldPassword, String newPassword)
```

##### - modifyInfo(plan)

#### 管理员操作相关

##### - getUserList
管理员获得用户列表

```java
Map<String, EasyUserBean> getUserList(String session)
```

##### - addUser
管理员添加用户

```java
boolean addUser(String session, String username, String password, String role, String realname, String moreInfoJson)
```

##### - modifyUser
管理员修改用户信息

```java
boolean modifyUser(String session, String username, String password, String role, String realname, String moreInfoJson)
```

##### - deleteUser
管理员删除用户

```java
boolean deleteUser(String session, String username)
```

##### - addUsers
批量添加用户,返回因无权限或用户重复而未能成功添加的用户

```java
List<EasyUserBean> addUsers(String session, List<EasyUserBean> userList)
```

##### - getRoleList

管理员获得角色列表

```java
Map<String, EasyRoleBean> getRoleList(String session)
```

##### - addRole(plan)

##### - modifyRole(plan)

##### - deleteRole(plan)
