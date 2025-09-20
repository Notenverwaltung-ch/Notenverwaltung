# Build-less NGINX image that serves the prebuilt Angular app from dist
# The GitHub Actions job builds the Angular app and the dist is copied into the image.

FROM nginx:1.27-alpine

# Expose port 80 to serve the Angular application
EXPOSE 80

# Remove default site config and add our SPA-friendly config
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the prebuilt Angular app into Nginx html directory
# The workflow builds to frontend/dist/frontend-notenverwaltung
COPY dist/frontend-notenverwaltung/ /usr/share/nginx/html/

# Healthcheck (optional)
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ || exit 1

