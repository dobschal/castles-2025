#!/bin/sh

# To be able to set env-variables in a kubernetes cluster

ROOT_DIR=/usr/share/nginx/html

# Replace env vars in JavaScript files
echo "Replacing env constants in JS"
echo "#API_GATEWAY_URL=${API_GATEWAY_URL}"
echo "#AUTH_BASE_URL=${AUTH_BASE_URL}"
echo "#CI_APPLICATION_ID= ${CI_APPLICATION_ID}"
echo "#TENANT_ID=${TENANT_ID}"
echo "#AUTH_CLIENT_URL=${AUTH_CLIENT_URL}"

# shellcheck disable=SC2231
for file in $ROOT_DIR/js/app.*.js* $ROOT_DIR/js/app.js $ROOT_DIR/js/chunk-vendors.js $ROOT_DIR/index.html $ROOT_DIR/assets/index-*.js $ROOT_DIR/precache-manifest*.js;
do
echo "Processing $file ...";
# 's|oldValue|newValue|g inputfile' - g to replace all occurrence
sed -i 's|#API_GATEWAY_URL|'"${API_GATEWAY_URL}"'|g' $file
sed -i 's|#AUTH_BASE_URL|'"${AUTH_BASE_URL}"'|g' $file
sed -i 's|#CI_APPLICATION_ID|'"${CI_APPLICATION_ID}"'|g' $file
sed -i 's|#TENANT_ID|'"${TENANT_ID}"'|g' $file
sed -i 's|#AUTH_CLIENT_URL|'"${AUTH_CLIENT_URL}"'|g' $file

done

echo "Starting Nginx"
nginx -g 'daemon off;'
