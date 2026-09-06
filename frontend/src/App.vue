<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import ru from 'element-plus/es/locale/lang/ru'
import pt from 'element-plus/es/locale/lang/pt'
import es from 'element-plus/es/locale/lang/es'

const { locale } = useI18n()

const elementLocale = computed(() => ({ 'zh-CN':zhCn, 'en-US':en, 'ru-RU':ru, 'pt-PT':pt, 'es-ES':es }[locale.value] || en))
</script>

<style>
* {
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
  margin: 0;
}

body {
  font-family:
    Inter, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}
</style>
