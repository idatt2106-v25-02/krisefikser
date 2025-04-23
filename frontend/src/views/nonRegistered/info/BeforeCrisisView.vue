<script lang ="ts">
import { Home, ChevronRight, Plus, Coffee, Flashlight, ClipboardList, Radio, Info, AlertTriangle } from 'lucide-vue-next';
import QuizComponent from '@/components/quiz/QuizComponent.vue';

export default {
  name: 'BeforeCrisisView',
  components: {
    Home,
    ChevronRight,
    Plus,
    Coffee,
    Flashlight,
    ClipboardList,
    Radio,
    Info,
    AlertTriangle,
    QuizComponent: QuizComponent
  },
  data() {
    return {
      quizQuestions: [
        {
          question: 'Hvor mange liter vann anbefales per person per dag i en krisesituasjon?',
          options: ['1 liter', '2 liter', '3 liter', '5 liter'],
          correctAnswer: 2,
          explanation: 'DSB anbefaler 3 liter vann per person per dag til drikke og matlaging.'
        },
        {
          question: 'Hvor lenge bør en norsk husstand minimum være selvforsynt ved en krise?',
          options: ['1 døgn', '3 døgn', '7 døgn', '14 døgn'],
          correctAnswer: 1,
          explanation: 'Myndighetene anbefaler at alle norske husstander bør være selvforsynte i minst tre døgn.'
        },
        {
          question: 'Hvilken radiokanal er Norges offisielle beredskapskanal?',
          options: ['NRK P1', 'NRK P2', 'NRK P3', 'Radio Norge'],
          correctAnswer: 0,
          explanation: 'NRK P1 er Norges offisielle beredskapskanal og vil sende viktig informasjon ved kriser.'
        },
        {
          question: 'Hva bør en husstand gjøre med sin beredskapsplan?',
          options: [
            'Holde den hemmelig for barn for å ikke skremme dem',
            'Kun dele den med voksne familiemedlemmer',
            'Diskutere den med alle familiemedlemmer og oppdatere den årlig',
            'Kun lage en plan når en krise oppstår'
          ],
          correctAnswer: 2,
          explanation: 'En beredskapsplan bør diskuteres med alle familiemedlemmer, inkludert barn på en aldersvennlig måte, og oppdateres årlig eller når familiesituasjonen endrer seg.'
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
                <span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">Før krisen</span>
              </div>
            </li>
          </ol>
        </nav>
      </div>

      <!-- Header -->
      <div class="flex items-center mb-8">
        <div class="bg-blue-100 p-3 rounded-full mr-4">
          <Plus class="h-8 w-8 text-blue-600" />
        </div>
        <h1 class="text-3xl font-bold text-gray-800">Før krisen</h1>
      </div>

      <!-- Introduction -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Forbered deg og din husstand</h2>
        <p class="text-gray-700 mb-4">
          Å være forberedt før en krise inntreffer kan være avgjørende for hvordan du og
          dine nærmeste takler situasjonen. God beredskap handler om å ha tenkt gjennom
          mulige scenarier og ha nødvendige ressurser tilgjengelig.
        </p>
        <p class="text-gray-700 mb-4">
          Myndighetene anbefaler at alle norske husstander bør være selvforsynte i minst tre
          døgn. Det vil si at du bør ha tilstrekkelig med mat, vann, medisiner og andre
          nødvendigheter for å klare deg uten tilgang til strøm, vann eller butikker.
        </p>
      </div>

      <!-- Main content sections -->
      <div class="grid md:grid-cols-2 gap-8 mb-8">
        <!-- Food and Water -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <div class="bg-blue-50 p-2 rounded-full mr-3">
              <Coffee class="h-6 w-6 text-blue-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-800">Mat og vann</h3>
          </div>
          <h4 class="font-medium text-gray-700 mb-2">Anbefalt per person for 3 døgn:</h4>
          <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
            <li>9 liter vann (3 liter per dag)</li>
            <li>To pakker knekkebrød</li>
            <li>En pakke havregryn</li>
            <li>Tre bokser middagshermetikk eller tre poser tørrmat</li>
            <li>Tre bokser pålegg med lang holdbarhet</li>
            <li>Noen poser tørket frukt eller nøtter, kjeks og sjokolade</li>
          </ul>
          <p class="text-gray-700">
            Husk å tenke på spesielle behov i husstanden som babymat, allergier eller
            spesielle dietter. Sørg for at maten kan tilberedes uten strøm.
          </p>
        </div>

        <!-- Equipment and Supplies -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <div class="bg-blue-50 p-2 rounded-full mr-3">
              <Flashlight class="h-6 w-6 text-blue-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-800">Utstyr og forsyninger</h3>
          </div>
          <h4 class="font-medium text-gray-700 mb-2">Grunnleggende utstyr:</h4>
          <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
            <li>Førstehjelpsutstyr</li>
            <li>Batteridrevet DAB-radio</li>
            <li>Batterier, powerbank og ladere</li>
            <li>Lommelykt eller hodelykter</li>
            <li>Stearinlys, fyrstikker eller lighter</li>
            <li>Kontanter i mindre valører</li>
            <li>Våtservietter og desinfeksjonsmiddel</li>
            <li>Gasskoker med brensel</li>
            <li>Varme klær, pledd og soveposer</li>
            <li>Medisiner du er avhengig av</li>
          </ul>
          <p class="text-gray-700">
            Kontroller utstyret regelmessig for utløpsdato og funksjonalitet. Bytt ut
            batterier og forny mat og medisiner ved behov.
          </p>
        </div>
      </div>

      <!-- Emergency Plan -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-blue-50 p-2 rounded-full mr-3">
            <ClipboardList class="h-6 w-6 text-blue-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Familiens beredskapsplan</h3>
        </div>
        <p class="text-gray-700 mb-4">
          En beredskapsplan hjelper hele husstanden med å vite hva som skal gjøres i en
          krisesituasjon. Planen bør diskuteres med alle familiemedlemmer.
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>Avtal et møtested hvis dere ikke kan komme hjem</li>
          <li>Opprett en kontaktliste med viktige telefonnumre</li>
          <li>Bestem hvem som henter barn fra skole/barnehage ved en krise</li>
          <li>Identifiser nærmeste tilfluktsrom og evakueringsveier fra hjemmet</li>
          <li>Kartlegg hvem i nærområdet som kan trenge hjelp (eldre, syke, etc.)</li>
          <li>Skriv ned eventuelle medisinske behov og allergier for hvert familiemedlem</li>
        </ul>
        <div class="bg-blue-50 p-4 rounded-md">
          <div class="flex items-start">
            <Info class="h-5 w-5 text-blue-600 mt-0.5 mr-2 flex-shrink-0" />
            <p class="text-blue-800">
              Husk å oppdatere beredskapsplanen hvis familiesituasjonen endrer seg,
              og gå gjennom den med alle familiemedlemmer minst én gang i året.
            </p>
          </div>
        </div>
      </div>

      <!-- Information sources -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-blue-50 p-2 rounded-full mr-3">
            <Radio class="h-6 w-6 text-blue-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Informasjonskilder</h3>
        </div>
        <p class="text-gray-700 mb-4">
          I en krisesituasjon er pålitelig informasjon avgjørende. Kjenn til disse kildene i forkant:
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>NRK P1 er beredskapskanalen på radio (ha en batteridrevet radio)</li>
          <li>Offisielle nettsider: DSB.no, Helsedirektoratet.no, Politiet.no</li>
          <li>Kommunens nettsider og sosiale mediekanaler</li>
          <li>Nødnumre: 110 (brann), 112 (politi), 113 (ambulanse), 116 117 (legevakt)</li>
          <li>Last ned appen "Hjelp 113" for å kunne dele nøyaktig posisjon med nødetatene</li>
        </ul>
        <div class="bg-yellow-50 p-4 rounded-md">
          <div class="flex items-start">
            <AlertTriangle class="h-5 w-5 text-yellow-600 mt-0.5 mr-2 flex-shrink-0" />
            <p class="text-yellow-800">
              Vær kritisk til informasjon på sosiale medier. Stol på offisielle kanaler
              og informasjon fra myndighetene.
            </p>
          </div>
        </div>
      </div>

      <!-- Quiz section -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <QuizComponent
          title="Test din kunnskap"
          description="Svar på spørsmålene nedenfor for å se hvor godt forberedt du er på en krisesituasjon."
          :questions="quizQuestions"
          colorTheme="blue"
        />
      </div>

      <!-- CTA to register -->
      <div class="bg-blue-600 text-white rounded-lg shadow-md p-8 mb-8">
        <h3 class="text-2xl font-bold mb-4">Hold oversikt over beredskapslager</h3>
        <p class="mb-6">
          Registrer deg for å bygge og holde oversikt over ditt eget beredskapslager.
          Få påminnelser når varer i beredskapslageret nærmer seg utløpsdato, og del
          med husstanden din.
        </p>
        <div class="flex flex-col sm:flex-row gap-4">
          <router-link to="/register" class="bg-white text-blue-600 font-medium px-6 py-2 rounded-md hover:bg-blue-50 transition text-center">
            Registrer deg nå
          </router-link>
          <router-link to="/login" class="border border-white text-white px-6 py-2 rounded-md hover:bg-blue-700 transition text-center">
            Logg inn
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>


