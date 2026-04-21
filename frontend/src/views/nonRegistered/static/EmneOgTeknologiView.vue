<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, type Ref } from 'vue'
import { useIntersectionObserver } from '@vueuse/core'
import {
  AlertTriangle,
  BookOpen,
  Database,
  Home,
  Layers,
  Lock,
  Map,
  Monitor,
  ShieldCheck,
  TestTube2,
  Workflow,
} from 'lucide-vue-next'

function useReducedMotion(): Ref<boolean> {
  const reduce = ref(false)
  let mq: MediaQueryList | null = null
  const update = () => {
    reduce.value = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  }
  onMounted(() => {
    mq = window.matchMedia('(prefers-reduced-motion: reduce)')
    update()
    mq.addEventListener('change', update)
  })
  onUnmounted(() => mq?.removeEventListener('change', update))
  return reduce
}

function useRevealSection(reduceMotion: Ref<boolean>) {
  const target = ref<HTMLElement | null>(null)
  const visible = ref(false)
  useIntersectionObserver(
    target,
    ([entry]) => {
      if (entry?.isIntersecting) visible.value = true
    },
    { threshold: 0.08, rootMargin: '0px 0px -32px 0px' },
  )

  const motionClass = computed(() => {
    if (reduceMotion.value) {
      return 'opacity-100'
    }
    return [
      'transition-[opacity,transform] duration-700 ease-out motion-reduce:transition-none motion-reduce:opacity-100 motion-reduce:translate-y-0',
      visible.value ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8',
    ].join(' ')
  })

  return { target, motionClass }
}

const reduceMotion = useReducedMotion()
const { target: refContext, motionClass: clsContext } = useRevealSection(reduceMotion)
const { target: refPillars, motionClass: clsPillars } = useRevealSection(reduceMotion)
const { target: refChips, motionClass: clsChips } = useRevealSection(reduceMotion)
const { target: refNfr, motionClass: clsNfr } = useRevealSection(reduceMotion)
const { target: refStack, motionClass: clsStack } = useRevealSection(reduceMotion)
const { target: refFooter, motionClass: clsFooter } = useRevealSection(reduceMotion)

const chipLinks = [
  { to: '/info/for-krisen', label: 'Før krisen' },
  { to: '/info/under-krisen', label: 'Under krisen' },
  { to: '/info/etter-krisen', label: 'Etter krisen' },
  { to: '/scenarioer', label: 'Scenarioer' },
  { to: '/nyheter', label: 'Nyheter' },
  { to: '/registrer', label: 'Registrer' },
  { to: '/logg-inn', label: 'Logg inn' },
  { to: '/bli-med-eller-opprett-husstand', label: 'Husstand' },
  { to: '/husstand/beredskapslager', label: 'Beredskapslager' },
  { to: '/varsler', label: 'Varsler' },
  { to: '/mine-refleksjoner', label: 'Refleksjoner' },
  { to: '/personvern', label: 'Personvern' },
  { to: '/om-oss', label: 'Om oss' },
] as const

const nfrItems = [
  {
    title: 'Testing og dekning',
    body: 'JaCoCo ≥80 % linjer på backend ved verify. Frontend: Vitest, Cypress (røyk og fullstack der satt opp).',
    icon: TestTube2,
  },
  {
    title: 'Database og REST',
    body: 'MySQL i drift, H2 i tester. Klient mot Spring Web REST.',
    icon: Database,
  },
  {
    title: 'SPA og plattformer',
    body: 'Vue 3 + Vite — laget for vanlige nettlesere på PC og mobil.',
    icon: Monitor,
  },
  {
    title: 'WCAG 2.1 og UX',
    body: 'Hopp til innhold, tekst-til-tale-støtte og a11y-tester i pipeline.',
    icon: Layers,
  },
  {
    title: 'Sikkerhet',
    body: 'Spring Security, JWT, roller og inndatavalidering — OWASP-orientert.',
    icon: Lock,
  },
  {
    title: 'CI',
    body: 'GitHub Actions med automatiserte bygg og tester på PR og hovedgren.',
    icon: Workflow,
  },
] as const

const stackFront = [
  'Vue 3, TypeScript, Vite',
  'Pinia, TanStack Vue Query',
  'Tailwind, Reka UI, Lucide',
  'Leaflet / Vue Leaflet',
  'Axios, Orval, VeeValidate, Zod',
  'WebSocket / STOMP der aktivert',
] as const

const stackBack = [
  'Java, Spring Boot (Web, JPA, Security, Validation, WebSocket)',
  'REST + JPA-persistens',
  'MySQL (prod), H2 (test)',
  'SpringDoc OpenAPI',
  'Jacoco, JUnit, Spring Boot Test',
] as const
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-50 via-white to-slate-50">
    <div class="max-w-5xl mx-auto px-4 py-10 lg:py-14">
      <header
        class="relative overflow-hidden rounded-2xl border border-slate-200/80 bg-white/90 p-8 shadow-lg shadow-slate-900/5 backdrop-blur-sm lg:p-10"
      >
        <div
          class="pointer-events-none absolute -right-16 -top-16 h-48 w-48 rounded-full bg-blue-100/60 blur-3xl"
          aria-hidden="true"
        />
        <div
          class="pointer-events-none absolute -bottom-20 -left-10 h-56 w-56 rounded-full bg-slate-200/40 blur-3xl"
          aria-hidden="true"
        />
        <div class="relative flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p
              class="mb-2 inline-flex items-center gap-2 rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-800"
            >
              <BookOpen class="h-4 w-4 shrink-0" aria-hidden="true" />
              IDATT2106 · Krisefikser.no
            </p>
            <h1 class="text-3xl font-bold tracking-tight text-slate-900 lg:text-4xl">
              Emne, krav og teknologi
            </h1>
            <p class="mt-3 max-w-2xl text-lg text-slate-600">
              Kort sporbarhet fra emne og visjonsdokument til det som faktisk er bygget her — uten
              å erstatte full dokumentasjon i rapporten.
            </p>
          </div>
        </div>
      </header>

      <section ref="refContext" :class="['mt-10', clsContext]" aria-labelledby="ctx-heading">
        <h2 id="ctx-heading" class="sr-only">Kontekst</h2>
        <div
          class="rounded-2xl border border-slate-200 bg-white p-6 shadow-md transition-shadow hover:shadow-lg lg:p-8"
        >
          <div class="flex items-start gap-4">
            <div
              class="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-blue-600 text-white shadow-md"
            >
              <BookOpen class="h-6 w-6" aria-hidden="true" />
            </div>
            <div class="space-y-3 text-slate-700">
              <p class="text-base leading-relaxed lg:text-lg">
                Løsningen ble utviklet over
                <strong class="text-slate-900">tre uker</strong> som del av emnet, fordelt på
                <strong class="text-slate-900">to sprinter med koding</strong> og
                <strong class="text-slate-900">én sprint med dokumentasjon</strong>.
                Utviklet i
                <strong class="text-slate-900">IDATT2106 – Systemutvikling med smidig prosjekt</strong>
                ved NTNU. Krav og visjon følger produkteiernes
                <a
                  href="/docs/visjonsdokument-2025-final.pdf"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="font-semibold text-blue-800 underline decoration-blue-600/50 underline-offset-2 hover:text-blue-950"
                >
                  Visjonsdokument 2025 (final)
                </a>
                for Krisefikser.no: øke egenberedskap og samle spredte tjenester i én løsning —
                strukturert etter <strong class="text-slate-900">før, under og etter</strong> krise.
              </p>
              <p>
                <a
                  href="/docs/visjonsdokument-2025-final.pdf"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-sm font-medium text-blue-700 underline underline-offset-2 hover:text-blue-900"
                >
                  Åpne visjonsdokument (PDF) for videre innspill
                </a>
              </p>
              <p class="text-sm text-slate-500">
                Lavere prioriterte brukerhistorier kan være delvis eller ikke med — studentprosjekt
                med begrenset sprintkapasitet.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section ref="refPillars" :class="['mt-12', clsPillars]" aria-labelledby="pillars-heading">
        <div class="mb-6 text-center lg:text-left">
          <h2 id="pillars-heading" class="text-2xl font-bold text-slate-900">
            Hovedspor i løsningen
          </h2>
          <p class="mt-1 text-slate-600">
            Tre innganger som speiler kjernen i visjonsdokumentet.
          </p>
        </div>
        <div class="grid gap-5 md:grid-cols-3">
          <router-link
            to="/kart"
            class="group relative flex flex-col rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-md transition-all duration-300 hover:-translate-y-1 hover:border-blue-200 hover:shadow-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2 motion-reduce:hover:translate-y-0"
          >
            <div
              class="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-700 transition-colors group-hover:bg-blue-100"
            >
              <Map class="h-6 w-6" aria-hidden="true" />
            </div>
            <h3 class="text-lg font-semibold text-slate-900">Interaktivt kart</h3>
            <p class="mt-2 flex-1 text-sm leading-relaxed text-slate-600">
              Tilfluktsrom, hjertestartere og flere ressurser i nærheten.
            </p>
            <span
              class="mt-4 inline-flex items-center text-sm font-semibold text-blue-700 group-hover:gap-1"
            >
              Åpne kart
              <span class="transition-transform group-hover:translate-x-0.5" aria-hidden="true"
                >→</span
              >
            </span>
          </router-link>

          <router-link
            to="/kriser"
            class="group relative flex flex-col rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-md transition-all duration-300 hover:-translate-y-1 hover:border-amber-200 hover:shadow-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-500 focus-visible:ring-offset-2 motion-reduce:hover:translate-y-0"
          >
            <div
              class="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl bg-amber-50 text-amber-700 transition-colors group-hover:bg-amber-100"
            >
              <AlertTriangle class="h-6 w-6" aria-hidden="true" />
            </div>
            <h3 class="text-lg font-semibold text-slate-900">Kriser og hendelser</h3>
            <p class="mt-2 flex-1 text-sm leading-relaxed text-slate-600">
              Liste og detaljsider med situasjonsinformasjon og kobling til kart.
            </p>
            <span
              class="mt-4 inline-flex items-center text-sm font-semibold text-amber-800 group-hover:gap-1"
            >
              Se kriser
              <span class="transition-transform group-hover:translate-x-0.5" aria-hidden="true"
                >→</span
              >
            </span>
          </router-link>

          <router-link
            to="/bli-med-eller-opprett-husstand"
            class="group relative flex flex-col rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-md transition-all duration-300 hover:-translate-y-1 hover:border-emerald-200 hover:shadow-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-600 focus-visible:ring-offset-2 motion-reduce:hover:translate-y-0"
          >
            <div
              class="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700 transition-colors group-hover:bg-emerald-100"
            >
              <Home class="h-6 w-6" aria-hidden="true" />
            </div>
            <h3 class="text-lg font-semibold text-slate-900">Husstand</h3>
            <p class="mt-2 flex-1 text-sm leading-relaxed text-slate-600">
              Samarbeid om beredskap, lager og varsler når du er innlogget.
            </p>
            <span
              class="mt-4 inline-flex items-center text-sm font-semibold text-emerald-800 group-hover:gap-1"
            >
              Kom i gang
              <span class="transition-transform group-hover:translate-x-0.5" aria-hidden="true"
                >→</span
              >
            </span>
          </router-link>
        </div>
      </section>

      <section ref="refChips" :class="['mt-12', clsChips]" aria-labelledby="more-links-heading">
        <h2 id="more-links-heading" class="text-lg font-semibold text-slate-900">
          Flere spor i appen
        </h2>
        <p class="mt-1 text-sm text-slate-600">
          Infofasene, innhold, konto og juridisk — hurtiglenker.
        </p>
        <div class="mt-4 flex flex-wrap gap-2">
          <router-link
            v-for="link in chipLinks"
            :key="link.to"
            :to="link.to"
            class="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 shadow-sm transition hover:border-blue-300 hover:bg-blue-50 hover:text-blue-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
          >
            {{ link.label }}
          </router-link>
        </div>
        <p class="mt-4 text-xs text-slate-500">
          Admin for kart, hendelser, scenarioer og innhold — for autoriserte administratorer.
        </p>
      </section>

      <section ref="refNfr" :class="['mt-14', clsNfr]" aria-labelledby="nfr-heading">
        <h2 id="nfr-heading" class="text-2xl font-bold text-slate-900">
          Ikke-funksjonelle krav
        </h2>
        <p class="mt-1 text-slate-600">Hvordan løsningen svarer på emnets forventninger.</p>
        <div class="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="item in nfrItems"
            :key="item.title"
            class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
          >
            <component
              :is="item.icon"
              class="mb-3 h-8 w-8 text-blue-700"
              aria-hidden="true"
            />
            <h3 class="font-semibold text-slate-900">{{ item.title }}</h3>
            <p class="mt-2 text-sm leading-relaxed text-slate-600">{{ item.body }}</p>
          </div>
        </div>
      </section>

      <section ref="refStack" :class="['mt-14', clsStack]" aria-labelledby="stack-heading">
        <h2 id="stack-heading" class="text-2xl font-bold text-slate-900">Teknologistakk</h2>
        <div class="mt-6 grid gap-5 lg:grid-cols-2">
          <div
            class="rounded-2xl border border-slate-200 bg-gradient-to-br from-white to-slate-50 p-6 shadow-md"
          >
            <div class="mb-4 flex items-center gap-2 text-slate-900">
              <Monitor class="h-5 w-5 text-blue-600" aria-hidden="true" />
              <h3 class="text-lg font-semibold">Frontend</h3>
            </div>
            <ul class="space-y-2 text-sm text-slate-700">
              <li v-for="line in stackFront" :key="line" class="flex gap-2">
                <span class="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500" aria-hidden="true" />
                <span>{{ line }}</span>
              </li>
            </ul>
          </div>
          <div
            class="rounded-2xl border border-slate-200 bg-gradient-to-br from-white to-slate-50 p-6 shadow-md"
          >
            <div class="mb-4 flex items-center gap-2 text-slate-900">
              <ShieldCheck class="h-5 w-5 text-emerald-600" aria-hidden="true" />
              <h3 class="text-lg font-semibold">Backend</h3>
            </div>
            <ul class="space-y-2 text-sm text-slate-700">
              <li v-for="line in stackBack" :key="line" class="flex gap-2">
                <span class="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500" aria-hidden="true" />
                <span>{{ line }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <footer
        ref="refFooter"
        :class="['mt-14 flex flex-col gap-4 border-t border-slate-200 pt-8 sm:flex-row sm:items-center sm:justify-between', clsFooter]"
      >
        <p class="text-sm text-slate-600">
          Team og formål:
          <router-link
            to="/om-oss"
            class="font-medium text-blue-700 underline-offset-2 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
          >
            Om oss
          </router-link>
          . Full brukerhistorier og krav ligger i visjonsdokumentet for rapport og sporbarhet.
        </p>
        <router-link
          to="/"
          class="inline-flex items-center justify-center rounded-lg border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-800 shadow-sm transition hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
        >
          Tilbake til forsiden
        </router-link>
      </footer>
    </div>
  </div>
</template>
