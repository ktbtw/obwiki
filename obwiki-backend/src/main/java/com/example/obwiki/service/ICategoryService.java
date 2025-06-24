package com.example.obwiki.service;

import com.example.obwiki.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.obwiki.rep.CategoryQueryReq;
import com.example.obwiki.rep.CategorySaveReq;
import com.example.obwiki.resp.CategoryQueryResp;
import com.example.obwiki.resp.PageResp;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
public interface ICategoryService extends IService<Category> {

    PageResp<CategoryQueryResp> listByname(CategoryQueryReq req);

    void save(CategorySaveReq req);

    void delete(Long id);
}
