package org.seaboxdata.systemmng.service.Impl;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.cookie.DateUtils;
import org.flhy.ext.Task.ExecutionTraceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.seaboxdata.systemmng.bean.PageforBean;
import org.seaboxdata.systemmng.dao.ExecutionTraceDao;
import org.seaboxdata.systemmng.dao.TaskGroupDao;
import org.seaboxdata.systemmng.entity.TaskGroupAttributeEntity;
import org.seaboxdata.systemmng.entity.TaskGroupEntity;
import org.seaboxdata.systemmng.service.HistoryLogService;
import org.seaboxdata.systemmng.util.CommonUtil.StringDateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by cRAZY on 2017/4/5.
 */
@Service
public class HistoryLogServiceImpl implements HistoryLogService{
    @Autowired
    @Qualifier("taskExecutionTraceDao")
    private ExecutionTraceDao executionTraceDao;

    @Autowired
    private TaskGroupDao groupDao;

    @Override
    public String getAllHistoryLog(int start, int limit,String statu,String type,String startDate,String taskName,String userGroupName) throws Exception{
        if(!StringUtils.isEmpty(startDate)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dt = sdf.parse(startDate.substring(0,10));
            startDate = DateUtils.formatDate(dt,"yyyy-MM-dd");
        }
        List<ExecutionTraceEntity> traces=executionTraceDao.getAllLogByPage(start,limit,statu,type,startDate,taskName,userGroupName);
        for(ExecutionTraceEntity trace:traces){
            if(trace.getStatus().equals("成功")){
                trace.setStatus("<font color='green'>"+trace.getStatus()+"</font>");
            }else{
                trace.setStatus("<font color='red'>"+trace.getStatus()+"</font>");
            }
        }

        PageforBean json=new PageforBean();
        json.setTotalProperty(executionTraceDao.getAllLogCount(statu,type,startDate,taskName,userGroupName));
        json.setRoot(traces);

        return JSONObject.fromObject(json, StringDateUtil.configJson("yyyy-MM-dd HH:mm:ss")).toString();
    }

    @Override
    public String getExecutionTraceById(Integer id) throws Exception{
        ExecutionTraceEntity trace=executionTraceDao.getTraceById(id);
        //增加所属任务组属性
        String config=trace.getExecutionConfiguration();
        if(null!=config){
            JSONObject json=JSONObject.fromObject(config);
            List<TaskGroupAttributeEntity> groups=groupDao.getTaskGroupByTaskName(trace.getJobName(),trace.getType());
            if(null!=groups && groups.size()>0){
                String[] groupNames=new String[groups.size()];
                for(int i=0;i<groups.size();i++){
                    TaskGroupAttributeEntity group=groups.get(i);
                    groupNames[i]=group.getTaskGroupName();
                }
                json.put("group",groupNames);
            }else{
                json.put("group","暂未分配任务组");
            }
            trace.setExecutionConfiguration(json.toString());
            trace.setExecutionLog(trace.getExecutionLog().replaceAll("\\\\n","<br/>"));
        }
        return JSONObject.fromObject(trace).toString();
    }
}
