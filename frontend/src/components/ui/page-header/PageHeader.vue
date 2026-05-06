<script lang="ts" setup>
interface Props {
  title: string
}

defineProps<Props>()
defineOptions({ inheritAttrs: false })
</script>

<template>
  <div class="flex justify-between items-center mb-6">
    <h2 class="text-2xl font-bold text-gray-800" v-bind="$attrs">{{ title }}</h2>
    <slot name="actions" />
  </div>
</template>
