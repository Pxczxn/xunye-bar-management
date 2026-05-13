const { getProducts, getCategories, getTableInfo } = require('../../services/customer');

Page({
  data: {
    categories: [],
    activeCategoryId: null,
    products: [],
    loading: true,
    error: false,
    keyword: '',
    cartCount: 0,
    cartTotal: '0.00',
    tableName: '',
  },

  onLoad(options) {
    if (options.tableCode) {
      this.loadTableInfo(options.tableCode);
    }
    this.loadCategories();
    this.loadProducts();
  },

  onShow() {
    const currentTable = wx.getStorageSync('currentTable');
    if (!currentTable || !currentTable.id) {
      wx.redirectTo({ url: '/pages/table-select/index' });
      return;
    }
    this.setData({ tableName: currentTable.areaName + ' ' + currentTable.name });
    this.refreshCart();
  },

  loadTableInfo(tableCode) {
    getTableInfo(tableCode)
      .then((tableInfo) => {
        wx.setStorageSync('currentTable', tableInfo);
        this.setData({ tableName: tableInfo.areaName + ' ' + tableInfo.name });
      })
      .catch(() => {});
  },

  loadCategories() {
    getCategories()
      .then((list) => {
        const all = [{ id: null, name: '全部' }];
        this.setData({ categories: all.concat(list || []) });
      })
      .catch(() => {});
  },

  loadProducts() {
    this.setData({ loading: true, error: false });

    getProducts({ categoryId: this.data.activeCategoryId, keyword: this.data.keyword })
      .then((list) => {
        const products = (list || []).map((item) => {
          item.priceFormatted = Number(item.price).toFixed(2);
          return item;
        });
        this.setData({ products, loading: false });
      })
      .catch(() => {
        this.setData({ loading: false, error: true });
      });
  },

  onCategoryTap(e) {
    const id = e.currentTarget.dataset.id;
    if (id === this.data.activeCategoryId) return;
    this.setData({ activeCategoryId: id });
    this.loadProducts();
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadProducts();
  },

  onSearchClear() {
    this.setData({ keyword: '' });
    this.loadProducts();
  },

  onAddToCart(e) {
    const product = e.currentTarget.dataset.product;
    const cart = wx.getStorageSync('cart') || [];
    const idx = cart.findIndex((item) => item.productId === product.id);
    if (idx > -1) {
      if (cart[idx].quantity >= product.stock) {
        wx.showToast({ title: '已达库存上限', icon: 'none' });
        return;
      }
      cart[idx].quantity += 1;
    } else {
      cart.push({
        productId: product.id,
        name: product.name,
        price: product.price,
        quantity: 1,
        stock: product.stock,
        unit: product.unit,
        spec: product.spec || '',
      });
    }
    wx.setStorageSync('cart', cart);
    this.refreshCart();
    wx.showToast({ title: '已加入购物车', icon: 'success', duration: 1500 });
  },

  refreshCart() {
    const cart = wx.getStorageSync('cart') || [];
    let count = 0;
    let total = 0;
    for (let i = 0; i < cart.length; i++) {
      count += cart[i].quantity;
      total += cart[i].price * cart[i].quantity;
    }
    this.setData({ cartCount: count, cartTotal: total.toFixed(2) });
  },

  onGoCart() {
    wx.navigateTo({ url: '/pages/cart/index' });
  },

  onRetry() {
    this.loadProducts();
  },
});
