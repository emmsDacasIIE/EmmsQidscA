package cn.dacas.emmclient.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Sun RX on 2016-12-7.
 */

public class SerializableCMD implements Serializable {
    final public String cmdUUID;
    final public HashMap<String,String> paramMap;
    public SerializableCMD(CommandModel commandModel){
        cmdUUID = commandModel.getCommandUUID();
        this.paramMap = commandModel.getCommandMap();
    }
}
