package com.flyfish.ftp.property;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

public class FileExtensionFilter implements FTPFileFilter {
    private String extension = null;
    
    public FileExtensionFilter(String extension) {
        this.extension = extension;
    }
    
    public boolean accept(FTPFile file) {
        if(StringUtils.isBlank(extension)) return false;
        return file != null && file.isFile() && file.getName().endsWith(extension);
    }
}
