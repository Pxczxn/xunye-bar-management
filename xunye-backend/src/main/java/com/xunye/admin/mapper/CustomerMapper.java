package com.xunye.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunye.admin.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
