# Build stage
FROM node:22-alpine AS builder

# Install pnpm
RUN corepack enable && corepack prepare pnpm@10.10.0 --activate

WORKDIR /app

# Skip Cypress binary in image (prepare runs "cypress install"); speeds up builds and avoids huge downloads.
ENV CYPRESS_INSTALL_BINARY=0

# Copy package files
COPY package.json pnpm-lock.yaml ./

# Install dependencies
RUN pnpm install --frozen-lockfile

# Copy source code
COPY . .

ARG VITE_API_URL=http://localhost:8080
ARG VITE_WS_URL=ws://localhost:8080/ws
ARG VITE_TURNSTILE_SITE_KEY=0x4AAAAAABSTiPNZwrBLQkgr
ARG VITE_POSTHOG_ENABLED=false
ARG VITE_POSTHOG_KEY=
ARG VITE_POSTHOG_HOST=

ENV VITE_API_URL=$VITE_API_URL
ENV VITE_WS_URL=$VITE_WS_URL
ENV VITE_TURNSTILE_SITE_KEY=$VITE_TURNSTILE_SITE_KEY
ENV VITE_POSTHOG_ENABLED=$VITE_POSTHOG_ENABLED
ENV VITE_POSTHOG_KEY=$VITE_POSTHOG_KEY
ENV VITE_POSTHOG_HOST=$VITE_POSTHOG_HOST

# Build the application (ignoring type errors)
ENV NODE_ENV=production
RUN pnpm build-only

# Production stage
FROM nginx:1.29.5-alpine3.23

# Copy built files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
