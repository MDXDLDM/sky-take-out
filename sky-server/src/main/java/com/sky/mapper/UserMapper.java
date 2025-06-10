package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid=#{id}")
    public User getUserByOpenId(String id);

    public void insertUser(User user);

    @Select("select * from user where id=#{id}")
    public User getById(Long id);

    @Select("select count(*) from user where create_time>=#{timeStart} and create_time<=#{timeEnd}")
    Integer getByDate(LocalDateTime timeStart, LocalDateTime timeEnd);

    @Select("select count(*) from user where create_time<=#{timeEnd}")
    Integer getAllUserCounts(LocalDateTime timeEnd);
}
