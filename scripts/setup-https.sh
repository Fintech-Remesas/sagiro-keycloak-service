#!/usr/bin/env bash
# setup-https.sh — Instala Nginx + SSL autofirmado en el servidor Sagiro
set -euo pipefail

SERVER_IP="3.151.248.130"
CERT_DIR="/etc/nginx/ssl"
NGINX_CONF="/etc/nginx/sites-available/sagiro"

echo "=================================================="
echo "  SAGIRO — Setup HTTPS con certificado autofirmado"
echo "=================================================="

# ── 1. Instalar Nginx ────────────────────────────────────────────────────────
echo ""
echo "[1/5] Instalando Nginx..."
sudo apt-get update -qq
sudo apt-get install -y nginx
echo "     ✅ Nginx instalado"

# ── 2. Crear certificado SSL autofirmado (válido 2 años) ─────────────────────
echo ""
echo "[2/5] Creando certificado SSL autofirmado..."
sudo mkdir -p "$CERT_DIR"
sudo openssl req -x509 -nodes -days 730 \
  -newkey rsa:2048 \
  -keyout "$CERT_DIR/sagiro.key" \
  -out    "$CERT_DIR/sagiro.crt" \
  -subj "/C=PE/ST=Lima/L=Lima/O=Sagiro/CN=$SERVER_IP" \
  -addext "subjectAltName=IP:$SERVER_IP"
sudo chmod 600 "$CERT_DIR/sagiro.key"
echo "     ✅ Certificado creado en $CERT_DIR"

# ── 3. Crear configuración de Nginx ──────────────────────────────────────────
echo ""
echo "[3/5] Configurando Nginx como proxy inverso..."

sudo tee "$NGINX_CONF" > /dev/null << 'NGINX_CONF_EOF'
# ── Redirigir HTTP → HTTPS ──────────────────────────────────────────────────
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

# ── API Gateway (puerto principal de la app) ────────────────────────────────
server {
    listen 443 ssl;
    server_name _;

    ssl_certificate     /etc/nginx/ssl/sagiro.crt;
    ssl_certificate_key /etc/nginx/ssl/sagiro.key;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;
    ssl_session_cache   shared:SSL:10m;
    ssl_session_timeout 10m;

    # Headers de seguridad
    add_header Strict-Transport-Security "max-age=31536000" always;
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;

    # Tamaño máximo de request (para subida de documentos KYC, etc.)
    client_max_body_size 20M;

    # ── Proxy al API Gateway ──
    location / {
        proxy_pass         http://localhost:8765;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto https;
        proxy_read_timeout 60s;
    }

    # ── WebSocket para Socket.io (Web3 Monitor) ──
    location /socket.io/ {
        proxy_pass         http://localhost:8765;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade    $http_upgrade;
        proxy_set_header   Connection "upgrade";
        proxy_set_header   Host       $host;
        proxy_read_timeout 3600s;
    }
}

# ── Keycloak en HTTPS (puerto 8443) ────────────────────────────────────────
server {
    listen 8443 ssl;
    server_name _;

    ssl_certificate     /etc/nginx/ssl/sagiro.crt;
    ssl_certificate_key /etc/nginx/ssl/sagiro.key;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    location / {
        proxy_pass         http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto https;
        proxy_read_timeout 60s;
    }
}
NGINX_CONF_EOF

echo "     ✅ Configuración Nginx creada"

# ── 4. Activar el sitio y verificar config ────────────────────────────────────
echo ""
echo "[4/5] Activando configuración..."
sudo ln -sf "$NGINX_CONF" /etc/nginx/sites-enabled/sagiro
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl restart nginx
echo "     ✅ Nginx activo y corriendo"

# ── 5. Abrir puertos en el firewall del servidor ──────────────────────────────
echo ""
echo "[5/5] Verificando firewall local (ufw)..."
if command -v ufw > /dev/null 2>&1; then
    sudo ufw allow 80/tcp  > /dev/null
    sudo ufw allow 443/tcp > /dev/null
    sudo ufw allow 8443/tcp > /dev/null
    echo "     ✅ Puertos 80, 443, 8443 abiertos en ufw"
else
    echo "     ℹ️  ufw no está activo (los Security Groups de AWS controlan los puertos)"
fi

echo ""
echo "=================================================="
echo "  ✅ HTTPS configurado exitosamente!"
echo ""
echo "  🌐 API Gateway: https://$SERVER_IP"
echo "  🔑 Keycloak:   https://$SERVER_IP:8443"
echo ""
echo "  ⚠️  Certificado autofirmado — los navegadores"
echo "     mostrarán advertencia (es normal)."
echo "     En Android/iOS hay que aceptar el cert."
echo "=================================================="
