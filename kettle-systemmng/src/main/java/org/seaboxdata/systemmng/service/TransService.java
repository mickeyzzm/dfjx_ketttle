package org.seaboxdata.systemmng.service;

import net.sf.json.JSONObject;
import org.seaboxdata.systemmng.entity.TransformationEntity;

import java.util.List;

/**
 * Created by cRAZY on 2017/2/22.
 */
public interface TransService {
    public JSONObject findTrans(int start,int limit,String namme,String date,String userGroupName, String username) throws Exception;

    public void deleteTransformation(String transPath,String flag) throws Exception;

    public void executeTransformation(String path,Integer slaveId) throws Exception;

    public List<TransformationEntity> getTransPath(List<TransformationEntity> items) throws Exception;

    public TransformationEntity getTransByName(String transName) throws Exception;

    public boolean updateTransName(String oldName,String newName);
}
