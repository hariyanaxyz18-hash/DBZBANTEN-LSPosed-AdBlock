package com.dbzbanten.lsposed.adblock;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "DBZBANTEN-LSPosed";
    private static final Set<String> BLOCKED = new HashSet<>();
    private static boolean loaded;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        // LSPosed scope is the package selector. Only apps selected in LSPosed are hooked.
        if (lpparam.packageName.equals("android")) return;

        loadDomains(lpparam.classLoader);

        try {
            XposedHelpers.findAndHookMethod(
                    InetAddress.class,
                    "getAllByName",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String host = String.valueOf(param.args[0]);
                            if (!isBlocked(host)) return;

                            // Return a non-routable address so the ad endpoint cannot be reached.
                            param.setResult(new InetAddress[]{
                                    InetAddress.getByAddress(host, new byte[]{0, 0, 0, 0})
                            });
                            Log.i(TAG, "Blocked DNS lookup: " + host);
                        }
                    });

            XposedHelpers.findAndHookMethod(
                    InetAddress.class,
                    "getByName",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String host = String.valueOf(param.args[0]);
                            if (!isBlocked(host)) return;

                            param.setResult(InetAddress.getByAddress(
                                    host, new byte[]{0, 0, 0, 0}
                            ));
                            Log.i(TAG, "Blocked host: " + host);
                        }
                    });

            Log.i(TAG, "Loaded for " + lpparam.packageName);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": hook failed for " + lpparam.packageName + " " + t);
        }
    }

    private static synchronized void loadDomains(ClassLoader ignored) {
        if (loaded) return;
        loaded = true;

        try (InputStream in = MainHook.class.getClassLoader()
                .getResourceAsStream("ad_domains.txt")) {
            if (in == null) {
                Log.e(TAG, "ad_domains.txt not found");
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase(Locale.US);
                if (!line.isEmpty() && !line.startsWith("#")) {
                    BLOCKED.add(line);
                }
            }
            Log.i(TAG, "Loaded " + BLOCKED.size() + " blocked domains");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": cannot load domains " + t);
        }
    }

    private static boolean isBlocked(String host) {
        if (host == null) return false;
        host = host.trim().toLowerCase(Locale.US);
        if (host.endsWith(".")) host = host.substring(0, host.length() - 1);

        for (String domain : BLOCKED) {
            if (host.equals(domain) || host.endsWith("." + domain)) {
                return true;
            }
        }
        return false;
    }
}
