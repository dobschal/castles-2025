git fetch
git reset --hard origin/main
docker compose build server --no-cache
docker compose down server
docker compose up server -d
docker compose build client --no-cache
docker compose down client
docker compose up client -d