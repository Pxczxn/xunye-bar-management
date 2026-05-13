const { request } = require('./request');

/**
 * 统一支付入口。根据后端返回的 provider 自动路由：
 * - MOCK: 调用 confirm 接口完成模拟支付
 * - WECHAT: 调用 wx.requestPayment（待实现）
 */
function pay(orderNo) {
  return request({ url: '/orders/' + orderNo + '/payments', method: 'POST' })
    .then((payment) => {
      if (payment.provider === 'MOCK') {
        return request({ url: '/payments/' + payment.paymentNo + '/confirm', method: 'POST' });
      }
      return Promise.reject(new Error('微信支付尚未实现'));
    });
}

module.exports = { pay };
