#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8081"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="Admin_S4g1r0_2026!"
REALM="sagiro"
ADMIN_CLIENT_ID="iam-admin-client"
ADMIN_CLIENT_SECRET="admin-secret-123"
BACKEND_CLIENT_ID="iam-backend"
BACKEND_CLIENT_SECRET="backend-secret-123"

echo "=================================================="
echo "  SAGIRO - Keycloak Setup"
echo "=================================================="
echo "Server : $BASE_URL"
echo "Realm  : $REALM"
echo ""

# ── Paso 1: Token de admin ──────────────────────────────────────────────────
echo "[1/5] Obteniendo token de administrador..."

TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USERNAME" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password")

ADMIN_TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('access_token') or 'ERROR')" 2>/dev/null || echo "ERROR")

if [ "$ADMIN_TOKEN" = "ERROR" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "ERROR: No se pudo obtener el token de admin."
  echo "Respuesta: $TOKEN_RESPONSE"
  exit 1
fi

echo "     ✅ Token obtenido (${ADMIN_TOKEN:0:20}...)"

# ── Paso 2: Verificar realm ─────────────────────────────────────────────────
echo "[2/5] Verificando si realm '$REALM' ya existe..."

REALM_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM")

if [ "$REALM_STATUS" = "200" ]; then
  echo "     ⚠️  El realm '$REALM' YA EXISTE. Saltando creación."
else
  echo "     Realm no encontrado (HTTP $REALM_STATUS). Creando..."

  # ── Paso 3: Crear realm ───────────────────────────────────────────────────
  echo "[3/5] Creando realm '$REALM' con roles y clientes..."

  curl -fsS -X POST "$BASE_URL/admin/realms" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"realm\": \"$REALM\",
      \"enabled\": true,
      \"roles\": {
        \"realm\": [
          { \"name\": \"ROLE_CUSTOMER\" },
          { \"name\": \"ROLE_ADMIN\" },
          { \"name\": \"ROLE_COMPLIANCE_AGENT\" },
          { \"name\": \"ROLE_SERVICE\" }
        ]
      },
      \"clients\": [
        {
          \"clientId\": \"$ADMIN_CLIENT_ID\",
          \"name\": \"$ADMIN_CLIENT_ID\",
          \"enabled\": true,
          \"protocol\": \"openid-connect\",
          \"publicClient\": false,
          \"secret\": \"$ADMIN_CLIENT_SECRET\",
          \"serviceAccountsEnabled\": true,
          \"standardFlowEnabled\": false,
          \"directAccessGrantsEnabled\": false,
          \"fullScopeAllowed\": true,
          \"defaultClientScopes\": [\"basic\",\"profile\",\"email\",\"roles\"]
        },
        {
          \"clientId\": \"$BACKEND_CLIENT_ID\",
          \"name\": \"$BACKEND_CLIENT_ID\",
          \"enabled\": true,
          \"protocol\": \"openid-connect\",
          \"publicClient\": false,
          \"bearerOnly\": false,
          \"secret\": \"$BACKEND_CLIENT_SECRET\",
          \"standardFlowEnabled\": false,
          \"directAccessGrantsEnabled\": true,
          \"fullScopeAllowed\": true,
          \"defaultClientScopes\": [\"basic\",\"profile\",\"email\",\"roles\"]
        },
        {
          \"clientId\": \"mobile-app\",
          \"name\": \"mobile-app\",
          \"enabled\": true,
          \"protocol\": \"openid-connect\",
          \"publicClient\": true,
          \"bearerOnly\": false,
          \"standardFlowEnabled\": true,
          \"directAccessGrantsEnabled\": true,
          \"fullScopeAllowed\": true,
          \"redirectUris\": [\"*\"],
          \"webOrigins\": [\"*\"]
        },
        {
          \"clientId\": \"remittance-frontend\",
          \"name\": \"remittance-frontend\",
          \"enabled\": true,
          \"protocol\": \"openid-connect\",
          \"publicClient\": true,
          \"standardFlowEnabled\": true,
          \"directAccessGrantsEnabled\": true,
          \"fullScopeAllowed\": true
        }
      ]
    }" > /dev/null

  echo "     ✅ Realm '$REALM' creado!"
fi

# ── Paso 4: Asignar permisos al service account ─────────────────────────────
echo "[4/5] Configurando permisos del service account..."

# Refrescar token
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USERNAME" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

REALM_MGMT_ID=$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients?clientId=realm-management" | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")

ADMIN_CLIENT_UUID=$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients?clientId=$ADMIN_CLIENT_ID" | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")

SA_USER_ID=$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients/$ADMIN_CLIENT_UUID/service-account-user" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

ROLES=$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients/$REALM_MGMT_ID/roles" | python3 -c "
import sys,json
roles = json.load(sys.stdin)
needed = [r for r in roles if r['name'] in ['manage-users','query-users','view-users','view-realm']]
print(json.dumps(needed))
")

curl -fsS -X POST \
  "$BASE_URL/admin/realms/$REALM/users/$SA_USER_ID/role-mappings/clients/$REALM_MGMT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$ROLES" > /dev/null

echo "     ✅ Roles asignados: manage-users, query-users, view-users, view-realm"

# ── Paso 5: Verificar token técnico ─────────────────────────────────────────
echo "[5/5] Verificando token técnico del IAM service..."

sleep 2

TECH_RESULT=$(curl -s -X POST "$BASE_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=$ADMIN_CLIENT_ID" \
  -d "client_secret=$ADMIN_CLIENT_SECRET" | python3 -c "
import sys,json
d=json.load(sys.stdin)
if 'access_token' in d:
    print('OK')
else:
    print('ERROR: ' + d.get('error_description','unknown'))
")

echo ""
if [ "$TECH_RESULT" = "OK" ]; then
  echo "=================================================="
  echo "  ✅ SETUP COMPLETO — Keycloak está listo!"
  echo "  El IAM service puede registrar usuarios ahora."
  echo "=================================================="
else
  echo "⚠️  $TECH_RESULT"
  echo "El realm fue creado pero verifica el cliente $ADMIN_CLIENT_ID"
fi
