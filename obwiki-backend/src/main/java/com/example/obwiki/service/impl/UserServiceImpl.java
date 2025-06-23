package com.example.obwiki.service.impl;

import com.example.obwiki.entity.User;
import com.example.obwiki.mapper.UserMapper;
import com.example.obwiki.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
