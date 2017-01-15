package com.flyfish.ftp.ftp_hunter;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class Line {
    public void parseLine(String line) {
       if(StringUtils.isNotBlank(line)) {
           String[] arrays = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "##");
           System.out.println(Arrays.toString(arrays));
       }
    }
}
