package com.xunye.admin.service;

import com.xunye.admin.dto.ActivitySaveDTO;
import com.xunye.admin.vo.ActivityVO;
import com.xunye.admin.vo.PageResult;

public interface MemberActivityService {

    PageResult<ActivityVO> getActivityPage(Integer pageNum, Integer pageSize, String keyword, String type, Integer status);

    ActivityVO getActivityDetail(Long id);

    void createActivity(ActivitySaveDTO dto);

    void updateActivity(Long id, ActivitySaveDTO dto);

    void deleteActivity(Long id);

}
