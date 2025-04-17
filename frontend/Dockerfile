# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files and install dependencies
COPY package.json pnpm-lock.yaml ./
RUN npm install -g pnpm && pnpm install --frozen-lockfile --prod=false

# Copy source code
COPY . .

# Build the application
RUN pnpm build

# Production stage with Nginx
FROM nginx:stable-alpine AS production

# Copy the entrypoint script
COPY entrypoint.sh /entrypoint.sh
# Make it executable
RUN chmod +x /entrypoint.sh

# Copy custom nginx config
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Define where Nginx serves files from
ARG NGINX_ROOT_DIR=/usr/share/nginx/html

# Copy built assets to nginx serve directory
COPY --from=build /app/dist ${NGINX_ROOT_DIR}

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q --spider http://localhost:80/ || exit 1

# Expose port 80
EXPOSE 80

# Use ENTRYPOINT to run the script, which will start Nginx
ENTRYPOINT ["/entrypoint.sh"]
