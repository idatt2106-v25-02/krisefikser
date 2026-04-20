<template>
  <section
    class="mt-12 pt-10 border-t border-slate-200"
    aria-labelledby="tech-teaser-heading"
  >
    <div class="max-w-3xl mx-auto text-center px-2">
      <h2 id="tech-teaser-heading" class="text-2xl font-bold text-slate-800 mb-3">
        Prosjekt og teknologi
      </h2>
      <p class="text-slate-600 text-lg leading-relaxed mb-6">
        Kurset IDATT2106 og visjonsdokumentet for Krisefikser.no satte rammer og krav. Les en
        kort kobling fra emnekrav til den teknologistakken vi bruker, og hvordan løsningen møter
        dem.
      </p>
      <router-link
        to="/emne-og-teknologi"
        class="inline-flex items-center justify-center rounded-md bg-blue-700 text-white font-semibold px-6 py-3 hover:bg-blue-800 transition focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
      >
        Emne, krav og teknologistakk
      </router-link>
    </div>
  </section>
</template>

<script lang="ts">
export default {
  name: 'HomeTechStackTeaser',
}
</script>
