<template>
  <div class="news-container">
    <h1>Nyheter</h1>
    
    <div class="news-list">
      <article class="news-item">
        <div class="news-date">15. Mai 2023</div>
        <h2>Ny funksjonalitet for beredskapslagring lansert</h2>
        <p>
          Vi har lansert en ny funksjonalitet som gjør det enklere å holde oversikt over ditt beredskapslager.
          Nå kan du enkelt kategorisere matvarer, medisiner, og annet utstyr for å sikre at du er forberedt på krisesituasjoner.
        </p>
        <a href="#" class="read-more">Les mer</a>
      </article>

      <article class="news-item">
        <div class="news-date">2. April 2023</div>
        <h2>Samarbeid med lokale beredskapsgrupper</h2>
        <p>
          Vi har inngått samarbeid med flere lokale beredskapsgrupper for å forbedre beredskapen i lokalsamfunnet.
          Dette samarbeidet vil gi våre brukere tilgang til lokale ressurser og informasjon i krisesituasjoner.
        </p>
        <a href="#" class="read-more">Les mer</a>
      </article>

      <article class="news-item">
        <div class="news-date">15. Mars 2023</div>
        <h2>Ny versjon av appen er nå tilgjengelig</h2>
        <p>
          Vi har lansert en oppdatert versjon av vår app med forbedret brukergrensesnitt og nye funksjoner.
          Oppdateringen inkluderer forbedringer for inventarregistrering og deling med husstandsmedlemmer.
        </p>
        <a href="#" class="read-more">Les mer</a>
      </article>

      <article class="news-item">
        <div class="news-date">20. Februar 2023</div>
        <h2>Tips for å forberede seg på strømbrudd</h2>
        <p>
          Med økende forekomst av ekstremvær, har vi samlet noen nyttige tips for å forberede seg på strømbrudd.
          Artikkelen inkluderer en sjekkliste over nødvendige artikler og forslag til alternative energikilder.
        </p>
        <a href="#" class="read-more">Les mer</a>
      </article>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.news-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.news-list {
  margin-top: 2rem;
}

.news-item {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eaeaea;
}

.news-date {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

h2 {
  margin: 0.5rem 0;
  font-size: 1.5rem;
}

.read-more {
  display: inline-block;
  margin-top: 0.5rem;
  color: #0066cc;
  text-decoration: none;
}

.read-more:hover {
  text-decoration: underline;
}
</style> 