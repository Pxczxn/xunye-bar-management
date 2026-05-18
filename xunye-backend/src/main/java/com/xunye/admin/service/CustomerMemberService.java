package com.xunye.admin.service;

import com.xunye.admin.vo.CustomerMemberVO;
import com.xunye.admin.vo.MemberLevelVO;
import com.xunye.admin.vo.PageResult;

import java.util.List;

public interface CustomerMemberService {

    PageResult<CustomerMemberVO> getCustomerPage(Integer pageNum, Integer pageSize, String keyword, String memberLevel);

    CustomerMemberVO getCustomerDetail(Long id);

    void updateMemberLevel(Long id, String memberLevel);

    List<MemberLevelVO> listMemberLevels();

}
