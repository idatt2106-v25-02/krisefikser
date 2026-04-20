const CLOUDINARY_UPLOAD_SEGMENT = '/image/upload/'

export const useCloudinaryUrl = () => {
  const buildCloudinaryUrl = (
    rawUrl: string | undefined,
    transformation = 'f_auto,q_auto',
  ): string => {
    if (!rawUrl) {
      return ''
    }
    if (!rawUrl.includes('res.cloudinary.com') || !rawUrl.includes(CLOUDINARY_UPLOAD_SEGMENT)) {
      return rawUrl
    }

    const [prefix, suffix] = rawUrl.split(CLOUDINARY_UPLOAD_SEGMENT)
    if (!prefix || !suffix) {
      return rawUrl
    }

    return `${prefix}${CLOUDINARY_UPLOAD_SEGMENT}${transformation}/${suffix}`
  }

  return { buildCloudinaryUrl }
}
