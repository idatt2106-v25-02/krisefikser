export const getIconNames = (): string[] => {
  // Get all SVG files from the public/icons directory
  const iconFiles = Object.keys(import.meta.glob('../../public/icons/*.svg', { eager: true }))

  // Extract the icon names (remove path and .svg extension)
  return iconFiles.map((file: string) => {
    const fileName = file.split('/').pop() || ''
    return fileName.replace('.svg', '')
  })
}

// Example usage:
// const iconNames = getIconNames();
// console.log(iconNames); // ['zoom-out', 'zap-off', 'zap', ...]
