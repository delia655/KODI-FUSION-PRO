# TerraScan V9.1 — audit workspace

Source baseline: user-provided KODI FUSION PRO V9.0 APK (20 Nov 2026).

## Scope
- Review TerraScan HTML/JS/CSS and Android integration.
- Validate Sentinel-1 VV/VH, Sentinel-2, thermal and DEM data provenance.
- Improve 3D visualization without presenting satellite-only estimates as measured subsurface depths.
- Add regression tests for exports and layer controls.
- Preserve existing functionality and work on this branch before merging.

## Security gate
The repository is currently **public**. Do not commit APK files, Google API keys, Earth Engine service-account JSON, tokens, or private user data. Review source assets for embedded credentials before any source upload. Prefer server-side credentials and scoped access.

## Status
Initial audit branch created. No application code modified yet; build and device tests pending.
