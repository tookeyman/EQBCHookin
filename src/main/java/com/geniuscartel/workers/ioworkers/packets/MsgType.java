package com.geniuscartel.workers.ioworkers.packets;

public enum MsgType {
    CMD_DISCONNECT("\tDISCONNECT\n"), //bccmd disconnect
    CMD_NAMES("\tNAMES\n"), //no idea
    CMD_PONG("\tPONG\n"),   //response to ping?
    CMD_MSGALL("\tMSGALL\n"),   //global echo
    CMD_TELL("\tTELL\n"), //bct
    CMD_CHANNELS("\tCHANNELS\n"),//no idea
    CMD_LOCALECHO("\tLOCALECHO "), //no idea
    CMD_BCI("\tBCI\n"); //weird lowkey global echo

    private final String message;
    MsgType(String msg){
        this.message = msg;
    }
    public String getMessage(){
        return this.message;
    }
}
