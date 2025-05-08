<script setup lang="ts">
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { ref, computed } from 'vue'
import { Pencil, Trash2, Plus, Search, Image as ImageIcon } from 'lucide-vue-next'

// Import shadcn components
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'

// Import API hooks
import {
  useGetAllArticles,
  useCreateArticle,
  useUpdateArticle,
  useDeleteArticle,
} from '@/api/generated/article/article'
import type { ArticleRequest, ArticleResponse } from '@/api/generated/model'

// State
const showDialog = ref(false)
const isEditing = ref(false)
const searchQuery = ref('')
const selectedArticle = ref<ArticleResponse | null>(null)

// Form state
const articleForm = ref<ArticleRequest>({
  title: '',
  text: '',
  imageUrl: '',
})

// Fetch articles using TanStack Query
const {
  data: articlesData,
  isLoading: isLoadingArticles,
  refetch: refetchArticles,
} = useGetAllArticles()

// Create article mutation
const { mutate: createArticleMutation } = useCreateArticle({
  mutation: {
    onSuccess: () => {
      refetchArticles()
      closeDialog()
    },
  },
})

// Update article mutation
const { mutate: updateArticleMutation } = useUpdateArticle({
  mutation: {
    onSuccess: () => {
      refetchArticles()
      closeDialog()
    },
  },
})

// Delete article mutation
const { mutate: deleteArticleMutation } = useDeleteArticle({
  mutation: {
    onSuccess: () => {
      refetchArticles()
    },
  },
})

// Filter articles
const filteredArticles = computed(() => {
  if (!articlesData.value) return []
  let result = [...articlesData.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (article) =>
        article.title.toLowerCase().includes(query) || article.text.toLowerCase().includes(query),
    )
  }

  return result
})

// Open dialog for creating/editing
const openDialog = (article?: ArticleResponse) => {
  if (article) {
    isEditing.value = true
    selectedArticle.value = article
    articleForm.value = {
      title: article.title,
      text: article.text,
      imageUrl: article.imageUrl,
    }
  } else {
    isEditing.value = false
    selectedArticle.value = null
    articleForm.value = {
      title: '',
      text: '',
      imageUrl: '',
    }
  }
  showDialog.value = true
}

// Close dialog and reset form
const closeDialog = () => {
  showDialog.value = false
  selectedArticle.value = null
  articleForm.value = {
    title: '',
    text: '',
    imageUrl: '',
  }
}

// Handle form submission
const handleSubmit = () => {
  if (isEditing.value && selectedArticle.value) {
    updateArticleMutation({
      id: selectedArticle.value.id,
      data: articleForm.value,
    })
  } else {
    createArticleMutation({
      data: articleForm.value,
    })
  }
}

// Delete article
const deleteArticle = (id: number) => {
  if (confirm('Er du sikker på at du vil slette denne artikkelen?')) {
    deleteArticleMutation({ id })
  }
}

// Open image in new tab
const openImageInNewTab = (url: string) => {
  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  link.click()
}

const parseIsoString = (isoString?: string): Date | null => {
  if (!isoString) return null
  try {
    return new Date(isoString)
  } catch (e) {
    console.error('Error parsing date string:', isoString, e)
    return null
  }
}

// Format date
const formatDate = (isoString?: string) => {
  if (!isoString) return ''

  const date = parseIsoString(isoString)
  if (!date || isNaN(date.getTime())) return ''

  try {
    return date.toLocaleDateString('nb-NO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    // Fallback to English if Norwegian locale is not supported
    return date.toLocaleDateString('en', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }
}
</script>

<template>
  <AdminLayout>
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-800">Administrer artikler</h2>
        <Button
          variant="default"
          class="flex items-center bg-blue-600 hover:bg-blue-700 text-white"
          @click="openDialog()"
        >
          <Plus class="h-4 w-4 mr-1" />
          Ny artikkel
        </Button>
      </div>

      <!-- Search -->
      <div class="bg-white rounded-lg shadow overflow-hidden mb-6">
        <div class="p-4 flex items-center border-b">
          <div class="flex items-center rounded-lg bg-gray-100 px-3 py-2 w-full md:w-64">
            <Search class="h-4 w-4 text-gray-500" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Søk artikler..."
              class="bg-transparent border-0 outline-none ml-2 text-gray-700 w-full"
            />
          </div>
        </div>

        <!-- Loading state -->
        <div v-if="isLoadingArticles" class="p-8 text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
          <p class="mt-2 text-gray-500">Laster artikler...</p>
        </div>

        <!-- Articles list -->
        <div v-else class="relative">
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Tittel
                  </th>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Opprettet
                  </th>
                  <th
                    scope="col"
                    class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Handlinger
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="article in filteredArticles" :key="article.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4">
                    <div class="flex items-center">
                      <img
                        v-if="article.imageUrl"
                        :src="article.imageUrl"
                        :alt="article.title"
                        class="h-10 w-10 rounded-full object-cover mr-3"
                      />
                      <div class="text-sm font-medium text-gray-900">{{ article.title }}</div>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ formatDate(article.createdAt) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div class="flex justify-center space-x-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        class="text-blue-600 hover:text-blue-800"
                        @click="openDialog(article)"
                      >
                        <Pencil class="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        class="text-red-600 hover:text-red-800"
                        @click="deleteArticle(article.id)"
                      >
                        <Trash2 class="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Create/Edit Dialog -->
      <Dialog :open="showDialog" @update:open="(val) => !val && closeDialog()">
        <DialogContent class="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>{{ isEditing ? 'Rediger artikkel' : 'Opprett ny artikkel' }}</DialogTitle>
            <DialogDescription>
              Fyll ut informasjonen under for å
              {{ isEditing ? 'oppdatere' : 'opprette' }} artikkelen.
            </DialogDescription>
          </DialogHeader>

          <form @submit.prevent="handleSubmit" class="space-y-4">
            <div class="space-y-2">
              <label for="title" class="text-sm font-medium text-gray-700">Tittel</label>
              <Input
                id="title"
                v-model="articleForm.title"
                placeholder="Skriv inn tittel"
                required
              />
            </div>

            <div class="space-y-2">
              <label for="imageUrl" class="text-sm font-medium text-gray-700">Bilde URL</label>
              <div class="flex space-x-2">
                <Input
                  id="imageUrl"
                  v-model="articleForm.imageUrl"
                  placeholder="Lim inn bilde URL"
                  required
                />
                <Button
                  type="button"
                  variant="outline"
                  class="flex-shrink-0"
                  @click="openImageInNewTab(articleForm.imageUrl)"
                  :disabled="!articleForm.imageUrl"
                >
                  <ImageIcon class="h-4 w-4" />
                </Button>
              </div>
            </div>

            <div class="space-y-2">
              <label for="text" class="text-sm font-medium text-gray-700">Innhold</label>
              <Textarea
                id="text"
                v-model="articleForm.text"
                placeholder="Skriv inn artikkelinnhold"
                rows="10"
                required
              />
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" @click="closeDialog"> Avbryt </Button>
              <Button type="submit" variant="default">
                {{ isEditing ? 'Oppdater' : 'Opprett' }}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.overflow-x-auto {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #edf2f7;
}

.overflow-x-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #edf2f7;
  border-radius: 4px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background-color: #cbd5e0;
  border-radius: 4px;
  border: 2px solid #edf2f7;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background-color: #a0aec0;
}
</style>
