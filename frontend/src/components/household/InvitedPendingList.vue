<script setup lang="ts">
import { defineProps } from 'vue'

interface Invite {
  id: string;
  invitedUser?: {
    firstName: string;
    lastName: string;
  };
  invitedEmail?: string;
  status: string;
}

const _props = defineProps<{ invites: Invite[], declinedInvites?: Invite[] }>();</script>

<template>
  <div v-if="invites && invites.length" class="mb-6">
    <h2 class="text-lg font-semibold mb-2">Inviterte (venter på svar)</h2>
    <div class="max-h-[220px] overflow-y-auto pr-[2px] scrollbar-thin scrollbar-thumb-[#c7d7f5] scrollbar-thumb-rounded-md">
      <div v-for="invite in invites" :key="invite.id" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-2 flex items-center justify-between">
        <div>
          <div v-if="invite.invitedUser">
            <b>Bruker:</b> {{ invite.invitedUser.firstName }} {{ invite.invitedUser.lastName }}
          </div>
          <div v-else>
            <b>E-post:</b> {{ invite.invitedEmail }}
          </div>
          <div><b>Status:</b> {{ invite.status }}</div>
        </div>
        <div>
          <span class="text-xs text-gray-500">Venter på svar</span>
        </div>
      </div>
    </div>
  </div>
  <div v-if="declinedInvites && declinedInvites.length" class="mb-6">
    <h2 class="text-lg font-semibold mb-2">Avslåtte invitasjoner</h2>
    <div class="max-h-[220px] overflow-y-auto pr-[2px] scrollbar-thin scrollbar-thumb-[#c7d7f5] scrollbar-thumb-rounded-md">
      <div v-for="invite in declinedInvites" :key="invite.id" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-2 flex items-center justify-between">
        <div>
          <div v-if="invite.invitedUser">
            <b>Bruker:</b> {{ invite.invitedUser.firstName }} {{ invite.invitedUser.lastName }}
          </div>
          <div v-else>
            <b>E-post:</b> {{ invite.invitedEmail }}
          </div>
          <div><b>Status:</b> {{ invite.status }}</div>
        </div>
        <div>
          <span class="text-xs text-red-500">Avslått</span>
        </div>
      </div>
    </div>
  </div>
</template>
