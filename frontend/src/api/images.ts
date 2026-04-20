import { AXIOS_INSTANCE } from '@/api/axios'

export interface ImageUploadResponse {
  url: string
  publicId: string
}

export const uploadImage = async (file: File, folder?: string): Promise<ImageUploadResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  if (folder) {
    formData.append('folder', folder)
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
