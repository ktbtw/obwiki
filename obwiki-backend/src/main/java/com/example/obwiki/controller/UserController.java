package com.example.obwiki.controller;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSONObject;
import com.example.obwiki.rep.UserLoginReq;
import com.example.obwiki.rep.UserQueryReq;
import com.example.obwiki.rep.UserResetPasswordReq;
import com.example.obwiki.rep.UserSaveReq;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.resp.UserLoginResp;
import com.example.obwiki.resp.UserQueryResp;
import com.example.obwiki.service.IUserService;
import com.example.obwiki.utils.SnowFlake;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@RestController
@RequestMapping("/obwiki/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private IUserService userService;

    @Autowired
    private SnowFlake snowFlake;
    @Autowired
    private RedisTemplate redisTemplate;

    //登录
    @PostMapping("/login")
    public CommonResp login(@Valid @RequestBody UserLoginReq req) {
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp<UserLoginResp> resp = new CommonResp<>();
        UserLoginResp userLoginResp = userService.login(req);

        Long token = snowFlake.nextId();
        LOG.info("生成单点登录token：{}，并放入redis中", token);
        userLoginResp.setToken(token.toString());
        redisTemplate.opsForValue().set(token.toString(), JSONObject.toJSONString(userLoginResp), 3600 * 24, TimeUnit.SECONDS);

        resp.setContent(userLoginResp);
        return resp;
    }

    @GetMapping("/logout/{token}")
    public CommonResp logout(@PathVariable String token) {
        CommonResp resp = new CommonResp<>();
        redisTemplate.delete(token);
        LOG.info("从redis中删除token: {}", token);
        return resp;
    }

    @GetMapping("/list")
    public CommonResp list(@Valid UserQueryReq req) {
        System.out.println("req = " + req);
        CommonResp<PageResp<UserQueryResp>> resp = new CommonResp<>(true,"查询成功",null);

        PageResp<UserQueryResp> pageResp = userService.listByname((UserQueryReq) req);
        resp.setContent(pageResp);
        System.out.println("pageResp = " + pageResp);
        return resp;
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody UserSaveReq req) {
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp resp = new CommonResp<>(true,"成功",null);
        userService.save(req);
        return resp;
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>(true,"删除成功",null);
        userService.delete(id);
        return resp;
    }

    @PostMapping("resetPassword")
    public CommonResp resetPassword(@Valid @RequestBody UserResetPasswordReq req) {
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp resp = new CommonResp<>(true,"成功",null);
        userService.resetPassword(req);
        return resp;
    }

    @PostMapping("/uploadAvatar")
    public CommonResp<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            LOG.warn("上传头像失败：文件为空");
            return new CommonResp<>(false, "文件为空", null);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        // 使用项目根目录下的avatar文件夹，确保目录存在
        String filePath = System.getProperty("user.dir") + "/avatar/";
        File dir = new File(filePath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            LOG.info("头像目录不存在，已自动创建：{}，结果：{}", filePath, created);
        }
        File dest = new File(filePath + fileName);
        try {
            file.transferTo(dest);
            String url = "/avatar/" + fileName;
            LOG.info("头像上传成功，文件名：{}，存储路径：{}，访问URL：{}", fileName, dest.getAbsolutePath(), url);
            return new CommonResp<>(true, "上传成功", url);
        } catch (IOException e) {
            LOG.error("头像上传异常：", e);
            return new CommonResp<>(false, "上传失败", null);
        }
    }
}