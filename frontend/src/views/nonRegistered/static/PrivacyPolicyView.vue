<script setup lang="ts">
import { PRIVACY_POLICY_VERSION } from '@/plugins/docs/posthog-consent'
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8 lg:py-12">
    <h1 class="text-3xl lg:text-5xl font-bold mb-8 text-gray-800 border-b border-gray-200 pb-3">Personvern</h1>
    <p class="text-sm text-gray-500 mb-6">Dokumentversjon: {{ PRIVACY_POLICY_VERSION }}</p>

    <section class="mb-10">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Hvordan vi behandler dine data</h2>
      <p class="text-lg text-gray-700 leading-relaxed mb-6">
        Som et studentprosjekt ved NTNU tar vi personvern på alvor og følger GDPR-retningslinjene nøye.
        Denne siden forklarer hvordan vi behandler dine data i forbindelse med vårt akademiske prosjekt.
      </p>

      <div class="mb-8 bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 class="text-xl font-semibold text-gray-800">Informasjonskapsler og samtykke</h3>
        </div>
        <div class="p-6">
          <p class="text-lg text-gray-700 mb-3">
            Vi skiller mellom <strong>nødvendige</strong> og <strong>valgfrie</strong> informasjonskapsler:
          </p>
          <ul class="list-disc ml-6 mb-4 text-lg text-gray-700 space-y-2">
            <li>
              <strong>Nødvendige</strong> brukes for at tjenesten skal fungere (for eksempel innlogging og
              sikkerhet). Disse kan ikke slås av i nettleseren vår uten at funksjonalitet faller bort.
            </li>
            <li>
              <strong>Analyse (PostHog)</strong> er valgfritt. Vi starter ikke analyseverktøyet før du har gitt
              eksplisitt samtykke via cookie-banneret eller «Cookie-innstillinger» i bunnteksten. Uten samtykke
              lastes ikke PostHog SDK for analyseformål.
            </li>
          </ul>
          <p class="text-lg text-gray-700 mb-3">
            <strong>Trekke samtykke / endre valg:</strong> Bruk
            <strong>«Cookie-innstillinger»</strong> i bunnteksten på nettstedet. Du kan når som helst slå av analyse;
            valget lagres lokalt i nettleseren din sammen med tidspunkt og versjon av denne personvernteksten
            (revisjonsspor).
          </p>
        </div>
      </div>

      <div class="mb-8 bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 class="text-xl font-semibold text-gray-800">PostHog (valgfri analyse)</h3>
        </div>
        <div class="p-6">
          <p class="text-lg text-gray-700 mb-3">
            Når du har samtykket til analyse, kan vi bruke PostHog (leverandør PostHog Inc., med EU-hosting der det er
            konfigurert) til å forstå hvordan nettstedet brukes, for eksempel sidevisninger og teknisk feilsøking.
            Vi bruker ikke «autocapture» for generell klikksporing i denne konfigurasjonen; vi sender i hovedsak
            strukturerte hendelser som sidevisning.
          </p>
          <ul class="list-disc ml-6 mb-3 text-lg text-gray-700 space-y-2">
            <li><strong>Behandlingsgrunnlag:</strong> samtykke (GDPR art. 6 nr. 1 bokstav a), når du har slått på analyse.</li>
            <li><strong>Oppbevaring:</strong> følger innstillingene hos PostHog og vårt behov for statistikk i prosjektperioden.</li>
            <li><strong>Overføring:</strong> PostHog kan behandle data i EU/USA avhengig av produktoppsett; se PostHogs personvern for detaljer.</li>
          </ul>
        </div>
      </div>

      <div class="mb-8 bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 class="text-xl font-semibold text-gray-800">For ikke-registrerte brukere</h3>
        </div>
        <div class="p-6">
          <p class="text-lg text-gray-700 mb-3">
            For ikke-registrerte brukere kan vi behandle begrenset informasjon:
          </p>
          <ul class="list-disc ml-6 mb-3 text-lg text-gray-700 space-y-2">
            <li>Tekniske logger (f.eks. IP-adresse i begrenset omfang der servere logger trafikk)</li>
            <li>Nødvendige informasjonskapsler for drift</li>
            <li>
              Valgfri besøksanalyse via PostHog <strong>kun etter samtykke</strong> (se over). Uten samtykke bruker vi
              ikke PostHog til analyse for ditt besøk.
            </li>
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
            <li>Valgfri analyse (PostHog) etter samtykke, som beskrevet over</li>
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
            <li>Trekke eller endre samtykke til informasjonskapsler og analyse (via «Cookie-innstillinger»)</li>
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
