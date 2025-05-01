<script lang="ts">
/* eslint-disable vue/no-unused-components */
import { AlertTriangle, Radio, Info, Shield, AlertCircle, Zap, Droplets, Cloud,
  Stethoscope, Heart, PhoneCall, Users, BookOpen } from 'lucide-vue-next';
import QuizComponent from '@/components/quiz/QuizComponent.vue';
import PageLayout from '@/components/layout/PageLayout.vue';
import ContentCard from '@/components/ui/ContentCard.vue';
import AlertBox from '@/components/ui/AlertBox.vue';
import CTASection from '@/components/ui/CTASection.vue';

export default {
  name: 'DuringCrisisView',
  components: {
    PageLayout,
    ContentCard,
    AlertBox,
    CTASection,
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
    BookOpen,
    QuizComponent
  },
  data() {
    return {
      // Store references to icon components
      iconComponents: {
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
        BookOpen
      },
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
  <PageLayout
    pageTitle="Under krisen"
    sectionName="Under krisen"
    :iconComponent="iconComponents.AlertTriangle"
    iconBgColor="yellow"
  >
    <!-- Introduction with Quiz Highlight -->
    <ContentCard fullWidth>
      <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200 mb-4">
        <div class="flex items-center mb-2">
          <component :is="iconComponents.BookOpen" class="h-6 w-6 text-yellow-600 mr-2" />
          <h3 class="text-lg font-semibold text-gray-800">Test din kunnskap</h3>
        </div>
        <p class="text-gray-700">
          Denne siden inneholder en quiz nederst hvor du kan teste din kunnskap om hva du bør
          gjøre under en krisesituasjon. Du får umiddelbar tilbakemelding og kan lære mens du tester deg selv.
        </p>
        <a href="#quiz-section" class="inline-flex items-center text-yellow-600 font-medium mt-2 hover:underline">
          Gå til quiz
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </a>
      </div>

      <h2 class="text-2xl font-semibold text-gray-800 mb-4">Når krisen inntreffer</h2>
      <p class="text-gray-700 mb-4">
        Når en krise oppstår, er det viktig å holde roen og handle rasjonelt. Din evne til
        å ta gode beslutninger under press kan være avgjørende for din sikkerhet og de rundt deg.
      </p>
      <p class="text-gray-700 mb-4">
        Denne siden gir deg viktig informasjon om hvordan du bør handle under ulike typer
        krisesituasjoner, fra strømbrudd til naturkatastrofer og andre akutte hendelser.
      </p>
    </ContentCard>

    <!-- Main content sections -->
    <div class="grid md:grid-cols-2 gap-8 mb-8">
      <!-- Stay Informed -->
      <ContentCard title="Hold deg informert" :iconComponent="iconComponents.Radio" iconColor="yellow">
        <p class="text-gray-700 mb-4">
          Korrekt og oppdatert informasjon er avgjørende under en krise:
        </p>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
          <li>
            <span><strong>NRK P1</strong> er beredskapskanalen på radio -
              <a href="https://radio.nrk.no/direkte/p1" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Lytt direkte</a>
            </span>
          </li>
          <li>
            <span>Følg med på offisielle nettsider:
              <a href="https://www.dsb.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">DSB.no</a>,
              <a href="https://www.helsedirektoratet.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Helsedirektoratet.no</a>,
              <a href="https://www.kriseinfo.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Kriseinfo.no</a>
            </span>
          </li>
          <li>
            <span>Sjekk lokale varsler fra din kommune og
              <a href="https://www.yr.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Yr.no</a> for værvarsler
            </span>
          </li>
          <li>
            <span>Vær kritisk til informasjon fra ikke-offisielle kilder, spesielt på sosiale medier</span>
          </li>
          <li>
            <span>Spar på mobilbatteribruk ved å redusere skjermtid og slå av unødvendige applikasjoner</span>
          </li>
        </ul>
        <AlertBox type="warning" :iconComponent="iconComponents.Info">
          Hvis mobilnettet er nede, kan DAB-radio være din viktigste informasjonskilde.
          Sørg for å ha batterier til radioen din.
        </AlertBox>
      </ContentCard>

      <!-- Basic Safety -->
      <ContentCard title="Grunnleggende sikkerhet" :iconComponent="iconComponents.Shield" iconColor="yellow">
        <p class="text-gray-700 mb-4">
          Uavhengig av krisetype, følg disse grunnleggende sikkerhetstipsene:
        </p>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
          <li>
            <span>Følg instruksjoner fra myndighetene og nødetater</span>
          </li>
          <li>
            <span>Evakuer kun hvis du blir bedt om det eller hvis det er tydelig usikkert å bli</span>
          </li>
          <li>
            <span>Hold husstanden samlet eller oppretthold kontakt hvis dere er adskilt</span>
          </li>
          <li>
            <span>Ha alltid tilgang til nødvendige medisiner</span>
          </li>
          <li>
            <span>Bruk sunn fornuft og tenk nøye gjennom konsekvenser av handlinger</span>
          </li>
          <li>
            <span>Hjelp andre i nærområdet som trenger det, spesielt sårbare grupper</span>
          </li>
        </ul>
        <div class="bg-red-50 p-4 rounded-lg border border-red-200">
          <h4 class="font-semibold text-gray-800 mb-2">Viktige nødnumre:</h4>
          <div class="grid grid-cols-2 gap-2">
            <a href="tel:110" class="flex items-center space-x-2 text-red-600 font-bold">
              <span>110</span>
              <span class="text-gray-700 font-normal">Brann</span>
            </a>
            <a href="tel:112" class="flex items-center space-x-2 text-red-600 font-bold">
              <span>112</span>
              <span class="text-gray-700 font-normal">Politi</span>
            </a>
            <a href="tel:113" class="flex items-center space-x-2 text-red-600 font-bold">
              <span>113</span>
              <span class="text-gray-700 font-normal">Ambulanse</span>
            </a>
            <a href="tel:116117" class="flex items-center space-x-2 text-blue-600 font-bold">
              <span>116 117</span>
              <span class="text-gray-700 font-normal">Legevakt</span>
            </a>
          </div>
          <p class="text-sm text-gray-600 mt-2">Ring kun i faktiske nødssituasjoner</p>
        </div>
      </ContentCard>
    </div>

    <!-- Specific Crisis Scenarios
    <ContentCard title="Spesifikke krisesituasjoner" fullWidth>
       Power outage
      <div class="mb-6 border-b border-gray-200 pb-6">
        <div class="flex items-center mb-3">
          <div class="bg-yellow-50 p-2 rounded-full mr-3">
            <component :is="iconComponents.Zap" class="h-5 w-5 text-yellow-600" />
          </div>
          <h4 class="text-lg font-medium text-gray-800">Strømbrudd</h4>
        </div>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-3 space-y-2">
          <li>Behold varmen: Kle på deg varme klær, samle familien i ett rom, bruk tepper</li>
          <li>Unngå å åpne kjøleskap og fryser mer enn nødvendig</li>
          <li>Bruk alternative lyskilder som lommelykter og stearinlys (pass på brannfare)</li>
          <li>
            <span class="text-red-600 font-semibold">ADVARSEL:</span> Bruk ikke grill, gassgrill eller lignende inne - fare for kullosforgiftning
          </li>
          <li>Slå av elektriske apparater for å unngå skade når strømmen kommer tilbake</li>
          <li>
            Sjekk ditt lokale nettselskap for oppdateringer:
            <a href="https://www.statnett.no/strombrudd/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Statnett strømbruddsinformasjon</a>
          </li>
        </ul>
      </div>

     Water supply disruption
      <div class="mb-6 border-b border-gray-200 pb-6">
        <div class="flex items-center mb-3">
          <div class="bg-yellow-50 p-2 rounded-full mr-3">
            <component :is="iconComponents.Droplets" class="h-5 w-5 text-yellow-600" />
          </div>
          <h4 class="text-lg font-medium text-gray-800">Vannmangel</h4>
        </div>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-3 space-y-2">
          <li>Rasjonér tilgjengelig vann - prioriter drikke og matlaging</li>
          <li>Bruk flaskevann eller lagret vann først</li>
          <li>Fyll badekar, bøtter og andre beholdere hvis du får forvarsel</li>
          <li>Følg råd fra myndighetene om koking eller rensing av vann</li>
          <li>Reduser toalettspyling - bruk minimal vannmengde eller alternativ løsning</li>
          <li>
            Informasjon om trygt drikkevann:
            <a href="https://www.mattilsynet.no/mat_og_vann/vann/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Mattilsynet</a>
          </li>
        </ul>
      </div>

      Natural disasters
      <div class="mb-6 border-b border-gray-200 pb-6">
        <div class="flex items-center mb-3">
          <div class="bg-yellow-50 p-2 rounded-full mr-3">
            <component :is="iconComponents.Cloud" class="h-5 w-5 text-yellow-600" />
          </div>
          <h4 class="text-lg font-medium text-gray-800">Ekstremvær og naturkatastrofer</h4>
        </div>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-3 space-y-2">
          <li>Følg evakueringsordre umiddelbart hvis myndighetene ber om det</li>
          <li>Hold deg innendørs, unna vinduer og yttervegg under storm</li>
          <li>Ved flom: Søk høyere terreng og unngå å kjøre eller gå gjennom flomvann</li>
          <li>Ved skred: Evakuer området umiddelbart hvis det er tegn til ustabil grunn</li>
          <li>Ha avklart møtested for familien hvis dere blir adskilt</li>
          <li>
            Følg med på varsler om ekstremvær:
            <a href="https://www.yr.no/varsel/farevarsel/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Yr farevarsel</a>
          </li>
        </ul>
      </div>

    Health crises and pandemics
      <div>
        <div class="flex items-center mb-3">
          <div class="bg-yellow-50 p-2 rounded-full mr-3">
            <component :is="iconComponents.Stethoscope" class="h-5 w-5 text-yellow-600" />
          </div>
          <h4 class="text-lg font-medium text-gray-800">Helsekrise og pandemier</h4>
        </div>
        <ul class="list-disc list-outside px-4 text-gray-700 mb-3 space-y-2">
          <li>Følg råd fra helsemyndighetene angående smittevern</li>
          <li>Unngå folkemengder og begrens sosial kontakt hvis anbefalt</li>
          <li>Praktiser god hygiene, spesielt håndvask og hosteetikette</li>
          <li>Bruk munnbind, hansker eller annet smittevernutstyr som anbefalt</li>
          <li>Sikre forsyninger av nødvendige medisiner, mat og hygieneprodukter</li>
          <li>Vær oppmerksom på symptomer og følg anbefalinger for testing/isolasjon</li>
          <li>
            Oppdatert helseinformasjon:
            <a href="https://www.fhi.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Folkehelseinstituttet</a> og
            <a href="https://www.helsenorge.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Helsenorge</a>
          </li>
        </ul>
      </div>
    </ContentCard> -->

    <!-- Mental Health Section -->
    <ContentCard
      title="Mental helse under kriser"
      :iconComponent="iconComponents.Heart"
      iconColor="yellow"
      fullWidth
    >
      <p class="text-gray-700 mb-4">
        En krisesituasjon kan være psykisk belastende. Her er noen strategier for å ta vare på mental helse:
      </p>
      <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
        <li>Aksepter at følelser som frykt, angst og usikkerhet er normale reaksjoner</li>
        <li>Oppretthold rutiner for å skape forutsigbarhet, spesielt for barn</li>
        <li>Begrens medieeksponering for å unngå overbelastning av kriseinfo</li>
        <li>Hold kontakt med familie og venner, del bekymringer og støtt hverandre</li>
        <li>Fokuser på det du kan kontrollere, ikke det som er utenfor din kontroll</li>
        <li>Bruk avslapningsteknikker som dyp pust, meditasjon eller fysisk aktivitet</li>
        <li>Vær ekstra oppmerksom på barns reaksjoner og behov</li>
      </ul>
      <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200">
        <h4 class="font-semibold text-gray-800 mb-2">Psykisk helsehjelp:</h4>
        <ul class="space-y-2">
          <li>
            <a href="tel:116123" class="flex items-center text-yellow-600 hover:underline">
              <PhoneCall class="h-4 w-4 mr-2" />
              <span class="font-bold">Mental Helse Hjelpetelefon: 116 123</span>
              <span class="ml-2 text-sm text-gray-600">(døgnåpen)</span>
            </a>
          </li>
          <li>
            <a href="https://mentalhelse.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">
              Mental Helse - Nettressurser og informasjon
            </a>
          </li>
          <li>
            <a href="https://www.rodekors.no/tilbudene/samtaletilbud/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">
              Røde Kors Samtaletilbud: 800 33 321
            </a>
          </li>
        </ul>
      </div>
    </ContentCard>

    <!-- Helping Others -->
    <ContentCard
      title="Å hjelpe andre"
      :iconComponent="iconComponents.Users"
      iconColor="yellow"
      fullWidth
    >
      <p class="text-gray-700 mb-4">
        Samfunnet blir sterkere når vi hjelper hverandre i kriser. Her er hvordan du kan bidra:
      </p>
      <ul class="list-disc list-outside px-4 text-gray-700 mb-4 space-y-2">
        <li>Sjekk på naboer, særlig eldre, syke eller andre sårbare grupper</li>
        <li>Del ressurser som vann, mat eller nødvendig utstyr hvis du har til overs</li>
        <li>Tilby transport eller praktisk hjelp til de som trenger det</li>
        <li>
          Vurder å delta i frivillig arbeid gjennom organisasjoner som
          <a href="https://www.rodekors.no/engasjerdeg/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Røde Kors</a> eller
          <a href="https://www.frivillig.no/" target="_blank" rel="noopener noreferrer" class="text-yellow-600 hover:underline">Frivillig.no</a>
        </li>
        <li>Spre kun verifisert informasjon, ikke rykter eller ubekreftet info</li>
      </ul>
      <p class="text-gray-700">
        Merk: Sørg for din egen sikkerhet først. Du kan ikke hjelpe andre hvis du selv er i fare.
      </p>
    </ContentCard>

    <!-- Quiz section -->
    <ContentCard fullWidth id="quiz-section">
      <div class="bg-yellow-50 p-6 rounded-lg mb-6">
        <h3 class="text-2xl font-bold text-gray-800 mb-2 flex items-center">
          <component :is="iconComponents.BookOpen" class="h-6 w-6 text-yellow-600 mr-2" />
          Test din kunnskap om krisehåndtering
        </h3>
        <p class="text-gray-700 mb-4">
          Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre under en krisesituasjon.
          Hver riktig besvarelse styrker din mentale beredskap.
        </p>
      </div>

      <QuizComponent
        title="Test din kunnskap"
        description="Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre under en krisesituasjon."
        :questions="quizQuestions"
        colorTheme="yellow"
      />
    </ContentCard>

    <!-- CTA for emergency locations -->
    <CTASection
      title="Finn viktige ressurser nær deg"
      description="Se kartet for å finne nærmeste tilfluktsrom, hjertestarter, og andre viktige ressurser i nærområdet ditt. Registrer deg for å få varsler om krisesituasjoner i ditt område."
      authTitle="Finn viktige ressurser nær deg"
      authDescription="Bruk kartet for å lokalisere tilfluktsrom, hjertestartere og andre kritiske ressurser. Få varsler om krisesituasjoner i ditt området basert på din registrerte adresse."
      primaryButtonText="Se kart"
      primaryButtonRoute="/kart"
      secondaryButtonText="Registrer deg"
      secondaryButtonRoute="/register"
      authPrimaryButtonText="Se kart"
      authPrimaryButtonRoute="/kart"
      authSecondaryButtonText="Min profil"
      authSecondaryButtonRoute="/dashboard"
      colorTheme="yellow"
    />
  </PageLayout>
</template>
