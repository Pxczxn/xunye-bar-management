ALTER TABLE member_activity
    ADD COLUMN settings JSON NULL COMMENT '活动配置JSON' AFTER cover_image;

UPDATE member_activity
SET settings = CASE type
    WHEN 'DISCOUNT' THEN JSON_OBJECT('discountRate', 8.0, 'minAmount', 0)
    WHEN 'COUPON' THEN JSON_OBJECT('discountAmount', 20, 'minAmount', 100)
    WHEN 'POINTS' THEN JSON_OBJECT('pointsMultiplier', 2)
    WHEN 'SPECIAL' THEN JSON_OBJECT('specialPrice', 99, 'originalPrice', 129, 'stockLimit', 50)
    ELSE JSON_OBJECT()
END
WHERE settings IS NULL;
