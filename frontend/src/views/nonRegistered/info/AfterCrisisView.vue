<script lang="ts">
/* eslint-disable vue/no-unused-components */
import { CheckCircle, Info, Heart, PhoneCall, Users, BookOpen, Calendar, ClipboardCheck } from 'lucide-vue-next';
import QuizComponent from '@/components/quiz/QuizComponent.vue';
import PageLayout from '@/components/layout/PageLayout.vue';
import ContentCard from '@/components/ui/ContentCard.vue';
import AlertBox from '@/components/ui/AlertBox.vue';
import CTASection from '@/components/ui/CTASection.vue';

export default {
  name: 'AfterCrisisView',
  components: {
    PageLayout,
    ContentCard,
    AlertBox,
    CTASection,
    CheckCircle,
    Info,
    Heart,
    PhoneCall,
    Users,
    BookOpen,
    Calendar,
    ClipboardCheck,
    QuizComponent
  },
  data() {
    return {
      // Store references to icon components
      iconComponents: {
        CheckCircle,
        Info,
        Heart,
        PhoneCall,
        Users,
        BookOpen,
        Calendar,
        ClipboardCheck
      },
      quizQuestions: [
        {
          question: 'Hva er et viktig første steg etter at en krise er over?',
          options: [
            'Glemme hele opplevelsen og gå videre',
            'Dokumentere eventuelle skader for forsikringsformål',
            'Fortelle alle du kjenner om opplevelsen',
            'Kritisere myndighetenes håndtering'
          ],
          correctAnswer: 1,
          explanation: 'Å dokumentere skader med bilder og notater er viktig for forsikringskrav og for å få nødvendig støtte til gjenoppbygging.'
        },
        {
          question: 'Hva er en sunn tilnærming til mental helse etter en krise?',
          options: [
            'Undertrykke alle negative følelser for å komme seg videre',
            'Isolere seg for å unngå å vise svakhet',
            'Akseptere at sterke følelser er normale og gi deg selv tid til å bearbeide',
            'Konsumere så mye nyheter som mulig om hendelsen'
          ],
          correctAnswer: 2,
          explanation: 'Det er normalt å ha sterke følelsesmessige reaksjoner etter en krise. Å akseptere disse følelsene og gi seg selv tid til å bearbeide er en sunn tilnærming.'
        },
        {
          question: 'Hvordan kan du best forbedre din beredskap basert på erfaringer fra en krise?',
          options: [
            'Kjøpe inn så mye beredskapsutstyr som mulig uansett kostnad',
            'Evaluere hva som fungerte og hva som manglet, deretter oppdatere beredskapsplanen',
            'Fokusere kun på de største problemene som oppstod',
            'Stole på at myndighetene vil håndtere alle fremtidige kriser bedre'
          ],
          correctAnswer: 1,
          explanation: 'Å evaluere dine erfaringer systematisk og bruke dette til å oppdatere beredskapsplanen er den mest effektive måten å forbedre fremtidig beredskap.'
        },
        {
          question: 'Hvordan kan du best bidra til samfunnets gjenoppbygging etter en krise?',
          options: [
            'Fokusere utelukkende på din egen families behov',
            'Kritisere myndighetenes innsats på sosiale medier',
            'Delta i lokale dugnader og støtte naboer som fortsatt trenger hjelp',
            'Vente på at myndighetene skal løse alle problemer'
          ],
          correctAnswer: 2,
          explanation: 'Et sterkt lokalsamfunn kommer seg raskere etter en krise. Ved å delta i lokale dugnader og støtte naboer bidrar du til raskere normalisering for alle.'
        }
      ]
    }
  }
}
</script>

<template>
  <PageLayout
    pageTitle="Etter krisen"
    sectionName="Etter krisen"
    :iconComponent="iconComponents.CheckCircle"
    iconBgColor="green"
  >
    <!-- Introduction with Quiz Highlight -->
    <ContentCard fullWidth>
      <div class="bg-green-50 p-4 rounded-lg border border-green-200 mb-4">
        <div class="flex items-center mb-2">
          <component :is="iconComponents.BookOpen" class="h-6 w-6 text-green-600 mr-2" />
          <h3 class="text-lg font-semibold text-gray-800">Test din kunnskap</h3>
        </div>
        <p class="text-gray-700">
          Denne siden inneholder en quiz nederst hvor du kan teste din kunnskap om hva du bør
          gjøre etter en krisesituasjon. Du får umiddelbar tilbakemelding og kan lære mens du tester deg selv.
        </p>
        <a href="#quiz-section" class="inline-flex items-center text-green-600 font-medium mt-2 hover:underline">
          Gå til quiz
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </a>
      </div>

      <h2 class="text-2xl font-semibold text-gray-800 mb-4">Gjenoppbygging og normalisering</h2>
      <p class="text-gray-700 mb-4">
        Når en krise er over, starter en viktig fase med gjenoppbygging, normalisering og læring.
        Hvordan du håndterer tiden etter en krise kan påvirke både din fysiske og mentale helse på lang sikt.
      </p>
      <p class="text-gray-700 mb-4">
        Denne siden gir deg veiledning om steg du bør ta etter en krise for å komme
        tilbake til hverdagen, evaluere dine erfaringer og forbedre din beredskap for fremtiden.
      </p>
    </ContentCard>

    <!-- Main content sections -->
    <div class="grid md:grid-cols-2 gap-8 mb-8">
      <!-- Immediate steps -->
      <ContentCard title="Umiddelbare tiltak" :iconComponent="iconComponents.ClipboardCheck" iconColor="green">
        <p class="text-gray-700 mb-4">
          Første steg etter at den akutte fasen er over:
        </p>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
          <li>Sikre at alle i husstanden er trygge og har det de trenger</li>
          <li>Sjekk boligen for skader og potensielle farer</li>
          <li>
            <span>Kontakt forsikringsselskap hvis du har materielle skader -
              <a href="https://www.finansportalen.no/forsikring/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Finansportalen</a> for forsikringsoversikt
            </span>
          </li>
          <li>Dokumenter eventuelle skader med bilder for forsikringsformål</li>
          <li>
            <span>Følg råd fra myndighetene via
              <a href="https://www.kriseinfo.no/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Kriseinfo.no</a> og din kommunes nettsider
            </span>
          </li>
          <li>
            <span>Sjekk aktuelle støtteordninger hos
              <a href="https://www.nav.no/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">NAV</a> og
              <a href="https://www.dsb.no/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">DSB</a>
            </span>
          </li>
        </ul>
        <AlertBox type="success" :iconComponent="iconComponents.Info">
          Vær oppmerksom på at det kan være ettereffekter av krisen som fortsatt krever
          forsiktighet, som forurenset vann eller ustabile bygg.
        </AlertBox>
      </ContentCard>

      <!-- Mental Health Recovery -->
      <ContentCard title="Mental helse og restitusjon" :iconComponent="iconComponents.Heart" iconColor="green">
        <p class="text-gray-700 mb-4">
          Psykologisk bearbeiding etter en krise er viktig:
        </p>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
          <li>Anerkjenn at det er normalt å ha sterke følelser etter en krise</li>
          <li>Ta tid til å bearbeide opplevelsen sammen med familie og venner</li>
          <li>Gjenoppta rutiner gradvis for å skape stabilitet</li>
          <li>Tillat deg å sørge over tap, enten materielle eller relasjonelle</li>
          <li>
            <span>Vær oppmerksom på symptomer på posttraumatisk stress (PTSD) -
              <a href="https://www.helsenorge.no/psykisk-helse/reaksjoner-etter-kriser-og-katastrofer/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Les mer på Helsenorge</a>
            </span>
          </li>
          <li>Søk profesjonell hjelp hvis du opplever vedvarende symptomer</li>
          <li>Vær spesielt oppmerksom på barns behov for å bearbeide</li>
        </ul>
        <div class="bg-green-50 p-4 rounded-lg border border-green-200">
          <h4 class="font-semibold text-gray-800 mb-2">Psykisk helsehjelp:</h4>
          <ul class="space-y-2">
            <li>
              <a href="tel:116123" class="flex items-center text-green-600 hover:underline">
                <PhoneCall class="h-4 w-4 mr-2" />
                <span class="font-bold">Mental Helse Hjelpetelefon: 116 123</span>
                <span class="ml-2 text-sm text-gray-600">(døgnåpen)</span>
              </a>
            </li>
            <li>
              <a href="tel:80033321" class="flex items-center text-green-600 hover:underline">
                <PhoneCall class="h-4 w-4 mr-2" />
                <span class="font-bold">Røde Kors Samtaletilbud: 800 33 321</span>
              </a>
            </li>
            <li>
              <a href="https://helsenorge.no/psykisk-helse" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">
                Helsenorge - Informasjon om psykisk helse
              </a>
            </li>
          </ul>
        </div>
      </ContentCard>
    </div>

    <!-- Helping community recover -->
    <ContentCard
      title="Bidra til samfunnets gjenoppbygging"
      :iconComponent="iconComponents.Users"
      iconColor="green"
      fullWidth
    >
      <p class="text-gray-700 mb-4">
        Et lokalsamfunn kommer raskere på fote når alle bidrar:
      </p>
      <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
        <li>
          <span>Delta i lokale opprydningsaksjoner og dugnader - spør via kommunens nettsider eller lokale Facebook-grupper</span>
        </li>
        <li>Tilby hjelp til naboer som fremdeles strever</li>
        <li>Del ressurser med de som har mistet mer enn deg</li>
        <li>Støtt lokale virksomheter som er rammet av krisen</li>
        <li>
          <span>Delta i frivillig arbeid gjennom etablerte organisasjoner som
            <a href="https://www.rodekors.no/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Røde Kors</a>,
            <a href="https://www.frivillig.no/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Frivillig.no</a> eller
            <a href="https://www.kirkens-sos.no/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Kirkens SOS</a>
          </span>
        </li>
        <li>
          <span>Del dine erfaringer med
            <a href="https://www.dsb.no/rapporter-og-evalueringer/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">DSB</a> for å bidre til bedre krisehåndtering
          </span>
        </li>
      </ul>
      <p class="text-gray-700">
        Et sterkt lokalsamfunn er en av de beste beskyttelsene mot fremtidige kriser. Ved å
        bygge sosiale nettverk før en krise, står man bedre rustet til å komme seg gjennom den sammen.
      </p>
    </ContentCard>

    <!-- Learning and Improving -->
    <ContentCard
      title="Lær av erfaringen - forbedre beredskapen"
      :iconComponent="iconComponents.BookOpen"
      iconColor="green"
      fullWidth
    >
      <p class="text-gray-700 mb-4">
        Hver krise gir verdifull erfaring for fremtiden:
      </p>
      <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
        <li>Evaluer hvordan beredskapsplanen din fungerte</li>
        <li>
          <span>Noter hva som manglet i beredskapslageret -
            <a href="https://www.sikkerhverdag.no/din-beredskap/hendelser-og-kriser/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Sjekk anbefalte forsyninger på Sikker Hverdag</a>
          </span>
        </li>
        <li>Dokumenter hva du lærte og hva du ville gjort annerledes</li>
        <li>
          <span>Oppdater beredskapsplanen basert på erfaringene -
            <a href="https://www.dsb.no/egenberedskap/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">DSBs råd om egenberedskap</a>
          </span>
        </li>
        <li>Etterfyll beredskapslageret med nye forsyninger</li>
        <li>
          <span>Revurder om det er nye ferdigheter du bør lære -
            <a href="https://www.rodekors.no/tilbudene/forstehjelp/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Førstehjelpskurs hos Røde Kors</a>
          </span>
        </li>
        <li>Del lærdommene med familie, venner og nærmiljø</li>
      </ul>
      <div class="mt-4 bg-green-50 p-6 rounded-lg">
        <h4 class="font-semibold text-gray-800 mb-3">Refleksjonsøvelse</h4>
        <p class="text-gray-700 mb-3">
          Sett av tid til å reflektere over følgende spørsmål:
        </p>
        <ul class="list-decimal list-inside text-gray-700 space-y-2">
          <li>Hva var de største utfordringene under krisen?</li>
          <li>Hvilke forberedelser var mest nyttige?</li>
          <li>Hva manglet jeg som ville gjort situasjonen enklere?</li>
          <li>Hvordan fungerte kommunikasjonen i familien/husstanden?</li>
          <li>Hva lærte jeg om egne evner og utholdenhet?</li>
        </ul>
        <p class="mt-3 text-gray-700">
          Registrerte brukere kan lagre sine refleksjoner i appen for fremtidig referanse.
        </p>
      </div>
    </ContentCard>

    <!-- Restoring normal life -->
    <ContentCard
      title="Tilbake til hverdagen"
      :iconComponent="iconComponents.Calendar"
      iconColor="green"
      fullWidth
    >
      <p class="text-gray-700 mb-4">
        Gjenopprettelse av normalitet er viktig, men kan ta tid:
      </p>
      <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
        <li>Gjenoppta rutiner gradvis, spesielt for barn</li>
        <li>Tillat fleksibilitet i forventninger og planer</li>
        <li>Balansér behovet for å snakke om opplevelsen med å se fremover</li>
        <li>Markér milepæler i gjenoppbyggingen for å anerkjenne fremgang</li>
        <li>Finn måter å ta med seg positive erfaringer fra krisen (samhold, etc.)</li>
        <li>
          <span>Vær tålmodig med deg selv og andre - gjenoppbygging tar tid -
            <a href="https://www.psykologforeningen.no/publikum/videoer/videoer-om-psykisk-helse/resiliens-hvordan-takle-motgang" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">Les om resiliens</a>
          </span>
        </li>
      </ul>
      <p class="text-gray-700">
        Å komme tilbake til normalitet betyr ikke nødvendigvis å gå tilbake til akkurat
        slik livet var før. Mange opplever at prioriteringer og perspektiver endres etter
        en krise, noe som kan føre til positive endringer på lang sikt.
      </p>
      <div class="bg-green-50 p-4 rounded-lg border border-green-200 mt-4">
        <h4 class="font-medium text-gray-800 mb-2">Praktiske ressurser for nystart:</h4>
        <ul class="space-y-2">
          <li>
            <a href="https://www.nav.no/no/person/flere-tema/sosiale-tjenester/okonomisk-sosialhjelp" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">
              NAV - Økonomisk støtte
            </a>
          </li>
          <li>
            <a href="https://www.husbanken.no/om-husbanken/virkemidler/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">
              Husbanken - Boligstøtte
            </a>
          </li>
          <li>
            <a href="https://www.skatteetaten.no/person/" target="_blank" rel="noopener noreferrer" class="text-green-600 hover:underline">
              Skatteetaten - Skattemessige konsekvenser ved tap
            </a>
          </li>
        </ul>
      </div>
    </ContentCard>

    <!-- Quiz section -->
    <ContentCard fullWidth id="quiz-section">
      <div class="bg-green-50 p-6 rounded-lg mb-6">
        <h3 class="text-2xl font-bold text-gray-800 mb-2 flex items-center">
          <component :is="iconComponents.BookOpen" class="h-6 w-6 text-green-600 mr-2" />
          Test din kunnskap om tiden etter en krise
        </h3>
        <p class="text-gray-700 mb-4">
          Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre etter en krisesituasjon.
          Hver riktig besvarelse styrker din mentale beredskap.
        </p>
      </div>

      <QuizComponent
        title="Test din kunnskap"
        description="Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre etter en krisesituasjon."
        :questions="quizQuestions"
        colorTheme="green"
      />
    </ContentCard>

    <!-- CTA section -->
    <CTASection
      title="Forbedre din beredskap basert på erfaringer"
      description="Ved å registrere deg kan du opprette og administrere din beredskapsplan, få oversikt over nødvendig utstyr og være bedre forberedt til neste gang."
      authTitle="Oppdater beredskapen basert på dine erfaringer"
      authDescription="Nå som krisen er over, er det viktig å evaluere hva som fungerte og hva som manglet i din beredskap. Oppdater beholdningen din og forbedre beredskapsplanen."
      primaryButtonText="Registrer deg nå"
      primaryButtonRoute="/register"
      secondaryButtonText="Logg inn"
      secondaryButtonRoute="/logg-inn"
      authPrimaryButtonText="Oppdater beredskapslager"
      authPrimaryButtonRoute="/husstand/:id/beredskapslager"
      authSecondaryButtonText="Administrer husstand"
      authSecondaryButtonRoute="/husstand"
      colorTheme="green"
    />
  </PageLayout>
</template>
