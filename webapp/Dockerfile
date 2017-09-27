FROM nginx:1.13.5-alpine

COPY resources/public/img /usr/share/nginx/html/img
COPY resources/public/css /usr/share/nginx/html/css
COPY resources/public/js/compiled/min.js /usr/share/nginx/html/js/compiled/app.js.template
COPY resources/public/index.html /usr/share/nginx/html/index.html

COPY docker/setup-env.sh /setup-env.sh

RUN chmod +x /setup-env.sh

EXPOSE 80

CMD /setup-env.sh