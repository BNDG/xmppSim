package com.hjq.demo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

import com.hjq.demo.R;
import com.hjq.permissions.XXPermissions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 跳转自启动页面解决方案
 *
 * @author zhj
 */
public class AutoStartPermissionManager {

    private static HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>() {
        {
            put("Xiaomi", Arrays.asList(
                    "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity",
                    "com.miui.securitycenter"
            ));
            put("samsung", Arrays.asList(
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity",
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
                    "com.samsung.android.sm_cn/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
                    "com.samsung.android.sm_cn/.ui.ram.RamActivity",
                    "com.samsung.android.sm_cn/.app.dashboard.SmartManagerDashBoardActivity",

                    "com.samsung.android.sm/com.samsung.android.sm.ui.ram.AutoRunActivity",
                    "com.samsung.android.sm/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
                    "com.samsung.android.sm/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
                    "com.samsung.android.sm/.ui.ram.RamActivity",
                    "com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity",

                    "com.samsung.android.lool/com.samsung.android.sm.ui.battery.BatteryActivity",
                    "com.samsung.android.sm_cn",
                    "com.samsung.android.sm"
            ));
            put("HUAWEI", Arrays.asList(
                    "com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity",
                    "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity",
                    "com.huawei.systemmanager/.optimize.process.ProtectActivity",
                    "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity",
                    "com.huawei.systemmanager"
            ));
            put("vivo", Arrays.asList(
                    "com.iqoo.secure/.ui.phoneoptimize.BgStartUpManager",
                    "com.iqoo.secure/.safeguard.PurviewTabActivity",
                    "com.vivo.permissionmanager/.activity.BgStartUpManagerActivity",
//                    "com.iqoo.secure/.ui.phoneoptimize.AddWhiteListActivity", //这是白名单, 不是自启动
                    "com.iqoo.secure",
                    "com.vivo.permissionmanager"
            ));
            put("Meizu", Arrays.asList(
                    "com.meizu.safe/.permission.SmartBGActivity",
                    "com.meizu.safe/.permission.PermissionMainActivity",
                    "com.meizu.safe"
            ));
            put("OPPO", Arrays.asList(
                    "com.coloros.safecenter/.startupapp.StartupAppListActivity",
                    "com.coloros.safecenter/.permission.startup.StartupAppListActivity",
                    "com.oppo.safe/.permission.startup.StartupAppListActivity",
                    "com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerUsageModelActivity",
                    "com.coloros.safecenter/com.coloros.privacypermissionsentry.PermissionTopActivity",
                    "com.coloros.safecenter",
                    "com.oppo.safe",
                    "com.coloros.oppoguardelf"
            ));
            put("oneplus", Arrays.asList(
                    "com.oneplus.security/.chainlaunch.view.ChainLaunchAppListActivity",
                    "com.oneplus.security"
            ));
            put("letv", Arrays.asList(
                    "com.letv.android.letvsafe/.AutobootManageActivity",
                    "com.letv.android.letvsafe/.BackgroundAppManageActivity",
                    "com.letv.android.letvsafe"
            ));
            put("zte", Arrays.asList(
                    "com.zte.heartyservice/.autorun.AppAutoRunManager",
                    "com.zte.heartyservice"
            ));
            //金立
            put("F", Arrays.asList(
                    "com.gionee.softmanager/.MainActivity",
                    "com.gionee.softmanager"
            ));
            //以下为未确定(厂商名也不确定)
            put("smartisanos", Arrays.asList(
                    "com.smartisanos.security/.invokeHistory.InvokeHistoryActivity",
                    "com.smartisanos.security"
            ));
            //360
            put("360", Arrays.asList(
                    "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
                    "com.yulong.android.coolsafe"
            ));
            //360
            put("ulong", Arrays.asList(
                    "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
                    "com.yulong.android.coolsafe"
            ));
            //酷派
            put("coolpad"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain",
                    "com.yulong.android.security"
            ));
            //联想
            put("lenovo"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.lenovo.security/.purebackground.PureBackgroundActivity",
                    "com.lenovo.security"
            ));
            put("htc"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.htc.pitroad/.landingpage.activity.LandingPageActivity",
                    "com.htc.pitroad"
            ));
            //华硕
            put("asus"/*厂商名称不确定是否正确*/, Arrays.asList(
                    "com.asus.mobilemanager/.MainActivity",
                    "com.asus.mobilemanager"
            ));
            //酷派
            put("YuLong", Arrays.asList(
                    "com.yulong.android.softmanager/.SpeedupActivity",
                    "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain",
                    "com.yulong.android.security"
            ));
            // 荣耀 实测机型（HONOR 50 SE）
            put("HONOR", Arrays.asList(
                    "com.hihonor.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                    "com.hihonor.systemmanager"
            ));
        }
    };

    public static void startToAutoStartSetting(Context context) {
        Trace.d("Util", "******************当前手机型号为：" + Build.MANUFACTURER);
        Set<Map.Entry<String, List<String>>> entries = hashMap.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            String manufacturer = entry.getKey();
            List<String> actCompatList = entry.getValue();
            if (Build.MANUFACTURER.equalsIgnoreCase(manufacturer)) {
                Intent intent = null;
                for (String act : actCompatList) {
                    PackageManager pm = context.getPackageManager();
                    ComponentName componentName = ComponentName.unflattenFromString(act);
                    intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(componentName);
                    ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (info != null) {
                        // ComponentName 存在
                        // 可以继续进行相关操作
                        Trace.d("startToAutoStartSetting:  ComponentName 存在");
                        break;
                    } else {
                        // ComponentName 不存在
                        Trace.d("startToAutoStartSetting: ComponentName 不存在");
                        intent = null;
                    }
                }
                if (intent != null) {
                    context.startActivity(intent);
                    return;
                }
            }
        }
        Trace.d("startToAutoStartSetting: ");
        XXPermissions.startPermissionActivity(context);
    }

    /**
     * 跳转不同机型的电池策略界面
     */
    public static void startBatteryStrategyActivity(Context context) {
        Intent intent = new Intent();
        PackageManager pm = context.getPackageManager();
        if (isHuawei()) {
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.Settings$HighPowerApplicationsActivity");
            intent.setComponent(component);
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                // ComponentName 存在
                Trace.d("startToAutoStartSetting:  ComponentName 存在");
            } else {
                // ComponentName 不存在
                Trace.d("startToAutoStartSetting: ComponentName 不存在");
                intent = null;
            }
        } else if (isXiaomi()) {
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("package_label", context.getResources().getString(R.string.app_name));
            intent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                // ComponentName 存在
                Trace.d("startToAutoStartSetting:  ComponentName 存在");
            } else {
                // ComponentName 不存在
                Trace.d("startToAutoStartSetting: ComponentName 不存在");
                intent = null;
            }
        } else if (isOPPO()) {
            intent.setComponent(new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"));
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                // ComponentName 存在
                Trace.d("startToAutoStartSetting:  ComponentName 存在");
            } else {
                // ComponentName 不存在
                Trace.d("startToAutoStartSetting: ComponentName 不存在");
                intent = null;
            }
        } else if (isVIVO()) {
            intent.setComponent(new ComponentName("com.iqoo.powersaving", "com.iqoo.powersaving.PowerSavingManagerActivity"));
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                // ComponentName 存在
                Trace.d("startToAutoStartSetting:  ComponentName 存在");
            } else {
                // ComponentName 不存在
                Trace.d("startToAutoStartSetting: ComponentName 不存在");
                intent = null;
            }
        }
        if (intent != null) {
            context.startActivity(intent);
        } else {
             intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }

    /**
     * 华为厂商判断
     *
     * @return
     */
    public static boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.toLowerCase().equals("huawei") || Build.BRAND.equalsIgnoreCase("honor");
        }
    }

    /**
     * 小米厂商判断
     * minSdkVersion在19以上，红米系列得到的是redmi的厂商名字，所以在这里适配下红米厂商
     *
     * @return
     */
    public static boolean isXiaomi() {
        return Build.BRAND != null && (Build.BRAND.toLowerCase().equals("xiaomi") || Build.BRAND.toLowerCase().equals("redmi"));
    }

    /**
     * OPPO厂商判断
     *
     * @return
     */
    public static boolean isOPPO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("oppo");
    }

    /**
     * VIVO厂商判断
     *
     * @return
     */
    public static boolean isVIVO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("vivo");
    }
}