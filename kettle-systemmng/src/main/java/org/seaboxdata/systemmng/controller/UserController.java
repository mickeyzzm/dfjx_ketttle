package org.seaboxdata.systemmng.controller;

import com.google.code.kaptcha.Constants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.google.code.kaptcha.Producer;
import org.pentaho.di.i18n.LanguageChoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.seaboxdata.systemmng.entity.UserEntity;
import org.seaboxdata.systemmng.entity.UserGroupAttributeEntity;
import org.seaboxdata.systemmng.service.UserService;
import org.seaboxdata.systemmng.util.TaskUtil.KettleEncr;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

/**
 * Created by cRAZY on 2017/3/28.
 */
@Controller
@RequestMapping(value="/user")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    private Producer producer;

    //分配用户组
    @RequestMapping(value="/allotUserGroup")
    @ResponseBody
    protected void allotUserGroup(HttpServletResponse response,HttpServletRequest request) throws Exception{
        Integer type = ((UserGroupAttributeEntity)request.getSession().getAttribute("userInfo")).getUserType();
        if (type != null && type == 2) {
            throw new Exception("你不是管理员");
        }
        try {
            UserGroupAttributeEntity attr=new UserGroupAttributeEntity();
            Integer userType=Integer.valueOf(request.getParameter("rdaUserType"));
            String username=request.getParameter("username");
            String userGroupName=request.getParameter("userGroupCombobox");
            attr.setUserType(userType);
            attr.setUserGroupName(userGroupName);
            attr.setUserName(username);
            if(userType==1){
                attr.setSlavePremissonType(1);
                attr.setTaskPremissionType(1);
            }else{
                Integer slavePower=Integer.valueOf(request.getParameter("rdaSlavePower"));
                Integer taskGroupPower=Integer.valueOf(request.getParameter("rdaTaskGroup"));
                attr.setSlavePremissonType(slavePower);
                attr.setTaskPremissionType(taskGroupPower);
            }
            userService.allotUserGroup(attr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //分页形式获取用户集合
    @RequestMapping(value="/getUsers")
    @ResponseBody
    protected void getUsers(HttpServletResponse response,HttpServletRequest request) throws Exception{
        try{
            Integer start=Integer.valueOf(request.getParameter("start"));
            Integer limit=Integer.valueOf(request.getParameter("limit"));
            String result=userService.getUsersLimit(start,limit,request);
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out=response.getWriter();
            out.write(result);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //删除用户
    @RequestMapping(value="/deleteUser")
    @ResponseBody
    protected void deleteUser(HttpServletResponse response,HttpServletRequest request) throws Exception{
        try {
            String userId=request.getParameter("userId");
            String username=request.getParameter("username");
            userService.deleteUser(Integer.valueOf(userId),username);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //修改用户
    @RequestMapping(value="/updateUser")
    @ResponseBody
    protected void updateUser(HttpServletResponse response,HttpServletRequest request) throws Exception{
        try{
            UserGroupAttributeEntity attr=new UserGroupAttributeEntity();
            //接收参数
            String description=request.getParameter("userDescription");
            Integer userId=Integer.valueOf(request.getParameter("userId"));
            String username=request.getParameter("userLogin");
            //
            if(null!=request.getParameter("rdaSlavePower") && !request.getParameter("rdaSlavePower").equals("")){
                Integer slavePower=Integer.valueOf(request.getParameter("rdaSlavePower"));
                Integer taskGroupPower=Integer.valueOf(request.getParameter("rdaTaskGroup"));
                attr.setSlavePremissonType(slavePower);
                attr.setTaskPremissionType(taskGroupPower);
                attr.setUserName(username);
            }else{
                attr=null;
            }
            //组装用户对象
            UserEntity user=new UserEntity();
            user.setDescription(description);
            user.setUserId("userId");
            //添加 - -返回结果
            userService.updateUser(user,attr);
            JSONObject json=new JSONObject();
            json.put("success",true);
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out=response.getWriter();
            out.write(json.toString());
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //添加用户
    @RequestMapping(value="/addUser")
    @ResponseBody
    protected void addUser(HttpServletResponse response,HttpServletRequest request) throws Exception{
        try{
            //接收参数
            String description=request.getParameter("desc");
            String password=request.getParameter("password");
            String login=request.getParameter("username");
            String slavePower=request.getParameter("slavePower");
            String taskGroupPower=request.getParameter("taskGroupPower");
            String userType=request.getParameter("userType");
            String userGroupName=request.getParameter("userGroupName");
            //组装user对象
            UserEntity user=new UserEntity();
            user.setPassword(KettleEncr.encryptPassword(password));
            user.setDescription(description);
            user.setEnabled('Y');
            user.setLogin(login);
            user.setName(login);
            //组装userAttribute对象
            UserGroupAttributeEntity attribute=new UserGroupAttributeEntity();
            attribute.setUserGroupName(userGroupName);
            if(Integer.valueOf(userType)!=1){
                attribute.setSlavePremissonType(Integer.valueOf(slavePower));
                attribute.setTaskPremissionType(Integer.valueOf(taskGroupPower));
            }else{
                attribute.setSlavePremissonType(1);
                attribute.setTaskPremissionType(1);
            }
            attribute.setUserGroupName(userGroupName);
            attribute.setUserType(Integer.valueOf(userType));
            attribute.setUserName(login);
            //添加 - -返回结果
            boolean isSuccess=userService.addUser(user,attribute);
            JSONObject json=new JSONObject();
            json.put("success",true);
            json.put("isSuccess",isSuccess);
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out=response.getWriter();
            out.write(json.toString());
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }


    //登陆
    @RequestMapping(value="/doLogin")
    @ResponseBody
    protected void doLogin(HttpServletResponse response,HttpServletRequest request,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String captcha) throws Exception{
        String code = (String)request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(captcha) || !code.equals(captcha)) {
            PrintWriter out=response.getWriter();
            out.write("验证码无效");
            out.flush();
            out.close();
            return;
        }

        try{
            UserEntity loginUser=(UserEntity)request.getSession().getAttribute("login");
            String result="success";
            if(null==loginUser){
//                result=userService.jxLogin(username,password,request); 
                result=userService.login(username,password,request);
            }
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out=response.getWriter();
            out.write(result);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //获取当前登录的用户
    //登陆
    @RequestMapping(value="/getLoginUser")
    @ResponseBody
    protected void getLoginUser(HttpServletResponse response,HttpServletRequest request) throws Exception{
        try{
            UserEntity loginUser=(UserEntity)request.getSession().getAttribute("login");
            UserGroupAttributeEntity userInfo=(UserGroupAttributeEntity)request.getSession().getAttribute("userInfo");
            JSONObject json=new JSONObject();
            json.put("user",loginUser);
            json.put("userInfo", userInfo);
            PrintWriter out=response.getWriter();
            out.write(json.toString());
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //登出
    @RequestMapping(value="/loginOut")
    @ResponseBody
    protected void loginOut(HttpServletResponse response,HttpServletRequest request) throws Exception{
        try{
           request.getSession().invalidate();
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //获取某个用户组下的用户
    @RequestMapping(value="/getUsersByInfo")
    @ResponseBody
    protected void getUsersByInfo(HttpServletResponse response,HttpServletRequest request,@RequestParam String userGroupName) throws Exception{
        try{
            List<UserEntity> users=userService.getUsers(userGroupName);
            PrintWriter out=response.getWriter();
            out.write(JSONArray.fromObject(users).toString());
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    //修改密码
    @RequestMapping(value="/updatePassword")
    @ResponseBody
    protected void updatePassword(@RequestParam String password,@RequestParam String userId) throws Exception{
        try {
            UserEntity user=new UserEntity();
            user.setUserId(userId);
            user.setPassword(KettleEncr.encryptPassword(password));
            userService.updatePassword(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }




    @GetMapping(value="/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        //生成文字验证码
        String text = producer.createText();
        //生成图片验证码
        BufferedImage image = producer.createImage(text);
        //保存到shiro session
        request.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
    }
}
