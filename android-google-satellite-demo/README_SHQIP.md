# Google Satellite — Android demonstrim

Ky është **prototip i veçantë** i Maps SDK for Android. Nuk është ende zëvendësim apo përditësim i APK-së së KODI FUSION PRO V6.2.

## Hapja në Android Studio
1. Hap dosjen \`android-google-satellite-demo\` në Android Studio me Android SDK 35, JDK 17 dhe Gradle.
2. Krijo dosjen/skedarin \`local.properties\` në rrënjë të kësaj dosjeje me \`sdk.dir=...\` dhe \`MAPS_API_KEY=...\`. **Mos e publiko çelësin**.
3. Në Google Cloud, aktivizo \`Maps SDK for Android\` dhe kufizo çelësin për package name \`com.kodi.fusion.mapsdemo\` dhe SHA-1 të certifikatës që nënshkruan **këtë prototip**, jo të APK-së V6.2.
4. Ndërtoje dhe instaloje në telefon Android me Google Play Services. Kontrollo nëse ngarkohet harta.

## Funksionet
- Google Satellite brenda aplikacionit Android
- Prek hartën për të vendosur pikën WGS84
- Grid gjeometrik 25×25 m / 50×50 m
- Kalim Satellite / Hybrid
- Eksport i poligonit GeoJSON përmes Android Share

Këto gride nuk janë skanime satelitore të nëntokës. Pa skedarë matjesh reale nuk japin rezultate nëntokësore.

**Kufizime:** Nuk përmban ende WebView-in e V6.2, SURFER, sensorët apo migrimin e arkivës. Duhet testuar dhe integruar me projektin e plotë Android; skanuesi ekzistues nuk ndryshohet.
