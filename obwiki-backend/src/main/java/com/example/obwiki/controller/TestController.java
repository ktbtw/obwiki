package com.example.obwiki.controller;

import com.example.obwiki.rep.DemoReq;
import com.example.obwiki.resp.CommonResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Api("测试Web接口")//Swagger声明类加@Api注解, 并说明
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法") //Swagger声明方法添加@ApiOperation注解,并说明
    public CommonResp sayHello(@Valid @RequestBody DemoReq demoReq){
//        if( result.hasErrors()){//如果获取到了错误
//            Map<String,String> map=new HashMap<>();
//            //1.获取错误的校验结果
//            result.getFieldErrors().forEach((item)->{
//                //获取发生错误时的message
//                String message = item.getDefaultMessage();
//                //获取发生错误的字段
//                String field = item.getField();
//                map.put(field,message);
//            });
//            return new CommonResp(false,"查询出错!",map);
//        }else {
//            //TODO 执行注册功能
//            //registService.regist(registVo);
//        }
        return new CommonResp(true,"查询成功",demoReq);
    }
}
