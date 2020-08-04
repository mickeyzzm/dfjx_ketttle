package org.seaboxdata.systemmng.dao;

import org.springframework.stereotype.Repository;
import org.seaboxdata.systemmng.entity.DatabaseConnEntity;
import org.seaboxdata.systemmng.entity.SlaveEntity;
import org.seaboxdata.systemmng.entity.TaskGroupAttributeEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by cRAZY on 2017/5/31.
 */
@Repository
public interface CommonDao {
    public List<DatabaseConnEntity> getDababasesConn(String userGroupName);

    public void deleteDatabaseAttr(Integer id);

    public void deleteDatabaseMeta(Integer id);

    public void deleteJobDatabase(Integer id);

    public void deleteTransDatabase(Integer id);

    public void updateDatabaseUserName(DatabaseConnEntity dbConn);

	public List<TaskGroupAttributeEntity> getTaskGroupAttribute(TaskGroupAttributeEntity attr);

	public List<Map<String, Object>> queryRepositoryByUserGroup(TaskGroupAttributeEntity entity);

	public List<DatabaseConnEntity> selectDatabaseByParams(DatabaseConnEntity dbConn);
}
