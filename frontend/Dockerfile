# Build the frontend assets
FROM node:20-alpine AS build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ .
ARG VITE_API_BASE_URL=http://localhost:8000
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

# Serve with Nginx and reverse proxy API traffic
FROM nginx:1.25-alpine AS runtime
RUN apk add --no-cache curl
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/frontend/dist /usr/share/nginx/html
COPY frontend/public/health /usr/share/nginx/html/health

EXPOSE 80
HEALTHCHECK CMD curl -f http://localhost/health || exit 1
CMD ["nginx", "-g", "daemon off;"]
