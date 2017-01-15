package com.flyfish.ftp.property;

import java.io.Serializable;
/**
 * <p>
 * FTP 连接相关属性
 * </p> 
 * @author 824785559@qq.com
 */
public class FtpProperty implements Serializable{

    /**
     */
    private static final long serialVersionUID = 1L;
    
    private String host;
    
    private int port = 21;
    
    private String userName;
    
    private String password;
    
    private String remoteDirectory;
    
    private String localDirectory;

    private String flagExtension;
    
    private String extension;
    
    private String encode = "UTF-8";
    
    private int skipLine;
    
    private int allowReadMax = Integer.MAX_VALUE;
    
    private Class<?> clazz;
    
    private String callMethod;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getFlagExtension() {
        return flagExtension;
    }

    public void setFlagExtension(String flagExtension) {
        this.flagExtension = flagExtension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public int getSkipLine() {
        return skipLine;
    }

    public int getAllowReadMax() {
        return allowReadMax;
    }

    public void setAllowReadMax(int allowReadMax) {
        this.allowReadMax = allowReadMax;
    }

    public void setSkipLine(int skipLine) {
        this.skipLine = skipLine;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(String callMethod) {
        this.callMethod = callMethod;
    }
    
}
