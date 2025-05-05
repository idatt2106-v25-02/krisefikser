<script setup lang="ts">
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const content = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  content.value = newValue
})

watch(content, (newValue) => {
  emit('update:modelValue', newValue)
})

const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ 'header': 1 }, { 'header': 2 }],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'script': 'sub'}, { 'script': 'super' }],
      [{ 'indent': '-1'}, { 'indent': '+1' }],
      [{ 'direction': 'rtl' }],
      [{ 'size': ['small', false, 'large', 'huge'] }],
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
      [{ 'color': [] }, { 'background': [] }],
      [{ 'font': [] }],
      [{ 'align': [] }],
      ['clean']
    ]
  }
}
</script>

<template>
  <div class="rich-text-editor">
    <QuillEditor
      v-model:content="content"
      :options="editorOptions"
      :placeholder="placeholder"
      contentType="html"
      class="h-64"
    />
  </div>
</template>

<style>
.rich-text-editor {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  overflow: hidden;
}

.rich-text-editor .ql-toolbar {
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.rich-text-editor .ql-container {
  border: none;
}

.rich-text-editor .ql-editor {
  min-height: 200px;
  padding: 1rem;
}

.rich-text-editor .ql-editor.ql-blank::before {
  color: #9ca3af;
}
</style>
