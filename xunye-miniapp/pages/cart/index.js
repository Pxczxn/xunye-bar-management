const { submitOrder } = require('../../services/customer');

const CART_KEY = 'cart';

Page({
  data: {
    cartItems: [],
    totalAmount: '0.00',
    remark: '',
    submitting: false,
    tableName: '',
  },

  onShow() {
    this.loadCart();
    this.refreshTableName();
  },

  loadCart() {
    const cartItems = wx.getStorageSync(CART_KEY) || [];
    this.setData({ cartItems: this.computeSubtotals(cartItems) });
    this.calcTotal();
  },

  refreshTableName() {
    const currentTable = wx.getStorageSync('currentTable');
    if (currentTable) {
      this.setData({ tableName: currentTable.areaName + ' ' + currentTable.name });
    } else {
      this.setData({ tableName: '' });
    }
  },

  computeSubtotals(cartItems) {
    return cartItems.map((item) => {
      item.priceFormatted = Number(item.price).toFixed(2);
      item.subtotal = (item.price * item.quantity).toFixed(2);
      return item;
    });
  },

  calcTotal() {
    const total = this.data.cartItems.reduce((sum, item) => {
      return sum + item.price * item.quantity;
    }, 0);
    this.setData({ totalAmount: total.toFixed(2) });
  },

  saveCart(cartItems) {
    wx.setStorageSync(CART_KEY, cartItems);
  },

  onQuantityAdd(e) {
    const idx = e.currentTarget.dataset.index;
    const cartItems = this.data.cartItems;
    if (cartItems[idx].quantity >= cartItems[idx].stock) {
      wx.showToast({ title: '已达库存上限', icon: 'none' });
      return;
    }
    cartItems[idx].quantity += 1;
    cartItems[idx].subtotal = (cartItems[idx].price * cartItems[idx].quantity).toFixed(2);
    this.setData({ cartItems });
    this.calcTotal();
    this.saveCart(cartItems);
  },

  onQuantityReduce(e) {
    const idx = e.currentTarget.dataset.index;
    const cartItems = this.data.cartItems;
    if (cartItems[idx].quantity <= 1) return;
    cartItems[idx].quantity -= 1;
    cartItems[idx].subtotal = (cartItems[idx].price * cartItems[idx].quantity).toFixed(2);
    this.setData({ cartItems });
    this.calcTotal();
    this.saveCart(cartItems);
  },

  onDelete(e) {
    const idx = e.currentTarget.dataset.index;
    const cartItems = this.data.cartItems;
    cartItems.splice(idx, 1);
    this.setData({ cartItems });
    this.calcTotal();
    this.saveCart(cartItems);
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  onGoMenu() {
    wx.reLaunch({ url: '/pages/menu/index' });
  },

  onSubmit() {
    const { cartItems, remark, submitting } = this.data;

    if (submitting) return;

    if (cartItems.length === 0) {
      wx.showToast({ title: '购物车为空', icon: 'none' });
      return;
    }

    const currentTable = wx.getStorageSync('currentTable');
    if (!currentTable || !currentTable.id) {
      wx.navigateTo({ url: '/pages/table-select/index' });
      return;
    }

    const data = {
      tableId: currentTable.id,
      items: cartItems.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
      remark: remark || '',
    };

    this.setData({ submitting: true });

    submitOrder(data)
      .then((res) => {
        wx.removeStorageSync(CART_KEY);
        const currentTable = wx.getStorageSync('currentTable');
        const tableName = currentTable ? (currentTable.areaName + ' ' + currentTable.name) : '';
        wx.redirectTo({
          url: '/pages/payment/index?orderNo=' + res.orderNo
            + '&totalAmount=' + res.totalAmount
            + '&tableName=' + encodeURIComponent(tableName),
        });
      })
      .catch(() => {})
      .finally(() => {
        this.setData({ submitting: false });
      });
  },
});
