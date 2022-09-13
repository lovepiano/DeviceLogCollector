package com.java.snmp.utils;

import com.java.snmp.entity.SnmpBean;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SNMPTool {
    //获取列表oid数据
    public List<String> walkByTable(String oid, SnmpBean snmpBean){
        Snmp snmp = null;
        PDU pdu;
        CommunityTarget target;
        List<String> result = new ArrayList<String>();
        String communityName = snmpBean.getCommunityName();
        String hostIp = snmpBean.getHostIp();
        int port = snmpBean.getPort();
        int version = snmpBean.getVersion();
        try {
            DefaultUdpTransportMapping dm = new DefaultUdpTransportMapping();
            dm.setSocketTimeout(5000);
            snmp = new Snmp(dm);
            snmp.listen();
            target = new CommunityTarget();
            target.setCommunity(new OctetString(communityName));
            target.setVersion(version);
            target.setAddress(new UdpAddress(hostIp+"/"+port));
            target.setTimeout(1000);
            target.setRetries(1);
            TableUtils tutils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
            OID[] columns = new OID[1];
            columns[0] = new VariableBinding(new OID(oid)).getOid();
            List<TableEvent> list = (List<TableEvent>) tutils.getTable(target, columns, null, null);
            for(TableEvent e : list){
                VariableBinding[] vb = e.getColumns();
                if(null == vb)continue;
                result.add(vb[0].getVariable().toString());
            }
            snmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(snmp != null)
                {
                    snmp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    //发送snmp查询请求
    public static String  sendRequestGet(Snmp snmp, String oid, SnmpBean snmpBean)
            throws IOException {
        String str=null;
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(snmpBean.getCommunityName()));
        target.setVersion(snmpBean.getVersion());
        target.setAddress(new UdpAddress(snmpBean.getHostIp()+"/"+snmpBean.getPort()));
        target.setTimeout(5000);
        target.setRetries(1);
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(new OID(oid)));
        ResponseEvent responseEvent = snmp.send(pdu, target);
        PDU response = responseEvent.getResponse();
        if (response == null) {
            System.out.println("TimeOut...");//可能原因，1：snmp服务未开启。2：Community未配置
        } else {
            if (response.getErrorStatus() == PDU.noError) {
                Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                for (VariableBinding vb : vbs) {
                    System.out.println(vb + " ," + vb.getVariable().getSyntaxString());
                    str =  vb.getVariable().toString();
                }
            } else {
                System.out.println("Error:" + response.getErrorStatusText());
            }
        }
        return str;
    }
    //测试网络是否畅通,类似ping
    public boolean isEthernetConnection(SnmpBean bean) throws IOException {

        InetAddress ad = InetAddress.getByName(bean.getHostIp());
        boolean state = ad.isReachable(2000);// 测试是否可以达到该地址 2秒超时
        return state;
    }
}
