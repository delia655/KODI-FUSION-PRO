# Google Satellite + NASA SAR brenda KODI FUSION PRO

**Version prototipi: 0.4 – në zhvillim, jo V6.2.**

## Funksionimi

Nga ikona **GeoTrace** hapet moduli GeoTrace. Butoni `HARTA GOOGLE · SHËNO PIKA` hap `GoogleSatelliteActivity` **brenda të njëjtës APK**, jo aplikacionin e jashtëm Google Maps.

Në të njëjtin ekran ka katër kontrolle:
- **SAR · SENTINEL-1** – merr një raster real vizualizimi NASA GIBS / OPERA RTC-S1 (pamje ditore);
- **SAR · NISAR** – merr një raster real vizualizimi NASA GIBS / NISAR L-band GCOV (pamje ditore);
- **DATA SAR** – ndryshon datën e kërkuar me kalendar;
- **HIQ SAR** – largon shtresën radar dhe lë Google Satellite të dukshme.

Rrëshqitësi **Dukshmëria SAR** ndryshon transparencën mbi hartën Google. Butonat e gridit 25×25 m dhe 50×50 m funksionojnë veçmas. Krijimi dhe eksporti i pikave GeoTrace është lokal.

Kërkesa HTTPS drejt NASA GIBS WMS GetMap përdor `EPSG:4326`, me të njëjtin `LatLngBounds` gjeografik që përdor `GoogleMap.addGroundOverlay`. Bëhet për një zonë rreth 1×1 km me qendër te gridi, pa pretenduar rezolucion 25 cm apo 1 m nga sateliti.

**Burimet e sakta**:
- Sentinel-1: `OPERA_L2_Radiometric_Terrain_Corrected_SAR_Sentinel-1`
- NISAR L-band: `NISAR_L2_Geocoded_Polarimetric_Covariance`
- WMS: `https://gibs.earthdata.nasa.gov/wms/epsg4326/best/wms.cgi`
- Dokumentacion: https://nasa-gibs.github.io/gibs-api-docs/access-basics/
- GIBS + NISAR: https://hyp3-docs.asf.alaska.edu/nisar-docs/worldview/
- GIBS + Sentinel-1 OPERA: https://asf.alaska.edu/notices/opera-rtc-s1-products-are-now-available-in-nasa-worldview/

Këto janë **vizualizime satelitore të datuara**, jo vlerat origjinale VV/VH, GCOV HDF5, GPR apo ERT. Për analizë numerike duhet shkarkuar produkti origjinal nga ASF/NASA dhe përpunuar në format të përshtatshëm; për disa shkarkime duhet NASA Earthdata Login. NISAR S-band i ISRO nuk është i njëjtë me këtë burim L-band në GIBS.

## Mbulimi dhe kontrolli

- NASA nuk siguron marrje të dhënash për çdo pikë në çdo datë; nëse nuk ka pikselë, aplikacioni duhet të raportojë mungesë të dhënash.
- Kur ndryshon qendra e gridit, shtresa e vjetër hiqet që të mos ngatërrohet me zonën e re.
- Kur një shkarkim i ri dështon, shtresa e mëparshme nuk mbahet si rezultat i datës së re.
- Në ekran tregohet burimi dhe data e kërkuar, jo një përqindje e sajuar besueshmërie.
- MOS nxirr thellësi, metale, dhoma apo tunele nga vizualizime SAR.

## Çfarë mungon që Google të funksionojë në një telefon konkret

Ky projekt përdor **Maps SDK for Android**. Duhet të jetë aktiv në Google Cloud dhe të konfigurohet me faturim sipas kushteve të Google. Harta nuk shfaqet pa një **API key të vlefshëm dhe të kufizuar**.

Në repository publik kodi **nuk përmban API key**. Workflow i GitHub Actions lexon secret-in `KODI_GOOGLE_MAPS_KEY` nga Settings → Secrets and variables → Actions. Ky secret duhet të krijohet nga pronari i repository-t dhe nuk mund të vendoset nga lidhja jonë GitHub.

**Kufizimi në Google Cloud duhet të jetë:**
- Android package: `com.kodi.fusion.mapsdemo`
- API restriction: `Maps SDK for Android`
- Certificate SHA-1: SHA-1 e APK-së reale të nënshkruar që instalohet, JO SHA-1 e APK-së origjinale V6.2.

GitHub Actions aktualisht ndërton debug APK me certifikatë testimi që mund të ndryshojë mes ndërtimesh. Për një konfigurim të qëndrueshëm duhet keystore i përhershëm privat dhe nënshkrim i kontrolluar në CI. **Mos vendos keystore, çelësa, tokena ose materiale klientësh në repository publik.** Çelësi i publikuar më parë në bisedë duhet të rrotullohet dhe të mos përdoret në build të shpërndarë.

## Statusi i testimit

Kodi është kompiluar në GitHub Actions. Ende **nuk janë verifikuar në një telefon real** renderimi i Google Maps dhe marrja e imazhit NASA WMS për një zonë dhe datë të caktuar, pasi kërkohet çelësi privat përkatës. Ngarkimi WMS dhe data specifike mund të kthejnë dështim ose mungesë mbulimi. Këto janë veçori prove; **nuk është APK-ja e plotë Kodi Fusion Pro V6.2** dhe nuk duhet të çinstalohet aplikacioni ekzistues.
