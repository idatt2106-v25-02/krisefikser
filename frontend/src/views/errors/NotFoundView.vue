<script setup lang="ts">
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

// Random movement for the blobs
onMounted(() => {
  const blobs = document.querySelectorAll('.blob')

  blobs.forEach((blob) => {
    animateBlob(blob as SVGElement)
  })
})

const animateBlob = (blob: SVGElement) => {
  const duration = 15000 + Math.random() * 10000
  const xMovement = 10 + Math.random() * 5
  const yMovement = 10 + Math.random() * 5

  const animate = () => {
    const translateX = Math.sin(Date.now() / duration) * xMovement
    const translateY = Math.cos(Date.now() / duration) * yMovement

    blob.style.transform = `translate(${translateX}px, ${translateY}px)`
    requestAnimationFrame(animate)
  }

  animate()
}
</script>

<template>
  <div
    class="min-h-screen flex flex-col items-center justify-center bg-slate-50 px-4 overflow-hidden relative"
  >
    <!-- Blob background -->
    <div class="absolute inset-0 overflow-hidden">
      <svg
        class="blob absolute opacity-10 text-blue-300 -top-24 -left-24 w-96 h-96"
        viewBox="0 0 200 200"
      >
        <path
          fill="currentColor"
          d="M37.6,-64.1C51.1,-56.9,66.2,-50.5,74.8,-39C83.5,-27.4,85.8,-10.7,81.9,3.9C78.1,18.5,68.2,31.1,57.5,42.4C46.8,53.8,35.3,64,21.7,70.8C8.1,77.6,-7.6,81,-21.9,77.1C-36.3,73.2,-49.2,62,-59.5,48.2C-69.8,34.5,-77.5,18.2,-77.8,1.8C-78.2,-14.7,-71.3,-29.4,-62.1,-41.8C-52.9,-54.1,-41.4,-64,-29.3,-70.8C-17.1,-77.5,-4.3,-81.1,5.7,-76.6C15.8,-72.1,24.1,-71.3,37.6,-64.1Z"
          transform="translate(100 100)"
        />
      </svg>

      <svg
        class="blob absolute opacity-10 text-indigo-400 -bottom-24 -right-12 w-full max-w-xl h-full max-h-xl"
        viewBox="0 0 200 200"
      >
        <path
          fill="currentColor"
          d="M45.3,-77.5C58.2,-71.1,67.6,-55.9,74.9,-40.6C82.2,-25.3,87.5,-9.8,85,4.5C82.5,18.8,72.3,32.1,61.9,44.2C51.5,56.3,41,67.3,27.8,73.9C14.7,80.5,-1.1,83,-16.6,80.3C-32.1,77.7,-47.3,70,-59.6,57.9C-71.8,45.9,-81.1,29.5,-84.4,11.8C-87.7,-5.9,-85,-25,-75.3,-39.2C-65.6,-53.5,-48.8,-62.8,-33.5,-67.9C-18.1,-73,-4.5,-74,8.5,-68.7C21.4,-63.5,32.5,-83.9,45.3,-77.5Z"
          transform="translate(100 100)"
        />
      </svg>

      <svg
        class="blob absolute opacity-10 text-blue-500 top-1/2 left-3/5 w-80 h-80"
        viewBox="0 0 200 200"
      >
        <path
          fill="currentColor"
          d="M34.9,-59.2C45,-51.7,53,-41.8,59.8,-30.2C66.6,-18.6,72.3,-5.2,71.7,7.8C71.1,20.9,64.2,33.6,55.2,44.8C46.1,56,34.9,65.8,21.7,70.3C8.5,74.9,-6.7,74.3,-19.9,69.5C-33.1,64.8,-44.3,55.9,-53.6,44.9C-62.9,33.9,-70.4,20.8,-72.7,6.2C-75.1,-8.4,-72.5,-24.5,-64.6,-36.7C-56.7,-48.8,-43.6,-57,-30.7,-63.9C-17.8,-70.9,-5.2,-76.6,3.3,-71.4C11.8,-66.2,24.8,-66.8,34.9,-59.2Z"
          transform="translate(100 100)"
        />
      </svg>
    </div>

    <!-- Content -->
    <div class="text-center z-10 relative">
      <h1 class="text-9xl font-bold text-blue-600 mb-4">404</h1>
      <h2 class="text-3xl font-semibold text-gray-800 mb-6">Siden ble ikke funnet</h2>
      <p class="text-gray-600 mb-8 max-w-md mx-auto">
        Beklager! Siden du leter etter eksisterer ikke eller har blitt flyttet.
      </p>
      <button
        @click="goHome"
        class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-lg transition-colors duration-200"
      >
        Gå tilbake til hjem
      </button>
    </div>
  </div>
</template>

<style scoped>
.blob {
  transition: transform 3s ease-in-out;
  will-change: transform;
  pointer-events: none;
}
</style>
