package com.xuqiqiang.uikit.requester.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.xuqiqiang.uikit.utils.ArrayUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by xuqiqiang on 2019/05/17.
 */
public class ShortcutUtils {

    //region hasShortcut
    public static boolean hasShortcut(Context context, String id, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return hasShortcutBelowO(context, name);
        } else {
            return hasShortcutForO(context, id, name);
        }
    }

    @SuppressLint("WrongConstant")
    public static String getAuthorityFromPermission(Context context) {
        String authority = "com.android.launcher.settings";
        // 先得到默认的Launcher
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager mPackageManager = context.getPackageManager();
        ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
        if (resolveInfo == null) {
            return authority;
        }
        List<ProviderInfo> info = mPackageManager.queryContentProviders(resolveInfo.activityInfo.packageName,
                resolveInfo.activityInfo.applicationInfo.uid, PackageManager.GET_PROVIDERS);
        if (info != null) {
            for (int j = 0; j < info.size(); j++) {
                ProviderInfo provider = info.get(j);
                if (provider.readPermission == null) {
                    continue;
                }
                if (Pattern.matches(".*launcher.*READ_SETTINGS", provider.readPermission)) {
                    return provider.authority;
                }
            }
        }
        return authority;
    }

    public static boolean hasShortcutBelowO(Context context, String name) {
        boolean isInstallShortcut = false;
        final ContentResolver cr = context.getContentResolver();
        final String AUTHORITY = getAuthorityFromPermission(context);
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/favorites?notify=true");

        Cursor c = cr.query(CONTENT_URI,
                new String[]{"title", "iconResource"}, "title=?",
                new String[]{name}, null);

        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        if (c != null) {
            c.close();
        }
        return isInstallShortcut;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean hasShortcutForO(Context context, String id, String name) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
        List<ShortcutInfo> list = shortcutManager.getPinnedShortcuts();
        for (ShortcutInfo shortcutInfo : list) {
            if (id != null) {
                if (id.equals(shortcutInfo.getId()) && shortcutInfo.isEnabled()) return true;
            } else {
                if (TextUtils.equals(shortcutInfo.getShortLabel(), name) && shortcutInfo.isEnabled()) return true;
            }
        }
        return false;
    }
    //endregion

    //region getShortcutCount
    public static int getShortcutCount(Context context, String id, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return 0;
        } else {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            return shortcutManager.getPinnedShortcuts().size();
        }
    }
    //endregion

    //region createShortcut
    public static void createShortcut(Context context, String id, String name,
                                      Class<?> className, int iconId) {
        createShortcut(context, id, name, className, iconId, null);
    }

    public static void createShortcut(Context context, String id, String name,
                                      Class<?> className, int iconId, Bundle data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            createShortcutBelowO(context, name, className, iconId, data);
        } else {
            createShortcutForO(context, id, name, className, iconId, data);
        }
    }

    public static void createShortcutBelowO(Context context, String name,
                                            Class<?> className, int iconId, Bundle data) {
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");

        // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        shortcut.putExtra("duplicate", false); // 不允许重复创建

        Intent shortcutIntent = new Intent(context, className);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        shortcutIntent.putExtra("url", "https://news.baidu.com/");
        if (data != null) shortcutIntent.putExtras(data);
//        if (extra != null) {
//            for (Map.Entry<String, String> entry : extra.entrySet()) {
//                shortcutIntent.putExtra(entry.getKey(), entry.getValue());
//            }
//        }
//        shortcutIntent.setPackage("com.snailstdio.software.p2psearcher");
//        shortcutIntent.setClassName(context, className);

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        // 快捷方式的图标
        ShortcutIconResource iconRes = ShortcutIconResource.fromContext(
                context, iconId);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        context.sendBroadcast(shortcut);
        Toast.makeText(context, "已创建“" + name + "”桌面快捷方式", Toast.LENGTH_SHORT)
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createShortcutForO(Context context, String id, String name,
                                          Class<?> className, int iconId, Bundle data) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
//        if (true) {
//            List<ShortcutInfo> list = shortcutManager.getPinnedShortcuts();
//            Logger.i("_test_j_ getPinnedShortcuts:" + list);
//            Logger.i("_test_j_ getPinnedShortcuts isPinned:" + ArrayUtils.convert(list, new ArrayUtils.Converter<ShortcutInfo>() {
//                @Override
//                public Object convert(ShortcutInfo shortcutInfo) {
//                    return shortcutInfo.isPinned();
//                }
//            }));
//            Logger.i("_test_j_ getMaxShortcutCountPerActivity:" + shortcutManager.getMaxShortcutCountPerActivity());
//            shortcutManager.disableShortcuts(ArrayUtils.createList("shortcutId_5021"), "fsfsas");
////            shortcutManager.removeDynamicShortcuts();
////            shortcutManager.enableShortcuts();
//            return;
//        }
//        int shortcutId = -1;

        if (shortcutManager.isRequestPinShortcutSupported()) {
            Intent shortcutInfoIntent = new Intent(context, className);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW); //action必须设置，不然报错
            if (data != null) shortcutInfoIntent.putExtras(data);
//            if (extra != null) {
//                for (Map.Entry<String, String> entry : extra.entrySet()) {
//                    shortcutInfoIntent.putExtra(entry.getKey(), entry.getValue());
//                }
//            }

//            shortcutId = (int) (Math.random() * 10000);
            ShortcutInfo info = new ShortcutInfo.Builder(context, id)//"shortcutId_" + shortcutId)
                    .setIcon(Icon.createWithResource(context, iconId))
                    .setShortLabel(name)
                    .setIntent(shortcutInfoIntent)
                    .build();

            //当添加快捷方式的确认弹框弹出来时，将被回调
            PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context,
                    0, new Intent(context, ShortcutReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
//            Logger.i("_test_j_ shortcutId" + shortcutId);
            shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
        }
    }
    //endregion

    //region removeShortcut
    public static void removeShortcut(Context context, String id, String name, Class<?> className) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            removeShortcutBelowO(context, name, className);
        } else {
            removeShortcutForO(context, id);
        }
    }

    public static void removeShortcutBelowO(Context context, String name, Class<?> className) {
        // remove shortcut的方法在小米系统上不管用，在三星上可以移除
        Intent intent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        // 名字
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        // 设置关联程序
        Intent launcherIntent = new Intent(context, className).setAction(Intent.ACTION_MAIN);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        // 发送广播
        context.sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void removeShortcutForO(Context context, String id) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
        shortcutManager.disableShortcuts(ArrayUtils.createList(id), "快捷方式已失效，请手动移除");
    }
    //endregion

    //region DynamicShortcut
    @RequiresApi(api = 25)
    public static void addDynamicShortcut(Context context, String id, String name,
                                          String disabledMessage,
                                          String action,
                                          Class<?> className, int iconId, Bundle data) {
        if (Build.VERSION.SDK_INT >= 25) {
            ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, id)
                    .setShortLabel(name)
                    .setLongLabel(name)
                    .setIcon(Icon.createWithResource(context, iconId))
                    .setIntent(new Intent(context.getApplicationContext(), className)
                            .setAction(action)
                            .putExtras(data));
            if (!TextUtils.isEmpty(disabledMessage))
                builder.setDisabledMessage(disabledMessage);
            ShortcutInfo shortcutInfo = builder.build();
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcutInfo));
        }
    }

    @RequiresApi(api = 25)
    public static void removeDynamicShortcut(Context context, String id) {
        if (Build.VERSION.SDK_INT >= 25) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.removeDynamicShortcuts(Collections.singletonList(id));
        }
    }
    //endregion
}