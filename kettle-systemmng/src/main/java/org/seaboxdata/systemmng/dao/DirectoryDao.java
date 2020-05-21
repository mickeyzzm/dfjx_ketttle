package org.seaboxdata.systemmng.dao;

import org.springframework.stereotype.Repository;
import org.seaboxdata.systemmng.entity.DirectoryEntity;

/**
 * Created by cRAZY on 2017/2/27.
 */
@Repository
public interface DirectoryDao {
    public DirectoryEntity getDirectoryById(Integer id);
}
