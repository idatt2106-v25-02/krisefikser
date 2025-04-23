<script lang = "ts">
import { Home, ChevronRight, AlertTriangle, Radio, Info, Shield, AlertCircle, Zap, Droplets, Cloud,
  Stethoscope, Heart, PhoneCall, Users } from 'lucide-vue-next';
import QuizComponent from '@/components/quiz/QuizComponent.vue';

export default {
  name: 'DuringCrisisView',
  components: {
    Home,
    ChevronRight,
    AlertTriangle,
    Radio,
    Info,
    Shield,
    AlertCircle,
    Zap,
    Droplets,
    Cloud,
    Stethoscope,
    Heart,
    PhoneCall,
    Users,
    QuizComponent: QuizComponent
  },
  data() {
    return {
      quizQuestions: [
        {
          question: 'Hva er det viktigste å gjøre for å få pålitelig informasjon under en krise?',
          options: [
            'Sjekke sosiale medier for raskest mulig oppdateringer',
            'Lytte til ryktebørsen for å få mest mulig informasjon',
            'Følge med på offisielle kanaler som NRK P1 og myndighetenes nettsider',
            'Ringe venner og familie for å høre hva de har hørt'
          ],
          correctAnswer: 2,
          explanation: 'Offisielle kanaler som NRK P1, myndighetenes nettsider og kommunale informasjonskanaler gir mest pålitelig informasjon under en krise.'
        },
        {
          question: 'Hva bør du IKKE gjøre under et langvarig strømbrudd?',
          options: [
            'Kle på deg flere lag med klær for å holde varmen',
            'Bruke gassgrill innendørs for matlaging',
            'Samle familien i ett rom for å bevare varmen',
            'Begrense åpning av kjøleskap for å bevare kulden'
          ],
          correctAnswer: 1,
          explanation: 'Bruk av gassgrill, grill eller andre forbrenningsapparater innendørs kan føre til kullosforgiftning, som kan være livsfarlig.'
        },
        {
          question: 'Hvordan bør du prioritere bruken av vann under en vannmangel?',
          options: [
            'Først til vask og hygiene, deretter til drikke',
            'Først til matlaging, deretter til vask og drikke',
            'Først til drikke og matlaging, deretter til hygiene',
            'Bruk like mye til alle formål'
          ],
          correctAnswer: 2,
          explanation: 'Under vannmangel bør du prioritere vann til drikke og matlaging først, da dette er kritisk for overlevelse. Hygiene er viktig, men kan løses med alternative metoder som våtservietter.'
        },
        {
          question: 'Hva er en god strategi for å bevare mental helse under en krise?',
          options: [
            'Følge med på nyheter kontinuerlig for å ikke gå glipp av informasjon',
            'Isolere seg for å unngå stress fra andre mennesker',
            'Opprettholde daglige rutiner og begrense nyhetspåvirkning',
            'Fokusere på worst-case scenarioer for å være forberedt på alt'
          ],
          correctAnswer: 2,
          explanation: 'Å opprettholde daglige rutiner gir en følelse av normalitet og kontroll. Å begrense medieeksponering hindrer overbelastning av kriseinformasjon, som kan forverre angst og stress.'
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
                <span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">Under krisen</span>
              </div>
            </li>
          </ol>
        </nav>
      </div>

      <!-- Header -->
      <div class="flex items-center mb-8">
        <div class="bg-yellow-100 p-3 rounded-full mr-4">
          <AlertTriangle class="h-8 w-8 text-yellow-600" />
        </div>
        <h1 class="text-3xl font-bold text-gray-800">Under krisen</h1>
      </div>

      <!-- Introduction -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Når krisen inntreffer</h2>
        <p class="text-gray-700 mb-4">
          Når en krise oppstår, er det viktig å holde roen og handle rasjonelt. Din evne til
          å ta gode beslutninger under press kan være avgjørende for din sikkerhet og de rundt deg.
        </p>
        <p class="text-gray-700 mb-4">
          Denne siden gir deg viktig informasjon om hvordan du bør handle under ulike typer
          krisesituasjoner, fra strømbrudd til naturkatastrofer og andre akutte hendelser.
        </p>
      </div>

      <!-- Main content sections -->
      <div class="grid md:grid-cols-2 gap-8 mb-8">
        <!-- Stay Informed -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <div class="bg-yellow-50 p-2 rounded-full mr-3">
              <Radio class="h-6 w-6 text-yellow-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-800">Hold deg informert</h3>
          </div>
          <p class="text-gray-700 mb-4">
            Korrekt og oppdatert informasjon er avgjørende under en krise:
          </p>
          <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
            <li>Lytt til NRK P1 eller andre offisielle radiokanaler</li>
            <li>Følg med på offisielle nettsider som DSB.no, Helsedirektoratet.no, og din kommunes nettsider</li>
            <li>Sjekk varsler fra myndighetene via offentlige varslingssystemer</li>
            <li>Vær kritisk til informasjon fra ikke-offisielle kilder, spesielt på sosiale medier</li>
            <li>Spar på mobilbatteribruk ved å redusere skjermtid og slå av unødvendige applikasjoner</li>
          </ul>
          <div class="bg-yellow-50 p-4 rounded-md">
            <div class="flex items-start">
              <Info class="h-5 w-5 text-yellow-600 mt-0.5 mr-2 flex-shrink-0" />
              <p class="text-yellow-800">
                Hvis mobilnettet er nede, kan DAB-radio være din viktigste informasjonskilde.
                Sørg for å ha batterier til radioen din.
              </p>
            </div>
          </div>
        </div>

        <!-- Basic Safety -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <div class="bg-yellow-50 p-2 rounded-full mr-3">
              <Shield class="h-6 w-6 text-yellow-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-800">Grunnleggende sikkerhet</h3>
          </div>
          <p class="text-gray-700 mb-4">
            Uavhengig av krisetype, følg disse grunnleggende sikkerhetstipsene:
          </p>
          <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
            <li>Følg instruksjoner fra myndighetene og nødetater</li>
            <li>Evakuer kun hvis du blir bedt om det eller hvis det er tydelig usikkert å bli</li>
            <li>Hold husstanden samlet eller oppretthold kontakt hvis dere er adskilt</li>
            <li>Ha alltid tilgang til nødvendige medisiner</li>
            <li>Bruk sunn fornuft og tenk nøye gjennom konsekvenser av handlinger</li>
            <li>Hjelp andre i nærområdet som trenger det, spesielt sårbare grupper</li>
          </ul>
          <div class="bg-red-50 p-4 rounded-md">
            <div class="flex items-start">
              <AlertCircle class="h-5 w-5 text-red-600 mt-0.5 mr-2 flex-shrink-0" />
              <p class="text-red-800">
                Ring nødnumre kun i faktiske nødssituasjoner: 110 (brann),
                112 (politi), 113 (ambulanse), 116 117 (legevakt).
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Specific Crisis Scenarios -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <h3 class="text-xl font-semibold text-gray-800 mb-4">Spesifikke krisesituasjoner</h3>

        <!-- Power outage -->
        <div class="mb-6 border-b border-gray-200 pb-6">
          <div class="flex items-center mb-3">
            <div class="bg-yellow-50 p-2 rounded-full mr-3">
              <Zap class="h-5 w-5 text-yellow-600" />
            </div>
            <h4 class="text-lg font-medium text-gray-800">Strømbrudd</h4>
          </div>
          <ul class="list-disc list-inside text-gray-700 mb-3 space-y-1">
            <li>Behold varmen: Kle på deg varme klær, samle familien i ett rom, bruk tepper</li>
            <li>Unngå å åpne kjøleskap og fryser mer enn nødvendig</li>
            <li>Bruk alternative lyskilder som lommelykter og stearinlys (pass på brannfare)</li>
            <li>Bruk ikke grill, gassgrill eller lignende inne - fare for kullosforgiftning</li>
            <li>Slå av elektriske apparater for å unngå skade når strømmen kommer tilbake</li>
          </ul>
        </div>

        <!-- Water supply disruption -->
        <div class="mb-6 border-b border-gray-200 pb-6">
          <div class="flex items-center mb-3">
            <div class="bg-yellow-50 p-2 rounded-full mr-3">
              <Droplets class="h-5 w-5 text-yellow-600" />
            </div>
            <h4 class="text-lg font-medium text-gray-800">Vannmangel</h4>
          </div>
          <ul class="list-disc list-inside text-gray-700 mb-3 space-y-1">
            <li>Rasjonér tilgjengelig vann - prioriter drikke og matlaging</li>
            <li>Bruk flaskevann eller lagret vann først</li>
            <li>Fyll badekar, bøtter og andre beholdere hvis du får forvarsel</li>
            <li>Følg råd fra myndighetene om koking eller rensing av vann</li>
            <li>Reduser toalettspyling - bruk minimal vannmengde eller alternativ løsning</li>
          </ul>
        </div>

        <!-- Natural disasters -->
        <div class="mb-6 border-b border-gray-200 pb-6">
          <div class="flex items-center mb-3">
            <div class="bg-yellow-50 p-2 rounded-full mr-3">
              <Cloud class="h-5 w-5 text-yellow-600" />
            </div>
            <h4 class="text-lg font-medium text-gray-800">Ekstremvær og naturkatastrofer</h4>
          </div>
          <ul class="list-disc list-inside text-gray-700 mb-3 space-y-1">
            <li>Følg evakueringsordre umiddelbart hvis myndighetene ber om det</li>
            <li>Hold deg innendørs, unna vinduer og yttervegg under storm</li>
            <li>Ved flom: Søk høyere terreng og unngå å kjøre eller gå gjennom flomvann</li>
            <li>Ved skred: Evakuer området umiddelbart hvis det er tegn til ustabil grunn</li>
            <li>Ha avklart møtested for familien hvis dere blir adskilt</li>
          </ul>
        </div>

        <!-- Health crises and pandemics -->
        <div>
          <div class="flex items-center mb-3">
            <div class="bg-yellow-50 p-2 rounded-full mr-3">
              <Stethoscope class="h-5 w-5 text-yellow-600" />
            </div>
            <h4 class="text-lg font-medium text-gray-800">Helsekrise og pandemier</h4>
          </div>
          <ul class="list-disc list-inside text-gray-700 mb-3 space-y-1">
            <li>Følg råd fra helsemyndighetene angående smittevern</li>
            <li>Unngå folkemengder og begrens sosial kontakt hvis anbefalt</li>
            <li>Praktiser god hygiene, spesielt håndvask og hosteetikette</li>
            <li>Bruk munnbind, hansker eller annet smittevernutstyr som anbefalt</li>
            <li>Sikre forsyninger av nødvendige medisiner, mat og hygieneprodukter</li>
            <li>Vær oppmerksom på symptomer og følg anbefalinger for testing/isolasjon</li>
          </ul>
        </div>
      </div>

      <!-- Mental Health Section -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-yellow-50 p-2 rounded-full mr-3">
            <Heart class="h-6 w-6 text-yellow-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Mental helse under kriser</h3>
        </div>
        <p class="text-gray-700 mb-4">
          En krisesituasjon kan være psykisk belastende. Her er noen strategier for å ta vare på mental helse:
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>Aksepter at følelser som frykt, angst og usikkerhet er normale reaksjoner</li>
          <li>Oppretthold rutiner for å skape forutsigbarhet, spesielt for barn</li>
          <li>Begrens medieeksponering for å unngå overbelastning av kriseinfo</li>
          <li>Hold kontakt med familie og venner, del bekymringer og støtt hverandre</li>
          <li>Fokuser på det du kan kontrollere, ikke det som er utenfor din kontroll</li>
          <li>Bruk avslapningsteknikker som dyp pust, meditasjon eller fysisk aktivitet</li>
          <li>Vær ekstra oppmerksom på barns reaksjoner og behov</li>
        </ul>
        <div class="bg-green-50 p-4 rounded-md">
          <div class="flex items-start">
            <PhoneCall class="h-5 w-5 text-green-600 mt-0.5 mr-2 flex-shrink-0" />
            <p class="text-green-800">
              Hvis du eller noen i familien opplever vedvarende psykiske plager,
              kontakt Mental Helse Hjelpetelefon: 116 123 (døgnåpen).
            </p>
          </div>
        </div>
      </div>

      <!-- Helping Others -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <div class="flex items-center mb-4">
          <div class="bg-yellow-50 p-2 rounded-full mr-3">
            <Users class="h-6 w-6 text-yellow-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800">Å hjelpe andre</h3>
        </div>
        <p class="text-gray-700 mb-4">
          Samfunnet blir sterkere når vi hjelper hverandre i kriser. Her er hvordan du kan bidra:
        </p>
        <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
          <li>Sjekk på naboer, særlig eldre, syke eller andre sårbare grupper</li>
          <li>Del ressurser som vann, mat eller nødvendig utstyr hvis du har til overs</li>
          <li>Tilby transport eller praktisk hjelp til de som trenger det</li>
          <li>Vurder å delta i frivillig arbeid gjennom organisasjoner som Røde Kors</li>
          <li>Spre kun verifisert informasjon, ikke rykter eller ubekreftet info</li>
        </ul>
        <p class="text-gray-700">
          Merk: Sørg for din egen sikkerhet først. Du kan ikke hjelpe andre hvis du selv er i fare.
        </p>
      </div>

      <!-- Quiz section -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-8">
        <QuizComponent
          title="Test din kunnskap"
          description="Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre under en krisesituasjon."
          :questions="quizQuestions"
          colorTheme="yellow"
        />
      </div>

      <!-- CTA for emergency locations -->
      <div class="bg-yellow-600 text-white rounded-lg shadow-md p-8 mb-8">
        <h3 class="text-2xl font-bold mb-4">Finn viktige ressurser nær deg</h3>
        <p class="mb-6">
          Se kartet for å finne nærmeste tilfluktsrom, hjertestarter, og andre viktige ressurser i nærområdet ditt.
          Registrer deg for å få varsler om krisesituasjoner i ditt område.
        </p>
        <div class="flex flex-col sm:flex-row gap-4">
          <router-link to="/map" class="bg-white text-yellow-600 font-medium px-6 py-2 rounded-md hover:bg-yellow-50 transition text-center">
            Se kart
          </router-link>
          <router-link to="/register" class="border border-white text-white px-6 py-2 rounded-md hover:bg-yellow-700 transition text-center">
            Registrer deg
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

