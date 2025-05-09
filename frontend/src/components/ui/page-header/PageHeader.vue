<script lang="ts" setup>
interface Props {
  title: string
}

defineProps<Props>()
</script>

<template>
  <div class="flex justify-between items-center mb-6">
    <h2 class="text-2xl font-bold text-gray-800">{{ title }}</h2>
    <slot name="actions" />
  </div>
</template>
