package com.example.obwiki.service;

import com.example.obwiki.entity.Ebook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.obwiki.rep.EbookQueryReq;
import com.example.obwiki.resp.EbookQueryResp;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 电子书 服务类
 * </p>
 *
 * @author cr
 * @since 2025-06-23
 */
public interface IEbookService extends IService<Ebook> {

    List<EbookQueryResp> listByname(@Valid EbookQueryReq req);
}
