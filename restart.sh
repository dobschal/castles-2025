git fetch
git pull
docker compose down server
docker compose build server --no-cache
docker compose up server -d
docker compose down client
docker compose build client --no-cache
docker compose up client -d