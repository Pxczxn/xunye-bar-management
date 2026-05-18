<script lang="ts" setup>
import XunyeShell from '@/components/xunye/XunyeShell.vue'

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '点餐',
  },
})
</script>

<template>
  <XunyeShell initial-view="menu" />
</template>
