package com.example.obwiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.obwiki.entity.Doc;
import com.example.obwiki.mapper.DocMapper;
import com.example.obwiki.rep.DocQueryReq;
import com.example.obwiki.rep.DocSaveReq;
import com.example.obwiki.resp.DocQueryResp;
import com.example.obwiki.resp.PageResp;
import com.example.obwiki.service.IDocService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.obwiki.utils.CopyUtil;
import com.example.obwiki.utils.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
@Service
public class DocServiceImpl extends ServiceImpl<DocMapper, Doc> implements IDocService {
    @Autowired
    private SnowFlake snowFlake;

    @Override
    public PageResp<DocQueryResp> listByname(DocQueryReq req) {

        QueryWrapper<Doc> queryWrapper = new QueryWrapper<Doc>();
        //第一个参数：该参数是一个布尔类型，只有该参数是true时，才将like条件拼接到sql中；本例中，如果name字段不为空，则拼接name字段的like查询条件；
        queryWrapper.like(StringUtils.isNotBlank(req.getName()),"name",req.getName());
        //创建分页对象
        Page<Doc> page = new Page<>(req.getPage(),req.getSize());
        page = this.baseMapper.selectPage(page, queryWrapper);

        List<DocQueryResp> resps = CopyUtil.copyList(page.getRecords(), DocQueryResp.class);
        //创建返回对象
        PageResp<DocQueryResp> pageResp = new PageResp<>();
        pageResp.setList(resps);
        pageResp.setTotal(page.getTotal());
        return pageResp;
    }

    @Override
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            // 新增
            long id = snowFlake.nextId();
            doc.setId(id);
            this.baseMapper.insert(doc);
        } else {
            // 更新
            this.baseMapper.updateById(doc);
        }
    }

    @Override
    public void delete(Long id) {
        this.baseMapper.deleteById(id);
    }

    @Override
    public List<DocQueryResp> all() {
        List<Doc> categories = this.baseMapper.selectList(new QueryWrapper<Doc>().orderByAsc("sort"));
        List<DocQueryResp> list = CopyUtil.copyList(categories, DocQueryResp.class);
        return list;
    }
}
