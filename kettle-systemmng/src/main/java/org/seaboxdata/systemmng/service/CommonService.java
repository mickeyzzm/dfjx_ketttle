package org.seaboxdata.systemmng.service;

import org.seaboxdata.systemmng.entity.DatabaseConnEntity;
import org.seaboxdata.systemmng.entity.SlaveEntity;

import java.util.List;

/**
 * Created by cRAZY on 2017/6/1.
 */
public interface CommonService {
    public List<DatabaseConnEntity> getDatabases(String userGroupName);

    public void deleteDatabaseConn(Integer id);


    public void updateDatabaseUserName(DatabaseConnEntity dbConn);




}

