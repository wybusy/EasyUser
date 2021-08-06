# EasyUser

> 一个极简的用户管理，建议管理用户不超过1千。不使用数据库，用文本文件保存数据。username是唯一标识，建议使用手机号。

## 更新日志

##### - V2.0.0

- 规范路径cn.sh.wy->com.wybusy
- 规范类名EasyUserService->EasyUser
- 增加logo
- 增加权限管理

## spring boot使用

##### - V2.0.0

```java
<dependency>
    <groupId>com.wybusy</groupId>
    <artifactId>EasyUser</artifactId>
    <version>2.0.0</version>
</dependency>
```

## TODO

- 指定天数后，强制修改密码
- 找回密码
- 日志
- 管理员可迫使某个用户全部登录失效
- 记录lastvisit
- 根据lastvisit显示在线用户

## 策略

- 登录失效
- 自登录起，指定时间（30天）失效
- (plan)自最后活动，30分钟失效
- 互踢
- 多次登录互不影响 人数=0
- (plan)指定同时登录人数 人数>0
- 文本数据库，JSON文件
- 数据备份方案，同时保存两份
- session也持久保存，重启程序不影响用户登录状态
- 管理员不能通过用户列表修改自己的密码，信息，不能删除自己
- 一个角色可以包含多个权限，用逗号分隔
- 一个用户可以包含多个角色，用逗号分隔

- 默认数据存储路径：/publicdata/easyuser

- 默认管理员 administrator/EasyUser，不可删除，身份不可修改

- 默认两种角色，必然存在：easyAdmin管理员，easyUser用户，不可删除

- 默认一种权限，必然存在：easyAdmin用户管理权限，不可删除

## 用户自我管理使用功能


##### - login
用户登录

> param username    
> param password    
> return EasyUserBean

##### - info
根据session取得userBean。当用户被删除后，用户即被登录失效

> param session    
> return EasyUserBean

##### - getUserAuthorities
获得用户的全部权限

> param userBean    
> return Set<EasyAuthorityBean>

##### - haveAuthority
用户是否具有某种特定权限

> param userBean    
> param authorityName    
> return

##### - logout
用户登出

> param session    
> return

##### - changePassWord
用户修改密码

> param userBean    
> param oldPassword    
> param newPassword    
> return

##### - getUserData
管理员获得用户列表

> return

##### - getRoleData
管理员获得角色列表

> return

##### - getAuthorityData
管理员获得权限列表

> return Map<String, EasyAuthorityBean>

##### - register(plan)

##### - poke(plan)


## 管理员用户管理功能


##### - addUser
管理员添加用户

> param userBean    
> param username    
> param password    
> param role    
> param realname    
> param moreInfoJson    
> return boolean

##### - modifyUser
管理员修改用户信息

> param userBean    
> param username    
> param password    
> param role    
> param realname    
> param moreInfoJson    
> return boolean

##### - delUser
管理员删除用户

> param userBean    
> param username    
> return boolean

##### - addUsers
批量添加用户,返回因无权限或用户重复而未能成功添加的用户

> param userBean    
> param userList    
> return

## 管理员角色管理


##### - addRole
增加角色

> param userBean    
> param roleName    
> param description    
> param authority    
> param moreInfoJson    
> return boolean

##### - delRole
删除角色

> param userBean    
> param roleName    
> return boolean

##### - modifyRole(plan)


## 管理员权限管理


##### - addAuthority
增加权限

> param userBean    
> param authorityName    
> param description    
> param moreInfoJson    
> return boolean

##### - delAuthority
删除角色

> param userBean    
> param authorityName    
> return boolean    
