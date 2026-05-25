package com.xunye.admin.service;

import com.xunye.admin.dto.DiscountRuleSaveDTO;
import com.xunye.admin.vo.DiscountRuleVO;
import com.xunye.admin.vo.PageResult;

import java.util.List;

public interface DiscountRuleService {

    /**
     * 分页查询折扣规则
     */
    PageResult<DiscountRuleVO> getPage(Integer pageNum, Integer pageSize, String keyword, String ruleType, Integer status);

    /**
     * 获取所有折扣规则
     */
    List<DiscountRuleVO> listAll();

    /**
     * 获取折扣规则详情
     */
    DiscountRuleVO getDetail(Long id);

    /**
     * 创建折扣规则
     */
    Long create(DiscountRuleSaveDTO dto);

    /**
     * 更新折扣规则
     */
    void update(Long id, DiscountRuleSaveDTO dto);

    /**
     * 删除折扣规则
     */
    void delete(Long id);

    /**
     * 更新折扣规则状态
     */
    void updateStatus(Long id, Integer status);

}
