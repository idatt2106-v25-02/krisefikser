<script lang="ts">
import HeroSection from '@/components/home/HeroSection.vue';
import CrisisInfoSection from '@/components/home/CrisisInfoSection.vue';
import FeaturesSection from '@/components/home/FeaturesSection.vue';
import NewsSection from '@/components/home/NewsSection.vue';

export default {
  name: 'HomeView',
  components: {
    HeroSection,
    CrisisInfoSection,
    FeaturesSection,
    NewsSection
  }
}

</script>
<template>
  <div class="min-h-screen flex flex-col bg-slate-50 overflow-hidden">


    <HeroSection />

    <!-- Main Content -->
    <main class="flex-grow container mx-auto px-4 py-12 border-t-0">
      <CrisisInfoSection />
      <FeaturesSection />
      <NewsSection />
    </main>

  </div>
</template>


