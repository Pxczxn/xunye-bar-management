package com.xunye.admin.service;

import com.xunye.admin.dto.MemberLevelConfigSaveDTO;
import com.xunye.admin.vo.MemberLevelConfigVO;

import java.util.List;

public interface MemberLevelConfigService {

    /**
     * 获取所有会员等级配置
     */
    List<MemberLevelConfigVO> listAll();

    /**
     * 获取会员等级配置详情
     */
    MemberLevelConfigVO getDetail(Long id);

    /**
     * 创建会员等级配置
     */
    Long create(MemberLevelConfigSaveDTO dto);

    /**
     * 更新会员等级配置
     */
    void update(Long id, MemberLevelConfigSaveDTO dto);

    /**
     * 删除会员等级配置
     */
    void delete(Long id);

    /**
     * 更新会员等级状态
     */
    void updateStatus(Long id, Integer status);

}
