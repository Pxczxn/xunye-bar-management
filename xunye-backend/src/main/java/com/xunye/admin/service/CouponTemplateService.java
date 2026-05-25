package com.xunye.admin.service;

import com.xunye.admin.dto.CouponTemplateSaveDTO;
import com.xunye.admin.vo.CouponTemplateVO;
import com.xunye.admin.vo.PageResult;

public interface CouponTemplateService {

    /**
     * 分页查询优惠券模板
     */
    PageResult<CouponTemplateVO> getPage(Integer pageNum, Integer pageSize, String keyword, String type, Integer status);

    /**
     * 获取优惠券模板详情
     */
    CouponTemplateVO getDetail(Long id);

    /**
     * 创建优惠券模板
     */
    Long create(CouponTemplateSaveDTO dto);

    /**
     * 更新优惠券模板
     */
    void update(Long id, CouponTemplateSaveDTO dto);

    /**
     * 删除优惠券模板
     */
    void delete(Long id);

    /**
     * 启用/禁用优惠券模板
     */
    void updateStatus(Long id, Integer status);

}
