<script lang="ts">
 
import { Plus, Coffee, Flashlight, ClipboardList, Radio, Info, AlertTriangle, BookOpen } from 'lucide-vue-next';
import QuizComponent from '@/components/quiz/QuizComponent.vue';
import PageLayout from '@/components/layout/PageLayout.vue';
import ContentCard from '@/components/ui/ContentCard.vue';
import AlertBox from '@/components/ui/AlertBox.vue';
import CTASection from '@/components/ui/CTASection.vue';

export default {
  name: 'BeforeCrisisView',
  components: {
    PageLayout,
    ContentCard,
    AlertBox,
    CTASection,
    Coffee,
    Flashlight,
    ClipboardList,
    Radio,
    Info,
    AlertTriangle,
    BookOpen,
    QuizComponent,
    Plus // Make sure Plus is registered
  },
  data() {
    return {
      // Store references to icon components
      iconComponents: {
        Plus,
        Coffee,
        Flashlight,
        ClipboardList,
        Radio,
        Info,
        AlertTriangle,
        BookOpen
      },
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
  <PageLayout
    pageTitle="Før krisen"
    sectionName="Før krisen"
    :iconComponent="iconComponents.Plus"
    iconBgColor="blue"
  >
    <!-- Introduction with Quiz Highlight -->
    <ContentCard fullWidth>
      <div class="bg-blue-50 p-4 rounded-lg border border-blue-200 mb-4">
        <div class="flex items-center mb-2">
          <component :is="iconComponents.BookOpen" class="h-6 w-6 text-blue-600 mr-2" />
          <h3 class="text-lg font-semibold text-gray-800">Test din kunnskap</h3>
        </div>
        <p class="text-gray-700">
          Denne siden inneholder en quiz nederst hvor du kan teste din kunnskap om hva du bør
          gjøre før en krisesituasjon. Du får umiddelbar tilbakemelding og kan lære mens du tester deg selv.
        </p>
        <a href="#quiz-section" class="inline-flex items-center text-blue-600 font-medium mt-2 hover:underline">
          Gå til quiz
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </a>
      </div>

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
    </ContentCard>

    <!-- Main content sections -->
    <div class="grid md:grid-cols-2 gap-8 mb-8">
      <!-- Food and Water -->
      <ContentCard title="Mat og vann" :iconComponent="iconComponents.Coffee" iconColor="blue">
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
      </ContentCard>

      <!-- Equipment and Supplies -->
      <ContentCard title="Utstyr og forsyninger" :iconComponent="iconComponents.Flashlight" iconColor="blue">
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
      </ContentCard>
    </div>

    <!-- Emergency Plan -->
    <ContentCard
      title="Familiens beredskapsplan"
      :iconComponent="iconComponents.ClipboardList"
      iconColor="blue"
      fullWidth
    >
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
      <AlertBox type="info" :iconComponent="iconComponents.Info">
        Husk å oppdatere beredskapsplanen hvis familiesituasjonen endrer seg,
        og gå gjennom den med alle familiemedlemmer minst én gang i året.
      </AlertBox>
    </ContentCard>

    <!-- Information sources -->
    <ContentCard
      title="Informasjonskilder"
      :iconComponent="iconComponents.Radio"
      iconColor="blue"
      fullWidth
    >
      <p class="text-gray-700 mb-4">
        I en krisesituasjon er pålitelig informasjon avgjørende. Kjenn til disse kildene i forkant:
      </p>
      <ul class="list-disc list-inside text-gray-700 mb-4 space-y-1">
        <li class="flex items-start">
          <span class="inline-block mt-1 mr-2">•</span>
          <span><strong>NRK P1</strong> er beredskapskanalen på radio (ha en batteridrevet radio) -
        <a href="https://radio.nrk.no/direkte/p1" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:underline">Lytt direkte</a>
      </span>
        </li>
        <li class="flex items-start">
          <span class="inline-block mt-1 mr-2">•</span>
          <span>Offisielle nettsider:
        <a href="https://www.dsb.no/" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:underline">DSB.no</a>,
        <a href="https://www.helsedirektoratet.no/" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:underline">Helsedirektoratet.no</a>,
        <a href="https://www.politiet.no/" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:underline">Politiet.no</a>
      </span>
        </li>
        <li class="flex items-start">
          <span class="inline-block mt-1 mr-2">•</span>
          <span>Kommunens nettsider og sosiale mediekanaler - Søk opp din kommune</span>
        </li>
        <li class="flex items-start">
          <span class="inline-block mt-1 mr-2">•</span>
          <span>Nødnumre: <strong class="text-red-600">110</strong> (brann), <strong class="text-red-600">112</strong> (politi), <strong class="text-red-600">113</strong> (ambulanse), <strong class="text-blue-600">116 117</strong> (legevakt)</span>
        </li>
        <li class="flex items-start">
          <span class="inline-block mt-1 mr-2">•</span>
          <span>Last ned appen "Hjelp 113" for å kunne dele nøyaktig posisjon med nødetatene -
        <a href="https://www.hjelp113.no/" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:underline">Hjelp113.no</a>
      </span>
        </li>
        <li class="flex items-start">
          <span class="inline-block mt-1 mr-2">•</span>
          <span>Kriseinfo samler viktig informasjon under kriser -
        <a href="https://www.kriseinfo.no/" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:underline">Kriseinfo.no</a>
      </span>
        </li>
      </ul>
      <AlertBox type="warning" :iconComponent="iconComponents.AlertTriangle">
        Vær kritisk til informasjon på sosiale medier. Stol på offisielle kanaler
        og informasjon fra myndighetene.
      </AlertBox>
    </ContentCard>
    <!-- Quiz section -->
    <ContentCard fullWidth id="quiz-section">
      <div class="bg-blue-50 p-6 rounded-lg mb-6">
        <h3 class="text-2xl font-bold text-gray-800 mb-2 flex items-center">
          <component :is="iconComponents.BookOpen" class="h-6 w-6 text-blue-600 mr-2" />
          Test din kunnskap om kriseberedskap
        </h3>
        <p class="text-gray-700 mb-4">
          Svar på spørsmålene nedenfor for å teste din kunnskap om hva du bør gjøre før en krisesituasjon.
          Hver riktig besvarelse styrker din mentale beredskap.
        </p>
      </div>

      <QuizComponent
        title="Test din kunnskap"
        description="Svar på spørsmålene nedenfor for å se hvor godt forberedt du er på en krisesituasjon."
        :questions="quizQuestions"
        colorTheme="blue"
      />
    </ContentCard>

    <!-- CTA to register -->
    <CTASection
      title="Hold oversikt over beredskapslager"
      description="Registrer deg for å bygge og holde oversikt over ditt eget beredskapslager. Få påminnelser når varer i beredskapslageret nærmer seg utløpsdato, og del med husstanden din."
      authTitle="Administrer ditt beredskapslager"
      authDescription="Sjekk utløpsdatoer, oppdater beholdningen og legg til nye varer i ditt beredskapslager. Hold hele husstanden oppdatert og beredt."
      primaryButtonText="Registrer deg nå"
      primaryButtonRoute="/registrer"
      secondaryButtonText="Logg inn"
      secondaryButtonRoute="/logg-inn"
      authPrimaryButtonText="Gå til beredskapslager"
      authPrimaryButtonRoute="/husstand/:id/beredskapslager"
      authSecondaryButtonText="Administrer husstand"
      authSecondaryButtonRoute="/husstand"
      colorTheme="blue"
    />
  </PageLayout>
</template>
