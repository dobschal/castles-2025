# build stage
FROM node:18.19-alpine as build-stage

RUN apk --no-cache add --virtual native-deps \
    g++ gcc libgcc libstdc++ linux-headers make python3 && \
    npm install --quiet node-gyp -g

ARG BUILD_ENV

# We want to show the Git commit hash in the running app to see exactly which version is running
# We cannot run the git CMD to get it, because the build happens inside the docker container. So we
# insert it here via the GitLab CI. See .gitlab.ci.yml
ARG GIT_COMMIT_HASH
ENV VITE_GIT_COMMIT_HASH=${GIT_COMMIT_HASH}

# make the 'app' folder the current working directory
WORKDIR /app
# copy both 'package.json' and 'package-lock.json' (if available)
COPY package*.json ./
# copy '.npmrc'
COPY .npmrc ./
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
RUN npm run $BUILD_ENV



# deploy stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

COPY ./deploy/entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD [ "nginx", "-g", "daemon off;" ]
