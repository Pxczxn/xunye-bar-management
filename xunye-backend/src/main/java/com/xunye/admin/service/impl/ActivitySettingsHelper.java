package com.xunye.admin.service.impl;

import com.xunye.admin.common.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

final class ActivitySettingsHelper {

    private ActivitySettingsHelper() {
    }

    static Map<String, Object> normalize(String type, Map<String, Object> rawSettings) {
        Map<String, Object> settings = rawSettings == null ? Map.of() : rawSettings;
        Map<String, Object> normalized = switch (type) {
            case "DISCOUNT" -> normalizeDiscount(settings);
            case "COUPON" -> normalizeCoupon(settings);
            case "POINTS" -> normalizePoints(settings);
            case "SPECIAL" -> normalizeSpecial(settings);
            default -> throw new BusinessException(400, "不支持的活动类型");
        };
        copyScopeSettings(settings, normalized);
        return normalized;
    }

    static String summarize(String type, Map<String, Object> settings) {
        Map<String, Object> safeSettings = settings == null ? Map.of() : settings;
        return switch (type) {
            case "DISCOUNT" -> toDiscountSummary(safeSettings);
            case "COUPON" -> toCouponSummary(safeSettings);
            case "POINTS" -> toPointsSummary(safeSettings);
            case "SPECIAL" -> toSpecialSummary(safeSettings);
            default -> "";
        };
    }

    private static Map<String, Object> normalizeDiscount(Map<String, Object> settings) {
        BigDecimal discountRate = requiredNumber(settings, "discountRate", "请输入折扣力度");
        ensureRange(discountRate, new BigDecimal("0.1"), new BigDecimal("9.9"), "折扣力度需在0.1到9.9折之间");

        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("discountRate", scale(discountRate));

        BigDecimal minAmount = optionalNumber(settings, "minAmount");
        if (minAmount != null) {
            ensureMin(minAmount, BigDecimal.ZERO, "折扣门槛金额不能小于0");
            normalized.put("minAmount", scale(minAmount));
        }
        return normalized;
    }

    private static Map<String, Object> normalizeCoupon(Map<String, Object> settings) {
        BigDecimal discountAmount = requiredNumber(settings, "discountAmount", "请输入优惠券金额");
        ensureMin(discountAmount, new BigDecimal("0.01"), "优惠券金额必须大于0");

        BigDecimal minAmount = requiredNumber(settings, "minAmount", "请输入优惠券使用门槛");
        ensureMin(minAmount, discountAmount, "优惠券使用门槛不能小于优惠金额");

        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("discountAmount", scale(discountAmount));
        normalized.put("minAmount", scale(minAmount));
        return normalized;
    }

    private static Map<String, Object> normalizePoints(Map<String, Object> settings) {
        BigDecimal pointsMultiplier = requiredNumber(settings, "pointsMultiplier", "请输入积分倍率");
        ensureRange(pointsMultiplier, BigDecimal.ONE, new BigDecimal("10"), "积分倍率需在1到10倍之间");

        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("pointsMultiplier", scale(pointsMultiplier));
        return normalized;
    }

    private static Map<String, Object> normalizeSpecial(Map<String, Object> settings) {
        BigDecimal specialPrice = requiredNumber(settings, "specialPrice", "请输入特惠价");
        ensureMin(specialPrice, new BigDecimal("0.01"), "特惠价必须大于0");

        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("specialPrice", scale(specialPrice));

        BigDecimal originalPrice = optionalNumber(settings, "originalPrice");
        if (originalPrice != null) {
            ensureMin(originalPrice, specialPrice, "原价不能低于特惠价");
            normalized.put("originalPrice", scale(originalPrice));
        }

        BigDecimal stockLimit = optionalNumber(settings, "stockLimit");
        if (stockLimit != null) {
            BigDecimal rounded = stockLimit.setScale(0, RoundingMode.DOWN);
            ensureMin(rounded, BigDecimal.ONE, "限量份数必须大于0");
            normalized.put("stockLimit", rounded.intValue());
        }
        return normalized;
    }

    private static String toDiscountSummary(Map<String, Object> settings) {
        BigDecimal discountRate = optionalNumber(settings, "discountRate");
        BigDecimal minAmount = optionalNumber(settings, "minAmount");
        if (discountRate == null) {
            return "";
        }
        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) {
            return "满" + plain(minAmount) + "元打" + plain(discountRate) + "折";
        }
        return "直接打" + plain(discountRate) + "折";
    }

    private static String toCouponSummary(Map<String, Object> settings) {
        BigDecimal discountAmount = optionalNumber(settings, "discountAmount");
        BigDecimal minAmount = optionalNumber(settings, "minAmount");
        if (discountAmount == null || minAmount == null) {
            return "";
        }
        return "满" + plain(minAmount) + "减" + plain(discountAmount);
    }

    private static String toPointsSummary(Map<String, Object> settings) {
        BigDecimal pointsMultiplier = optionalNumber(settings, "pointsMultiplier");
        if (pointsMultiplier == null) {
            return "";
        }
        return "消费积分" + plain(pointsMultiplier) + "倍";
    }

    private static String toSpecialSummary(Map<String, Object> settings) {
        BigDecimal specialPrice = optionalNumber(settings, "specialPrice");
        BigDecimal originalPrice = optionalNumber(settings, "originalPrice");
        Object stockLimit = settings.get("stockLimit");
        if (specialPrice == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder("特惠价").append(plain(specialPrice)).append("元");
        if (originalPrice != null) {
            summary.append("，原价").append(plain(originalPrice)).append("元");
        }
        if (stockLimit instanceof Number number && number.intValue() > 0) {
            summary.append("，限量").append(number.intValue()).append("份");
        }
        return summary.toString();
    }

    private static BigDecimal requiredNumber(Map<String, Object> settings, String key, String message) {
        BigDecimal value = optionalNumber(settings, key);
        if (value == null) {
            throw new BusinessException(400, message);
        }
        return value;
    }

    private static BigDecimal optionalNumber(Map<String, Object> settings, String key) {
        Object value = settings.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String str && str.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(400, "活动设置格式不正确");
        }
    }

    private static void ensureRange(BigDecimal value, BigDecimal min, BigDecimal max, String message) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException(400, message);
        }
    }

    private static void ensureMin(BigDecimal value, BigDecimal min, String message) {
        if (value.compareTo(min) < 0) {
            throw new BusinessException(400, message);
        }
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private static String plain(BigDecimal value) {
        return scale(value).toPlainString();
    }

    private static void copyScopeSettings(Map<String, Object> source, Map<String, Object> target) {
        if (source.containsKey("scopeProductType")) {
            target.put("scopeProductType", source.get("scopeProductType"));
        }
        if (source.containsKey("productIds")) {
            target.put("productIds", source.get("productIds"));
        }
        if (source.containsKey("categoryIds")) {
            target.put("categoryIds", source.get("categoryIds"));
        }
        if (source.containsKey("scopeTableType")) {
            target.put("scopeTableType", source.get("scopeTableType"));
        }
        if (source.containsKey("tableIds")) {
            target.put("tableIds", source.get("tableIds"));
        }
        if (source.containsKey("areaIds")) {
            target.put("areaIds", source.get("areaIds"));
        }
    }
}
