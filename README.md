# FaceGate

Face authentication as a service. Send a photo: if the face matches the enrolled one,
you get a short-lived JWT that unlocks the reserved area. Java 21, Spring Boot,
OpenCV (YuNet + SFace) — all verification happens **server-side**.

## How it works

```
 client (curl / browser / app)                FaceGate service
 ─────────────────────────────               ─────────────────────────────────
 POST /api/verify  [photo]   ─────────────▶   YuNet → SFace → cosine match
                                              │  (enrolled template: AES-GCM
        ◀─── 200 { jwt, expires in 60s } ─────┘   encrypted, on disk)
 GET /api/area-riservata
   Authorization: Bearer <jwt> ────────────▶   signature + expiry check
        ◀─── 200 reserved content ─────────┘
```

1. The client posts a photo to `/api/verify`
2. **YuNet** locates the face; **SFace** turns it into a 128-dimension embedding
3. Cosine similarity against the enrolled template (threshold 0.6)
4. Match → a **60-second JWT**; the token opens the reserved area
5. After expiry the token is dead — entering again requires a new face check

The enrolled face is never stored as an image: only the encrypted 128-d embedding is.

## Quick start

Requires Java 21 and Maven.

1. Download the ONNX models (gitignored, ~37 MB total):

```bash
mkdir -p models
curl -L -o models/face_detection_yunet_2023mar.onnx \
  https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx
curl -L -o models/face_recognition_sface_2021dec.onnx \
  https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx
```

2. Build and run (from the project root — models load via relative path):

```bash
mvn package
java -jar target/facegate-0.0.1-SNAPSHOT.jar
```

3. Enroll a face (one time only, admin-gated):

```bash
curl -X POST http://localhost:8080/api/enroll \
     -H 'X-Admin-Token: dev-admin-secret' \
     -F frame=@my-face.jpg
```

4. Verify and grab a token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/verify -F frame=@my-face.jpg \
        | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

5. Enter the reserved area:

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/area-riservata
```

Tip — capture a test frame from a webcam (skip the first seconds, auto-exposure
needs to settle):

```bash
ffmpeg -y -f v4l2 -input_format yuyv422 -framerate 5 -i /dev/video0 \
       -ss 4 -frames:v 1 -update 1 selfie.png
```

## API

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | — | public landing page |
| POST | `/api/enroll` | `X-Admin-Token` header | enroll exactly one face from an uploaded photo |
| POST | `/api/verify` | — | check a photo against the enrolled face; returns a JWT on match |
| GET | `/api/area-riservata` | `Authorization: Bearer <jwt>` | reserved content |

Verify success:

```json
{"authenticated":true,"similarity":0.78,"token":"eyJ...","expiresInSeconds":60}
```

Failures return `401` with the reason (`no face detected`, `below threshold`, …);
malformed uploads return `400`.

## Configuration

| Property | Default | Purpose |
|----------|---------|---------|
| `facegate.admin-token` | `dev-admin-secret` | secret for `/api/enroll` (env `FACEGATE_ADMIN_TOKEN`) |
| `facegate.jwt-secret` | dev value | HMAC signing key (env `FACEGATE_JWT_SECRET`) |
| `facegate.jwt-ttl-seconds` | `60` | token lifetime |

Override both secrets via environment variables before any real use.

## Security model

- **Server-side verification** — the client is never trusted with the decision;
  only the server holds the enrolled template.
- **Encrypted enrollment** — the embedding is stored AES-256-GCM encrypted in
  `~/.facegate` (key file `0600`), never the image itself.
- **Short-lived tokens** — a leaked token dies within 60 seconds.
- **Constant-time** admin-token comparison.

## Known limitations

- **No liveness check** — a photo of the enrolled face shown to another camera
  passes. Anti-spoofing is roadmap, not implemented.
- **Single user** — one enrolled face per instance.
- **No TLS** — put it behind an HTTPS reverse proxy for anything beyond localhost.
- **Dev secrets by default** — override via environment variables.
- Threshold (0.6) is not formally calibrated (no FAR/FRR study).

## Roadmap

- Liveness challenge (e.g. two-frame head-movement check)
- Multi-user enrollment and management API
- Rate limiting on `/api/verify`
- CI + demo GIF
- VPN-gated deployment (WireGuard) — the original FaceVPN idea, as a deployment chapter

## Tests

```bash
mvn test
```

10 unit tests: cosine-similarity matching logic, and JWT issue/validate/expiry.
