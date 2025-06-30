package com.example.obwiki;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.example.obwiki.entity.User;
import com.example.obwiki.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

@SpringBootTest
public class ObwikiApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void queryUser(){
        List<User> users = userMapper.selectList(new QueryWrapper<>());
        System.out.println(users);
    }
}