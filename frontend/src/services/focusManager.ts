// src/services/focusManager.ts
import speechService from './speechService';

class FocusManager {
  private focusableSelector = 'a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])';

  initialize(): void {
    document.addEventListener('keydown', this.handleKeyDown.bind(this));
    document.addEventListener('focusin', this.handleFocusIn.bind(this));

    // Add focus outline styles
    const style = document.createElement('style');
    style.innerHTML = `
      :focus-visible {
        outline: 2px solid #4285f4;
        outline-offset: 2px;
      }
    `;
    document.head.appendChild(style);
  }

  private handleKeyDown(event: KeyboardEvent): void {
    // Add any global keyboard navigation enhancements here
  }

  private handleFocusIn(event: FocusEvent): void {
    const target = event.target as HTMLElement;

    // Determine what text to speak when element is focused
    let textToSpeak = '';

    // Read aria-label if present
    if (target.getAttribute('aria-label')) {
      textToSpeak = target.getAttribute('aria-label') || '';
    }
    // For buttons, read the text content
    else if (target.tagName === 'BUTTON') {
      textToSpeak = target.textContent?.trim() || '';
    }
    // For inputs, read placeholder or label
    else if (target.tagName === 'INPUT') {
      const inputEl = target as HTMLInputElement;
      const id = inputEl.id;
      let label = '';

      if (id) {
        const labelEl = document.querySelector(`label[for="${id}"]`);
        if (labelEl) {
          label = labelEl.textContent?.trim() || '';
        }
      }

      // Use the existing Norwegian content - no translation needed
      textToSpeak = label || inputEl.placeholder || inputEl.type;
    }
    // For links, read the text content
    else if (target.tagName === 'A') {
      textToSpeak = target.textContent?.trim() || '';
    }

    // If we have text to speak, do it
    if (textToSpeak) {
      speechService.speak(textToSpeak);
    }
  }
}

export const focusManager = new FocusManager();
export default focusManager;
