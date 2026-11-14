# BACKEND_DEPLOYMENT_PL — NeuroLab AI Gateway (produkcja / domowy serwer)

Backend działa na Node ≥ 18 (rekomendowane 22), **zero zależności npm**. Plik główny: `backend/server.js`.

## 1. Wymogi

- Serwer z Node 18+ (VPS, Raspberry Pi, stary laptop, Docker, Fly.io, Render — cokolwiek).
- Konto u dostawcy modelu z kluczem API (OpenAI, OpenRouter, Azure OpenAI, lokalny vLLM/Ollama w trybie OpenAI-compatible).
- Dla produkcji: domena + HTTPS (reverse proxy). Dla LAN: http://192.168.x.x:8787 — wystarczy.

## 2. Szybki start (VPS / serwer domowy)

```bash
git clone https://github.com/WojtasYT/projekt-neuro.git
cd projekt-neuro/backend
cp .env.example .env   # edytuj
```

`.env`:
```
PORT=8787
AI_BASE_URL=https://api.openai.com/v1     # albo inny OpenAI-compatible endpoint
AI_MODEL=gpt-4o-mini
AI_API_KEY=sk-...                          # RZECZYWISTY KLUCZ — tylko tu, nigdy w APK/repo
RATE_LIMIT_PER_MIN=30
REQUEST_TIMEOUT_MS=20000
```

Uruchomienie (jedno-linijkowe ładowanie .env, bez paczek):
```bash
set -a; . ./.env; set +a; npm start
```

Sprawdzenie:
```bash
curl -s http://localhost:8787/health
# {"status":"ok",...,"provider":{"configured":true,"reachable":true,"detail":"Dostawca odpowiada."}}
```

## 3. Utrzymanie procesu

**systemd** (`/etc/systemd/system/neurolab-ai.service`):
```ini
[Unit]
Description=NeuroLab AI Gateway
After=network.target
[Service]
WorkingDirectory=/opt/projekt-neuro/backend
EnvironmentFile=/opt/projekt-neuro/backend/.env
ExecStart=/usr/bin/node server.js
Restart=on-failure
User=neurolab
NoNewPrivileges=true
ProtectSystem=full
[Install]
WantedBy=multi-user.target
```
`sudo systemctl enable --now neurolab-ai`

**Docker**:
```bash
docker run -d --name neurolab-ai --restart=unless-stopped \
  -p 8787:8787 \
  -e AI_API_KEY=sk-... -e AI_BASE_URL=https://api.openai.com/v1 -e AI_MODEL=gpt-4o-mini \
  -v /opt/projekt-neuro:/app -w /app/backend \
  node:22-alpine node server.js
```
(brak obrazu do zbudowania — zero zależności, `node server.js` wystarcza)

## 4. HTTPS (zalecane poza LAN)

Caddy (najprostszy, automatyczne certyfikaty):
```
ai.twojadomena.pl {
    reverse_proxy localhost:8787
}
```
Nginx: `proxy_pass http://127.0.0.1:8787; proxy_read_timeout 60s;` + certbot.

## 5. Bezpieczeństwo

- **Nigdy** nie commituj `.env`. Klucz tylko w env / systemd `EnvironmentFile` / sekretach hostingu.
- Jeśli bramka ma być publiczna: postaw przed nią auth (basic auth w Caddy, Cloudflare Access, Tailscale/VPN).
  Referencyjna bramka jest jedno-posiadaczowa — nie ma kont użytkowników.
- Rate-limit per IP domyślnie 30/min — zwiększ ostrożnie (`RATE_LIMIT_PER_MIN`).
- Logi (`journalctl -u neurolab-ai`) zawierają wyłącznie metodę/ścieżkę/status/czas — bez treści.

## 6. Weryfikacja po wdrożeniu

```bash
curl -s https://ai.twojadomena.pl/health          # provider.reachable: true
curl -s https://ai.twojadomena.pl/v1/ai/status    # configured: true, model: ...
curl -s https://ai.twojadomena.pl/v1/models       # lista modeli dostawcy
curl -s -X POST https://ai.twojadomena.pl/v1/chat/completions \
  -H 'content-type: application/json' \
  -d '{"mode":"impuls","messages":[{"role":"user","content":"test"}]}'
```

Następnie w aplikacji: Ustawienia → Asystent → Adres bramki AI → „Sprawdź połączenie”.

## 7. Kody błędów (diagnoza)

| Objaw | Przyczyna | Działanie |
|---|---|---|
| `/health` → `configured:false` | brak `AI_API_KEY` | dopisz do .env i zrestartuj |
| `/health` → `reachable:false`, detail 401/403 | klucz odrzucony | popraw klucz |
| `MODEL_NOT_FOUND` | zła nazwa modelu | sprawdź `/v1/models` |
| `RATE_LIMITED` | limit dostawcy | odczekaj / zmień model / inny plan |
| timeout | `REQUEST_TIMEOUT_MS` vs wolny dostawca | zwiększ limit po stronie serwera |
| `429` z bramki | Twój własny rate-limit | `RATE_LIMIT_PER_MIN` |
