<!-- AdminInviteLink.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue';
import { ArrowLeft, Copy, Check, UserPlus, Mail } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

// Define interface for User
interface User {
  id: string;
  name: string;
  email: string;
  role: string;
}

const props = defineProps<{
  preselectedUser?: User | null;
}>();

const emit = defineEmits(['back']);

// Mock data for potential admins
const potentialAdmins = ref([
  { id: 'p1', name: 'Nina Johansen', email: 'nina@example.no' },
  { id: 'p2', name: 'Erik Olsen', email: 'erik@example.no' },
  { id: 'p3', name: 'Marte Pedersen', email: 'marte@example.no' }
]);

// State
const selectedUser = ref('');
const newAdminEmail = ref('');
const inviteMethod = ref('existing');
const generatedLink = ref('');
const copied = ref(false);
const linkSent = ref(false);

// Watch for preselected user
watch(() => props.preselectedUser, (newVal) => {
  if (newVal) {
    // If a user is preselected, we use their data directly
    newAdminEmail.value = newVal.email;
    generateLink();
  }
}, { immediate: true });

// Generate invite link
const generateLink = () => {
  // In a real app, this would call an API to create a secure token
  const token = Math.random().toString(36).substring(2, 15) +
    Math.random().toString(36).substring(2, 15);
  const baseUrl = window.location.origin;
  generatedLink.value = `${baseUrl}/admin/invite/${token}`;
};

// Generate admin invite
const inviteAdmin = () => {
  if (inviteMethod.value === 'existing' && selectedUser.value) {
    // Get selected user details
    const user = potentialAdmins.value.find(u => u.id === selectedUser.value);
    if (user) {
      newAdminEmail.value = user.email;
    }
  }

  if (newAdminEmail.value) {
    generateLink();
  }
};

// Copy link to clipboard
const copyLink = () => {
  navigator.clipboard.writeText(generatedLink.value);
  copied.value = true;
  setTimeout(() => {
    copied.value = false;
  }, 2000);
};

// Send email with link
const sendEmail = () => {
  // In a real app, this would call an API to send the email
  console.log(`Sending email to ${newAdminEmail.value} with link ${generatedLink.value}`);
  linkSent.value = true;
  setTimeout(() => {
    linkSent.value = false;
  }, 2000);
};

// Go back to admin section
const goBack = () => {
  emit('back');
};
</script>

<template>
  <div class="p-6">
    <div class="mb-6">
      <Button variant="ghost" @click="goBack" class="text-gray-600">
        <ArrowLeft class="h-4 w-4 mr-1" />
        Tilbake til admin-panel
      </Button>
    </div>

    <!-- When user is not preselected -->
    <Card v-if="!generatedLink && !props.preselectedUser" class="max-w-md mx-auto">
      <CardHeader>
        <CardTitle>Inviter ny admin</CardTitle>
        <CardDescription>
          Velg en eksisterende bruker eller inviter en ny person som admin
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs v-model="inviteMethod" class="w-full">
          <TabsList class="grid w-full grid-cols-2">
            <TabsTrigger value="existing">Eksisterende bruker</TabsTrigger>
            <TabsTrigger value="new">Ny bruker</TabsTrigger>
          </TabsList>
          <TabsContent value="existing">
            <div class="space-y-4 mt-4">
              <div class="space-y-2">
                <Label for="user-select">Velg en bruker</Label>
                <select
                  id="user-select"
                  v-model="selectedUser"
                  class="w-full border rounded-lg px-3 py-2 text-gray-700"
                >
                  <option value="" disabled>Velg bruker</option>
                  <option v-for="user in potentialAdmins" :key="user.id" :value="user.id">
                    {{ user.name }} ({{ user.email }})
                  </option>
                </select>
              </div>
            </div>
          </TabsContent>
          <TabsContent value="new">
            <div class="space-y-4 mt-4">
              <div class="space-y-2">
                <Label for="email">E-post</Label>
                <Input id="email" type="email" v-model="newAdminEmail" placeholder="navn@example.no" />
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
      <CardFooter>
        <Button
          class="w-full"
          :disabled="(inviteMethod === 'existing' && !selectedUser) || (inviteMethod === 'new' && !newAdminEmail)"
          @click="inviteAdmin"
        >
          <UserPlus class="h-4 w-4 mr-1" />
          Generer invitasjonslink
        </Button>
      </CardFooter>
    </Card>

    <!-- When user is preselected or link is generated -->
    <Card v-else class="max-w-md mx-auto">
      <CardHeader>
        <CardTitle>Admin-invitasjon klar</CardTitle>
        <CardDescription v-if="props.preselectedUser">
          Send invitasjon til {{ props.preselectedUser.name }}
        </CardDescription>
        <CardDescription v-else>
          Send eller kopier invitasjonslinken nedenfor
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="space-y-4">
          <div class="space-y-2">
            <Label for="invite-link">Invitasjonslink</Label>
            <div class="flex">
              <Input id="invite-link" type="text" readonly :value="generatedLink" class="rounded-r-none" />
              <Button
                variant="outline"
                class="rounded-l-none border-l-0"
                @click="copyLink"
              >
                <Copy v-if="!copied" class="h-4 w-4" />
                <Check v-else class="h-4 w-4 text-green-500" />
              </Button>
            </div>
            <p v-if="copied" class="text-sm text-green-500 mt-1">Kopiert til utklippstavle!</p>
          </div>

          <div class="space-y-2">
            <Label for="email-to">Send til</Label>
            <Input id="email-to" type="email" v-model="newAdminEmail" readonly />
          </div>
        </div>
      </CardContent>
      <CardFooter class="flex flex-col space-y-2">
        <Button class="w-full" @click="sendEmail">
          <Mail class="h-4 w-4 mr-1" />
          Send invitasjonslink på e-post
        </Button>
        <p v-if="linkSent" class="text-sm text-green-500 text-center">E-post sendt!</p>
      </CardFooter>
    </Card>
  </div>
</template>
