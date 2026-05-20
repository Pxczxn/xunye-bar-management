package com.xunye.admin.service;

import com.xunye.admin.dto.OrderCreateDTO;
import com.xunye.admin.dto.CustomerProfileUpdateDTO;
import com.xunye.admin.dto.CustomerWxLoginDTO;
import com.xunye.admin.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CustomerService {

    ShopInfoVO getShopInfo();

    CustomerTableVO getTableByCode(String tableCode);

    List<CustomerTableVO> listAvailableTables();

    List<CustomerCategoryVO> listActiveCategories();

    List<CustomerProductVO> listProducts(Long categoryId, String keyword);

    CustomerProductVO getProductDetail(Long id);

    CustomerOrderSubmitVO createOrder(OrderCreateDTO dto);

    List<OrderPageVO> listOrders(Integer page, Integer size, String status, LocalDate date, LocalDate startDate, LocalDate endDate, Boolean all);

    List<OrderDateMarkerVO> listOrderDateMarkers(LocalDate month);

    OrderPageVO getOrderDetailByOrderNo(String orderNo);

    List<CustomerMessageVO> listMessages(String phone);

    CustomerStatsVO getCustomerStats(String phone);

    CustomerInfoVO getCustomerMemberInfo(String phone);

    CustomerInfoVO updateCustomerProfile(CustomerProfileUpdateDTO dto);

    CustomerInfoVO wxLogin(CustomerWxLoginDTO dto);

    String uploadAvatar(String phone, MultipartFile file);

    List<CustomerCouponVO> listCoupons(String phone);

    CustomerCouponVO exchangePointsForCoupon(String phone, Long rewardId);

    List<CustomerPointsRecordVO> listPointsRecords(String phone);

    List<MemberLevelVO> listMemberLevels();

    List<ActivityVO> listActiveActivities();

}
