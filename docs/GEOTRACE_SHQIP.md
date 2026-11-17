# GeoTrace — modul i veçantë nga SURFER

GeoTrace nuk është pjesë e menyve SURFER 6D. Në Android ekziston si **kartë e veçantë në KODI FUSION PRO** dhe si **ikonë GeoTrace në ekranin e aplikacioneve**. Të dyja ikonat hapin të njëjtin modul dhe ndajnë të njëjtat shënime lokale.

## Parimi i pikave të numëruara
VeldScan shfaq numrin 1 si kandidatin me prioritetin më të lartë, 2 si kandidatin pasues etj. Dokumentimi i tyre: https://veldscan.com/?lang=sq

**GeoTrace 0.3 nuk bën analizë automatike satelitore.** Numri 1, 2, 3... është vetëm rendi i vendosjes manuale të pikës në hartë ose me koordinata. Nuk i kopjojmë algoritmet, përqindjet apo raportet e VeldScan.

## Çfarë shkruan kur hap pikën 1, pikën 2, ...
- Emri: `GeoTrace · Raporti i pikës N`
- Koordinata: WGS84 latitude / longitude
- Burimi: `Pikë e shënuar nga përdoruesi`
- Kategoria: zgjedhet manualisht (`Vëzhgim manual`, `Gjurmë sipërfaqësore`, `Ndryshim bimësie`, `Ndryshim relievi`, `Strukturë e dukshme`, `Pikë reference`, `Për kontroll në terren`)
- Shpjegimi: shënimi i futur nga përdoruesi
- Shtresat satelitore: `Nuk ka analizë të bashkëngjitur`
- Besueshmëria: `Nuk është llogaritur`
- Thellësia: `Nuk është matur`
- Statusi: `JO I VERIFIKUAR NË TERREN`
- Plan kontrolli: dokumento fotografinë, mjedisin dhe matjet reale përpara interpretimit.

Shënimet mbahen në `SharedPreferences` brenda aplikacionit dhe nuk dërgohen automatikisht te administratori ose serveri. `GeoTraceOverlay` mban pika të numëruara dhe vijën që lidh pikat në Google Maps SDK. Eksporti GeoJSON nis **vetëm kur përdoruesi zgjedh ndarjen**.

## Google Satellite dhe API key
Kjo është një APK prove me paketën `com.kodi.fusion.mapsdemo`, jo përditësim i V6.2. Harta Google kërkon `Maps SDK for Android`, çelës të kufizuar për këtë paketë dhe SHA-1 të saktë të certifikatës së nënshkrimit. Kur mungon çelësi, GeoTrace vazhdon të lejojë krijimin e pikave manualisht me koordinata pa hartë.

## Çfarë mungon për funksionalitetin VeldScan
Skema automatike e poligoneve, leximi i produkteve reale Sentinel/NISAR, shfaqja e heatmap nga matje, renditja shkencore e kandidatëve, eksport PDF/KML dhe navigimi GPS me gjurmim të verifikuar janë funksione të veçanta për t'u zhvilluar. Mos paraqit numrat manualë si sinjale të verifikuara ose si zbulim metali apo zgavre.
