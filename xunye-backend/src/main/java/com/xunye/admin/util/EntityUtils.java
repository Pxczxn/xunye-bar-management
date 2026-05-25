package com.xunye.admin.util;

import com.xunye.admin.common.BusinessException;

/**
 * 实体工具类
 */
public class EntityUtils {

    /**
     * 检查实体是否为空，如果为空则抛出业务异常
     *
     * @param entity     实体对象
     * @param entityName 实体名称（用于错误提示）
     * @param <T>        实体类型
     * @return 非空的实体对象
     * @throws BusinessException 当实体为空时抛出 404 异常
     */
    public static <T> T requireNonNull(T entity, String entityName) {
        if (entity == null) {
            throw new BusinessException(404, entityName + "不存在");
        }
        return entity;
    }

    /**
     * 检查实体是否为空，如果为空则抛出自定义错误码的业务异常
     *
     * @param entity     实体对象
     * @param code       错误码
     * @param message    错误消息
     * @param <T>        实体类型
     * @return 非空的实体对象
     * @throws BusinessException 当实体为空时抛出异常
     */
    public static <T> T requireNonNull(T entity, int code, String message) {
        if (entity == null) {
            throw new BusinessException(code, message);
        }
        return entity;
    }

}
