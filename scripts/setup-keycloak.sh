#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${KEYCLOAK_SETUP_BASE_URL:-http://localhost:8081}"
ADMIN_USERNAME="${KEYCLOAK_SETUP_ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_SETUP_ADMIN_PASSWORD:-admin}"
REALM="${KEYCLOAK_REALM:-remittance-thesis}"
ADMIN_CLIENT_ID="${KEYCLOAK_ADMIN_CLIENT_ID:-iam-admin-client}"
ADMIN_CLIENT_SECRET="${KEYCLOAK_ADMIN_CLIENT_SECRET:-change-me}"
BACKEND_CLIENT_ID="${KEYCLOAK_BACKEND_CLIENT_ID:-iam-backend}"

get_admin_token() {
  curl -fsS -X POST "$BASE_URL/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=admin-cli" \
    -d "username=$ADMIN_USERNAME" \
    -d "password=$ADMIN_PASSWORD" \
    -d "grant_type=password" | jq -r ".access_token"
}

require_tool() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required tool: $1" >&2
    exit 1
  fi
}

require_tool curl
require_tool jq

ADMIN_TOKEN="$(get_admin_token)"

realm_status="$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM")"

if [ "$realm_status" = "404" ]; then
  curl -fsS -X POST "$BASE_URL/admin/realms" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d @- >/dev/null <<JSON
{
  "realm": "$REALM",
  "enabled": true,
  "roles": {
    "realm": [
      { "name": "ROLE_CUSTOMER" },
      { "name": "ROLE_ADMIN" },
      { "name": "ROLE_COMPLIANCE_AGENT" },
      { "name": "ROLE_SERVICE" }
    ]
  },
  "clients": [
    {
      "clientId": "$ADMIN_CLIENT_ID",
      "name": "$ADMIN_CLIENT_ID",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "secret": "$ADMIN_CLIENT_SECRET",
      "serviceAccountsEnabled": true,
      "standardFlowEnabled": false,
      "directAccessGrantsEnabled": false,
      "fullScopeAllowed": true,
      "defaultClientScopes": ["basic", "profile", "email", "roles"]
    },
    {
      "clientId": "$BACKEND_CLIENT_ID",
      "name": "$BACKEND_CLIENT_ID",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "bearerOnly": true,
      "standardFlowEnabled": false,
      "directAccessGrantsEnabled": false,
      "fullScopeAllowed": true
    }
  ]
}
JSON
fi

ADMIN_TOKEN="$(get_admin_token)"

REALM_MANAGEMENT_ID="$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients?clientId=realm-management" | jq -r ".[0].id")"

ADMIN_CLIENT_UUID="$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients?clientId=$ADMIN_CLIENT_ID" | jq -r ".[0].id")"

SERVICE_ACCOUNT_USER_ID="$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients/$ADMIN_CLIENT_UUID/service-account-user" | jq -r ".id")"

curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients/$ADMIN_CLIENT_UUID" | \
  jq '.fullScopeAllowed = true' >/tmp/"$ADMIN_CLIENT_ID"-client.json

curl -fsS -X PUT \
  "$BASE_URL/admin/realms/$REALM/clients/$ADMIN_CLIENT_UUID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  --data @/tmp/"$ADMIN_CLIENT_ID"-client.json >/dev/null

REALM_MANAGEMENT_ROLES="$(curl -fsS \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$BASE_URL/admin/realms/$REALM/clients/$REALM_MANAGEMENT_ID/roles" | \
  jq '[.[] | select(
    .name == "manage-users" or
    .name == "query-users" or
    .name == "view-users" or
    .name == "view-realm"
  ) | {id, name}]')"

curl -fsS -X POST \
  "$BASE_URL/admin/realms/$REALM/users/$SERVICE_ACCOUNT_USER_ID/role-mappings/clients/$REALM_MANAGEMENT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$REALM_MANAGEMENT_ROLES" >/dev/null

curl -fsS -X POST "$BASE_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=$ADMIN_CLIENT_ID" \
  -d "client_secret=$ADMIN_CLIENT_SECRET" | \
  jq '{configured: has("access_token"), error, error_description}'
