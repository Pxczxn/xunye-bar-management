package com.xunye.admin.service;

import com.xunye.admin.dto.*;
import com.xunye.admin.vo.PageResult;
import com.xunye.admin.vo.StaffPageVO;

/**
 * 员工账号 Service 接口
 */
public interface StaffService {

    /**
     * 分页查询员工
     */
    PageResult<StaffPageVO> getStaffPage(StaffQueryDTO queryDTO);

    /**
     * 查询员工详情
     */
    StaffPageVO getStaffDetail(Long id);

    /**
     * 新增员工
     */
    void createStaff(StaffSaveDTO dto);

    /**
     * 编辑员工
     */
    void updateStaff(Long id, StaffUpdateDTO dto);

    /**
     * 更新员工状态
     */
    void updateStaffStatus(Long id, StaffStatusDTO dto);

    /**
     * 重置员工密码
     */
    void resetPassword(Long id, StaffPasswordDTO dto);

    /**
     * 删除员工
     */
    void deleteStaff(Long id);

}
