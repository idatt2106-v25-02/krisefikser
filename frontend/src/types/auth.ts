export type LoginFormData = {
  email: string
  password: string
}

export type ApiError = {
  response?: { data?: { message?: string }; status?: number }
}
