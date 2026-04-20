import { AXIOS_INSTANCE } from '@/api/axios'

export interface ImageUploadResponse {
  url: string
  publicId: string
}

export interface ImageUploadOptions {
  folder?: string
  tags?: string
  context?: string
  metadata?: string
  uploadPreset?: string
}

export const uploadImage = async (
  file: File,
  options?: ImageUploadOptions,
): Promise<ImageUploadResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  if (options?.folder) {
    formData.append('folder', options.folder)
  }
  if (options?.tags) {
    formData.append('tags', options.tags)
  }
  if (options?.context) {
    formData.append('context', options.context)
  }
  if (options?.metadata) {
    formData.append('metadata', options.metadata)
  }
  if (options?.uploadPreset) {
    formData.append('uploadPreset', options.uploadPreset)
  }

  const { data } = await AXIOS_INSTANCE.post<ImageUploadResponse>('/api/images/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export const uploadAvatar = async (file: File): Promise<ImageUploadResponse> => {
  const formData = new FormData()
  formData.append('file', file)

  const { data } = await AXIOS_INSTANCE.post<ImageUploadResponse>('/api/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}
