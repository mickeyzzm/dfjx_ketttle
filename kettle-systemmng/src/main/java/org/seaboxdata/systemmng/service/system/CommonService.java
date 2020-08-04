package org.seaboxdata.systemmng.service.system;

import java.util.List;
import java.util.Map;

import org.seaboxdata.systemmng.entity.DatabaseConnEntity;
import org.seaboxdata.systemmng.entity.TaskGroupAttributeEntity;

/**
 * Created by cRAZY on 2017/6/1.
 */
public interface CommonService {
    public List<DatabaseConnEntity> getDatabases(String userGroupName);

    public void deleteDatabaseConn(Integer id);


    public void updateDatabaseUserName(DatabaseConnEntity dbConn);

    
    public List<TaskGroupAttributeEntity> getTaskGroupAttribute(TaskGroupAttributeEntity attr);

	public List<Map<String, Object>> queryRepositoryByUserGroup(TaskGroupAttributeEntity entity);


}

