package com.zgw.cola;

import com.zgw.cola.domain.ResultDataTo;
import com.zgw.cola.domain.ResultMessageTo;
import com.zgw.cola.util.ssh.JschShellClient;

public class MainTest {
    public static void main(String[] args) {
        String host = "192.168.236.130";
        String username = "root";
        String password = "zgw12#$";
        JschShellClient jschShellClient = new JschShellClient(host, username, password);
        ResultMessageTo connectMsg = jschShellClient.connect();
        if (!connectMsg.isSuccess()) {
            System.out.println(connectMsg.getMessage());
            return;
        }
        String[] commands = new String[]{
                "cd /data",
                "ls",
                "cat hello.txt"
        };
        ResultDataTo<String> resultDataTo = jschShellClient.execMultiCommand(commands);
        if (!resultDataTo.isSuccess()) {
            System.out.println(resultDataTo.getMessage());
            return;
        }
        System.out.println(resultDataTo.getData());
        jschShellClient.disconnect();
    }
}
