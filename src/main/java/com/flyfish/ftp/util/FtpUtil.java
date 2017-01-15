package com.flyfish.ftp.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flyfish.ftp.property.FtpProperty;

public class FtpUtil extends AbstractFtp{
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpUtil.class);

    public FtpUtil(FtpProperty ftpProperty) {
        super(ftpProperty);
    }

    @Override
    public void findDataFileByFlg(FTPFile[] ftpFile) {
        Map<String, List<String>> dataFileMap = new HashMap<String, List<String>>();
        for(FTPFile file : ftpFile) {
            // flg 文件名
            // 根据flg查找指定的数据文件
            String flgFileName = getAfterSplitBySeparator(file.getName(), "_", 3);
            try {
                // 移动文件到指定的目录
                moveRemoteFileToBak(file.getName());
                
                FTPFile[] dataFileArray = getOtherExtensionFiles();
                for(FTPFile dataFile : dataFileArray) {
                    String dataFileName = getAfterSplitBySeparator(dataFile.getName(), "_", 3);
                    if(dataFileName.startsWith(flgFileName)){
                        dataFileGroup(flgFileName, dataFile.getName(), dataFileMap);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // 下载远程数据文件到本地
        downLoadToLocal(dataFileMap);
        
        // 开始解析已下载到本地的数据文件
        readLocalFile(dataFileMap);
    }

    @Override
    public void findDataFile() {
        Map<String, List<String>> dataFileMap = new HashMap<String, List<String>>();
        
        // 直接查找数据文件
        try {
            FTPFile[] dataFileArray = getOtherExtensionFiles();
            for(FTPFile dataFile : dataFileArray) {
                String dataFileName = dataFile.getName();
                String shortFileName= getAfterSplitBySeparator(dataFileName, "_", 3);
                dataFileGroup(shortFileName, dataFileName, dataFileMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 下载远程数据文件到本地
        downLoadToLocal(dataFileMap);
        
        // 开始解析已下载到本地的数据文件
        readLocalFile(dataFileMap);
    }

    @Override
    protected void downLoadToLocal(Map<String, List<String>> dataFileMap) {
        
        if(MapUtils.isEmpty(dataFileMap)) {
            throw new IllegalArgumentException("数据文件不存在");
        }

        // 待下载的数据文件集合
        Set<Map.Entry<String, List<String>>> entryseSet = dataFileMap.entrySet();
        
        FileOutputStream fos = null;
        
        try {
            // 切换到待下到本地的目录
            ftpClient.changeWorkingDirectory(ftpProperty.getRemoteDirectory());
            
            // 校验本地目录是否存在
            boolean existDirectory = existDirectory(ftpProperty.getLocalDirectory());
            
            for (Map.Entry<String, List<String>> entry : entryseSet) {
                String groupFlgName = entry.getKey();
                LOGGER.info("开始下载{}组下的数据文件, {}", groupFlgName, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                List<String> dataFileList = entry.getValue();
                if(CollectionUtils.isNotEmpty(dataFileList)) {
                   if(existDirectory) {
                       for(String fileName : dataFileList) {
                           File file = new File(ftpProperty.getLocalDirectory() + "/" + fileName);
                           fos = new FileOutputStream(file);
                           ftpClient.retrieveFile(fileName, fos);
                           fos.close();
                           
                           // 开始移动文件
                           moveRemoteFileToBak(fileName);
                       }
                   }
                }
                LOGGER.info("结束下载{}组下的数据文件, {}", groupFlgName, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            }  
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            IOUtils.closeQuietly(fos);
            disConnection(ftpClient);
        }
    }

    @Override
    protected void readLocalFile(Map<String, List<String>> dataFileMap) {
        if(MapUtils.isEmpty(dataFileMap)) {
            throw new IllegalArgumentException("数据文件不存在");
        }

        FileInputStream fis     = null;
        BufferedInputStream bis = null;
        InputStreamReader isr   = null;
        BufferedReader br       = null;
        
        // 待下载的数据文件集合
        Set<Map.Entry<String, List<String>>> entryseSet = dataFileMap.entrySet();
        for (Map.Entry<String, List<String>> entry : entryseSet) {
            String groupFlgName = entry.getKey();
            LOGGER.info("开始解析组{}下的数据文件, {}", groupFlgName, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            List<String> dataFileList = entry.getValue();
            if(CollectionUtils.isNotEmpty(dataFileList)) {
                
                // 一个总文件下允许读的最大记录数
                int maxRecord = 0;
                
                for(String fileName : dataFileList) {
                    LOGGER.info("开始解析{}文件, {}", fileName, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    
                    if(checkLineLimit(maxRecord)) break;
                    
                    try {
                        fis = new FileInputStream(new File(ftpProperty.getLocalDirectory() + "/" + fileName));
                        bis = new BufferedInputStream(fis);
                        isr = new InputStreamReader(bis, ftpProperty.getEncode());
                        br  = new BufferedReader(isr, 1024 * 1024);

                        int lineIndex = 0;
                        
                        Object object = ftpProperty.getClazz().newInstance();
                        Method method = ftpProperty.getClazz().getDeclaredMethod(ftpProperty.getCallMethod(), String.class);
                        
                        while (br.ready()) {
                            // 逐行获得数据
                            String line = br.readLine();
                            
                            if(checkLineLimit(maxRecord)) break;
                            
                            // 指定从第几行开始解析数据(防止有文件头)
                            if(lineIndex >= ftpProperty.getSkipLine()) {
                                // 回调,将为务数据交给业务系统
                                method.invoke(object, line);
                                
                                maxRecord++;
                            }
                            lineIndex++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(br);
                        IOUtils.closeQuietly(isr);
                        IOUtils.closeQuietly(bis);
                        IOUtils.closeQuietly(fis);
                        
                        // 移除已经读取过的数据文件
                        dataFileMap.get(groupFlgName).clear();
                    }
                    LOGGER.info("结束解析{}文件, {}", fileName, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                }
            }
            LOGGER.info("结束解析组{}下的数据文件, {}", groupFlgName, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        }
        // 清除文件缓存
        dataFileMap.clear();
    }
}
