// src/services/focusManager.ts
import speechService from './speechService';

class FocusManager {
  private focusableSelector = 'a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])';
  private lastDialogTitle: string | null = null;
  private previousActiveElement: HTMLElement | null = null;

  initialize(): void {
    document.addEventListener('focusin', this.handleFocusIn.bind(this));
    document.addEventListener('click', this.handleClick.bind(this));
    document.addEventListener('keydown', this.handleKeyDown.bind(this));

    // Add focus outline styles
    const style = document.createElement('style');
    style.innerHTML = `
      :focus-visible {
        outline: 2px solid #4285f4;
        outline-offset: 2px;
      }
    `;
    document.head.appendChild(style);

    // Watch for dialog mutations
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'childList') {
          mutation.addedNodes.forEach((node) => {
            if (node instanceof HTMLElement && node.closest('[data-slot="dialog-content"]')) {
              this.handleDialogOpen(node as HTMLElement);
            }
          });
        }
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  private handleDialogOpen(dialogContent: HTMLElement): void {
    // Store current active element
    this.previousActiveElement = document.activeElement as HTMLElement;

    // Find the dialog title
    const title = dialogContent.querySelector('[id$="-title"]');
    if (title) {
      const titleText = title.textContent?.trim();
      if (titleText && titleText !== this.lastDialogTitle) {
        this.lastDialogTitle = titleText;
        speechService.speak(titleText);

        // Focus on the dialog content after announcing the title
        const content = dialogContent.querySelector('.privacy-policy-content');
        if (content instanceof HTMLElement) {
          setTimeout(() => {
            content.focus();
          }, 100);
        }
      }
    }
  }

  private handleKeyDown(event: KeyboardEvent): void {
    // Only handle arrow keys
    if (!['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(event.key)) {
      return;
    }

    const currentElement = document.activeElement;
    if (!currentElement) return;

    // Check if we're in a dialog
    const dialog = currentElement.closest('[data-slot="dialog-content"]');

    // Get all focusable elements, filtered by dialog context if in a dialog
    const focusableElements = Array.from(document.querySelectorAll(this.focusableSelector))
      .filter(el => {
        // Filter out hidden elements
        const style = window.getComputedStyle(el as Element);
        if (style.display === 'none' || style.visibility === 'hidden') {
          return false;
        }

        // If in a dialog, only include elements in the dialog
        if (dialog) {
          return el.closest('[data-slot="dialog-content"]') === dialog;
        }

        // If not in a dialog, exclude elements in any dialog
        return !el.closest('[data-slot="dialog-content"]');
      });

    const currentIndex = focusableElements.indexOf(currentElement as Element);
    if (currentIndex === -1) return;

    let nextIndex = currentIndex;

    switch (event.key) {
      case 'ArrowLeft':
      case 'ArrowUp':
        event.preventDefault();
        nextIndex = currentIndex - 1;
        if (nextIndex < 0) {
          nextIndex = focusableElements.length - 1;
        }
        break;
      case 'ArrowRight':
      case 'ArrowDown':
        event.preventDefault();
        nextIndex = currentIndex + 1;
        if (nextIndex >= focusableElements.length) {
          nextIndex = 0;
        }
        break;
    }

    // Focus the next element
    const nextElement = focusableElements[nextIndex] as HTMLElement;
    if (nextElement) {
      nextElement.focus();
    }
  }

  private handleClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;

    // Check if we're opening a dialog
    if (target.closest('[data-slot="dialog-content"]')) {
      // Find the dialog title
      const dialog = target.closest('[data-slot="dialog-content"]');
      if (dialog) {
        const title = dialog.querySelector('[id$="-title"]')?.textContent?.trim();
        if (title && title !== this.lastDialogTitle) {
          this.lastDialogTitle = title;
          speechService.speak(title);
        }
      }
    }
  }

private handleFocusIn(event: FocusEvent): void {
    const target = event.target as HTMLElement;

    // Determine what text to speak when element is focused
    let textToSpeak = '';

    // Read aria-label if present
    if (target.getAttribute('aria-label')) {
      textToSpeak = target.getAttribute('aria-label') || '';
    }
    // For buttons, read the text content and any parent label text
    else if (target.tagName === 'BUTTON') {
      const buttonText = target.textContent?.trim() || '';
      const parentLabel = target.closest('label');
      textToSpeak = parentLabel?.textContent?.trim() || buttonText;
    }
    // For inputs, read label, placeholder, and current value
    else if (target.tagName === 'INPUT') {
      const inputEl = target as HTMLInputElement;
      let label = '';

      // Try to find label by id
      const id = inputEl.id;
      if (id) {
        const labelEl = document.querySelector(`label[for="${id}"]`);
        if (labelEl) {
          label = labelEl.textContent?.trim() || '';
        }
      }

      // If no label found by id, try to find parent label
      if (!label) {
        const parentLabel = inputEl.closest('label');
        if (parentLabel) {
          label = parentLabel.textContent?.trim() || '';
        }
      }

      // If still no label, try to find previous sibling label
      if (!label) {
        const prevLabel = inputEl.previousElementSibling;
        if (prevLabel?.tagName === 'LABEL') {
          label = prevLabel.textContent?.trim() || '';
        }
      }

      // If still no label, try to find FormLabel by class
      if (!label) {
        const formLabel = inputEl.closest('.form-label') || inputEl.previousElementSibling?.querySelector('.form-label');
        if (formLabel) {
          label = formLabel.textContent?.trim() || '';
        }
      }

      // Speak the label when the input is focused
      textToSpeak = label || inputEl.placeholder || inputEl.type;

      // Add input event listener to read each character aloud
      inputEl.addEventListener('input', (e) => {
        const inputEvent = e.target as HTMLInputElement;
        const lastChar = inputEvent.value.slice(-1); // Get the last typed character
        if (lastChar) {
          speechService.speak(lastChar);
        }
      });
    }
    // For links, read the text content
    else if (target.tagName === 'A') {
      textToSpeak = target.textContent?.trim() || '';
    }

    // If we have text to speak, do it
    if (textToSpeak) {
      speechService.speak(textToSpeak);
    }
  }}

export const focusManager = new FocusManager();
export default focusManager;
