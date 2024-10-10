# build stage
FROM node:18.19-alpine as build-stage

RUN apk --no-cache add --virtual native-deps \
    g++ gcc libgcc libstdc++ linux-headers make python3 && \
    npm install --quiet node-gyp -g

# make the 'app' folder the current working directory
WORKDIR /app
# copy both 'package.json' and 'package-lock.json' (if available)
COPY package*.json ./
# copy project files and folders to the current working directory (i.e. 'app' folder)
COPY public ./public
COPY .env* *.js *.json ./
COPY index.html ./
COPY vite.config.ts ./
COPY src ./src
# install project dependencies
RUN npm ci
# build app for production with minification
RUN ls
RUN npm run build



# deploy stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

COPY ./deploy/entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD [ "nginx", "-g", "daemon off;" ]
