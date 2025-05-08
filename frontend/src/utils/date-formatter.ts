const formatDate = (dateInput?: string | number | null) => {
  if (!dateInput) return 'Ikke tilgjengelig'

  // Handle numeric timestamps (like 1746434861.478286)
  if (typeof dateInput === 'number' || !isNaN(Number(dateInput))) {
    const timestamp = typeof dateInput === 'number' ? dateInput : Number(dateInput)
    const date = new Date(timestamp * 1000) // Convert seconds to milliseconds
    if (isNaN(date.getTime())) return 'Ikke tilgjengelig'
    return date.toLocaleString('nb-NO', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  // Handle string dates
  const date = new Date(dateInput)
  if (isNaN(date.getTime())) return 'Ikke tilgjengelig'
  return date.toLocaleString('nb-NO', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export { formatDate }
