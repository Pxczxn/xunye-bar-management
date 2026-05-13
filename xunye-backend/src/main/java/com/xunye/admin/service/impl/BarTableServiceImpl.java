package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.BarTableQueryDTO;
import com.xunye.admin.dto.BarTableSaveDTO;
import com.xunye.admin.dto.BarTableStatusDTO;
import com.xunye.admin.entity.BarTable;
import com.xunye.admin.entity.OrderInfo;
import com.xunye.admin.entity.TableArea;
import com.xunye.admin.mapper.BarTableMapper;
import com.xunye.admin.mapper.OrderInfoMapper;
import com.xunye.admin.mapper.TableAreaMapper;
import com.xunye.admin.service.BarTableService;
import com.xunye.admin.vo.BarTableVO;
import com.xunye.admin.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 桌台 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class BarTableServiceImpl implements BarTableService {

    private final BarTableMapper barTableMapper;
    private final TableAreaMapper tableAreaMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    public PageResult<BarTableVO> getTablePage(BarTableQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<BarTable> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getAreaId() != null) {
            wrapper.eq(BarTable::getAreaId, queryDTO.getAreaId());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(BarTable::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(BarTable::getName, queryDTO.getKeyword());
        }

        wrapper.orderByDesc(BarTable::getCreatedAt);

        long total = barTableMapper.selectCount(wrapper);

        wrapper.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize);
        List<BarTable> tables = barTableMapper.selectList(wrapper);

        // 批量查询区域名称
        List<Long> areaIds = tables.stream()
                .map(BarTable::getAreaId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> areaNameMap = Map.of();
        if (!areaIds.isEmpty()) {
            LambdaQueryWrapper<TableArea> areaWrapper = new LambdaQueryWrapper<>();
            areaWrapper.in(TableArea::getId, areaIds);
            areaNameMap = tableAreaMapper.selectList(areaWrapper).stream()
                    .collect(Collectors.toMap(TableArea::getId, TableArea::getName));
        }

        Map<Long, String> finalAreaNameMap = areaNameMap;
        List<BarTableVO> voList = tables.stream().map(table -> {
            BarTableVO vo = new BarTableVO();
            BeanUtils.copyProperties(table, vo);
            vo.setAreaName(finalAreaNameMap.get(table.getAreaId()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public void createTable(BarTableSaveDTO dto) {
        TableArea area = tableAreaMapper.selectById(dto.getAreaId());
        if (area == null) {
            throw new BusinessException(404, "区域不存在");
        }

        BarTable table = new BarTable();
        BeanUtils.copyProperties(dto, table);
        if (table.getCapacity() == null) {
            table.setCapacity(1);
        }
        barTableMapper.insert(table);
    }

    @Override
    public void updateTable(Long id, BarTableSaveDTO dto) {
        BarTable table = barTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }

        TableArea area = tableAreaMapper.selectById(dto.getAreaId());
        if (area == null) {
            throw new BusinessException(404, "区域不存在");
        }

        BeanUtils.copyProperties(dto, table);
        barTableMapper.updateById(table);
    }

    @Override
    public void updateTableStatus(Long id, BarTableStatusDTO dto) {
        BarTable table = barTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }

        table.setStatus(dto.getStatus());
        barTableMapper.updateById(table);
    }

    @Override
    public void clearTable(Long id) {
        BarTable table = barTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }
        if ("DISABLED".equals(table.getStatus())) {
            throw new BusinessException("停用桌台不能清台");
        }

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getTableId, id)
               .ne(OrderInfo::getStatus, "CANCELLED")
               .ne(OrderInfo::getServeStatus, "FINISHED");
        long activeOrderCount = orderInfoMapper.selectCount(wrapper);
        if (activeOrderCount > 0) {
            throw new BusinessException("该桌台仍有待处理或制作中的订单，不能清台");
        }

        table.setStatus("EMPTY");
        barTableMapper.updateById(table);
    }

    @Override
    public void deleteTable(Long id) {
        BarTable table = barTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException(404, "桌台不存在");
        }

        barTableMapper.deleteById(id);
    }

}
