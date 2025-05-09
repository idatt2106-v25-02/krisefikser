<script lang="ts" setup>
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { computed, ref } from 'vue'
import { Image as ImageIcon, Pencil, Plus, Trash2 } from 'lucide-vue-next'

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

// Import our new components
import PageHeader from '@/components/ui/page-header/PageHeader.vue'
import SearchBar from '@/components/ui/search/SearchBar.vue'
import DataTable from '@/components/ui/data-table/DataTable.vue'
import ContentCard from '@/components/ui/card/ContentCard.vue'

import {
  useCreateArticle,
  useDeleteArticle,
  useGetAllArticles,
  useUpdateArticle,
} from '@/api/generated/article/article'
import type { ArticleRequest, ArticleResponse } from '@/api/generated/model'
import { formatDate } from '../../api/Utils.ts'

const showDialog = ref(false)
const isEditing = ref(false)
const searchQuery = ref('')
const selectedArticle = ref<ArticleResponse | null>(null)

const articleForm = ref<ArticleRequest>({
  title: '',
  text: '',
  imageUrl: '',
})

const {
  data: articlesData,
  isLoading: isLoadingArticles,
  refetch: refetchArticles,
} = useGetAllArticles()

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

const tableColumns = [
  { key: 'title', label: 'Tittel', align: 'left' as const },
  { key: 'createdAt', label: 'Opprettet', align: 'left' as const },
  { key: 'actions', label: 'Handlinger', align: 'center' as const },
]

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
</script>

<template>
  <AdminLayout>
    <div class="p-6">
      <PageHeader title="Administrer artikler">
        <template #actions>
          <Button
            class="flex items-center bg-blue-600 hover:bg-blue-700 text-white"
            variant="default"
            @click="openDialog()"
          >
            <Plus class="h-4 w-4 mr-1" />
            Ny artikkel
          </Button>
        </template>
      </PageHeader>

      <ContentCard>
        <div class="p-4 flex items-center border-b">
          <SearchBar v-model="searchQuery" placeholder="Søk artikler..." />
        </div>

        <DataTable
          :columns="tableColumns"
          :is-loading="isLoadingArticles"
          empty-message="Ingen artikler funnet"
        >
          <tr v-for="article in filteredArticles" :key="article.id" class="hover:bg-gray-50">
            <td class="px-4 py-4">
              <div class="flex items-center">
                <img
                  v-if="article.imageUrl"
                  :alt="article.title"
                  :src="article.imageUrl"
                  class="h-10 w-10 rounded-full object-cover mr-3"
                />
                <div class="text-sm font-medium text-gray-900">{{ article.title }}</div>
              </div>
            </td>
            <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
              {{ formatDate(article.createdAt) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <div class="flex justify-center space-x-2">
                <Button
                  class="text-blue-600 hover:text-blue-800"
                  size="icon"
                  variant="ghost"
                  @click="openDialog(article)"
                >
                  <Pencil class="h-4 w-4" />
                </Button>
                <Button
                  class="text-red-600 hover:text-red-800"
                  size="icon"
                  variant="ghost"
                  @click="deleteArticle(article.id)"
                >
                  <Trash2 class="h-4 w-4" />
                </Button>
              </div>
            </td>
          </tr>
        </DataTable>
      </ContentCard>

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

          <form class="space-y-4" @submit.prevent="handleSubmit">
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700" for="title">Tittel</label>
              <Input
                id="title"
                v-model="articleForm.title"
                placeholder="Skriv inn tittel"
                required
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700" for="imageUrl">Bilde URL</label>
              <div class="flex space-x-2">
                <Input
                  id="imageUrl"
                  v-model="articleForm.imageUrl"
                  placeholder="Lim inn bilde URL"
                  required
                />
                <Button
                  :disabled="!articleForm.imageUrl"
                  class="flex-shrink-0"
                  type="button"
                  variant="outline"
                  @click="openImageInNewTab(articleForm.imageUrl)"
                >
                  <ImageIcon class="h-4 w-4" />
                </Button>
              </div>
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700" for="text">Innhold</label>
              <Textarea
                id="text"
                v-model="articleForm.text"
                placeholder="Skriv inn artikkelinnhold"
                required
                rows="10"
              />
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" @click="closeDialog"> Avbryt</Button>
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
