const { request } = require('./request');

function getShopInfo() {
  return request({ url: '/shop/info' });
}

function getTableInfo(tableCode) {
  return request({ url: '/tables/' + tableCode });
}

function getCategories() {
  return request({ url: '/categories' });
}

function getProducts(params) {
  return request({ url: '/products', data: params });
}

function getProductDetail(id) {
  return request({ url: '/products/' + id });
}

function submitOrder(data) {
  return request({ url: '/orders', method: 'POST', data });
}

function getOrderDetail(orderNo) {
  return request({ url: '/orders/' + orderNo });
}

function listTables() {
  return request({ url: '/tables' });
}

module.exports = {
  getShopInfo,
  getTableInfo,
  getCategories,
  getProducts,
  getProductDetail,
  submitOrder,
  getOrderDetail,
  listTables,
};
