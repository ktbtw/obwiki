package com.example.obwiki.service.impl;

import com.example.obwiki.entity.Category;
import com.example.obwiki.mapper.CategoryMapper;
import com.example.obwiki.service.ICategoryService;
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
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
