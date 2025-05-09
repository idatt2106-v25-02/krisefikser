import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import InvitedPendingList from '@/components/household/InvitedPendingList.vue'

interface Invite {
  id: string;
  invitedUser?: {
    firstName: string;
    lastName: string;
  };
  invitedEmail?: string;
  status: string;
}

interface Props {
  invites: Invite[];
  declinedInvites?: Invite[];
}

describe('InvitedPendingList', () => {
  const mockInvites = [
    {
      id: '1',
      invitedUser: {
        firstName: 'John',
        lastName: 'Doe'
      },
      status: 'pending'
    },
    {
      id: '2',
      invitedEmail: 'test@example.com',
      status: 'pending'
    }
  ]

  const mockDeclinedInvites = [
    {
      id: '3',
      invitedUser: {
        firstName: 'Jane',
        lastName: 'Smith'
      },
      status: 'declined'
    },
    {
      id: '4',
      invitedEmail: 'declined@example.com',
      status: 'declined'
    }
  ]

  const defaultProps: Props = {
    invites: []
  }

  const mountComponent = (props: Partial<Props> = {}) => {
    return mount(InvitedPendingList, {
      props: { ...defaultProps, ...props }
    })
  }

  it('does not render when no invites are provided', () => {
    const wrapper = mountComponent({ invites: [] })
    expect(wrapper.find('h2').exists()).toBe(false)
  })

  it('renders pending invites correctly', () => {
    const wrapper = mountComponent({ invites: mockInvites })

    // Check if pending invites section exists
    expect(wrapper.text()).toContain('Inviterte (venter på svar)')

    // Check if both invites are rendered
    const inviteElements = wrapper.findAll('.bg-blue-50')
    expect(inviteElements).toHaveLength(2)

    // Check user invite
    expect(inviteElements[0].text()).toContain('John Doe')
    expect(inviteElements[0].text()).toContain('Bruker:')
    expect(inviteElements[0].text()).toContain('Venter på svar')

    // Check email invite
    expect(inviteElements[1].text()).toContain('test@example.com')
    expect(inviteElements[1].text()).toContain('E-post:')
    expect(inviteElements[1].text()).toContain('Venter på svar')
  })

  it('renders declined invites correctly', () => {
    const wrapper = mountComponent({
      invites: [],
      declinedInvites: mockDeclinedInvites
    })

    // Check if declined invites section exists
    expect(wrapper.text()).toContain('Avslåtte invitasjoner')

    // Check if both declined invites are rendered
    const declinedElements = wrapper.findAll('.bg-red-50')
    expect(declinedElements).toHaveLength(2)

    // Check declined user invite
    expect(declinedElements[0].text()).toContain('Jane Smith')
    expect(declinedElements[0].text()).toContain('Bruker:')
    expect(declinedElements[0].text()).toContain('Avslått')

    // Check declined email invite
    expect(declinedElements[1].text()).toContain('declined@example.com')
    expect(declinedElements[1].text()).toContain('E-post:')
    expect(declinedElements[1].text()).toContain('Avslått')
  })

  it('renders both pending and declined invites when provided', () => {
    const wrapper = mountComponent({
      invites: mockInvites,
      declinedInvites: mockDeclinedInvites
    })

    // Check if both sections exist
    expect(wrapper.text()).toContain('Inviterte (venter på svar)')
    expect(wrapper.text()).toContain('Avslåtte invitasjoner')

    // Check if all invites are rendered
    expect(wrapper.findAll('.bg-blue-50')).toHaveLength(2)
    expect(wrapper.findAll('.bg-red-50')).toHaveLength(2)
  })

  it('applies correct styling classes', () => {
    const wrapper = mountComponent({
      invites: mockInvites,
      declinedInvites: mockDeclinedInvites
    })

    // Check pending invites container
    const pendingContainer = wrapper.find('.bg-blue-50')
    expect(pendingContainer.classes()).toContain('border')
    expect(pendingContainer.classes()).toContain('border-blue-200')
    expect(pendingContainer.classes()).toContain('rounded-lg')
    expect(pendingContainer.classes()).toContain('p-4')

    // Check declined invites container
    const declinedContainer = wrapper.find('.bg-red-50')
    expect(declinedContainer.classes()).toContain('border')
    expect(declinedContainer.classes()).toContain('border-red-200')
    expect(declinedContainer.classes()).toContain('rounded-lg')
    expect(declinedContainer.classes()).toContain('p-4')

    // Check scrollable containers
    const scrollContainers = wrapper.findAll('.overflow-y-auto')
    scrollContainers.forEach(container => {
      expect(container.classes()).toContain('max-h-[220px]')
      expect(container.classes()).toContain('scrollbar-thin')
      expect(container.classes()).toContain('scrollbar-thumb-[#c7d7f5]')
      expect(container.classes()).toContain('scrollbar-thumb-rounded-md')
    })
  })
})
