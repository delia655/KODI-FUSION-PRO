# NISAR MANUAL · KODI FUSION PRO 0.5

**Qëllimi:** NISAR kërkohet **drejtpërdrejt brenda GeoTrace / Google Satellite**, por nuk ndizet, shkarkohet dhe as nuk vendoset automatikisht mbi hartë pa një veprim të qartë të përdoruesit.

## Procesi brenda aplikacionit
1. Hap ikonën `GeoTrace`, pastaj `HARTA GOOGLE · SHËNO PIKA`.
2. Prek `NISAR · KËRKO`. Kjo kryen kërkim publik të skenave reale nga **NASA CMR** në 2 km përreth qendrës së zgjedhur. Kërkon të dhënat `NISAR_L2_GCOV_V1`, `NISAR_L2_GCOV_PROVISIONAL_V1` dhe `NISAR_L2_GCOV_BETA_V1`, sipas disponueshmërisë. Nuk kërkon fjalëkalimin NASA në aplikacion.
3. Lista tregon datën reale të skenës, nivelin e përgatitjes së produktit (VALIDATED, PROVISIONAL, BETA), titullin dhe ID-në e NASA CMR.
4. Zgjedh skenën që të intereson. Aplikacioni shfaq konfirmimin: **VENDOS MBI GOOGLE** ose **Mos e shfaq**.
5. Vetëm pas konfirmimit, GeoTrace merr pamjen vizuale **NASA GIBS WMS** për atë datë dhe e vendos mbi Google Satellite, brenda të njëjtës APK. Nëse GIBS nuk ka pikselë për vendin/datën e kërkuar, aplikacioni njofton **NUK KA TË DHËNA**. Rezultati i kërkimit CMR nuk garanton imazh të disponueshëm në GIBS po atë ditë.
6. Butoni **FSHEH / SHFAQ SAR** ndez/fik vetëm shtresën SAR. **HIQ SAR** e heq. Rrëshqitësi ndryshon transparencën; gridi 25×25 apo 50×50 m mbetet i ndarë nga rezolucioni i sensorit.
7. **FILTRAT · PAMJA** ofron Origjinal, Bardhë e Zi, Kontrast dhe Konture. Këto filtra punojnë vetëm mbi PNG-në e vizualizimit, **nuk ndryshojnë vlerat shkencore GCOV/HDF5 dhe nuk identifikojnë metale apo zgavra**.

## Çfarë është dhe çfarë nuk është

- **Real dhe i implementuar:** kërkim **CMR** i metadatave origjinale NISAR, përzgjedhja e datës, kërkesë WMS ndaj NASA GIBS, mbivendosja gjeografike në Google Maps SDK, kontrollet manuale, filtrat e pamjes.
- **Nuk është implementuar ende:** hyrja NASA Earthdata brenda aplikacionit me OAuth të sigurt; shkarkimi i skedarëve të mëdhenj autentikuar GCOV/HDF5; nxjerrja e matricës VV/VH dhe vlerave radiometrike nga HDF5; eksporti XYZ numerik për SURFER me rezolucion origjinal; algoritme klasifikimi të validuara.
- **NASA CMR**: https://cmr.earthdata.nasa.gov/search/site/docs/search/api.html
- **Emrat zyrtarë NISAR GCOV**: https://nisar-docs.asf.alaska.edu/earthdata-search/
- **NASA GIBS vizualizime SAR**: https://hyp3-docs.asf.alaska.edu/nisar-docs/worldview/

## Testimi
Ndërtimi i APK-së kontrollohet automatikisht nga GitHub Actions. Google Satellite kërkon `KODI_GOOGLE_MAPS_KEY` të aktivizuar dhe nënshkrim SHA-1 që përputhet me Google Cloud. Një ndërtim i ri debug mund të ketë SHA-1 tjetër. Mos çinstalo aplikacionin ekzistues pa eksportuar më parë të gjitha shënimet lokale GeoTrace.
