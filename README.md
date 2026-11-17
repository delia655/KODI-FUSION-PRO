# KODI FUSION PRO

Projekt publik zhvillimi për KODI FUSION PRO dhe integrimin e Google Satellite në Android.

**Statusi:** në zhvillim. Projekti aktual përmban prototipin e integrimit; nuk është ende APK përfundimtare që përditëson V6.2.

## Synimet
- Google Maps SDK for Android: hartë satelitore dhe hibride, me koordinata WGS84.
- Grid gjeometrik 25 × 25 m dhe 50 × 50 m mbi hartë.
- Integrimi i moduleve ekzistuese SURFER, analiza 2D/3D, skaneri dhe arkivat personale, pasi të kontrollohet kodi Android.
- Shfaqja e qartë e dallimit mes matjeve reale, përpunimit numerik dhe demonstrimeve.

## Privatësia dhe siguria
Ky repository nuk duhet të përmbajë çelësa Google API, tokena, certifikata private nënshkrimi, skanime klientësh ose dorëshkrimin e librit **Kodi i Thesarëve**. Mos publikoni kredenciale në commits apo screenshots.

**Kujdes:** imazhet satelitore Google nuk janë matje nëntokësore; për analiza kërkohen të dhëna burimore të përshtatshme (si Sentinel ose DEM) dhe verifikim në terren.

## Hapi tjetër
Integrimi i projektit demonstrues Android dhe testimi në Android Studio. Kodi i plotë burimor i V6.2 nuk është rikuperuar nga APK-ja.

## NISAR (L-band / S-band)
Prototipi Android përfshin butonin **NISAR** për të hapur burimet zyrtare [NASA/ASF Vertex](https://vertex-plus.asf.alaska.edu/) dhe [ISRO Bhoonidhi](https://bhoonidhi.nrsc.gov.in/NISAR/), me koordinatat WGS84 të qendrës së gridit. Kjo është qasje te katalogët e skenave **jo** një shtresë NISAR e përpunuar brenda hartës.

Shiko [planin e integrimit NISAR](docs/NISAR_INTEGRIMI_SHQIP.md) për hapat HDF5/GeoTIFF dhe kufijtë shkencorë.

## SURFER · ANALIZA 6D (version demonstrues 0.2)
Ekrani kryesor i **APK-së së provës** tani ka gjashtë karta në dy rreshta (si pamja e referencës):
- `SATELIT` — Google Satellite (kërkon Maps API key të kufizuar për nënshkrimin e këtij APK).
- `HEATMAP` — import lokal i matricës numerike CSV/TXT dhe pamje paraprake me ngjyra.
- `SAR VV/VH` — panel paraprak për të dhëna radar të përpunuara (jo shkarkim automatik i skenave).
- `SHTRESAT` — panel i të dhënave të importuara; bashkërenditja gjeohapësinore është në zhvillim.
- `PROFIL 3D` — panel paraprak numerik 2D; **nuk paraqet ende volum 3D** ose thellësi të matura.
- `TERREN 3D` — panel paraprak numerik 2D; model real 3D DEM është fazë e ardhshme.

Ka edhe `KRAHASO 6 PAMJET`, zgjedhje gridi 25/50 m dhe `NISAR L/S`.
Të gjitha panelet që nuk kanë skedar real tregojnë qartë **NUK KA TË DHËNA**, jo anomali të shpikura.

### Prova në Android
GitHub Actions krijon APK testimi të veçantë me package `com.kodi.fusion.mapsdemo`. Kjo nuk është përditësim i V6.2. Për shkak se GitHub Actions gjeneron certifikatë debug të re, një ndërtim i ardhshëm mund të mos instalohet sipër një ndërtimi më të vjetër; për azhurnime të qëndrueshme duhet një certifikatë private e përhershme.

[GitHub Actions](https://github.com/delia655/KODI-FUSION-PRO/actions) — shiko ndërtimet dhe skedarët e testimit. Data nuk dërgohet në server nga importi lokal CSV/TXT.

## GeoTrace (0.3 — modul i ndarë nga SURFER)
- Dy ikona hyrjeje në Android: **KODI FUSION PRO** dhe **GeoTrace**.
- KODI FUSION PRO ka dy karta kryesore: **SURFER 6D** dhe **GeoTrace**.
- GeoTrace ruan lokalisht pikat WGS84 (1, 2, 3...), shënimet, kategorinë dhe gjurmën manuale.
- Raporti i pikës tregon burimin, shpjegimin, statusin e kontrollit në terren, dhe e deklaron qartë që nuk janë llogaritur besueshmëri ose thellësi.
- Kur Maps SDK është i konfiguruar, shënimet vizatohen direkt mbi Google Satellite; kur nuk është i konfiguruar, mund të shtohen pika me koordinata pa hartë.
- GeoJSON eksporti kryhet me veprim të qartë të përdoruesit, jo automatikisht.
- **Numrat në GeoTrace janë rend kronologjik i shënimeve manuale; nuk tregojnë renditje anomalish si te VeldScan.**
- Versioni 0.3 mbetet prototip i veçantë, jo përditësim i garantuar i V6.2.

Shiko [dokumentimin GeoTrace](docs/GEOTRACE_SHQIP.md).

## Google Satellite + SAR nga NASA GIBS (v0.4)
Në GeoTrace, `HARTA GOOGLE · SHËNO PIKA` hap hartën native **brenda APK-së**. Butonat `SAR · SENTINEL-1` dhe `SAR · NISAR` marrin pamje të gjeoreferencuara të NASA GIBS WMS dhe i shtojnë si `GroundOverlay` mbi Google Satellite. Ka zgjedhje të datës, transparencës dhe grid 25/50 m; pamja SAR merret për një zonë rreth 1 km rreth qendrës. Nuk trajtohet si zbulim automatik nëntokësor.

**Verifikime të kryera:** CI ka kompiluar APK-në dhe i ka marrë me sukses imazhet PNG nga endpoint-et zyrtare NASA për të dy burimet. **Pa testim në telefon me Google Maps API key të aktivizuar nuk është verifikuar vizualisht mbivendosja.** Nuk ka të dhëna SAR të garantuara për çdo zonë/datё. Produkti mbetet aplikacion demonstrues i ndarë nga V6.2.

**Për hartën Google** duhet secret-i privat `KODI_GOOGLE_MAPS_KEY`, me kufizim Android për `com.kodi.fusion.mapsdemo` dhe SHA-1 të certifikatës së APK-së. Për SHA-1 të qëndrueshme, CI mbështet secret-in opsional `KODI_DEBUG_KEYSTORE_B64`, por nuk ka ende të konfiguruar një keystore të përhershëm.

Shiko [udhëzimet Google + SAR](docs/GOOGLE_SATELLITE_SAR_SHQIP.md). Mos publiko kredenciale ose skedarë privatë.

## NISAR / Sentinel-1 manual në Google Satellite (v0.6)

1. Google Satellite hapet **brenda aplikacionit** si hartë bazë dhe **SAR është i fshehur** kur hapet.
2. `KËRKO NISAR` kërkon metadata të skenave `L2 GCOV` në NASA CMR; mund të zgjedhësh skenën e datën (nuk shkarkohet HDF5 në këtë version).
3. `MERR PAMJEN` shkarkon vetëm pamjen vizuale NASA GIBS WMS dhe e mban përkohësisht në memorien e telefonit. **Nuk shfaqet ende mbi Google**.
4. `FILTRA PAMORË` aplikon Origjinal, Bardhë e zi, Kontrast ose Konture mbi pikselët PNG të marrë. Nuk janë filtra shkencorë të vlerave radar.
5. `SHFAQ SAR` është veprimi i **vetëm** që e vendos shtresën mbi Google; `FSHEH SAR` e fsheh pa hequr pamjen nga memoria dhe `HIQ SAR` e fshin nga memoria.
6. `Dukshmëria SAR` kontrollon transparencën dhe gridi 25×25/50×50 m mbetet gjeometrik, jo rezolucion sensorik.

Produkti **L-band NISAR GCOV/HDF5** kërkon autentikim NASA Earthdata për shkarkimin origjinal dhe përpunim shtesë të vlerave; kjo pjesë ende nuk ekziston në prototip. Nuk premtohet mbulim në çdo ditë apo koordinatë. Skenat dhe pikselët nuk janë verifikuar në telefon për çdo zonë.
