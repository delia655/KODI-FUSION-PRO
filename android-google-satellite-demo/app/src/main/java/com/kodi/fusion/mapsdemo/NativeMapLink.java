package com.kodi.fusion.mapsdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * WebView navigation bridge. Call ONLY from a trusted local WebViewClient.
 * Do not expose this to remote websites or untrusted JavaScript.
 */
public final class NativeMapLink {
    private NativeMapLink() {}

    /** Returns true when the link was consumed, including malformed map commands. */
    public static boolean open(Context context, Uri uri) {
        if (uri == null || !"kodi-maps".equals(uri.getScheme())
                || !"satellite".equals(uri.getHost())) return false;
        try {
            double lat = Double.parseDouble(uri.getQueryParameter("lat"));
            double lon = Double.parseDouble(uri.getQueryParameter("lon"));
            int meters = Integer.parseInt(uri.getQueryParameter("size"));
            if (!Double.isFinite(lat) || !Double.isFinite(lon)
                || Math.abs(lat) > 85 || Math.abs(lon) > 180) return true;

            Intent intent = new Intent(context, GoogleSatelliteActivity.class);
            intent.putExtra(GoogleSatelliteActivity.EXTRA_LAT, lat);
            intent.putExtra(GoogleSatelliteActivity.EXTRA_LON, lon);
            intent.putExtra(GoogleSatelliteActivity.EXTRA_GRID, meters == 50 ? 50 : 25);
            context.startActivity(intent);
        } catch (RuntimeException ignored) {
            // Ignore invalid local map commands; never navigate to an unknown scheme.
        }
        return true;
    }
}
