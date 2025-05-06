<script setup lang="ts">
import { X } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';

defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const dsbChecklistUrl = 'https://www.dsb.no/siteassets/sikkerhverdag/egenberedskap/sjekkliste-egenberedskap-a4-bokmal.pdf';
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
    @click.self="emit('close')"
  >
    <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full transform transition-all overflow-y-auto max-h-[90vh]">
      <div class="flex items-center justify-between p-4 sm:p-6 border-b border-gray-200">
        <h2 class="text-xl sm:text-2xl font-semibold text-gray-800">
          Informasjon om beredskapsberegning
        </h2>
        <Button variant="ghost" size="icon" @click="emit('close')" class="rounded-full">
          <X class="h-5 w-5 text-gray-500" />
        </Button>
      </div>

      <div class="p-4 sm:p-6 space-y-5 text-gray-700 text-sm sm:text-base">
        <p>
          DSB (Direktoratet for samfunnssikkerhet og beredskap) anbefaler at de fleste bør kunne klare seg selv i minst <strong>tre døgn</strong>.
          Vårt mål i denne applikasjonen er å hjelpe deg å planlegge for <strong>7 dager</strong>.
        </p>

        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-1.5">Hvordan "Dager forberedt" beregnes:</h3>
          <ul class="list-disc list-inside space-y-1 pl-1">
            <li>Dine 'Dager forberedt' er den laveste dekningen du har for enten mat eller vann, opp til målet på 7 dager.</li>
            <li>Har du for eksempel mat for 5 dager og vann for 3 dager, vil 'Dager forberedt' vise 3 dager.</li>
          </ul>
        </div>

        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-1.5">Mat:</h3>
          <ul class="list-disc list-inside space-y-1 pl-1">
            <li>Matbehovet beregnes ut fra et gjennomsnitt på <strong>2250 kcal</strong> per person per dag.</li>
            <li>
              Forbruksfaktoren du setter for gjester (f.eks. for barn eller kjæledyr) i husstandsinnstillingene,
              justerer dette kaloribehovet for dem. Medlemmer teller som 1.0.
            </li>
            <li>Øk 'Dager forberedt' ved å legge til flere matvarer.</li>
          </ul>
        </div>

        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-1.5">Vann:</h3>
          <ul class="list-disc list-inside space-y-1 pl-1">
            <li>Vannbehovet beregnes ut fra et gjennomsnitt på <strong>3 liter</strong> per person per dag.</li>
            <li>Forbruksfaktoren for gjester påvirker også det totale vannbehovet for husstanden.</li>
            <li>Øk 'Dager forberedt' ved å registrere mer vann.</li>
          </ul>
        </div>

        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-1.5">Sjekkliste (Annet):</h3>
          <ul class="list-disc list-inside space-y-1 pl-1">
            <li>Listen over 'Annet' (utstyr og andre nødvendigheter) er basert på DSBs anbefalte sjekkliste for egenberedskap.</li>
            <li>
              Se den fullstendige sjekklisten fra DSB:
              <a :href="dsbChecklistUrl" target="_blank" rel="noopener noreferrer" class="text-blue-600 hover:text-blue-700 underline">
                DSBs Sjekkliste for Egenberedskap (PDF)
              </a>.
            </li>
          </ul>
        </div>

        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-1.5">Ditt mål:</h3>
          <p>
            Målet for din husstand (f.eks. totalt kcal-mål og vannlitermål) er satt for <strong>7 dager</strong>.
            Dette målet justeres automatisk basert på antall medlemmer i husstanden og forbruksfaktorene til eventuelle gjester.
          </p>
        </div>
      </div>

      <div class="px-4 sm:px-6 py-4 border-t border-gray-200 flex justify-end">
        <Button @click="emit('close')" class="bg-blue-600 hover:bg-blue-700 text-white">
          Lukk
        </Button>
      </div>
    </div>
  </div>
</template>
