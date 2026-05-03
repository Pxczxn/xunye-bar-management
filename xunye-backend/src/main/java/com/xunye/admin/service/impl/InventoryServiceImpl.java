package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.InventoryAdjustDTO;
import com.xunye.admin.entity.InventoryRecord;
import com.xunye.admin.entity.Product;
import com.xunye.admin.mapper.InventoryRecordMapper;
import com.xunye.admin.mapper.ProductMapper;
import com.xunye.admin.service.InventoryService;
import com.xunye.admin.vo.InventoryRecordVO;
import com.xunye.admin.vo.InventoryRecordsPageVO;
import com.xunye.admin.vo.InventoryWarningVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductMapper productMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    private static final Map<String, String> TYPE_TEXT_MAP = Map.of(
            "IN", "入库",
            "OUT", "出库",
            "LOSS", "损耗",
            "ADJUST", "盘点调整"
    );

    private static final List<String> VALID_TYPES = Arrays.asList("IN", "OUT", "LOSS", "ADJUST");

    @Override
    public List<InventoryWarningVO> getWarnings() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0)
               .gt(Product::getSafeStock, 0)
               .apply("stock < safe_stock");

        List<Product> products = productMapper.selectList(wrapper);

        return products.stream().map(p -> {
            String level;
            if (p.getStock() <= p.getSafeStock() * 0.5) {
                level = "HIGH";
            } else {
                level = "MEDIUM";
            }
            InventoryWarningVO vo = new InventoryWarningVO();
            vo.setProductId(p.getId());
            vo.setProductName(p.getName());
            vo.setCurrentStock(p.getStock());
            vo.setSafeStock(p.getSafeStock());
            vo.setUnit(p.getUnit());
            vo.setWarningLevel(level);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public InventoryRecordsPageVO getRecords(Integer pageNum, Integer pageSize, String productName, String type) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(productName)) {
            wrapper.like(InventoryRecord::getProductName, productName);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(InventoryRecord::getType, type);
        }

        wrapper.orderByDesc(InventoryRecord::getCreatedAt);

        long total = inventoryRecordMapper.selectCount(wrapper);

        wrapper.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize);

        List<InventoryRecord> records = inventoryRecordMapper.selectList(wrapper);

        List<InventoryRecordVO> voList = records.stream().map(r -> {
            InventoryRecordVO vo = new InventoryRecordVO();
            vo.setId(r.getId());
            vo.setProductId(r.getProductId());
            vo.setProductName(r.getProductName());
            vo.setType(r.getType());
            vo.setTypeText(TYPE_TEXT_MAP.getOrDefault(r.getType(), r.getType()));
            vo.setChangeQuantity(r.getChangeQuantity());
            vo.setBeforeStock(r.getBeforeStock());
            vo.setAfterStock(r.getAfterStock());
            vo.setReason(r.getReason());
            vo.setOperatorName(r.getOperatorName());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        return new InventoryRecordsPageVO(voList, total, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(InventoryAdjustDTO dto) {
        if (!VALID_TYPES.contains(dto.getType())) {
            throw new BusinessException("无效的操作类型，必须为IN、OUT、LOSS、ADJUST之一");
        }

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        int beforeStock = product.getStock();
        int afterStock;
        int changeQuantity;

        switch (dto.getType()) {
            case "IN":
                afterStock = beforeStock + dto.getQuantity();
                changeQuantity = dto.getQuantity();
                break;
            case "OUT":
            case "LOSS":
                afterStock = beforeStock - dto.getQuantity();
                if (afterStock < 0) {
                    throw new BusinessException("库存不足，无法扣减");
                }
                changeQuantity = -dto.getQuantity();
                break;
            case "ADJUST":
                afterStock = dto.getQuantity();
                changeQuantity = dto.getQuantity() - beforeStock;
                break;
            default:
                throw new BusinessException("无效的操作类型");
        }

        product.setStock(afterStock);
        productMapper.updateById(product);

        InventoryRecord record = new InventoryRecord();
        record.setProductId(product.getId());
        record.setProductName(product.getName());
        record.setType(dto.getType());
        record.setChangeQuantity(changeQuantity);
        record.setBeforeStock(beforeStock);
        record.setAfterStock(afterStock);
        record.setReason(dto.getReason());
        record.setOperatorName("系统管理员");
        inventoryRecordMapper.insert(record);
    }
}
