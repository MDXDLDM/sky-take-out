package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private UserMapper userMapper;
    //微信相关配置
    @Autowired
    private WeChatProperties properties;
    @Autowired
    private JwtProperties jwtProperties;

    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String openid=getOpenId(userLoginDTO.getCode());
        //判断openid是否为空
        if (openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断用户是否是第一次登录
        User user=userMapper.getUserByOpenId(openid);
        if (user==null){
            //是新用户
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insertUser(user);
        }
        //将User转换为UserLoginVO
        UserLoginVO userLoginVO =new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        //生成JWT令牌
        Map<String,Object> claims=new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,userLoginVO.getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        userLoginVO.setToken(jwt);
        return userLoginVO;
    }
    private String getOpenId(String code){
        //给接口发送请求获得用户openId
        Map<String, String> map = new HashMap<>();
        map.put("appid", properties.getAppid());
        map.put("secret", properties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String s = HttpClientUtil.doGet(WX_LOGIN, map);
        //
        JSONObject jsonObject = JSON.parseObject(s);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
