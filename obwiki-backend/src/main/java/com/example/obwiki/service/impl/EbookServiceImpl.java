package com.example.obwiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.obwiki.entity.Ebook;
import com.example.obwiki.mapper.EbookMapper;
import com.example.obwiki.rep.EbookQueryReq;
import com.example.obwiki.rep.EbookSaveReq;
import com.example.obwiki.resp.EbookQueryResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.service.IEbookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.obwiki.utils.CopyUtil;
import com.example.obwiki.utils.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 电子书 服务实现类
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@Service
public class EbookServiceImpl extends ServiceImpl<EbookMapper, Ebook> implements IEbookService {


    @Autowired
    private SnowFlake snowFlake;

    @Override
    public PageResp<EbookQueryResp> listByname(EbookQueryReq req) {

        QueryWrapper<Ebook> queryWrapper = new QueryWrapper<Ebook>();
        //第一个参数：该参数是一个布尔类型，只有该参数是true时，才将like条件拼接到sql中；本例中，如果name字段不为空，则拼接name字段的like查询条件；
        queryWrapper.like(StringUtils.isNotBlank(req.getName()),"name",req.getName());
        queryWrapper.eq(ObjectUtils.isNotEmpty(req.getCategoryId2()),"category2_id",req.getCategoryId2());
        //创建分页对象
        Page<Ebook> page = new Page<>(req.getPage(),req.getSize());
        page = this.baseMapper.selectPage(page, queryWrapper);

        System.out.println("数据库查询实体："+page.getRecords());
        List<EbookQueryResp> resps = CopyUtil.copyList(page.getRecords(), EbookQueryResp.class);
        System.out.println("转化后的DTO："+resps);
        //创建返回对象
        PageResp<EbookQueryResp> pageResp = new PageResp<>();
        pageResp.setList(resps);
        pageResp.setTotal(page.getTotal());
        return pageResp;
    }

    @Override
    public void save(EbookSaveReq req) {
        Ebook ebook = CopyUtil.copy(req, Ebook.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            // 雪花算法生成id
            long id = snowFlake.nextId();
            ebook.setId(String.valueOf(id));
            // 新增
            this.baseMapper.insert(ebook);
        } else {
            // 更新
            this.baseMapper.updateById(ebook);
        }
    }

    //删除
    @Override
    public void delete(Long id) {

        this.baseMapper.deleteById(id);
    }

}
