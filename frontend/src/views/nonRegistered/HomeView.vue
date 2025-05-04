<script lang="ts">
import HeroSection from '@/components/home/HeroSection.vue';
import CrisisInfoSection from '@/components/home/CrisisInfoSection.vue';
import FeaturesSection from '@/components/home/FeaturesSection.vue';
import NewsSection from '@/components/home/NewsSection.vue';
import ScenariosSection from '@/components/home/ScenariosSection.vue';

export default {
  name: 'HomeView',
  components: {
    HeroSection,
    CrisisInfoSection,
    FeaturesSection,
    NewsSection,
    ScenariosSection
  }
}

</script>
<template>
  <div class="min-h-screen flex flex-col bg-slate-50 overflow-hidden">


    <HeroSection />
    <main class="flex-grow container mx-auto px-4 py-12 border-t-0">
      <CrisisInfoSection />
      <ScenariosSection />
      <NewsSection />
      <FeaturesSection />
    </main>

  </div>
</template>


