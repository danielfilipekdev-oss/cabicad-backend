# cabicad-backend
Backend for CabiCAD application

## Stack
- Kotlin 2.2 + Spring Boot 3.5 (Web, Validation, Actuator)
- Gradle (Kotlin DSL) with wrapper, Java 21 toolchain

## Run
```bash
./gradlew bootRun      # http://localhost:8080/api/ping
./gradlew build        # compile + tests
```

## Frontend integration
- Server listens on `http://localhost:8080` (`server.port`, override with `SERVER_PORT`).
- REST API lives under `/api/**`.
- CORS for `/api/**` allows origins from `cabicad.cors.allowed-origins` in `application.yml`
  (Vite dev `:5173` and preview `:4173`). In dev the frontend normally uses the Vite proxy, so CORS is only a fallback.

## Board catalog (`BoardCatalog.kt`, `BoardCatalogController.kt`)
- `GET /api/boards/catalog` → `{ types, rawBrand, defaultBoardId, boards[] }` – board models for the frontend picker
  (type → brand → board). A board: `id`, `brand`, `type`, `seriesCode`, `name`, `textureUrl`, `textureSize` (mm),
  `grainMatters`, `thicknesses` (mm; empty = any thickness entered by hand), `color`.
- `GET /api/boards/{id}/texture` → the texture image proxied from the manufacturer (browsers need it from our origin for
  WebGL), cached in memory; 404 for an unknown board / a board without an image, 502 when the download fails.
- Data: `MockBoardCatalogRepository` – hard-coded mockup (raw chipboard / MDF / HDF, Kronospan K003 PW, K350 RT, K520 SU,
  Egger H1145 ST10, W1000 ST9, F186 ST9). Firebase will be another `BoardCatalogRepository` implementation.
- Texture images: the grain / pattern runs along the image height.

## Edge band catalog (`EdgeBandCatalog.kt`)
- `GET /api/edge-bands/catalog` → `{ types, bands[] }` – edge bands (okleiny krawędzi) for the frontend picker (type → brand →
  band). A band: `id`, `brand`, `type`, `seriesCode`, `name`, `textureUrl`, `textureSize`, `grainMatters`, `thicknesses`,
  `widths` (roll widths, mm), `color`, `matchingBoardIds` (boards of the same decor).
- `GET /api/edge-bands/{id}/texture` → the decor image (shared `TextureProxy`, same as the board textures).
- Data: `MockEdgeBandCatalogRepository` – Egger ABS (H1145 ST10, W1000 ST9, F186 ST9; 0.4 / 0.8 / 2 mm, rolls 22–43 mm),
  ABS for Kronospan decors (Spander K003 PW, K520 SU; Schilsner K350 RT), PCV black (thickness by hand), white melamine.
