envsubst '${SCHEME} ${HOST} ${PORT}' < /usr/share/nginx/html/js/compiled/app.js.template > /usr/share/nginx/html/js/compiled/app.js && nginx -g 'daemon off;'
