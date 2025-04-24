<script setup lang="ts">
import { ref } from 'vue';
import MapComponent from '@/components/map/MapComponent.vue';
import ShelterLayer from '@/components/map/ShelterLayer.vue';
import CrisisLayer from '@/components/map/CrisisLayer.vue';
import MapLegend from '@/components/map/MapLegend.vue';
import { shelters, crisisAreas, type Shelter, type CrisisArea } from '@/components/map/mapData';
import L from 'leaflet';

// Map and related refs
const mapRef = ref<InstanceType<typeof MapComponent> | null>(null);
const mapInstance = ref<L.Map | null>(null);

// Form states
const activeTab = ref<'shelters' | 'events'>('shelters');
const newShelter = ref<Partial<Shelter>>({
  name: '',
  capacity: 0,
  position: [63.4305, 10.3951], // Default to Trondheim center
});
const newCrisisArea = ref<Partial<CrisisArea>>({
  name: '',
  radius: 500,
  severity: 1,
  description: '',
  center: [63.4305, 10.3951], // Default to Trondheim center
});

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map;

  // Add click handler for location selection
  map.on('click', (e) => {
    const { lat, lng } = e.latlng;
    if (activeTab.value === 'shelters') {
      newShelter.value.position = [lat, lng];
    } else {
      newCrisisArea.value.center = [lat, lng];
    }
  });
}

// Form submission handlers
function handleAddShelter() {
  // TODO: Implement shelter addition logic
  console.log('Adding shelter:', newShelter.value);
}

function handleAddCrisisArea() {
  // TODO: Implement crisis area addition logic
  console.log('Adding crisis area:', newCrisisArea.value);
}
</script>

<template>
  <div class="flex h-screen">
    <!-- Admin Sidebar -->
    <div class="w-96 bg-white border-r border-gray-200 p-4 overflow-y-auto">
      <h2 class="text-2xl font-bold mb-6">Kartstyring</h2>

      <!-- Tab Navigation -->
      <div class="flex space-x-2 mb-6">
        <button
          @click="activeTab = 'shelters'"
          :class="[
            'px-4 py-2 rounded-lg',
            activeTab === 'shelters'
              ? 'bg-primary text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          Tilfluktsrom
        </button>
        <button
          @click="activeTab = 'events'"
          :class="[
            'px-4 py-2 rounded-lg',
            activeTab === 'events'
              ? 'bg-primary text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          Krisehendelser
        </button>
      </div>

      <!-- Shelter Form -->
      <div v-if="activeTab === 'shelters'" class="space-y-4">
        <div class="space-y-2">
          <label class="text-sm font-medium">Navn</label>
          <input
            v-model="newShelter.name"
            type="text"
            class="w-full px-3 py-2 border rounded-lg"
            placeholder="Navn på tilfluktsrom"
          />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium">Kapasitet</label>
          <input
            v-model="newShelter.capacity"
            type="number"
            class="w-full px-3 py-2 border rounded-lg"
            placeholder="Antall personer"
          />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium">Plassering</label>
          <div class="flex space-x-2">
            <input
              :value="newShelter.position?.[0].toFixed(4)"
              readonly
              type="text"
              class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
              placeholder="Breddegrad"
            />
            <input
              :value="newShelter.position?.[1].toFixed(4)"
              readonly
              type="text"
              class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
              placeholder="Lengdegrad"
            />
          </div>
          <p class="text-sm text-gray-500">Klikk på kartet for å velge plassering</p>
        </div>

        <button
          @click="handleAddShelter"
          class="w-full bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary/90"
        >
          Legg til tilfluktsrom
        </button>
      </div>

      <!-- Crisis Event Form -->
      <div v-if="activeTab === 'events'" class="space-y-4">
        <div class="space-y-2">
          <label class="text-sm font-medium">Navn</label>
          <input
            v-model="newCrisisArea.name"
            type="text"
            class="w-full px-3 py-2 border rounded-lg"
            placeholder="Navn på hendelse"
          />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium">Beskrivelse</label>
          <textarea
            v-model="newCrisisArea.description"
            class="w-full px-3 py-2 border rounded-lg"
            rows="3"
            placeholder="Beskriv hendelsen"
          ></textarea>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium">Radius (meter)</label>
          <input
            v-model="newCrisisArea.radius"
            type="number"
            class="w-full px-3 py-2 border rounded-lg"
            placeholder="Radius i meter"
          />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium">Alvorlighetsgrad (1-3)</label>
          <input
            v-model="newCrisisArea.severity"
            type="number"
            min="1"
            max="3"
            class="w-full px-3 py-2 border rounded-lg"
            placeholder="1 = Lav, 2 = Middels, 3 = Høy"
          />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium">Plassering</label>
          <div class="flex space-x-2">
            <input
              :value="newCrisisArea.center?.[0].toFixed(4)"
              readonly
              type="text"
              class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
              placeholder="Breddegrad"
            />
            <input
              :value="newCrisisArea.center?.[1].toFixed(4)"
              readonly
              type="text"
              class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
              placeholder="Lengdegrad"
            />
          </div>
          <p class="text-sm text-gray-500">Klikk på kartet for å velge plassering</p>
        </div>

        <button
          @click="handleAddCrisisArea"
          class="w-full bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary/90"
        >
          Legg til krisehendelse
        </button>
      </div>
    </div>

    <!-- Map Container -->
    <div class="flex-1 relative">
      <MapComponent
        ref="mapRef"
        @map-created="onMapCreated"
      />

      <ShelterLayer
        :map="mapInstance"
        :shelters="shelters"
      />

      <CrisisLayer
        :map="mapInstance"
        :crisis-areas="crisisAreas"
      />

      <MapLegend
        :user-location-available="false"
        :show-user-location="false"
        :user-in-crisis-zone="false"
        @toggle-user-location="() => {}"
      />
    </div>
  </div>
</template>
