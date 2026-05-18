<script lang="ts" setup>
import XunyeShell from '@/components/xunye/XunyeShell.vue'

definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '寻野 XUNYE',
  },
})
</script>

<template>
  <XunyeShell initial-view="index" />
</template>
