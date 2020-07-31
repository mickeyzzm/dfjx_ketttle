package org.seaboxdata.systemmng.service.Impl;

import net.sf.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.seaboxdata.systemmng.dao.JobSchedulerDao;
import org.seaboxdata.systemmng.dao.SlaveDao;
import org.seaboxdata.systemmng.dao.UserDao;
import org.seaboxdata.systemmng.entity.JobTimeSchedulerEntity;
import org.seaboxdata.systemmng.bean.PageforBean;
import org.seaboxdata.systemmng.entity.SlaveEntity;
import org.seaboxdata.systemmng.entity.UserEntity;
import org.seaboxdata.systemmng.service.JobService;
import org.seaboxdata.systemmng.service.SchedulerService;
import org.seaboxdata.systemmng.util.CommonUtil.StringDateUtil;
import org.seaboxdata.systemmng.util.TaskUtil.CarteTaskManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.weeklyOnDayAndHourAndMinute;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by cRAZY on 2017/3/8.
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {
    @Autowired
    JobSchedulerDao jobSchedulerDao;
    @Autowired
    SlaveDao slaveDao;
    @Autowired
    UserDao userDao;

    @Override
    public List<JobTimeSchedulerEntity> getSchedulerJobByLogin(String userGroupName) {
        return jobSchedulerDao.getAllTimerJob(userGroupName);
    }

    //判断是否在数据库中已经存在相同类型 相同执行周期的同一个作业
    public boolean judgeJobIsAlike(JobTimeSchedulerEntity willAddJobTimer) throws Exception{
        boolean flag=false;
        List<JobTimeSchedulerEntity> jobs=jobSchedulerDao.getAllTimerJob("");

        if(jobs!=null && jobs.size()>=1){
            //遍历查找是否有作业名与用户选择的作业名相同的作业
            List<JobTimeSchedulerEntity> jobNameEqual=new ArrayList<>(); //存放作业名与用户选择的相同的作业
            for(JobTimeSchedulerEntity nameEqual:jobs){
                if(nameEqual.getJobName().equals(willAddJobTimer.getJobName())){
                    jobNameEqual.add(nameEqual);
                }
            }

            //遍历查找相同作业名的情况下 是否有作业定时类型也相同的作业
            if(jobNameEqual.size()>=1){
                List<JobTimeSchedulerEntity> typeEqualJobs=new ArrayList<>(); //存放定时类型与用户传递的定时运行相同的作业
                for(JobTimeSchedulerEntity jobTimer:jobNameEqual){
                    if(jobTimer.getSchedulertype()==willAddJobTimer.getSchedulertype()){
                        typeEqualJobs.add(jobTimer);
                    }
                }
                //进一步判断定时的具体时间是否相同
                if(typeEqualJobs.size()>=1){
                    Integer timerType=willAddJobTimer.getSchedulertype();
                    switch(timerType) {
                        //类型为1也就是间隔执行 判断间隔执行的分钟间隔是否相同
                        case 1:
                            for(JobTimeSchedulerEntity jobTime:typeEqualJobs){
                                if(willAddJobTimer.getIntervalminutes()==jobTime.getIntervalminutes()){
                                    flag=true;
                                }
                            }
                            break;

                        //类型为2也就每天执行类型  进一步判断每天执行的小时 分钟是否一样
                        case 2:
                            List<JobTimeSchedulerEntity> hourLikeByTypeTwo=new ArrayList<>(); //存放每天执行 小时与用户设置值一样的定时作业
                            for(JobTimeSchedulerEntity jobTime:typeEqualJobs){
                                if(willAddJobTimer.getHour()==jobTime.getHour()){
                                    hourLikeByTypeTwo.add(jobTime);
                                }
                            }
                            //如果有小时相同的记录 进一步判断是否分钟相同
                            if(hourLikeByTypeTwo.size()>=1){
                                for(JobTimeSchedulerEntity jobTime:hourLikeByTypeTwo){
                                    if(willAddJobTimer.getMinutes()==jobTime.getMinutes()){
                                        flag=true;
                                    }
                                }
                            }
                            break;

                        //类型为3 每周执行
                        case 3:
                            //首先判断每周执行的星期值week是否相同
                            List<JobTimeSchedulerEntity> weekLikeByTypeThree=new ArrayList<>();//存放星期数值与用户传递值相同的记录
                            for(JobTimeSchedulerEntity jobTime:typeEqualJobs){
                                if(jobTime.getWeekday()==willAddJobTimer.getWeekday()){
                                    weekLikeByTypeThree.add(jobTime);
                                }
                            }
                            List<JobTimeSchedulerEntity> weekAndHourLikeByTypeThree=new ArrayList<>();//存放星期数值和小时与用户传递值相同的记录
                            //如果星期值相同判断小时是是否也相同
                            if(weekLikeByTypeThree.size()>=1){
                                for(JobTimeSchedulerEntity weekLikeJob:weekAndHourLikeByTypeThree){
                                    if(weekLikeJob.getHour()==willAddJobTimer.getHour()){
                                        weekAndHourLikeByTypeThree.add(weekLikeJob);
                                    }
                                }
                            }
                            //如果星期值和小时值都相同则最终判断分钟是否相同
                            if(weekAndHourLikeByTypeThree.size()>=1){
                                for(JobTimeSchedulerEntity weekAndHourLikeJob:weekAndHourLikeByTypeThree){
                                    if(weekAndHourLikeJob.getMinutes()==willAddJobTimer.getMinutes()){
                                        flag=true;
                                    }
                                }
                            }
                            break;

                        //类型4 每月执行
                        case 4:
                            //首先判断每月执行的日期xx号是否相同
                            List<JobTimeSchedulerEntity> dayLikeByTypeFour=new ArrayList<>();
                            for(JobTimeSchedulerEntity jobTime:typeEqualJobs){
                                if(jobTime.getDayofmonth()==willAddJobTimer.getDayofmonth()){
                                    dayLikeByTypeFour.add(jobTime);
                                }
                            }
                            //如果日期xx号相同则继续判断时间hour是否相同
                            List<JobTimeSchedulerEntity> dayAndHourLikeByTypeFour=new ArrayList<>();
                            if(dayLikeByTypeFour.size()>=1){
                                for(JobTimeSchedulerEntity dayLike:dayLikeByTypeFour){
                                    if(dayLike.getHour()==willAddJobTimer.getHour()){
                                        dayAndHourLikeByTypeFour.add(dayLike);
                                    }
                                }
                            }
                            //如果日期和时间值都相同则最终判断minute是否相同
                            if(dayAndHourLikeByTypeFour.size()>=1){
                                for(JobTimeSchedulerEntity dayAndHourLike:dayAndHourLikeByTypeFour){
                                    if(dayAndHourLike.getMinutes()==willAddJobTimer.getMinutes()){
                                        flag=true;
                                    }
                                }
                            }
                            break;
                        case 5:
                            //首先判断每月执行的日期xx号是否相同
                            List<JobTimeSchedulerEntity> month=new ArrayList<>();
                            for(JobTimeSchedulerEntity jobTime:typeEqualJobs){
                                if(jobTime.getMonth()==willAddJobTimer.getMonth()){
                                	month.add(jobTime);
                                }
                            }
                            //首先判断每月执行的日期xx号是否相同
                            List<JobTimeSchedulerEntity> dayLikeByTypeFive=new ArrayList<>();
                            for(JobTimeSchedulerEntity jobTime:month){
                                if(jobTime.getDayofmonth()==willAddJobTimer.getDayofmonth()){
                                    dayLikeByTypeFive.add(jobTime);
                                }
                            }
                            //如果日期xx号相同则继续判断时间hour是否相同
                            List<JobTimeSchedulerEntity> dayAndHourLikeByTypeFive=new ArrayList<>();
                            if(dayLikeByTypeFive.size()>=1){
                                for(JobTimeSchedulerEntity dayLike:dayLikeByTypeFive){
                                    if(dayLike.getHour()==willAddJobTimer.getHour()){
                                        dayAndHourLikeByTypeFive.add(dayLike);
                                    }
                                }
                            }
                            //如果日期和时间值都相同则最终判断minute是否相同
                            if(dayAndHourLikeByTypeFive.size()>=1){
                                for(JobTimeSchedulerEntity dayAndHourLike:dayAndHourLikeByTypeFive){
                                    if(dayAndHourLike.getMinutes()==willAddJobTimer.getMinutes()){
                                        flag=true;
                                    }
                                }
                            }
                        default:
                            break;
                    }
                }
            }
        }
        return flag;
    }

    @Override
    //移除被选中的定时任务
    public void deleteScheduler(String[] taskIdArray) throws Exception{
        SchedulerFactory factory=new StdSchedulerFactory();
        Scheduler scheduler=factory.getScheduler();
        for(String taskId:taskIdArray){
            //trigger名  组名
            TriggerKey triggerKey=TriggerKey.triggerKey(taskId + "trigger", CarteTaskManager.JOB_TIMER_TASK_GROUP);
            scheduler.pauseTrigger(triggerKey); //停止触发器
            scheduler.unscheduleJob(triggerKey);  //移除触发器
            scheduler.deleteJob(JobKey.jobKey(taskId,CarteTaskManager.JOB_TIMER_TASK_GROUP));   //删除作业
            //删除表中的定时作业记录
            jobSchedulerDao.deleteScheduler(Long.valueOf(taskId));
        }
    }

    //分页形式获取所有的定时作业信息(包括查询功能)
    @Override
    public PageforBean getAllSchedulerByPage(int start,int limit,Integer typeId,String hostName,String jobName,String userGroupName) throws Exception {
        if(!StringDateUtil.isEmpty(hostName)){
            SlaveEntity slave=slaveDao.getSlaveByHostName(hostName);
            hostName=slave.getSlaveId()+"_"+hostName;
        }
        List<JobTimeSchedulerEntity> jobs=jobSchedulerDao.getTimerJobByPage(start,limit,typeId,hostName,jobName,userGroupName);
        for(JobTimeSchedulerEntity job:jobs){
            int index=job.getSlaves().indexOf("_");
            job.setHostName(job.getSlaves().substring(index + 1));
            switch(job.getSchedulertype()){
                case 1:
                    job.setTimerInfo("每隔"+job.getIntervalminutes()+"分钟执行");
                    break;
                case 2:
                    job.setTimerInfo("每天"+job.getHour()+":"+job.getMinutes()+"执行");
                    break;
                case 3:
                    String week= StringDateUtil.getWeekByValue(job.getWeekday());
                    job.setTimerInfo("每个"+week+"的"+job.getHour()+":"+job.getMinutes()+"执行");
                    break;
                case 4:
                    job.setTimerInfo("每月"+job.getDayofmonth()+"号"+job.getHour()+":"+job.getMinutes()+"执行");
                    break;
                case 5:
                    job.setTimerInfo("每年"+job.getMonth()+"月"+job.getDayofmonth()+"号"+job.getHour()+":"+job.getMinutes()+"执行");
                    break;
                default:
                    break;
            }
        }
        PageforBean bean=new PageforBean();
        //查询总条数
        bean.setRoot(jobs);
        bean.setTotalProperty(jobSchedulerDao.getTotalCount(typeId,hostName,jobName,userGroupName));
        return bean;
    }

    @Override
    public JSONObject beforeUpdate(String taskId) throws Exception{
        JobTimeSchedulerEntity schedulerJob=jobSchedulerDao.getSchedulerBytaskId(Long.valueOf(taskId));
        return JSONObject.fromObject(schedulerJob);
    }

    @Override
    public boolean updateSchedulerJob(Map<String, Object> params,HttpServletRequest request) throws Exception{
        boolean isSuccess=false;
        //获取修改前对象
        JobTimeSchedulerEntity oldJobScheduler=jobSchedulerDao.getSchedulerBytaskId(Long.valueOf(params.get("taskId").toString()));
        String taskJobId=oldJobScheduler.getIdJobtask();
        String triggerName=taskJobId+"trigger";

        //获取修改前的trigger
        SchedulerFactory factory=new StdSchedulerFactory();
        Scheduler scheduler=factory.getScheduler();
        
        TriggerKey triggerKey=TriggerKey.triggerKey(triggerName, CarteTaskManager.JOB_TIMER_TASK_GROUP);
        Trigger oldtrigger=scheduler.getTrigger(triggerKey);
        //把新值赋给旧对象 并且根据不同的定时类型拼接不同的cron表达式
        String slaves=oldJobScheduler.getSlaves();
        SlaveEntity slave=slaveDao.getSlaveByHostName(slaves.substring(slaves.indexOf("_") + 1));
        String type=params.get("typeChoose").toString().trim();
        if(type.equals("间隔重复")){
            Integer intervalMinutes=Integer.valueOf(params.get("intervalminute").toString());
            oldJobScheduler.setIntervalminutes(intervalMinutes);
            oldJobScheduler.setSchedulertype(1);
            oldJobScheduler.setWeekday(null);
            oldJobScheduler.setDayofmonth(null);
            oldJobScheduler.setHour(null);
            oldJobScheduler.setMinutes(null);
            //间隔执行
            long currentTime = System.currentTimeMillis() +  intervalMinutes* 60 * 1000;
            Date date = new Date(currentTime);
            oldtrigger= newTrigger()
                    .withIdentity(oldJobScheduler.getIdJobtask() + "trigger",CarteTaskManager.JOB_TIMER_TASK_GROUP)
                    .startAt(date)
                    .withSchedule(simpleSchedule().withIntervalInMinutes(intervalMinutes).repeatForever())
                    .build();
            /*oldtrigger = newTrigger().withIdentity(taskJobId + "trigger", CarteTaskManager.JOB_TIMER_TASK_GROUP).
                    withSchedule(cronSchedule("0 " + "*//*" + intervalMinutes + " * * * ?")).build();
            System.out.println("* " + "*//*" + intervalMinutes + " * * * ?");*/
        }else if(type.equals("每天执行")){
            Integer minutesByDay=Integer.valueOf(params.get("minute").toString());
            Integer hourByDay=Integer.valueOf(params.get("hour").toString());

            oldJobScheduler.setMinutes(minutesByDay);
            oldJobScheduler.setHour(hourByDay);
            oldJobScheduler.setSchedulertype(2);
            oldJobScheduler.setIntervalminutes(null);
            oldJobScheduler.setWeekday(null);
            oldJobScheduler.setDayofmonth(null);
            //设置每天执行的调度规则
            oldtrigger=newTrigger().withIdentity(triggerName,CarteTaskManager.JOB_TIMER_TASK_GROUP)
            .withSchedule(cronSchedule("0 " + minutesByDay + " " + hourByDay + " * * ?")).build();
        }else if(type.equals("每周执行")){
            Integer weekday=StringDateUtil.getIntWeek(params.get("weekChoose").toString());
            Integer minuteByWeek=Integer.valueOf(params.get("minute").toString());
            Integer hourByWeek=Integer.valueOf(params.get("hour").toString());

            oldJobScheduler.setWeekday(weekday);
            oldJobScheduler.setMinutes(Integer.valueOf(params.get("minute").toString()));
            oldJobScheduler.setHour(Integer.valueOf(params.get("hour").toString()));
            oldJobScheduler.setSchedulertype(3);
            oldJobScheduler.setDayofmonth(null);
            oldJobScheduler.setIntervalminutes(null);
            //设置每周执行调度规则
            oldtrigger = newTrigger()
                    .withIdentity(taskJobId + "trigger", CarteTaskManager.JOB_TIMER_TASK_GROUP)
                    .startNow()
                    .withSchedule(weeklyOnDayAndHourAndMinute(weekday,hourByWeek, minuteByWeek))
                    .build();

        }else if(type.equals("每月执行")){
            Integer dayOfMonth=StringDateUtil.getdayInt(params.get("monthChoose").toString());
            Integer minuteByMonth=Integer.valueOf(params.get("minute").toString());
            Integer hourByMonth=Integer.valueOf(params.get("hour").toString());

            oldJobScheduler.setDayofmonth(dayOfMonth);
            oldJobScheduler.setMinutes(minuteByMonth);
            oldJobScheduler.setHour(hourByMonth);
            oldJobScheduler.setWeekday(null);
            oldJobScheduler.setIntervalminutes(null);
            oldJobScheduler.setSchedulertype(4);
            //设置每月执行的trigger
            oldtrigger = newTrigger().withIdentity(taskJobId +"trigger", CarteTaskManager.JOB_TIMER_TASK_GROUP).
                    withSchedule(cronSchedule("0 " + minuteByMonth + " " + hourByMonth + " " + dayOfMonth + " * ?")).build();
        }else if(type.equals("每年执行")){
            Integer month=StringDateUtil.getMonth(params.get("month1Choose").toString());
            Integer dayOfMonth=StringDateUtil.getdayInt(params.get("monthChoose").toString());
            Integer minuteByMonth=Integer.valueOf(params.get("minute").toString());
            Integer hourByMonth=Integer.valueOf(params.get("hour").toString());

            oldJobScheduler.setMonth(month);
            oldJobScheduler.setDayofmonth(dayOfMonth);
            oldJobScheduler.setMinutes(minuteByMonth);
            oldJobScheduler.setHour(hourByMonth);
            oldJobScheduler.setWeekday(null);
            oldJobScheduler.setIntervalminutes(null);
            oldJobScheduler.setSchedulertype(5);
            //设置每月执行的trigger
            oldtrigger = newTrigger().withIdentity(taskJobId +"trigger", CarteTaskManager.JOB_TIMER_TASK_GROUP).
                    withSchedule(cronSchedule("0 " + minuteByMonth + " " + hourByMonth + " " + dayOfMonth + " "+ month + " ? * ")).build();
        }
        
        JobServiceImpl js=new JobServiceImpl();
        //判断是否在数据库中是否有执行周期完全相同的一个作业
        boolean isAlike=this.judgeJobIsAlike(oldJobScheduler);

        if(!isAlike){
            TriggerBuilder<Trigger> builder=TriggerBuilder.newTrigger();
            builder.withIdentity(oldJobScheduler.getIdJobtask() + "trigger", CarteTaskManager.JOB_TIMER_TASK_GROUP);
            //设置修改后的trigger
            builder.startNow();
            scheduler.rescheduleJob(triggerKey, oldtrigger);
			JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(taskJobId.toString(), CarteTaskManager.JOB_TIMER_TASK_GROUP));
            jobDetail.getJobDataMap().put("slave", slave);
            String username=oldJobScheduler.getUsername();
            jobDetail.getJobDataMap().put("loginUser",userDao.getUserbyName(username).get(0));
            //更新数据库
            jobSchedulerDao.updateScheduler(oldJobScheduler);
            isSuccess=true;
        }
        return isSuccess;
    }
}
