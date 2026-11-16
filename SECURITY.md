# KODI FUSION PRO — Security

- Do not commit Google Maps API keys, access tokens, passwords, certificate/private signing keys, customer data, private scans, or the **Kodi i Thesarëve** book manuscript.
- Keep your Maps API key in \`android-google-satellite-demo/local.properties\` (excluded from Git by .gitignore).
- Restrict the key to the **Maps SDK for Android**, the prototype's actual Android package name **and its signing certificate SHA-1**.
- This public repository is not a production-ready Android client and has not passed a security assessment.
- The Android map overlay is a geometric grid, **not subsurface sensing**. Use measured datasets for field analysis and verify independently.
- Google Maps imagery is displayed using the supported SDK; do not scrape/download tiles to derive unsupported underground claims.
