package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid=#{id}")
    public User getUserByOpenId(String id);

    public void insertUser(User user);

    @Select("select * from user where id=#{id}")
    public User getById(Long id);
}
