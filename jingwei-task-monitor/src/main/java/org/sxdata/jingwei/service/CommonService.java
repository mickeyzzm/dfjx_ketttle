package org.sxdata.jingwei.service;

import org.sxdata.jingwei.entity.DatabaseConnEntity;
import org.sxdata.jingwei.entity.SlaveEntity;

import java.util.List;

/**
 * Created by cRAZY on 2017/6/1.
 */
public interface CommonService {
    public List<DatabaseConnEntity> getDatabases(String userGroupName);

    public void deleteDatabaseConn(Integer id);


    public void updateDatabaseUserName(DatabaseConnEntity dbConn);




}

