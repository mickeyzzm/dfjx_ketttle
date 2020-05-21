package org.seaboxdata.systemmng.dao;

import org.springframework.stereotype.Repository;
import org.seaboxdata.systemmng.entity.DatabaseConnEntity;
import org.seaboxdata.systemmng.entity.SlaveEntity;

import java.util.List;

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
}
