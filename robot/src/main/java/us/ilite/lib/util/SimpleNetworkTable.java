package us.ilite.lib.util;

import edu.wpi.first.networktables.*;
import us.ilite.common.config.SystemSettings;

public class SimpleNetworkTable {
    private NetworkTable netTable;

    public SimpleNetworkTable(String name) {
        NetworkTableInstance.getDefault().setUpdateRate(SystemSettings.NETWORK_TABLE_UPDATE_RATE);
        netTable = NetworkTableInstance.getDefault().getTable(name);

        netTable.getInstance().setServerTeam(1885);
        netTable.getInstance().startClientTeam(1885);
    }

    public void initKeys() {

    }

    public synchronized NetworkTableEntry getEntry(String key) {
        return netTable.getEntry(key);
    }

    public synchronized void putDouble(String key, double value) {
        netTable.getEntry(key).forceSetDouble(value);
    }

    public synchronized void putNumber(String key, Integer value) {
        netTable.getEntry(key).forceSetNumber(value);
    }

    public synchronized void putNUmberArray(String key, Integer[] value) {
        netTable.getEntry(key).forceSetNumberArray(value);
    }

    public synchronized void putString(String key, String value) {
        netTable.getEntry(key).forceSetString(value);
    }

    public synchronized NetworkTable getInstance() {
        return netTable;        
    }

}