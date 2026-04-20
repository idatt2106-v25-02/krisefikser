import { globalIgnores } from 'eslint/config';
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript';
import pluginVue from 'eslint-plugin-vue';
import pluginVitest from '@vitest/eslint-plugin';
import pluginVueA11y from 'eslint-plugin-vuejs-accessibility';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import pluginCypress from 'eslint-plugin-cypress/flat';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';
// To allow more languages other than `ts` in `.vue` files, uncomment the following lines:
// import { configureVueProject } from '@vue/eslint-config-typescript'
// configureVueProject({ scriptLangs: ['ts', 'tsx'] })
// More info at https://github.com/vuejs/eslint-config-typescript/#advanced-setup
export default defineConfigWithVueTs({
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
}, globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']), pluginVue.configs['flat/essential'], vueTsConfigs.recommended, {
    ...pluginVitest.configs.recommended,
    files: ['src/**/__tests__/*'],
}, {
    ...pluginCypress.configs.recommended,
    files: ['cypress/e2e/**/*.{cy,spec}.{js,ts,jsx,tsx}', 'cypress/support/**/*.{js,ts,jsx,tsx}'],
}, skipFormatting, {
    plugins: {
        'vuejs-accessibility': pluginVueA11y,
    },
    rules: {
        'vuejs-accessibility/alt-text': 'error',
        'vuejs-accessibility/anchor-has-content': 'error',
        'vuejs-accessibility/aria-props': 'error',
        'vuejs-accessibility/click-events-have-key-events': 'off',
        'vuejs-accessibility/form-control-has-label': 'off',
        'vuejs-accessibility/interactive-supports-focus': 'warn',
        'vuejs-accessibility/label-has-for': 'off',
        'vuejs-accessibility/no-autofocus': 'error',
        'vuejs-accessibility/no-static-element-interactions': 'off',
        'vuejs-accessibility/tabindex-no-positive': 'error',
    },
}, {
    rules: {
        'vue/multi-word-component-names': 'off',
        '@typescript-eslint/no-unused-vars': [
            'error',
            {
                argsIgnorePattern: '^_',
                varsIgnorePattern: '^_',
                ignoreRestSiblings: true,
            },
        ],
    },
});
