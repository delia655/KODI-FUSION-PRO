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
