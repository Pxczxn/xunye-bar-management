const STATUS_MAP = {
  UNPAID: '待付款',
  PAID: '已付款',
  CANCELLED: '已取消',
};

Page({
  data: {
    orderNo: '',
    totalAmount: '0.00',
    statusText: '',
  },

  onLoad(options) {
    const raw = options.totalAmount || '0';
    this.setData({
      orderNo: options.orderNo || '',
      totalAmount: Number(raw).toFixed(2),
      statusText: STATUS_MAP[options.status] || options.status || '',
    });
  },

  onViewDetail() {
    wx.navigateTo({
      url: '/pages/order-detail/index?orderNo=' + this.data.orderNo,
    });
  },

  onBackMenu() {
    wx.reLaunch({ url: '/pages/menu/index' });
  },
});
