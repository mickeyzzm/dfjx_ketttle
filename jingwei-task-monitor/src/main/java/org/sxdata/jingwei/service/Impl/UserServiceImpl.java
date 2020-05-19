package org.sxdata.jingwei.service.Impl;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sxdata.jingwei.bean.PageforBean;
import org.sxdata.jingwei.dao.UserDao;
import org.sxdata.jingwei.dao.UserGroupDao;
import org.sxdata.jingwei.entity.UserEntity;
import org.sxdata.jingwei.entity.UserGroupAttributeEntity;
import org.sxdata.jingwei.service.UserService;
import org.sxdata.jingwei.util.CommonUtil.StringDateUtil;
import org.sxdata.jingwei.util.TaskUtil.HttpClientUtil;
import org.sxdata.jingwei.util.TaskUtil.KettleEncr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by cRAZY on 2017/3/28.
 */
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private   UserDao userDao;
   @Autowired
    private UserGroupDao userGroupDao;

    @Override
    public void deleteUser(Integer id,String username) throws Exception{
        userGroupDao.deleteUserAttributeByName(username);
        userDao.deleteUser(id);
    }

    @Override
    public void updateUser(UserEntity user,UserGroupAttributeEntity attr) throws Exception{
        //修改用户
        if(!StringDateUtil.isEmpty(user.getDescription())){
            userDao.updateUser(user);
            //被修改的用户需要重新登录 使其session失效
            List<String> invalidSession=new ArrayList<String>();
            Set<String> set=StringDateUtil.allSession.keySet();
            for(String sessionId:set){
                HttpSession session=StringDateUtil.allSession.get(sessionId);
                if(null!=session.getAttribute("login")){
                    UserEntity iUser=(UserEntity)session.getAttribute("login");
                    if(!iUser.getUserId().equals(user.getUserId())){
                        continue;
                    }else{
                        invalidSession.add(session.getId());
                    }
                }
            }
            for(String invalid:invalidSession){
                HttpSession hsession=StringDateUtil.allSession.get(invalid);
                StringDateUtil.allSession.remove(invalid);
                hsession.invalidate();
            }
        }
        //修改用户与用户组关系表中 节点任务组的权限
        if(null!=attr){
            userGroupDao.updateUserGroupAttrByName(attr);
        }
    }

    @Override
    public synchronized boolean addUser(UserEntity user,UserGroupAttributeEntity attribute) throws Exception{
        attribute.setCreateDate(new Date());
        List<UserEntity> allUser=userDao.getAllUsers();
        for(UserEntity item:allUser){
            if(item.getLogin().equals(user.getLogin()))
                return false;
        }
        Integer userId=userDao.selectMaxId();
        if(null!=userId)
            userId+=1;
        else
            userId=0;
        user.setUserId(userId.toString());
        userDao.addUser(user);
        userGroupDao.addUserGroupAttribute(attribute);
        return true;
    }

    @Override
    public String getUsersLimit(int start, int limit,HttpServletRequest request) throws Exception{
        String username=request.getParameter("username");
        String userType=request.getParameter("usertype");
        String userGroup=request.getParameter("usergroup");
        //获取当前用户所在的用户组
        UserGroupAttributeEntity userAttribute=(UserGroupAttributeEntity)request.getSession().getAttribute("userInfo");
        String userGroupName=userAttribute.getUserGroupName();
        //如果是admin用户则把查询条件赋给该值
        if(StringDateUtil.isEmpty(userGroupName))
            userGroupName=userGroup;
        //获取用户类型
        Integer userTypeI=null;
        if(userType.equals("管理员"))
            userTypeI=1;
        else if(userType.equals("普通用户"))
            userTypeI=2;

        //获取用户集合总记录数
        List<UserEntity> users=new ArrayList<>();
        Integer count= userDao.getUserCount(userGroupName);
        users=userDao.getUsersLimit(start,limit,userGroupName,username,userTypeI);
        //如果不是是admin用户 把该用户组下面所有用户权限为1的用户移除
       /* if(!StringDateUtil.isEmpty(userGroupName)){
            List<UserEntity> adminUserArray=new ArrayList<>();
            for(int i=0;i<users.size();i++){
                users.get(i).setPassword(KettleEncr.decryptPasswd(users.get(i).getPassword()));
                if(users.get(i).getUserType()==1){
                    adminUserArray.add(users.get(i));
                }
            }
            for(UserEntity adminUser:adminUserArray){
                users.remove(adminUser);
                count--;
            }
        }else{
            count--;
            for(UserEntity user:users){
                user.setPassword(KettleEncr.decryptPasswd(user.getPassword()));
            }
        }*/

        PageforBean bean=new PageforBean();
        bean.setRoot(users);
        bean.setTotalProperty(count);
        return JSONObject.fromObject(bean,StringDateUtil.configJson("yyyy-MM-dd HH:mm:ss")).toString();
    }

    @Override
    public List<UserEntity> getUserByName(String login) throws Exception{
        return userDao.getUserbyName(login);
    }

    private String  checkJxUser(String url) {
        String rs = "-1";
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        HttpResponse response = null;
        HttpClient httpClient = null;
        try {
            // 由客户端执行(发送)Get请求
            HttpGet httpGet = new HttpGet(url);
            httpClient = HttpClientUtil.getHttpClient();
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                rs = EntityUtils.toString(responseEntity);
                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                System.out.println("响应内容为:" + rs);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.getConnectionManager().closeExpiredConnections();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rs;
    }
    @Override
    //登录
    public String jxLogin(String userName, String password, HttpServletRequest request) throws Exception{
        Properties prop = new Properties();
        //获取文件流
        InputStream ips = this.getClass().getClassLoader().getResourceAsStream("jxpro.properties");
        //加载文件流
        try {
            prop.load(ips);
            String logincheckurl = prop.getProperty("logincheckurl");
            StringBuffer urlBuff = new StringBuffer();
            urlBuff.append(logincheckurl).append("?username=").append(userName)
                    .append("&password=").append(password);
            String userId = checkJxUser(urlBuff.toString());
            if(!userId.equals("0")){
                if(  userDao.isDeptMgr(userId)==1)
                {

                    if(null==request.getSession().getAttribute("login")){
                        UserEntity user = new UserEntity();
                        user.setUserId(userId);
                        user.setLogin(userName);
//                        user.set
//                        user.setPassword("");
//                        user.setPassword(KettleEncr.decryptPasswd(users.get(0).getPassword()));
                        //使用不同浏览器重复登录\清除缓冲再登录\关闭浏览器后再打开可能造成存在两个username属性相同的session
                        //如果之前该用户session已存在 则先移除以前的session
                        List<String> invalidSession=new ArrayList<String>();
                        Set<String> set=StringDateUtil.allSession.keySet();
                        for(String sessionId:set){
                            HttpSession session=StringDateUtil.allSession.get(sessionId);
                            if(null!=session.getAttribute("login")){
                                UserEntity iUser=(UserEntity)session.getAttribute("login");
                                if(!iUser.getLogin().equals(userName)){
                                    continue;
                                }else{
                                    invalidSession.add(session.getId());
                                }
                            }
                        }
                        //从内存中移除 并且使会话失效
                        for(String invalid:invalidSession){
                            HttpSession hsession=StringDateUtil.allSession.get(invalid);
                            StringDateUtil.allSession.remove(invalid);
                            hsession.invalidate();
                        }
                        //登录信息存放在当前session
                        request.getSession().setAttribute("login", user);
                        UserGroupAttributeEntity attribute=userGroupDao.getInfoByUserName(userName);
                        if(attribute == null)
                            attribute = new UserGroupAttributeEntity();
                        attribute.setUserName(userId);
                        if(null==attribute){
                            attribute=new UserGroupAttributeEntity();
                        }
                        request.getSession().setAttribute("userInfo",attribute);
                        StringDateUtil.allSession.put(request.getSession().getId(), request.getSession());
                    }
                    return "success";
                }
                else return "部门管理员才可以登录";
            }else{
                return "用户不存在";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "登录失败，请联系部门管理员";
    }

    @Override
    //登录
    public String login(String userName, String password,HttpServletRequest request) throws Exception{
        String result="success";
        List<UserEntity> users=this.getUserByName(userName);
        if(users.size()==0){
            result="该用户名不存在,请再次确认";
        }else{
            UserEntity user=users.get(0);
            String realPassword=KettleEncr.decryptPasswd(user.getPassword());
            if(!realPassword.equals(password)){
                result="密码输入错误,请再次确认";
            }else{
                if(null==request.getSession().getAttribute("login")){
                    user.setPassword(KettleEncr.decryptPasswd(users.get(0).getPassword()));
                    //使用不同浏览器重复登录\清除缓冲再登录\关闭浏览器后再打开可能造成存在两个username属性相同的session
                    //如果之前该用户session已存在 则先移除以前的session
                    List<String> invalidSession=new ArrayList<String>();
                    Set<String> set=StringDateUtil.allSession.keySet();
                    for(String sessionId:set){
                        HttpSession session=StringDateUtil.allSession.get(sessionId);
                        if(null!=session.getAttribute("login")){
                            UserEntity iUser=(UserEntity)session.getAttribute("login");
                            if(!iUser.getLogin().equals(userName)){
                                continue;
                            }else{
                                invalidSession.add(session.getId());
                            }
                        }
                    }
                    //从内存中移除 并且使会话失效
                    for(String invalid:invalidSession){
                        HttpSession hsession=StringDateUtil.allSession.get(invalid);
                        StringDateUtil.allSession.remove(invalid);
                        hsession.invalidate();
                    }
                    //登录信息存放在当前session
                    request.getSession().setAttribute("login", user);
                    UserGroupAttributeEntity attribute=userGroupDao.getInfoByUserName(userName);
                    if(null==attribute){
                        attribute=new UserGroupAttributeEntity();
                    }
                    attribute.setUserName(user.getUserId());
                    request.getSession().setAttribute("userInfo",attribute);
                    StringDateUtil.allSession.put(request.getSession().getId(), request.getSession());
                }
            }
        }
        return result;
    }

    @Override
    //给用户分配用户组
    public void allotUserGroup(UserGroupAttributeEntity attr) throws Exception{
        userGroupDao.updateUserGroupAttrByName(attr);
        //用户状态发生改变使session失效
        List<String> invalidSession=new ArrayList<String>();
        Set<String> set=StringDateUtil.allSession.keySet();
        for(String sessionId:set){
            HttpSession session=StringDateUtil.allSession.get(sessionId);
            if(null!=session.getAttribute("login")){
                UserEntity iUser=(UserEntity)session.getAttribute("login");
                if(!iUser.getLogin().equals(attr.getUserName())){
                    continue;
                }else{
                    invalidSession.add(session.getId());
                }
            }
        }
        for(String invalid:invalidSession){
            HttpSession hsession=StringDateUtil.allSession.get(invalid);
            StringDateUtil.allSession.remove(invalid);
            hsession.invalidate();
        }
    }

    @Override
    //获取某个用户组下的所有用户 不分页
    public List<UserEntity> getUsers(String userGroupName) throws Exception{
        return userDao.getUsers(userGroupName);
    }

    @Override
    public void updatePassword(UserEntity user) throws Exception{
        userDao.updateUser(user);
    }
}
