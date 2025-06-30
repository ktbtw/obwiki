package com.example.obwiki.service;

import com.example.obwiki.entity.Ebook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.obwiki.rep.EbookQueryReq;
import com.example.obwiki.rep.EbookSaveReq;
import com.example.obwiki.rep.PageReq;
import com.example.obwiki.resp.EbookQueryResp;
import com.example.obwiki.resp.PageResp;

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

    PageResp<EbookQueryResp> listByname(@Valid EbookQueryReq req);

    public void save(EbookSaveReq req);

    public void delete(Long id);
}
