package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.TableAreaSaveDTO;
import com.xunye.admin.entity.BarTable;
import com.xunye.admin.entity.TableArea;
import com.xunye.admin.mapper.BarTableMapper;
import com.xunye.admin.mapper.TableAreaMapper;
import com.xunye.admin.service.TableAreaService;
import com.xunye.admin.vo.TableAreaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 桌台区域 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class TableAreaServiceImpl implements TableAreaService {

    private final TableAreaMapper tableAreaMapper;
    private final BarTableMapper barTableMapper;

    @Override
    public List<TableAreaVO> listAreas() {
        LambdaQueryWrapper<TableArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TableArea::getSort);
        List<TableArea> areas = tableAreaMapper.selectList(wrapper);

        return areas.stream().map(area -> {
            TableAreaVO vo = new TableAreaVO();
            BeanUtils.copyProperties(area, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void createArea(TableAreaSaveDTO dto) {
        TableArea area = new TableArea();
        BeanUtils.copyProperties(dto, area);
        tableAreaMapper.insert(area);
    }

    @Override
    public void updateArea(Long id, TableAreaSaveDTO dto) {
        TableArea area = tableAreaMapper.selectById(id);
        if (area == null) {
            throw new BusinessException(404, "区域不存在");
        }

        BeanUtils.copyProperties(dto, area);
        tableAreaMapper.updateById(area);
    }

    @Override
    public void deleteArea(Long id) {
        TableArea area = tableAreaMapper.selectById(id);
        if (area == null) {
            throw new BusinessException(404, "区域不存在");
        }

        LambdaQueryWrapper<BarTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BarTable::getAreaId, id);
        long count = barTableMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该区域下还有桌台，无法删除");
        }

        tableAreaMapper.deleteById(id);
    }

}
