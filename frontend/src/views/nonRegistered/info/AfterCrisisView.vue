<script lang="ts">
import { Home, ChevronRight, CheckCircle, Info, Heart, PhoneCall, Users, BookOpen, Calendar, ClipboardCheck } from 'lucide-vue-next';
import QuizComponent from '@/components/quiz/QuizComponent.vue';

export default {
  name: 'AfterCrisisView',
  components: {
    Home,
    ChevronRight,
    CheckCircle,
    Info,
    Heart,
    PhoneCall,
    Users,
    BookOpen,
    Calendar,
    ClipboardCheck,
    QuizComponent: QuizComponent
  },
  data() {
    return {
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
  <div class="min-h-screen flex flex-col bg-slate-50 overflow-hidden">
    <!-- Page content -->
    <div class="flex-grow container mx-auto px-4 py-8">
      <!-- Breadcrumb navigation -->
      <div class="mb-8">
        <nav class="flex" aria-label="Breadcrumb">
          <ol class="inline-flex items-center space-x-1 md:space-x-3">
            <li class="inline-flex items-center">
              <router-link to="/" class="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600">
                <Home class="w-4 h-4 mr-2" />
                Hjem
              </router-link>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRight class="w-4 h-4 text-gray-400" />
                <span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">Etter krisen</span>
              </div>
            </li>
          </ol>
        </nav>
      </div>

      <!-- Header -->
      <div class="flex items-center mb-8">
        <div class="bg-green-100 p-3 rounded-full mr-4">
          <CheckCircle class="h-8 w-8 text-green-600" />
        </div>
        <h1 class="text-3xl font-bold text-gray-800">Etter krisen</h1>
      </div>

      <!-- Introduction -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Gjenoppbygging og normalisering</h2>
        <p class="text-gray-700 mb-4">
          Når en krise er over, starter en viktig fase med gjenoppbygging, normalisering og læring.
          Hvordan du håndterer tiden etter en krise kan påvirke både din fysiske og mentale helse på lang sikt.
        </p>
        <p class="text-gray-700 mb-4">
          Denne siden gir deg veiledning om steg du bør ta etter en krise for å komme
          tilbake til hverdagen, evaluere dine erfaringer og forbedre din beredskap for fremtiden.
        </p>
      </div>

      <!-- Main content sections -->
      <div class="grid md:grid-cols-2 gap-8 mb-8">
        <!-- Immediate steps -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <div class="bg-green-50 p-2 rounded-full mr-3">
              <ClipboardCheck class="h-6 w-6 text-green-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-800">Umiddelbare tiltak</h3>
          </div>
          <p class="text-gray-700 mb-4">
            Første steg etter at den akutte fasen er over:
          </p>
          <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
            <li>Sikre at alle i husstanden er trygge og har det de trenger</li>
            <li>Sjekk boligen for skader og potensielle farer</li>
            <li>Kontakt forsikringsselskap hvis du har materielle skader</li>
            <li>Dokumenter eventuelle skader med bilder for forsikringsformål</li>
            <li>Følg råd fra myndighetene angående rent vann, matsikkerhet og rengjøring</li>
            <li>Følg med på offisielle informasjonskanaler for oppdateringer</li>
          </ul>
          <div class="bg-green-50 p-4 rounded-md">
            <div class="flex items-start">
              <Info class="h-5 w-5 text-green-600 mt-0.5 mr-2 flex-shrink-0" />
              <p class="text-green-800">
                Vær oppmerksom på at det kan være ettereffekter av krisen som fortsatt krever
                forsiktighet, som forurenset vann eller ustabile bygg.
              </p>
            </div>
          </div>
        </div>

        <!-- Mental Health Recovery -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <div class="bg-green-50 p-2 rounded-full mr-3">
              <Heart class="h-6 w-6 text-green-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-800">Mental helse og restitusjon</h3>
          </div>
          <p class="text-gray-700 mb-4">
            Psykologisk bearbeiding etter en krise er viktig:
          </p>
          <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
            <li>Anerkjenn at det er normalt å ha sterke følelser etter en krise</li>
            <li>Ta tid til å bearbeide opplevelsen sammen med familie og venner</li>
            <li>Gjenoppta rutiner gradvis for å skape stabilitet</li>
            <li>Tillat deg å sørge over tap, enten materielle eller relasjonelle</li>
            <li>Vær oppmerksom på symptomer på posttraumatisk stress (PTSD)</li>
            <li>Søk profesjonell hjelp hvis du opplever vedvarende symptomer</li>
            <li>Vær spesielt oppmerksom på barns behov for å bearbeide</li>
          </ul>
          <div class="bg-blue-50 p-4 rounded-md">
            <div class="flex items-start">
              <PhoneCall class="h-5 w-5 text-blue-600 mt-0.5 mr-2 flex-shrink-0" />
              <p class="text-blue-800">
                Hvis du trenger noen å snakke med, kontakt fastlege, Mental Helse Hjelpetelefon: 116 123,
                eller Røde Kors samtaletilbud: 800 33 321.
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Helping community recover -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-green-50 p-2 rounded-full mr-3">
            <Users class="h-6 w-6 text-green-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Bidra til samfunnets gjenoppbygging</h3>
        </div>
        <p class="text-gray-700 mb-4">
          Et lokalsamfunn kommer raskere på fote når alle bidrar:
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>Delta i lokale opprydningsaksjoner og dugnader</li>
          <li>Tilby hjelp til naboer som fremdeles strever</li>
          <li>Del ressurser med de som har mistet mer enn deg</li>
          <li>Støtt lokale virksomheter som er rammet av krisen</li>
          <li>Delta i frivillig arbeid gjennom etablerte organisasjoner</li>
          <li>Del dine erfaringer for å bidra til bedre krisehåndtering i fremtiden</li>
        </ul>
        <p class="text-gray-700">
          Et sterkt lokalsamfunn er en av de beste beskyttelsene mot fremtidige kriser. Ved å
          bygge sosiale nettverk før en krise, står man bedre rustet til å komme seg gjennom den sammen.
        </p>
      </div>

      <!-- Learning and Improving -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-green-50 p-2 rounded-full mr-3">
            <BookOpen class="h-6 w-6 text-green-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Lær av erfaringen - forbedre beredskapen</h3>
        </div>
        <p class="text-gray-700 mb-4">
          Hver krise gir verdifull erfaring for fremtiden:
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>Evaluer hvordan beredskapsplanen din fungerte</li>
          <li>Noter hva som manglet i beredskapslageret</li>
          <li>Dokumenter hva du lærte og hva du ville gjort annerledes</li>
          <li>Oppdater beredskapsplanen basert på erfaringene</li>
          <li>Etterfyll beredskapslageret med nye forsyninger</li>
          <li>Revurder om det er nye ferdigheter du bør lære (førstehjelp, etc.)</li>
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
      </div>

      <!-- Restoring normal life -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-green-50 p-2 rounded-full mr-3">
            <Calendar class="h-6 w-6 text-green-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Tilbake til hverdagen</h3>
        </div>
        <p class="text-gray-700 mb-4">
          Gjenopprettelse av normalitet er viktig, men kan ta tid:
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>Gjenoppta rutiner gradvis, spesielt for barn</li>
          <li>Tillat fleksibilitet i forventninger og planer</li>
          <li>Balansér behovet for å snakke om opplevelsen med å se fremover</li>
          <li>Markér milepæler i gjenoppbyggingen for å anerkjenne fremgang</li>
          <li>Finn måter å ta med seg positive erfaringer fra krisen (samhold, etc.)</li>
          <li>Vær tålmodig med deg selv og andre - gjenoppbygging tar tid</li>
        </ul>
        <p class="text-gray-700">
          Å komme tilbake til normalitet betyr ikke nødvendigvis å gå tilbake til akkurat
          slik livet var før. Mange opplever at prioriteringer og perspektiver endres etter
          en krise, noe som kan føre til positive endringer på lang sikt.
        </p>
      </div>

      <!-- Quiz section -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <QuizComponent
          title="Test din kunnskap"
          description="Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre etter en krisesituasjon."
          :questions="quizQuestions"
          colorTheme="green"
        />
      </div>

      <!-- CTA section -->
      <div class="bg-green-600 text-white rounded-lg shadow-md p-8 mb-8">
        <h3 class="text-2xl font-bold mb-4">Del dine erfaringer og lær av andre</h3>
        <p class="mb-6">
          Ved å registrere deg kan du lagre dine refleksjoner, lese andres erfaringer og
          kontinuerlig forbedre din egenberedskap basert på faktiske opplevelser.
        </p>
        <div class="flex flex-col sm:flex-row gap-4">
          <router-link to="/register" class="bg-white text-green-600 font-medium px-6 py-2 rounded-md hover:bg-green-50 transition text-center">
            Registrer deg nå
          </router-link>
          <router-link to="/login" class="border border-white text-white px-6 py-2 rounded-md hover:bg-green-700 transition text-center">
            Logg inn
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

