package com.wdhis.common.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ALAN on 2018/5/22.
 */
public interface DataConvert<T> {

    public T GetEntity(ResultSet rs) throws SQLException;
}
