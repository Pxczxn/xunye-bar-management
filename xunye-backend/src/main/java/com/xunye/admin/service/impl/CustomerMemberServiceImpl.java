package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.entity.Customer;
import com.xunye.admin.mapper.CustomerMapper;
import com.xunye.admin.service.CustomerMemberService;
import com.xunye.admin.vo.CustomerMemberVO;
import com.xunye.admin.vo.MemberLevelVO;
import com.xunye.admin.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerMemberServiceImpl implements CustomerMemberService {

    private final CustomerMapper customerMapper;

    private static final Map<String, LevelConfig> LEVEL_CONFIG = new LinkedHashMap<>();

    static {
        LEVEL_CONFIG.put("REGULAR", new LevelConfig("普通会员", BigDecimal.ZERO, "REGULAR"));
        LEVEL_CONFIG.put("VIP", new LevelConfig("VIP会员", new BigDecimal("1000"), "VIP"));
        LEVEL_CONFIG.put("SVIP", new LevelConfig("SVIP会员", new BigDecimal("5000"), "SVIP"));
    }

    @Override
    public PageResult<CustomerMemberVO> getCustomerPage(Integer pageNum, Integer pageSize, String keyword, String memberLevel) {
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Customer::getNickname, keyword)
                    .or().like(Customer::getPhone, keyword));
        }
        if (StringUtils.hasText(memberLevel)) {
            wrapper.eq(Customer::getMemberLevel, memberLevel);
        }
        wrapper.orderByDesc(Customer::getTotalAmount);

        Long total = customerMapper.selectCount(wrapper);
        wrapper.last("LIMIT " + ((current - 1) * size) + "," + size);
        List<Customer> customers = customerMapper.selectList(wrapper);

        List<CustomerMemberVO> records = customers.stream()
                .map(this::toCustomerMemberVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, total, current, size);
    }

    @Override
    public CustomerMemberVO getCustomerDetail(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException(404, "顾客不存在");
        }
        return toCustomerMemberVO(customer);
    }

    @Override
    public void updateMemberLevel(Long id, String memberLevel) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException(404, "顾客不存在");
        }
        if (!LEVEL_CONFIG.containsKey(memberLevel)) {
            throw new BusinessException("无效的会员等级");
        }
        customer.setMemberLevel(memberLevel);
        customerMapper.updateById(customer);
    }

    @Override
    public List<MemberLevelVO> listMemberLevels() {
        return LEVEL_CONFIG.entrySet().stream().map(entry -> {
            MemberLevelVO vo = new MemberLevelVO();
            LevelConfig config = entry.getValue();
            vo.setLevel(entry.getKey());
            vo.setName(config.name);
            vo.setMinAmount(config.minAmount);
            vo.setDescription(config.name + " - 累计消费满" + config.minAmount + "元");
            return vo;
        }).collect(Collectors.toList());
    }

    private CustomerMemberVO toCustomerMemberVO(Customer customer) {
        CustomerMemberVO vo = new CustomerMemberVO();
        vo.setId(customer.getId());
        vo.setPhone(customer.getPhone());
        vo.setNickname(customer.getNickname());
        vo.setAvatar(customer.getAvatar());
        vo.setMemberLevel(customer.getMemberLevel());
        vo.setMemberLevelName(LEVEL_CONFIG.getOrDefault(customer.getMemberLevel(), LEVEL_CONFIG.get("REGULAR")).name);
        vo.setPoints(customer.getPoints() == null ? BigDecimal.ZERO : customer.getPoints());
        vo.setBalance(customer.getBalance() == null ? BigDecimal.ZERO : customer.getBalance());
        vo.setTotalOrders(customer.getTotalOrders() == null ? 0 : customer.getTotalOrders());
        vo.setTotalAmount(customer.getTotalAmount() == null ? BigDecimal.ZERO : customer.getTotalAmount());
        vo.setLastVisitAt(customer.getLastVisitAt());
        vo.setCreatedAt(customer.getCreatedAt());

        // 计算下一级
        BigDecimal totalAmount = customer.getTotalAmount() == null ? BigDecimal.ZERO : customer.getTotalAmount();
        String currentLevel = customer.getMemberLevel();
        String nextLevel = null;
        BigDecimal nextMin = BigDecimal.ZERO;
        for (Map.Entry<String, LevelConfig> entry : LEVEL_CONFIG.entrySet()) {
            if (entry.getValue().minAmount.compareTo(totalAmount) > 0) {
                nextLevel = entry.getKey();
                nextMin = entry.getValue().minAmount;
                break;
            }
        }
        if (nextLevel != null && !nextLevel.equals(currentLevel)) {
            vo.setNextLevelAmount(nextMin);
            vo.setNextLevelName(LEVEL_CONFIG.get(nextLevel).name);
        } else {
            vo.setNextLevelAmount(totalAmount);
            vo.setNextLevelName("已满级");
        }

        return vo;
    }

    private record LevelConfig(String name, BigDecimal minAmount, String level) {}
}
