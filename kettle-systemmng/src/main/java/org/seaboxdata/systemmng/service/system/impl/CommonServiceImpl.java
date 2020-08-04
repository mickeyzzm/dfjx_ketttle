package org.seaboxdata.systemmng.service.system.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pentaho.di.core.util.StringUtil;
import org.seaboxdata.systemmng.dao.CommonDao;
import org.seaboxdata.systemmng.entity.DatabaseConnEntity;
import org.seaboxdata.systemmng.entity.TaskGroupAttributeEntity;
import org.seaboxdata.systemmng.service.system.CommonService;

import java.util.List;
import java.util.Map;

/**
 * Created by cRAZY on 2017/6/1.
 */
@Service
public class CommonServiceImpl implements CommonService{
    @Autowired
    protected CommonDao cDao;

    @Override
    public List<DatabaseConnEntity> getDatabases(String userGroupName) {
        return cDao.getDababasesConn(userGroupName);
    }

    @Override
    public void deleteDatabaseConn(Integer id) {
        cDao.deleteDatabaseAttr(id);
        cDao.deleteDatabaseMeta(id);
        cDao.deleteJobDatabase(id);
        cDao.deleteTransDatabase(id);
    }

    @Override
    public void updateDatabaseUserName(DatabaseConnEntity dbConn) {
    	List<DatabaseConnEntity> dataBaseList = cDao.selectDatabaseByParams(dbConn);
    	if(dataBaseList.size() > 0) {
    		for (DatabaseConnEntity databaseConnEntity : dataBaseList) {
				if(StringUtil.isEmpty(databaseConnEntity.getUserGroup())) {
					cDao.updateDatabaseUserName(dbConn);
				}
			}
    	}
    }

	@Override
	public List<TaskGroupAttributeEntity> getTaskGroupAttribute(TaskGroupAttributeEntity attr) {
		return cDao.getTaskGroupAttribute(attr);
	}

	@Override
	public List<Map<String, Object>> queryRepositoryByUserGroup(TaskGroupAttributeEntity entity) {
		return cDao.queryRepositoryByUserGroup(entity);
	}
}
