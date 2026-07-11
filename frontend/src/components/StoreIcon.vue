<template>
  <img
    class="store-icon-image"
    :class="[`store-icon-image--${size}`]"
    :src="iconSrc"
    :alt="altText"
    loading="lazy"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StoreType } from '@/api/applications'
import appleIcon from '@/assets/logo/as.png'
import googlePlayIcon from '@/assets/logo/gp.png'

const props = withDefaults(
  defineProps<{
    storeType?: StoreType | null
    size?: 'sm' | 'md' | 'lg'
  }>(),
  {
    storeType: 'APP_STORE',
    size: 'md'
  }
)

const iconSrc = computed(() => (props.storeType === 'GOOGLE_PLAY' ? googlePlayIcon : appleIcon))
const altText = computed(() => {
  if (props.storeType === 'GOOGLE_PLAY') return 'Google Play'
  if (props.storeType === 'IPAD_STORE') return 'iPad Store'
  return 'App Store'
})
</script>

<style scoped>
.store-icon-image {
  display: inline-block;
  flex: 0 0 auto;
  object-fit: contain;
  vertical-align: middle;
}

.store-icon-image--sm {
  width: 16px;
  height: 16px;
}

.store-icon-image--md {
  width: 20px;
  height: 20px;
}

.store-icon-image--lg {
  width: 24px;
  height: 24px;
}
</style>
