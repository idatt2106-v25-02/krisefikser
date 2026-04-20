import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PreparednessInfoDialog from './PreparednessInfoDialog.vue'

describe('PreparednessInfoDialog', () => {
  it('does not render content when closed', () => {
    const wrapper = mount(PreparednessInfoDialog, {
      props: { isOpen: false },
    })

    expect(wrapper.text()).not.toContain('Informasjon om beredskapsberegning')
  })

  it('renders content and DSB link when open', () => {
    const wrapper = mount(PreparednessInfoDialog, {
      props: { isOpen: true },
    })

    expect(wrapper.text()).toContain('Informasjon om beredskapsberegning')
    const link = wrapper.find('a')
    expect(link.attributes('href')).toContain('dsb.no')
  })

  it('emits close from overlay click and both close buttons', async () => {
    const wrapper = mount(PreparednessInfoDialog, {
      props: { isOpen: true },
    })

    await wrapper.find('.fixed.inset-0').trigger('click')
    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')
    await buttons[1].trigger('click')

    expect(wrapper.emitted('close')).toHaveLength(3)
  })
})
