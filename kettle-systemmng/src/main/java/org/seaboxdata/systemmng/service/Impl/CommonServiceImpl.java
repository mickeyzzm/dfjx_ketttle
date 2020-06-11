package org.seaboxdata.systemmng.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.seaboxdata.systemmng.dao.CommonDao;
import org.seaboxdata.systemmng.entity.DatabaseConnEntity;
import org.seaboxdata.systemmng.entity.TaskGroupAttributeEntity;
import org.seaboxdata.systemmng.service.CommonService;

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
        cDao.updateDatabaseUserName(dbConn);
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
