# Si të rregullosh hartën Google bosh në GeoTrace / KODI FUSION PRO

## Problemi i konfirmuar nga fotografia e telefonit

Në ekranin **KODI FUSION PRO · GOOGLE SATELLITE** shihet logoja Google por zona e hartës është bosh. Aplikacioni dhe butonat hapen. Ky është problem i **ngarkimit/autorizimit të Google Maps SDK**, jo një mungesë e të dhënave NASA.

### Të dhënat reale të APK-së së instaluar për këtë test

- Versioni: `0.4.1-google-sar-keytest`
- Android application ID: `com.kodi.fusion.mapsdemo`
- SHA-1 e certifikatës **të APK-së së verifikuar në datën 17 nëntor 2026**:
  `04:39:7C:00:28:DD:C9:83:B3:3E:67:CA:50:54:AE:17:B1:5B:92:B0`
- Ky është nënshkrim **debug i përkohshëm**. Kur rindërtohet me GitHub Actions pa `KODI_DEBUG_KEYSTORE_B64`, mund të ndryshojë. Mos supozo se SHA-1 përdoret për çdo APK pasuese.
- GitHub Actions `KODI_GOOGLE_MAPS_KEY` tashmë është regjistruar nga pronari. Workflow e përfshiu çelësin në APK; **nuk do të thotë që Google e ka autorizuar për certifikatën e saj**.

### Rregullimi në Google Cloud (jo në GitHub)

1. Hape https://console.cloud.google.com/apis/credentials dhe zgjidh **të njëjtin projekt Google Cloud** që i përket çelësit të ruajtur në GitHub.
2. Hap API key-n e përdorur për `KODI_GOOGLE_MAPS_KEY`. Te **Application restrictions** zgjidh **Android apps**.
3. Shtyp **Add Android app** dhe jep paketën `com.kodi.fusion.mapsdemo` dhe SHA-1 të mësipërme. Ruaj ndryshimet. Mos publiko API key-n.
4. Te **API restrictions**, lejo **Maps SDK for Android**. Kontakto API Library për t'u siguruar që është **Enabled**.
5. Kontrollo **Billing** te i njëjti projekt. Google Maps SDK for Android kërkon një llogari faturimi aktive; mund të ketë kuota falas, por faturimi duhet të jetë i lidhur.
6. Mbylle plotësisht GeoTrace dhe rihape pas disa minutash. Mos e riinstalo përpara se të marrësh kopje të shënimeve personale.

**Referencë zyrtare e Google:** https://developers.google.com/maps/faq ("My app only shows blank grey tiles"), https://developers.google.com/maps/documentation/android-sdk/start.

### SAR: problem krejt tjetër

Për qendrën WGS84 të dukshme në fotografi, aplikacioni raporton `Nuk ka pikselë SAR në këtë zonë dhe datë`. Kjo vjen nga një përgjigje **transparente e NASA GIBS WMS** për 1 km përreth qendrës dhe datën e zgjedhur.

- Provo **DATA SAR** dhe ndrysho datën. Vizualizimet ditore nuk kanë mbulim për çdo ditë/pikë.
- **NASA WMS po funksionon**: GitHub Actions më parë verifikoi përgjigje PNG për emrat e dy shtresave NISAR L-band dhe Sentinel-1 OPERA.
- Nuk është provuar ende se në zonën konkrete ekziston një produkt i vlefshëm në ditën e zgjedhur. Mos e interpreto mungesën e pixeleve si mungesë anomalish/strukturash.

### Diagnostika nëse harta Google mbetet bosh

Nëse konfigurimi është i saktë dhe harta është sërish bosh, kontrollo autorizimin në `adb logcat -e "Google Maps Android API"` nga një kompjuter me Android SDK; gabimi më tipik është `Authorization failure`. Kërko ndihmë me screenshot-in e **restrictions**, **Maps SDK Enabled**, **Billing**, por mos shfaq sekretin.
