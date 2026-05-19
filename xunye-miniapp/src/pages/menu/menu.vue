<script lang="ts" setup>
import XunyeShell from '@/components/xunye/XunyeShell.vue'

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '我要喝',
  },
})
</script>

<template>
  <XunyeShell initial-view="menu" />
</template>
