<script lang="ts" setup>
import XunyeShell from '@/components/xunye/XunyeShell.vue'

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '订单',
  },
})
</script>

<template>
  <XunyeShell initial-view="orders" />
</template>
