# Lidhja e Google Satellite me WebView-in e KODI V6.2

Moduli i aplikacionit **demonstrues** përmban:
- \`GoogleSatelliteActivity\` — Google Maps SDK (Satelit/Hibrid), pikë WGS84 dhe grid 25/50 m.
- \`NativeMapLink\` — lidhja nga faqe HTML lokale te aktiviteti Android.

**Për t'u integruar në V6.2 duhet kodi origjinal i WebView Activity.** Kjo repository nuk përmban ende atë kod ose të gjitha modulet e vjetra të aplikacionit.

## Komanda lokale e hapjes

Në një faqe HTML të besuar, e ngarkuar si aset lokal i APK-së:

\`\`\`js
const lat = 42.312015, lon = 21.119746, side = 25;
window.location.href =
  'kodi-maps://satellite?lat=' + encodeURIComponent(lat) +
  '&lon=' + encodeURIComponent(lon) + '&size=' + side;
\`\`\`

Në **WebViewClient ekzistues**, pasi ke verifikuar që navigimi është iniciuar nga faqja lokale e besuar, trajto skemën e veçantë:

\`\`\`java
@Override
public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
    String originPage = view.getUrl();
    boolean trusted = originPage != null &&
        originPage.startsWith("file:///android_asset/");
    if (trusted && req.isForMainFrame() &&
        NativeMapLink.open(view.getContext(), req.getUrl())) {
        return true;
    }
    return super.shouldOverrideUrlLoading(view, req);
}
\`\`\`

**Siguri:** Mos lejo që faqet nga interneti të thërrasin \`NativeMapLink\`. Mos aktivizo akses të pakufizuar te skedarët lokalë në WebView. Mos u mbështet te URL-ja e dukshme për të provuar origjinën e një iframe; kontrollo gjithmonë main frame.

## Google Cloud

Ky demonstrim përdor:
- Paketa Android: \`com.kodi.fusion.mapsdemo\`.
- API: \`Maps SDK for Android\`.
- Çelësi privat vendoset si secret \`KODI_GOOGLE_MAPS_KEY\` në GitHub Actions dhe nuk futet në kod.
- SHA-1 e APK-së demonstrative **ndryshon sipas çelësit të nënshkrimit të ndërtimit**; nuk është SHA-1 e V6.2. Për përdorim afatgjatë, duhet certifikatë e qëndrueshme, e mbajtur private.

Pa këtë konfigurim, aplikacioni tregon një mesazh të qartë dhe mund të hapë Google Maps veçmas.

Modelet/gridet nuk janë matje fizike të nëntokës.
