<script lang="ts">
/* eslint-disable vue/no-unused-components */
import { Plus, Coffee, Flashlight, ClipboardList, Radio, Info, AlertTriangle } from 'lucide-vue-next';
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
        AlertTriangle
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
    <!-- Introduction -->
    <ContentCard fullWidth>
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
        <li>NRK P1 er beredskapskanalen på radio (ha en batteridrevet radio)</li>
        <li>Offisielle nettsider: DSB.no, Helsedirektoratet.no, Politiet.no</li>
        <li>Kommunens nettsider og sosiale mediekanaler</li>
        <li>Nødnumre: 110 (brann), 112 (politi), 113 (ambulanse), 116 117 (legevakt)</li>
        <li>Last ned appen "Hjelp 113" for å kunne dele nøyaktig posisjon med nødetatene</li>
      </ul>
      <AlertBox type="warning" :iconComponent="iconComponents.AlertTriangle">
        Vær kritisk til informasjon på sosiale medier. Stol på offisielle kanaler
        og informasjon fra myndighetene.
      </AlertBox>
    </ContentCard>

    <!-- Quiz section -->
    <ContentCard fullWidth>
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
      primaryButtonRoute="/register"
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
