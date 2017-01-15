package com.flyfish.ftp.ftp_hunter;

import java.io.Serializable;

public class LineDTO implements Serializable{
    /**
     */
    private static final long serialVersionUID = 1L;
    
    private String memberId;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
