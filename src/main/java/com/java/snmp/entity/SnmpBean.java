package com.java.snmp.entity;

import lombok.Data;

@Data
public class SnmpBean {

    //分区域，即密码，默认是public
    private String communityName;
    //服务器ip地址
    private String hostIp;
    //本机ip地址
    private String localIp;
    //snmp访问端口号
    private Integer port;
    //版本
    private int version;
    //是否同步查询
    private int async;
}
