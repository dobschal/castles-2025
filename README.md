# Castles 2025

This is the repository for the web browser game Castles (version 2025).
Client and server are in this repository. It supports Docker.

## Get Started

### JWT Secrets

In order to get the server running with docker you need to generate the JWT secrets. Just run the `generate-keys.sh` script.

### Database

Start the database via docker compose
```bash
docker compose up database -d
```

### (Re-)Start Client and Server

Use the `restart.sh` script to start the client and server application.

## Configuration

### Docker Compose

Check the `docker-compose.yml` file when you want to configure something like exposed ports.
Each component has an own Dockerfile that contain further information. 

### Nginx

The SSL certificates are handled via Certbot. Just add a server config to the default nginx config and run the following command to setup the SSL certificates. (They should get renewed automatically)
```bash
certbot --nginx
```


The nginx config should like:
```nginx
server {

	server_name castles.dobschal.eu;
	
	client_max_body_size 50M;
 
    location / { 
        proxy_pass http://localhost:4001/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /api { 
        proxy_pass http://localhost:4000/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    listen [::]:443 ssl; # managed by Certbot
    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/goldenfin.band/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/goldenfin.band/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot
}
```
