<script lang="ts" setup>
interface Props {
  noPadding?: boolean
}

defineProps<Props>()
</script>

<template>
  <div class="bg-white rounded-lg shadow overflow-hidden mb-6">
    <div :class="{ 'p-4': !noPadding }">
      <slot />
    </div>
  </div>
</template>
