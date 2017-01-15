package com.flyfish.ftp.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flyfish.ftp.property.FileExtensionFilter;
import com.flyfish.ftp.property.FtpProperty;

/***
 * <p>
 * 提供一个FTP公共操作类 
 * </p>
 * @author 824785559@qq.com
 * @since 0.0.1
 */
public abstract class AbstractFtp {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFtp.class);
    
    protected FTPClient ftpClient = null;
    
    protected FtpProperty ftpProperty = null;
    
    protected AbstractFtp(FtpProperty ftpProperty) {
        this.ftpProperty = ftpProperty;
    }
    /**
     * 
     * <p>
     * 公共FTP 入口
     * 该方法为对 {@link FtpProperty } 相关属性值进行校验 
     * </p>
     * @throws Exception
     */
    public void parseRemoteFile() throws Exception {
        // 1、校验数据合法性
        validateProperty();
        
        // 2、建立连接
        ftpClient = getFTPConnection();
        
        // 3、 查找 flg 文件
        FTPFile[] fileArray = getFlgExtensionFiles();
        
        if(ArrayUtils.isNotEmpty(fileArray)) {
            findDataFileByFlg(fileArray);
        } else {
            findDataFile();
        }
    }
    
    /**
     * 建立FTP连接
     * @return 如果连接成功返回FTPClient客户端，否则抛出异常
     * @throws SocketException
     * @throws IOException
     */
    private FTPClient getFTPConnection() throws SocketException, IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(ftpProperty.getHost(), ftpProperty.getPort());
        ftpClient.login(ftpProperty.getUserName(), ftpProperty.getPassword());
        
        int replyCode = ftpClient.getReplyCode();
        
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            ftpClient.disconnect();
            ftpClient = null;
        } 
        
        if(ftpClient == null) {
            throw new NullPointerException("<FTPCLIENT IS NOT INSTANCE>");
        }
        
        return ftpClient;
    }
    
    /**
     * 处理完成后关闭FTPClient连接
     * @param ftpClient 可用的 FTPClient 连接
     */
    protected void disConnection(FTPClient ftpClient) {
        if(ftpClient != null){
            try {
                if(ftpClient.logout()) {
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                LOGGER.error("FTP LOGOUT FAIL", e);
            }
        }
    }
    
    /**
     * 验证 {@link FtpProperty}参数信息
     * 验证不通过，直接抛出异常 
     */
    private void validateProperty() {
        if(ftpProperty == null) {
            throw new NullPointerException("<IS NOT AVALIABLE FtpProperty OBJECT>");
        }
        
        if(StringUtils.isBlank(ftpProperty.getHost())) {
            throw new IllegalArgumentException("<NOT FIND FTP HOST PROPERTY>");
        }
        
        if(StringUtils.isBlank(ftpProperty.getUserName())) {
            throw new IllegalArgumentException("<NOT FIND FTP USERNAME PROPERTY>");
        }
        
        if(StringUtils.isBlank(ftpProperty.getPassword())) {
            throw new IllegalArgumentException("<NOT FIND FTP PASSWORD PROPERTY>");
        }
        
        if(StringUtils.isBlank(String.valueOf(ftpProperty.getPort()))) {
            throw new IllegalArgumentException("<NOT FIND FTP PORT PROPERTY>");
        }
        
        if(StringUtils.isBlank(ftpProperty.getExtension())) {
            throw new IllegalArgumentException("<FILE EXTENSION IS NOT AVALIABLE>");
        }
        
        if(StringUtils.isBlank(ftpProperty.getCallMethod())) {
            throw new IllegalArgumentException("<NOT FIND CALL METHOD WHEN EXECUTE INVOKE>");
        }
        
        if(ftpProperty.getClazz() == null) {
            throw new IllegalArgumentException("<NOT FIND INVOKE CLASS WHEN EXECUTE INVOKE>");
        }
    }
    /**
     * <p>
     * 代表一组数据文件的标记文件，如：一个类型事件有5个数据文件
     * 但不知道什么时候数据文件结束，此时可以用一个标记文件(*.flg)代表所有数据文件
     * 上传完成，该方法就是去查找所有数据文件结束的标记文件
     * 依赖于{@link FtpProperty->remoteDirectory}
     * </p>
     * @return 返回flg标记文件数组
     * @throws IOException
     */
    private FTPFile[] getFlgExtensionFiles() throws IOException {
        return ftpClient.listFiles(ftpProperty.getRemoteDirectory(), new FileExtensionFilter(ftpProperty.getFlagExtension()));
    }
    
    /**
     * <p>
     * 只查找数据文件，依赖于{@link FtpProperty->remoteDirectory}
     * </p>
     * @return 返回符合条件的文件数组
     * @throws IOException
     */
    protected FTPFile[] getOtherExtensionFiles() throws IOException {
        return ftpClient.listFiles(ftpProperty.getRemoteDirectory(), new FileExtensionFilter(ftpProperty.getExtension()));
    }
    
    /**
     * <p>
     * 文件处理完成后将文件转移到当前目录的BAK目录中
     * 依赖于 {@link FtpProperty->remoteDirectory}
     * </p>
     * @param fileName 要上传的数据文件
     * @throws IOException
     */
    protected void moveRemoteFileToBak(String fileName) throws IOException {
        String absoluteFilePathBak = ftpProperty.getRemoteDirectory() + "/BAK";
        ftpClient.makeDirectory(absoluteFilePathBak);
        ftpClient.changeWorkingDirectory(absoluteFilePathBak);
        ftpClient.rename(ftpProperty.getRemoteDirectory() + "/" + fileName, absoluteFilePathBak + "/" + fileName);
    }
    
    /**
     * 
     * <p>
     * 处理数据文件时对同一批类型的数据文件进行分组
     * </p>
     * @param groupKey
     * @param groupValue
     * @param groupFileMap
     */
    protected void dataFileGroup(String groupKey, String groupValue, Map<String, List<String>> groupFileMap) {
        if(groupFileMap.containsKey(groupKey)) {
            groupFileMap.get(groupKey).add(groupValue);
        } else {
            List<String> dataFileList = new LinkedList<String>();
            dataFileList.add(groupValue);
            groupFileMap.put(groupKey, dataFileList);
        }
    }
    
    /**
     * 
     * <p>检查指定的目录是否存在</p>
     *
     * @param directory 指定的数据文件目录
     * @return  如果目录存在返回true，不存在返回false
     */
    protected boolean existDirectory(String directory) {
        File file = new File(directory);
        if(!file.exists()) {
            try {
                return file.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    protected String getAfterSplitBySeparator(String str, String separator, int max) {
        String[] array = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, separator, max);
        if(array.length > 2) {
            return StringUtils.join(array[0], array[1]);
        }
        return StringUtils.join(array);
    }
    
    // 是否达到允许读取的上限
    protected boolean checkLineLimit(int maxIndex) {
        if(maxIndex >= ftpProperty.getAllowReadMax()){
            LOGGER.info("数据达到上限 {}", ftpProperty.getAllowReadMax());
            return true;
        }
        return false;
    }
    
    /**
     * 上传数据文件,此处只需要传入具体的文件即可
     * 具体的路径请在 ftpProperty属性类中维护
     * @param files
     * @return 上传成功返回true，失败返回false
     * @throws SocketException
     * @throws IOException
     */
    public boolean uploadFile(String...files) throws SocketException, IOException {
        if(ArrayUtils.isEmpty(files)) {
            throw new IllegalArgumentException("上传数据的文件名为空");
        }
        FileInputStream fileInputStream = null;
        try {
            // 初始化FTPClient
            ftpClient = getFTPConnection();
            
            // 创建FTP目录
            ftpClient.makeDirectory(ftpProperty.getRemoteDirectory());
            // 跳转到创建好的目录中
            ftpClient.changeWorkingDirectory(ftpProperty.getRemoteDirectory());
            
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding(ftpProperty.getEncode());
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            
            // 批量上传数据文件
            for(String file : files) {
                if(StringUtils.isNotBlank(file)) {
                    fileInputStream = new FileInputStream(new File(ftpProperty.getLocalDirectory() + "/" + file));
                    ftpClient.storeFile(file, fileInputStream);
                }
            }
        } finally{
            IOUtils.closeQuietly(fileInputStream);
            disConnection(ftpClient);
        }
        return true;
    }
    
    /**
     * 
     * <p>
     * 删除指定目录下的文件
     * </p>
     * @param files 要删除服务器上的指定文件
     * @return 如果删除成功，则返回 true；否则返回 false
     */
    public boolean deleteRemoteFile(String...files) throws SocketException, IOException {
        // 初始化FTPClient
        try {
            ftpClient = getFTPConnection();
            for(String file : files) {
                ftpClient.deleteFile(ftpProperty.getRemoteDirectory() + "/" + file);
            }
            return true;
        } finally {
            disConnection(ftpClient);
        }
    }
    
    /***
     * 上传数据完成后,删除本地数据文件
     * @param localFiles  待删除的文件名
     */
    public void deleteFileAfterUpload(String...localFiles) {
        if(ArrayUtils.isNotEmpty(localFiles)) {
            File local = null;
            for(String localFile : localFiles) {
                local = new File(ftpProperty.getLocalDirectory() + "/" + localFile);
                local.delete();
            }
        }
    }
    
    // 根据标记文件去查找数据文件
    public abstract void findDataFileByFlg(FTPFile[] ftpFile);
    
    // 直接查找数据文件
    public abstract void findDataFile();
    
    // 下载数据文件
    protected abstract void downLoadToLocal(Map<String, List<String>> dataFileMap);
    
    // 读取以下载到本地的数据文件
    protected abstract void readLocalFile(Map<String, List<String>> dataFileMap);
}
