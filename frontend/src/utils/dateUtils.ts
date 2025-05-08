export const formatDate = (dateInput: string | number[] | undefined): string => {
  if (!dateInput) return '-'

  let notificationDate: Date

  if (typeof dateInput === 'string') {
    notificationDate = new Date(dateInput)
  } else if (Array.isArray(dateInput) && dateInput.length >= 6) {
    notificationDate = new Date(
      dateInput[0],
      dateInput[1] - 1,
      dateInput[2],
      dateInput[3],
      dateInput[4],
      dateInput[5],
      dateInput[6] ? Math.floor(dateInput[6] / 1000000) : 0,
    )
  } else {
    return 'Invalid date format'
  }

  if (isNaN(notificationDate.getTime())) {
    return 'Invalid date'
  }

  const now = new Date()
  const diffInSeconds = Math.floor((now.getTime() - notificationDate.getTime()) / 1000)

  if (diffInSeconds < 0) return 'In the future'
  if (diffInSeconds < 5) return 'Nå nettopp'
  if (diffInSeconds < 60) return `${diffInSeconds} sek siden`
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min siden`

  const diffInDays = Math.floor(diffInSeconds / (60 * 60 * 24))

  if (diffInDays === 0)
    return `I dag, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`
  if (diffInDays === 1) return 'I går'
  if (diffInDays < 7) {
    const days = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag']
    return days[notificationDate.getDay()]
  }
  return `${notificationDate.getDate().toString().padStart(2, '0')}.${(notificationDate.getMonth() + 1).toString().padStart(2, '0')}.${notificationDate.getFullYear()}`
} 