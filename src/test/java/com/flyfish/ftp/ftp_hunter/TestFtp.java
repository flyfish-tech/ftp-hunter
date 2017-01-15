package com.flyfish.ftp.ftp_hunter;
import com.flyfish.ftp.property.FtpProperty;
import com.flyfish.ftp.util.FtpUtil;


public class TestFtp {
    public static FtpProperty getFtpProperty() {
        FtpProperty ftpProperty = new FtpProperty();
        ftpProperty.setEncode("UTF-8");
        ftpProperty.setLocalDirectory("F://cmy");
        ftpProperty.setHost("10.27.97.**");
        ftpProperty.setPort(21);
        ftpProperty.setFlagExtension("flg");
        ftpProperty.setExtension("txt");
        ftpProperty.setUserName("ftp_cmf_w");
        ftpProperty.setPassword("ftp_cmf_w");
        ftpProperty.setRemoteDirectory("/20161219/SOSP_MA/");
        ftpProperty.setSkipLine(2);
        ftpProperty.setCallMethod("parseLine");
        ftpProperty.setClazz(Line.class);
//        ftpProperty.setAllowReadMax(4);
        return ftpProperty;
        
    }
    public static void main(String[] args) {
        
        FtpUtil ftpUtil = new FtpUtil(getFtpProperty());
        try {
//            ftpUtil.parseRemoteFile();
            ftpUtil.uploadFile("12345.txt");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
