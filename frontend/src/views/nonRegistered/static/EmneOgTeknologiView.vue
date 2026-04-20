<template>
  <div class="max-w-4xl mx-auto px-4 py-8 lg:py-12">
    <h1
      class="text-3xl lg:text-5xl font-bold mb-8 text-gray-800 border-b border-gray-200 pb-3"
    >
      Emne, krav og teknologi
    </h1>

    <section class="mb-10">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Kilde og kontekst</h2>
      <p class="text-lg text-gray-700 leading-relaxed mb-4">
        Denne nettsiden er utviklet i emnet
        <strong>IDATT2106 – Systemutvikling med smidig prosjekt</strong> ved NTNU. Krav og
        visjon for produktet er beskrevet i produkteiernes
        <strong>Visjonsdokument 2025 (final)</strong> for nettapplikasjonen Krisefikser.no: målet
        er å øke egenberedskap og kunnskap om krisehåndtering, og å samle funksjoner som ofte
        ligger spredt på ulike tjenester, i én løsning strukturert etter
        <strong>før, under og etter</strong> en krise.
      </p>
      <p class="text-lg text-gray-700 leading-relaxed">
        Siden under er en kort teknisk og faglig oversikt: den knytter dokumentets overordnede
        krav til det som er realisert i denne kodebasen. Fullstendige brukerhistorier og
        leveransekrav finnes i visjonsdokumentet og emnets retningslinjer.
      </p>
    </section>

    <section class="mb-10">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Funksjonelle spor i løsningen</h2>
      <p class="text-lg text-gray-700 leading-relaxed mb-4">
        Visjonsdokumentet beskriver mange brukerhistorier (kart, tilfluktsrom, kriser,
        registrering, husstand, beredskapslager, varsler, informasjon, nyheter, admin, m.m.). I
        denne applikasjonen finner du blant annet følgende, med lenker inn i løsningen:
      </p>
      <ul class="list-disc pl-6 space-y-2 text-lg text-gray-700">
        <li>
          <router-link class="text-blue-600 hover:underline" to="/kart">Interaktivt kart</router-link>
          med blant annet tilfluktsrom og andre ressurser i nærområdet.
        </li>
        <li>
          <router-link class="text-blue-600 hover:underline" to="/kriser">Kriser og hendelser</router-link>
          med detaljsider — støtter behovet for oppdatert situasjonsinformasjon og kartbruk.
        </li>
        <li>
          Informasjon etter fasene før / under / etter krise:
          <router-link class="text-blue-600 hover:underline" to="/info/for-krisen">Før krisen</router-link>,
          <router-link class="text-blue-600 hover:underline" to="/info/under-krisen">Under krisen</router-link>,
          <router-link class="text-blue-600 hover:underline" to="/info/etter-krisen">Etter krisen</router-link>.
        </li>
        <li>
          <router-link class="text-blue-600 hover:underline" to="/scenarioer">Scenarioer</router-link>
          for ulike krisetyper, og
          <router-link class="text-blue-600 hover:underline" to="/nyheter">nyheter</router-link>
          for løpende artikler.
        </li>
        <li>
          <router-link class="text-blue-600 hover:underline" to="/registrer">Registrering</router-link>,
          <router-link class="text-blue-600 hover:underline" to="/logg-inn">innlogging</router-link>,
          <router-link class="text-blue-600 hover:underline" to="/bli-med-eller-opprett-husstand"
            >husstand</router-link
          >,
          <router-link class="text-blue-600 hover:underline" to="/husstand/beredskapslager"
            >beredskapslager</router-link
          >,
          <router-link class="text-blue-600 hover:underline" to="/varsler">varsler</router-link>
          og
          <router-link class="text-blue-600 hover:underline" to="/mine-refleksjoner"
            >refleksjoner</router-link
          >
          for innloggede brukere.
        </li>
        <li>
          <router-link class="text-blue-600 hover:underline" to="/personvern">Personvern</router-link>
          og
          <router-link class="text-blue-600 hover:underline" to="/om-oss">Om oss</router-link>
          etter krav om informasjon og kontakt.
        </li>
        <li>Admin-funksjonalitet for kart, hendelser, scenarioer og innhold (for autoriserte administratorer).</li>
      </ul>
      <p class="text-lg text-gray-700 leading-relaxed mt-4">
        Enkelte historier i visjonsdokumentet (særlig lavere prioritet) kan være delvis eller ikke
        prioritert i denne versjonen — dette er et studentprosjekt med begrenset sprintkapasitet.
      </p>
    </section>

    <section class="mb-10">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Ikke-funksjonelle krav og hvordan vi har møtt dem</h2>
      <dl class="space-y-6 text-lg text-gray-700">
        <div>
          <dt class="font-semibold text-gray-900">Programmatisk testing og dekningsgrad</dt>
          <dd class="leading-relaxed mt-1">
            Emnet krever blant annet minst 50 % linjdekning på serversiden og 30 % på klienten,
            med flere testtyper. Backend bruker JaCoCo med
            <strong>minimum 80 %</strong> linjdekning ved verify (strengere enn minimumskravet).
            Frontend har Vitest for enhetstester og Cypress for e2e, inkludert røyk- og
            fullstackløp der det er satt opp.
          </dd>
        </div>
        <div>
          <dt class="font-semibold text-gray-900">Database og REST</dt>
          <dd class="leading-relaxed mt-1">
            Data persisteres i SQL-database (MySQL i drift, H2 brukes blant annet i tester).
            Klienten snakker med backend via <strong>REST API</strong> (Spring Web).
          </dd>
        </div>
        <div>
          <dt class="font-semibold text-gray-900">SPA og nettleserstøtte</dt>
          <dd class="leading-relaxed mt-1">
            Klienten er en <strong>single page application</strong> bygget med Vue 3 og Vite. Løsningen
            er laget for vanlige nettlesere på PC og mobil.
          </dd>
        </div>
        <div>
          <dt class="font-semibold text-gray-900">Tilgjengelighet og brukskvalitet (WCAG 2.1)</dt>
          <dd class="leading-relaxed mt-1">
            Studiekravene viser til WCAG 2.1 og god brukskvalitet for målgruppen. Vi jobber bevisst
            med tilgjengelighet: blant annet hopp til hovedinnhold, støtte for tekst-til-tale og
            dedikerte a11y-tester i frontend-pipeline.
          </dd>
        </div>
        <div>
          <dt class="font-semibold text-gray-900">Sikkerhet (autentisering, autorisering, OWASP)</dt>
          <dd class="leading-relaxed mt-1">
            Backend bruker Spring Security med blant annet JWT-basert autentisering, roller for
            admin, og validering av inndata — i tråd med krav om å adressere injeksjonsrisiko og
            relaterte OWASP-problemer.
          </dd>
        </div>
        <div>
          <dt class="font-semibold text-gray-900">Kontinuerlig integrasjon (CI)</dt>
          <dd class="leading-relaxed mt-1">
            Emnet viser til CI med testrapporter i GitLab. Dette prosjektet bruker
            <strong>GitHub Actions</strong> med tilsvarende intensjon: automatiserte bygg og tester på
            pull requests og hovedgren.
          </dd>
        </div>
      </dl>
    </section>

    <section class="mb-10">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Teknologistakk (oversikt)</h2>
      <div class="space-y-6 text-lg text-gray-700">
        <div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Frontend</h3>
          <ul class="list-disc pl-6 space-y-1">
            <li>Vue 3, TypeScript, Vite</li>
            <li>Pinia for tilstand, TanStack Vue Query for servertilstand</li>
            <li>Tailwind CSS, komponentbibliotek (bl.a. Reka UI), Lucide-ikoner</li>
            <li>Leaflet / Vue Leaflet for kart</li>
            <li>Axios, Orval-generert API-klient, VeeValidate og Zod for skjemaer</li>
            <li>WebSocket / STOMP-klienter for sanntidsvarsler der det er aktivert</li>
          </ul>
        </div>
        <div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Backend</h3>
          <ul class="list-disc pl-6 space-y-1">
            <li>Java med Spring Boot (Web, Data JPA, Security, Validation, WebSocket)</li>
            <li>REST-kontrollere og persistens via JPA</li>
            <li>MySQL (produksjon), H2 (typisk test)</li>
            <li>SpringDoc OpenAPI for API-dokumentasjon</li>
            <li>Jacoco, JUnit og Spring Boot Test for automatiserte tester</li>
          </ul>
        </div>
      </div>
    </section>

    <section class="mb-8">
      <h2 class="text-2xl lg:text-3xl font-semibold mb-4 text-blue-700">Les mer</h2>
      <p class="text-lg text-gray-700 leading-relaxed mb-4">
        For kontekst om team og formål, se
        <router-link to="/om-oss" class="text-blue-600 hover:underline">Om oss</router-link>.
        Full kravspesifikasjon og brukerhistorier ligger i visjonsdokumentet fra emnet — ta det
        frem som del av prosjekt- eller sluttrapporten når dere dokumenterer sporbarhet til
        enkeltkrav.
      </p>
      <p>
        <router-link
          to="/"
          class="inline-flex items-center text-blue-600 font-medium hover:underline"
        >
          Tilbake til forsiden
        </router-link>
      </p>
    </section>
  </div>
</template>
