# NISAR në KODI FUSION PRO — Integrimi i të dhënave reale

## Statusi aktual (16.11.2026)
Në **prototipin Android** është shtuar butoni `NISAR · L-band / S-band` në ekranin Google Satellite. Ai:
- tregon koordinatat WGS84 dhe përmasat e gridit të zgjedhur;
- hap kërkimin zyrtar `ASF Vertex` për NISAR L-band;
- hap portalin zyrtar `ISRO Bhoonidhi` për NISAR S-band;
- lejon kopjimin e koordinatave për të kërkuar skenat e disponueshme.

**Nuk shkarkon ende produkte HDF5 ose krijon një shtresë raster NISAR në hartën Android.** Përdoruesi duhet ta zgjedhë skenën dhe të identifikohet te Earthdata (ASF) ose ISRO sipas kërkesave të portalit.

## Burimet e verifikuara
- NASA NISAR: https://science.nasa.gov/mission/nisar/
- ASF NISAR availability (L-band): https://nisar-docs.asf.alaska.edu/availability-overview/
- ASF Vertex: https://vertex-plus.asf.alaska.edu/
- ISRO Bhoonidhi (S-band): https://bhoonidhi.nrsc.gov.in/NISAR/

NISAR L-band: 24 cm valë; S-band: 9.4 cm valë. Produktet `VALIDATED` L-band u publikuan më 6 nëntor 2026 dhe mbulojnë marrjet prej 21 tetorit 2026. Regjistrimet më të hershme mund të jenë `PROVISIONAL` ose `BETA`; këto nuk duhen përzier pa verifikuar ndryshimet e procesimit.

## Hapat për shtresë REALISHT funksionale në SURFER
1. Kërko skenat L-band në ASF sipas poligonit AOI dhe intervalit kohor.
2. Përzgjidh produktin gjeokoduar të përshtatshëm (p.sh. `L2_GCOV`) dhe identifiko polarizimet.
3. Hyr në NASA Earthdata Login dhe shkarko HDF5 te klienti ose në shërbim të autorizuar.
4. Lexo HDF5/NetCDF me bibliotekë që mbështet dimensionet e produktit NISAR.
5. Nxirr raster real gjeoreferencuar (GeoTIFF/COG, kur lejohet) dhe gjenero versione të vogla për shfaqje në telefon.
6. Ruaj metadata: data, orbitë, polarizim, maturitet VALIDATED/PROVISIONAL, numër produkti, rezolucion dhe burim.
7. Krahaso të njëjtin AOI me Sentinel-1, DEM dhe të dhëna terreni në ekranin 6/7 panele.
8. Eksporto raster ose seri pikash sipas kërkesës së Surfer; mos shpik vlera Z apo thellësi.

**Vërejtje shkencore:** NISAR nuk është GPR dhe imazhet L/S-band nuk konfirmojnë automatikisht metale, tunele apo zgavra në 5–30 m thellësi. Një grid 25×25 m nuk e rrit rezolucionin origjinal të skenës.

## Privatësia
Mos ruaj përdorues/fjalëkalim Earthdata, API keys, tokena NASA ose fjalëkalime GitHub në repository publik. Secili klient mban arkivën e vet lokale; importimi është me zgjedhje dhe eksporti nuk dërgohet automatikisht te administratori.
