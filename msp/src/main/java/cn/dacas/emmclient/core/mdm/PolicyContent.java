package cn.dacas.emmclient.core.mdm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.model.MdmWifiModel;

/**
 * Created by lenovo on 2016-1-21.
 */
public  class PolicyContent implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String name;
    private String version;
    public String type;
    public String effectTimeStart;
    public String effectTimeEnd;

    /*
     * all supported operations
     */
    private String alertLevel;
    private boolean disableCamera;
    private boolean isConfigPasswdPolicy;
    private int maximumFailedPasswordsForWipe;
    private int maximumTimeToLock;
    private String passwdQuality;
    private int passwordExpirationTimeout;
    private int passwordHistoryLength;
    private int passwordMinimumLength;
    private int passwordMinimumSymbols;

    private boolean disableWifi;
    private boolean disableDataNetwork;
    private boolean disableBluetooth;
    private List<String> ssidWhiteList = new ArrayList<String>();
    private List<String> ssidBlackList = new ArrayList<String>();

    private boolean disableGooglePlay;
    private boolean disableYouTube;
    private boolean disableEmail;
    private boolean disableBrowser;
    private boolean disableSettings;
    private boolean disableGallery;
    private boolean disableGmail;
    private boolean disableGoogleMap;
    private List<String> blackApps = new ArrayList<String>();
    private List<String> whiteApps = new ArrayList<String>();
    private List<String> mustApps = new ArrayList<String>();

    private ArrayList<MdmWifiModel> wifis=new ArrayList<>();

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public boolean isDisableCamera() {
        return disableCamera;
    }

    public void setDisableCamera(boolean disableCamera) {
        this.disableCamera = disableCamera;
    }

    public boolean isConfigPasswdPolicy() {
        return isConfigPasswdPolicy;
    }

    public void setConfigPasswdPolicy(boolean isConfigPasswdPolicy) {
        this.isConfigPasswdPolicy = isConfigPasswdPolicy;
    }

    public int getMaximumFailedPasswordsForWipe() {
        return maximumFailedPasswordsForWipe;
    }

    public void setMaximumFailedPasswordsForWipe(
            int maximumFailedPasswordsForWipe) {
        this.maximumFailedPasswordsForWipe = maximumFailedPasswordsForWipe;
    }

    public int getMaximumTimeToLock() {
        return maximumTimeToLock;
    }

    public void setMaximumTimeToLock(int maximumTimeToLock) {
        this.maximumTimeToLock = maximumTimeToLock;
    }

    public String getPasswdQuality() {
        return passwdQuality;
    }

    public void setPasswdQuality(String passwdQuality) {
        this.passwdQuality = passwdQuality;
    }

    public int getPasswordExpirationTimeout() {
        return passwordExpirationTimeout;
    }

    public void setPasswordExpirationTimeout(int passwordExpirationTimeout) {
        this.passwordExpirationTimeout = passwordExpirationTimeout;
    }

    public int getPasswordHistoryLength() {
        return passwordHistoryLength;
    }

    public void setPasswordHistoryLength(int passwordHistoryLength) {
        this.passwordHistoryLength = passwordHistoryLength;
    }

    public int getPasswordMinimumLength() {
        return passwordMinimumLength;
    }

    public void setPasswordMinimumLength(int passwordMinimumLength) {
        this.passwordMinimumLength = passwordMinimumLength;
    }

    public int getPasswordMinimumSymbols() {
        return passwordMinimumSymbols;
    }

    public void setPasswordMinimumSymbols(int passwordMinimumSymbols) {
        this.passwordMinimumSymbols = passwordMinimumSymbols;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNameAndVersion() {
        if (version == null) return name;
        return name + "(version:" + version + ")";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEffectTimeStart() {
        return effectTimeStart;
    }

    public void setEffectTimeStart(String effectTimeStart) {
        this.effectTimeStart = effectTimeStart;
    }

    public String getEffectTimeEnd() {
        return effectTimeEnd;
    }

    public void setEffectTimeEnd(String effectTimeEnd) {
        this.effectTimeEnd = effectTimeEnd;
    }

    public boolean isDisableWifi() {
        return disableWifi;
    }

    public void setDisableWifi(boolean disableWifi) {
        this.disableWifi = disableWifi;
    }

    public boolean isDisableDataNetwork() {
        return disableDataNetwork;
    }

    public void setDisableDataNetwork(boolean disableDataNetwork) {
        this.disableDataNetwork = disableDataNetwork;
    }

    public boolean isDisableBluetooth() {
        return disableBluetooth;
    }

    public void setDisableBluetooth(boolean disableBluetooth) {
        this.disableBluetooth = disableBluetooth;
    }

    public List<String> getSsidWhiteList() {
        return ssidWhiteList;
    }

    public void setSsidWhiteList(List<String> ssidWhiteList) {
        this.ssidWhiteList.clear();
        for (String s : ssidWhiteList)
            this.ssidWhiteList.add(s);
    }

    public List<String> getSsidBlackList() {
        return ssidBlackList;
    }

    public void setSsidBlackList(List<String> ssidBlackList) {
        this.ssidBlackList.clear();
        for (String s : ssidBlackList)
            this.ssidBlackList.add(s);
    }

    public boolean isDisableGooglePlay() {
        return disableGooglePlay;
    }

    public void setDisableGooglePlay(boolean disableGooglePlay) {
        this.disableGooglePlay = disableGooglePlay;
    }

    public boolean isDisableYouTube() {
        return disableYouTube;
    }

    public void setDisableYouTubey(boolean disableYouTube) {
        this.disableYouTube = disableYouTube;
    }

    public boolean isDisableEmail() {
        return disableEmail;
    }

    public void setDisableEmail(boolean disableEmail) {
        this.disableEmail = disableEmail;
    }

    public boolean isDisableBrowser() {
        return disableBrowser;
    }

    public void setDisableBrowser(boolean disableBrowser) {
        this.disableBrowser = disableBrowser;
    }

    public boolean isDisableSettings() {
        return disableSettings;
    }

    public void setDisableSettings(boolean disableSettings) {
        this.disableSettings = disableSettings;
    }

    public boolean isDisableGallery() {
        return disableGallery;
    }

    public void setDisableGallery(boolean disableGallery) {
        this.disableGallery = disableGallery;
    }

    public boolean isDisableGmail() {
        return disableGmail;
    }

    public void setDisableGmail(boolean disableGmail) {
        this.disableGmail = disableGmail;
    }

    public boolean isDisableGoogleMap() {
        return disableGoogleMap;
    }

    public void setDisableGoogleMap(boolean disableGoogleMap) {
        this.disableGoogleMap = disableGoogleMap;
    }

    public List<String> getBlackApps() {
        return blackApps;
    }

    public void setBlackApps(List<String> blacks) {
        blackApps.clear();
        if (blacks==null) return;
        for (String b : blacks)
            this.blackApps.add(b);
    }

    public List<String> getWhiteApps() {
        return whiteApps;
    }

    public void setWhiteApps(List<String> whites) {
        whiteApps.clear();
        if (whites==null) return;
        for (String b : whites)
            this.whiteApps.add(b);
    }

    public List<String> getMustApps() {
        return mustApps;
    }

    public void setMustApps(List<String> musts) {
        mustApps.clear();
        if (musts==null) return;
        for (String b : musts)
            this.mustApps.add(b);
    }

    public ArrayList<MdmWifiModel> getWifis() {
        return wifis;
    }

    public void clearWifis() {
        wifis.clear();
    }

    public void addWifi(String ssid,String password,String encryptionType) {
        MdmWifiModel wifi=new MdmWifiModel(ssid,password,encryptionType);
        wifis.add(wifi);
    }
}
