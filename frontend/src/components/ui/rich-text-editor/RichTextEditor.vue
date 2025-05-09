<!-- RichTextEditor.vue -->
<script lang="ts" setup>
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const content = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    content.value = newValue
  },
)

watch(content, (newValue) => {
  emit('update:modelValue', newValue)
})

// Enhanced editor options with font size and more formatting options
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'], // toggled buttons
      ['blockquote', 'code-block'],

      [{ header: 1 }, { header: 2 }, { header: 3 }], // custom button values

      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ indent: '-1' }, { indent: '+1' }], // outdent/indent

      [{ size: ['small', false, 'large', 'huge'] }], // font size
      [{ color: [] }, { background: [] }], // dropdown with defaults
      [{ align: [] }],

      ['clean'], // remove formatting button
    ],
  },
  formats: [
    'bold',
    'italic',
    'underline',
    'strike',
    'blockquote',
    'code-block',
    'header',
    'list',
    'bullet',
    'indent',
    'size',
    'color',
    'background',
    'align',
  ],
}

// This ensures that when the component is mounted, any existing content
// is properly processed by Quill's formatter
onMounted(() => {
  // Make sure we're initializing with clean HTML
  if (content.value) {
    // This will trigger the editor to properly parse existing HTML content
    setTimeout(() => {
      // Small delay to ensure QuillEditor is fully mounted
      const tempContent = content.value
      content.value = ''
      setTimeout(() => {
        content.value = tempContent
      }, 50)
    }, 100)
  }
})
</script>

<template>
  <div class="rich-text-editor h-64 max-h-96 overflow-y-auto">
    <QuillEditor
      v-model:content="content"
      :options="editorOptions"
      :placeholder="placeholder"
      class="h-64"
      contentType="html"
    />
  </div>
</template>
