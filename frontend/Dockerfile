FROM nginx:1.27-alpine

EXPOSE 80

RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY dist/frontend-notenverwaltung/browser/ /usr/share/nginx/html/

HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ || exit 1

