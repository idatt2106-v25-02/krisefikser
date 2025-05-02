//./plugins/posthog.js
import posthog, { PostHog } from "posthog-js";
import type { Plugin } from 'vue';

const posthogPlugin: Plugin = {
  install(app) {
    app.config.globalProperties.$posthog = posthog.init(
      'phc_tbpc1IzXD0vWSxTf6TwUmzKxXPdlBvMtb1pGBFW2zhc',
      {
        api_host: 'https://eu.i.posthog.com',
      }
    );
  },
};

export default posthogPlugin;
