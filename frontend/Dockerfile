# Build stage
# Expects build context = frontend directory (e.g. Railway Root Directory: frontend, or: docker build -f Dockerfile . from frontend/)
FROM node:22-alpine AS builder

# Install pnpm
RUN corepack enable && corepack prepare pnpm@10.10.0 --activate

WORKDIR /app

# Copy package files
COPY package.json pnpm-lock.yaml ./

# Install dependencies
RUN pnpm install --frozen-lockfile

# Copy source code
COPY . .

# Use committed API client (skip orval in build - API may be unreachable from build env)
ARG VITE_API_URL
ARG VITE_WS_URL
ARG VITE_POSTHOG_ENABLED

# Build the application (ignoring type errors)
ENV NODE_ENV=production
RUN pnpm build-only

# Production stage
FROM nginx:alpine

# Copy built files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
