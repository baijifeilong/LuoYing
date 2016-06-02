/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.game.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.fighter.Common;
import name.huliqing.fighter.Config;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.Fighter;
import name.huliqing.fighter.constants.IdConstants;
import name.huliqing.fighter.data.ConfigData;
import name.huliqing.fighter.object.DataLoaderFactory;
import name.huliqing.fighter.manager.HUDManager;
import name.huliqing.fighter.manager.ResourceManager;
import name.huliqing.fighter.manager.ShortcutManager;
import name.huliqing.fighter.save.SaveConfig;
import name.huliqing.fighter.save.SaveHelper;
import name.huliqing.fighter.utils.MathUtils;

/**
 *
 * @author huliqing
 */
public class ConfigServiceImpl implements ConfigService {
    
    private ConfigData cd;

    @Override
    public void inject() {
        // ignore
    }

    @Override
    public ConfigData getConfig() {
        return cd;
    }

    @Override
    public void loadGlobalConfig() {
        // 载入系统配置
        cd = DataLoaderFactory.createConfigData(IdConstants.CONFIG_GLOBAL);
        Config.debug = cd.isDebug();
        
        // 载入用户保存的配置并整合到全局配置中，注：只处理部分保存的参数。
        SaveConfig sc = SaveHelper.loadConfig();
        if (sc != null && sc.getConfig() != null) {
            ConfigData scd = sc.getConfig();
            cd.setLocale(scd.getLocale());
            cd.setLocaleAll(scd.getLocaleAll());
            cd.setPort(scd.getPort());
            cd.setPortDiscoverClient(scd.getPortDiscoverClient());
            cd.setPortDiscoverServer(scd.getPortDiscoverServer());
            cd.setShortcutLocked(scd.isShortcutLocked());
            cd.setShortcutSize(scd.getShortcutSize());
            cd.setSoundEnabled(scd.isSoundEnabled());
            cd.setSoundVolume(scd.getSoundVolume());
            cd.setSpeakTimeMax(scd.getSpeakTimeMax());
            cd.setSpeakTimeMin(scd.getSpeakTimeMin());
            cd.setSpeakTimeWorld(scd.getSpeakTimeWorld());
            cd.setUseHardwareSkinning(scd.isUseHardwareSkinning());
        }
    }
    
    @Override
    public boolean isSoundEnabled() {
        return cd.isSoundEnabled();
    }

    @Override
    public void setSoundEnabled(boolean enabled) {
        cd.setSoundEnabled(enabled);
    }

    @Override
    public float getSoundVolume() {
        return cd.getSoundVolume();
    }

    @Override
    public void setSoundVolume(float volume) {
        cd.setSoundVolume(MathUtils.clamp(volume, 0f, 1.0f));
    }

    @Override
    public boolean isShortcutLocked() {
        return cd.isShortcutLocked();
    }

    @Override
    public void setShortcutLocked(boolean locked) {
        cd.setShortcutLocked(locked);
        ShortcutManager.setShortcutLocked(locked);
    }

    @Override
    public float getShortcutSize() {
        return cd.getShortcutSize();
    }

    @Override
    public void setShortcutSize(float size) {
        if (size <= 0) {
            return;
        }
        cd.setShortcutSize(size);
        ShortcutManager.setShortcutSize(size);
    }

    @Override
    public void clearShortcuts() {
        ShortcutManager.clearShortcuts();
    }

    @Override
    public boolean isDebugEnabled() {
        return cd.isDebug();
    }

    @Override
    public void setDebug(boolean enabled) {
        cd.setDebug(enabled);
        Config.debug = enabled;
        Fighter fighter = (Fighter) Common.getApp();
        fighter.setDebugView(enabled);
        HUDManager.setDragEnabled(enabled);
    }

    @Override
    public float getSpeakTimeWorld() {
        return cd.getSpeakTimeWorld();
    }

    @Override
    public void setSpeakTimeWorld(float time) {
        cd.setSpeakTimeWorld(time);
    }

    @Override
    public float getSpeakTimeMin() {
        return cd.getSpeakTimeMin();
    }

    @Override
    public float getSpeakTimeMax() {
        return cd.getSpeakTimeMax();
    }

    @Override
    public float getSpeakMaxDistance() {
        return cd.getSpeakMaxDistance();
    }

    @Override
    public boolean isUseHardwareSkinning() {
        return cd.isUseHardwareSkinning();
    }

    @Override
    public void setUseHardwareSkining(boolean enabled) {
        cd.setUseHardwareSkinning(enabled);
        
        // remove20160209不要去动态开启角色的HardWareSkinning,目前有BUG
//        // 如果改变了该设置，则需要重新检测激活或关闭当前已经载入场景中模型
//        PlayState playState = Common.getPlayState();
//        if (playState != null) {
//            List<Actor> actors = playState.getActors();
//            if (actors != null && !actors.isEmpty()) {
//                for (Actor a : actors) {
//                    ActorLoader.checkEnableHardwareSkining(a.getModel());
//                }
//            }
//        }
    }

    @Override
    public String loadLocale() {
        cd.setLocale(detectLocale());
        return cd.getLocale();
    }

    @Override
    public void changeLocale(String locale) {
        cd.setLocale(locale);
        ResourceManager.clearResources();
        
        // 保存配置
        SaveConfig saveConfig = new SaveConfig();
        saveConfig.setConfig(cd);
        SaveHelper.saveConfig(saveConfig);
    }
    
    @Override
    public String getLocale() {
        return cd.getLocale();
    }

    @Override
    public float getDropFactor() {
        return cd.getDropFactor();
    }

    @Override
    public String[] getLanGames() {
        return cd.getLanGames().split(",");
    }

    @Override
    public String getGameName() {
        return cd.getGameName();
    }

    @Override
    public String getVersionName() {
        return cd.getVersionName();
    }

    @Override
    public int getVersionCode() {
        return cd.getVersionCode();
    }

    @Override
    public int getPort() {
        return cd.getPort();
    }

    @Override
    public int getPortDiscoverServer() {
        return cd.getPortDiscoverServer();
    }

    @Override
    public int getPortDiscoverClient() {
        return cd.getPortDiscoverClient();
    }

    @Override
    public String[] getAllSupportedLocale() {
        String localAll = cd.getLocaleAll();
        if (localAll != null) {
            return localAll.split(",");
        }
        return null;
    }

    @Override
    public boolean isUseLight() {
        return cd.isUseLight();
    }

    @Override
    public float getSummonLevelFactor() {
        return cd.getSummonLevelFactor();
    }

    @Override
    public float getBaseWalkSpeed() {
        return cd.getBaseWalkSpeed();
    }

    @Override
    public float getBaseRunSpeed() {
        return cd.getBaseRunSpeed();
    }
    
    // 检测及获取一个可用的语言环境。
    private String detectLocale() {
        String value = cd.getLocale();
        String[] localeAll = cd.getLocaleAll().split(",");
        String defLocale = localeAll[0];
        
        // 1.从配置和本地环境中查找，如果没有则使用默认配置
        if (value == null || value.equals("")) {
            value = Factory.get(EnvService.class).getLocale();
            if (value == null || value.equals("")) {
                return defLocale;
            }
        }
        
        // 2.优先从支持的语言环境中找出一个完全匹配的。
        for (String locale : localeAll) {
            if (value.equals(locale)) {
                return value;
            }
        }
        
        // 3.到这里，如果没有完全匹配的语言环境，则根据前缀匹配来查找环境
        String valuePrefix = value.split("_")[0];
        for (String locale : localeAll) {
            if (locale.startsWith(valuePrefix)) {
                return locale;
            }
        }
        
        // 4.前缀匹配也没有找到，则使用默认
        Logger.getLogger(ConfigServiceImpl.class.getName()).log(Level.WARNING
                , "Sorry, the locale {0} unsupported yet!"
                , new Object[] {value});
        return defLocale;
    }

    @Override
    public int getMaxLevel() {
        return cd.getMaxLevel();
    }

    @Override
    public boolean isUseShadow() {
        return cd.isUseShadow();
    }

    @Override
    public void setUseShadow(boolean useShadow) {
        cd.setUseShadow(useShadow);
    }

    
}
