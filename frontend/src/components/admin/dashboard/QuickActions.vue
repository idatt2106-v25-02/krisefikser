<script setup lang="ts">
import { AlertTriangle, FileText, Mail, Map } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { RouterLink as Link } from 'vue-router'

interface Props {
  isSuperAdmin: boolean
}

defineProps<Props>()
</script>

<template>
  <div class="bg-white rounded-lg shadow border-t-2">
    <div class="p-4 border-b">
      <h3 class="text-lg font-medium text-gray-800">Hurtighandlinger</h3>
    </div>
    <div class="p-4 grid grid-cols-1 md:grid-cols-4 gap-4">
      <Button
        as-child
        variant="outline"
        class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
      >
        <Link to="/admin/kart">
          <div class="bg-blue-100 p-2 rounded-full">
            <Map class="h-5 w-5 text-blue-600" />
          </div>
          <span class="ml-2 text-gray-700">Legg til kartposisjon</span>
        </Link>
      </Button>

      <Button
        as-child
        variant="outline"
        class="flex items-center p-3 justify-start h-auto border-orange-200 hover:bg-orange-50 hover:border-orange-300 transition-colors"
      >
        <Link to="/admin/kart" class="flex items-center w-full">
          <div class="bg-orange-100 p-2 rounded-full">
            <AlertTriangle class="h-5 w-5 text-orange-600" />
          </div>
          <span class="ml-2 text-gray-700">Registrer hendelse</span>
        </Link>
      </Button>

      <Button
        as-child
        variant="outline"
        class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
      >
        <Link to="/admin/artikler" class="flex items-center w-full">
          <div class="bg-blue-100 p-2 rounded-full">
            <FileText class="h-5 w-5 text-blue-600" />
          </div>
          <span class="ml-2 text-gray-700">Administrer artikler</span>
        </Link>
      </Button>

      <Button
        as-child
        v-if="isSuperAdmin"
        variant="outline"
        class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
      >
        <Link to="/admin/invite" class="flex items-center w-full">
          <div class="bg-blue-100 p-2 rounded-full">
            <Mail class="h-5 w-5 text-blue-600" />
          </div>
          <span class="ml-2 text-gray-700">Inviter admin</span>
        </Link>
      </Button>
    </div>
  </div>
</template>
