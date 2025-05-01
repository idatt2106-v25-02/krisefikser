<template>
  <div class="max-w-4xl mx-auto px-4 py-8 lg:py-12">
    <h1 class="text-3xl lg:text-5xl font-bold mb-8 text-gray-800 border-b border-gray-200 pb-3">Personvern</h1>

    <section class="mb-10">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Hvordan vi behandler dine data</h2>
      <p class="text-lg text-gray-700 leading-relaxed mb-6">
        Som et studentprosjekt ved NTNU tar vi personvern på alvor og følger GDPR-retningslinjene nøye.
        Denne siden forklarer hvordan vi behandler dine data i forbindelse med vårt akademiske prosjekt.
      </p>

      <div class="mb-8 bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 class="text-xl font-semibold text-gray-800">For ikke-registrerte brukere</h3>
        </div>
        <div class="p-6">
          <p class="text-lg text-gray-700 mb-3">
            For ikke-registrerte brukere samler vi kun begrenset informasjon:
          </p>
          <ul class="list-disc ml-6 mb-3 text-lg text-gray-700 space-y-2">
            <li>IP-adresse (anonymisert)</li>
            <li>Grunnleggende besøksstatistikk</li>
            <li>Nødvendige informasjonskapsler</li>
          </ul>
        </div>
      </div>

      <div class="mb-8 bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 class="text-xl font-semibold text-gray-800">For registrerte brukere</h3>
        </div>
        <div class="p-6">
          <p class="text-lg text-gray-700 mb-3">
            For testbrukere i vårt prosjekt lagrer vi:
          </p>
          <ul class="list-disc ml-6 mb-3 text-lg text-gray-700 space-y-2">
            <li>Basis kontaktinformasjon</li>
            <li>Testdata for beredskapsplanlegging</li>
            <li>Brukerpreferanser</li>
          </ul>
        </div>
      </div>

      <div class="mb-8 bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 class="text-xl font-semibold text-gray-800">Dine rettigheter</h3>
        </div>
        <div class="p-6">
          <p class="text-lg text-gray-700 mb-3">
            Som deltaker i vårt prosjekt har du følgende rettigheter:
          </p>
          <ul class="list-disc ml-6 mb-3 text-lg text-gray-700 space-y-2">
            <li>Innsyn i dine data</li>
            <li>Retting av feil informasjon</li>
            <li>Sletting av dine data</li>
            <li>Trekke ditt samtykke</li>
          </ul>
        </div>
      </div>
    </section>

    <section class="mb-8">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Kontakt angående personvern</h2>
      <div class="bg-blue-50 rounded-lg p-6 border border-blue-100 shadow-sm">
        <p class="text-lg text-gray-700 mb-4">
          For spørsmål om personvern i vårt prosjekt, kontakt oss:
        </p>
        <div class="space-y-3 text-lg text-gray-700">
          <p>
            <span class="font-medium">E-post:</span>
            <a href="mailto:kontakt@krisefikser.app" class="text-blue-600 hover:text-blue-800 hover:underline underline-offset-2">
              kontakt@krisefikser.app
            </a>
          </p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
// No additional script needed
</script>
