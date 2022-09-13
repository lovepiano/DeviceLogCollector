package com.java.snmp;

import com.java.snmp.entity.SnmpBean;
import com.java.snmp.utils.SNMPTool;
import org.snmp4j.Snmp;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.List;

public class SNMPApplication {

    public static void main(String[] args) {
        SNMPTool SnmpTool = new SNMPTool();
        //实例化实体snmp类
        SnmpBean snmpBean = new SnmpBean();
        //本机地址
        snmpBean.setLocalIp("127.0.0.1");
        snmpBean.setCommunityName("public");
        //服务器地址
        snmpBean.setHostIp("127.0.0.1");
        //NMS和代理进程之间的通信端口
        snmpBean.setPort(161);
        snmpBean.setVersion(1);
        //测试联通性
        try {
            //是否连接，测试网络连通性
            System.out.println("连接成功：" + SnmpTool.isEthernetConnection(snmpBean));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取cpu利用率
        List<String> result = SnmpTool.walkByTable(".1.3.6.1.2.1.25.3.3.1.2", snmpBean);
        double sum = 0;
        for (String s : result) {
            sum += Double.parseDouble(s);
        }
        int cpu = (int) (sum / result.size());
        System.out.println("cpu利用率："+cpu+"%");
        //get方式获取系统内存
        try {
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();
            String strMemory = SnmpTool.sendRequestGet(snmp,".1.3.6.1.2.1.25.2.2.0",snmpBean);
            System.out.println("系统内存："+Math.ceil(Double.parseDouble(strMemory)/1024/1024)+"G");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
