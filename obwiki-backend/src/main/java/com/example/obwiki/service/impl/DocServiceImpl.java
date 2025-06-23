package com.example.obwiki.service.impl;

import com.example.obwiki.entity.Doc;
import com.example.obwiki.mapper.DocMapper;
import com.example.obwiki.service.IDocService;
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
public class DocServiceImpl extends ServiceImpl<DocMapper, Doc> implements IDocService {

}
