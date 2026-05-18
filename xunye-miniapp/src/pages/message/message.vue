<script lang="ts" setup>
import XunyeShell from '@/components/xunye/XunyeShell.vue'

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '消息',
  },
})
</script>

<template>
  <XunyeShell initial-view="message" />
</template>
