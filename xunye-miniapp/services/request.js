const BASE_URL = 'http://localhost:8848';

function request(options) {
  return new Promise((resolve, reject) => {
    const customerToken = wx.getStorageSync('customerToken');
    const rawData = options.data || {};
    const data = Object.fromEntries(Object.entries(rawData).filter(([, v]) => v !== null && v !== undefined));
    wx.request({
      url: BASE_URL + '/api/customer' + options.url,
      method: options.method || 'GET',
      data,
      header: {
        'Content-Type': 'application/json',
        ...(customerToken ? { Authorization: 'Bearer ' + customerToken } : {}),
      },
      success(res) {
        if (res.statusCode === 200) {
          const body = res.data;
          if (body.code === 200) {
            resolve(body.data);
          } else {
            wx.showToast({
              title: body.message || '请求失败',
              icon: 'none',
            });
            reject(body);
          }
        } else if (res.statusCode === 401) {
          wx.showToast({
            title: '登录已过期',
            icon: 'none',
          });
          reject(res.data);
        } else {
          wx.showToast({
            title: res.data?.message || '服务器异常',
            icon: 'none',
          });
          reject(res.data);
        }
      },
      fail(err) {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none',
        });
        reject(err);
      },
    });
  });
}

module.exports = { request };
