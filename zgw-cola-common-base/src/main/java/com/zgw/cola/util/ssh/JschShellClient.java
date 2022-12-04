package com.zgw.cola.util.ssh;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zgw.cola.domain.ResultDataTo;
import com.zgw.cola.domain.ResultMessageTo;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Shell客户端
 *
 * @author 赵高文
 * @since 2022-09-03
 */
public class JschShellClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(JschShellClient.class);

    // ssh默认端口
    private static final int SSH_DEFAULT_PORT = 22;

    // 连接建立超时时长：30s
    private static final int CONNECT_TIMEOUT = 30000;

    // 命令执行默认超时时长：1s
    private static final int EXEC_COMMAND_TIMEOUT = 1000;

    // Linux上回车符
    private static final String ENTER = "\r";

    // 主机地址
    private final String host;

    // 主机端口
    private final int port;

    // 登录用户名
    private final String username;

    // 登录密码
    private final String password;

    private Session session;

    private ChannelShell channelShell;

    private InputStream inputStream;

    private OutputStream outputStream;

    /**
     * 构造函数
     *
     * @param host 主机地址
     * @param username 登录用户名
     * @param password 登录密码
     */
    public JschShellClient(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = SSH_DEFAULT_PORT;
    }

    /**
     * 建立shell通道（执行命令前必须先建立连接）
     *
     * @return 连接结果
     */
    public ResultMessageTo connect() {
        try {
            JSch jsch = new JSch();
            this.session = jsch.getSession(this.username, this.host, this.port);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            this.session.setConfig(sshConfig);
            this.session.setPassword(this.password);
            this.session.connect(CONNECT_TIMEOUT);
            this.channelShell = (ChannelShell) this.session.openChannel("shell");
            this.channelShell.setPty(true);
            this.channelShell.connect(CONNECT_TIMEOUT);
            return ResultMessageTo.buildSuccess("Connect success.");
        } catch (JSchException e) {
            return ResultMessageTo.buildFail("Connect fail: " + e.getMessage());
        }
    }

    /**
     * 执行shell命令，可多次调用
     *
     * @param command 要执行的命令
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> execCommand(String command) {
        return execCommand(command, EXEC_COMMAND_TIMEOUT);
    }

    /**
     * 执行shell命令，可多次调用
     *
     * @param command 要执行的命令
     * @param timeout 执行超时时间
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> execCommand(String command, int timeout) {
        return execCommand(command, timeout, StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name());
    }

    /**
     * 执行shell命令，可多次调用
     *
     * @param command 要执行的命令
     * @param timeout 执行超时时间
     * @param inCharset 输入流字符集
     * @param outCharset 输出溜字符集
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> execCommand(String command, int timeout, String inCharset, String outCharset) {
        if (StringUtils.isBlank(command)) {
            return new ResultDataTo<>(false, "command is blank!", null);
        }
        int execTimeout = (timeout > 0) ? timeout : EXEC_COMMAND_TIMEOUT;
        if (this.channelShell == null || !this.channelShell.isConnected()) {
            return new ResultDataTo<>(false, "Channel disconnect!", null);
        }
        try {
            if (this.inputStream == null) {
                this.inputStream = this.channelShell.getInputStream();
            }
            if (this.outputStream == null) {
                this.outputStream = this.channelShell.getOutputStream();
            }
            // 输入命令
            this.outputStream.write(command.getBytes(outCharset));
            this.outputStream.write(ENTER.getBytes(outCharset));
            this.outputStream.flush();
            // 等待命令执行完成
            waitCommandCompleted(execTimeout);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (this.inputStream.available() > 0) {
                byte[] buffers = new byte[this.inputStream.available()];
                int len = this.inputStream.read(buffers);
                if (len < 0) {
                    break;
                }
                byteArrayOutputStream.write(buffers);
            }
            byteArrayOutputStream.flush();
            String outputResult = byteArrayOutputStream.toString(inCharset);
            byteArrayOutputStream.close();
            return new ResultDataTo<>(true, "Execute completed.", outputResult);
        } catch (IOException e) {
            return new ResultDataTo<>(false, "Execute has exception: " + e.getMessage(), null);
        }
    }

    /**
     * 先切换root用户，再执行命令，执行完命令后会自动关闭通道
     *
     * @param command 切换root后要执行的命令
     * @param rootPassword root密码
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> suRootExecCommand(String command, String rootPassword) {
        return suRootExecCommand(command, rootPassword, EXEC_COMMAND_TIMEOUT);
    }

    /**
     * 先切换root用户，再执行命令，执行完命令后会自动关闭通道
     *
     * @param command 切换root后要执行的命令
     * @param rootPassword root密码
     * @param timeout 执行超时时间
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> suRootExecCommand(String command, String rootPassword, int timeout) {
        return suRootExecCommand(command, rootPassword, timeout, StandardCharsets.UTF_8.name(),
                StandardCharsets.UTF_8.name());
    }

    /**
     * 先切换root用户，再执行命令，执行完命令后会自动关闭通道
     *
     * @param command 切换root后要执行的命令
     * @param rootPassword root密码
     * @param timeout 执行超时时间
     * @param inCharset 输入流字符集
     * @param outCharset 输出溜字符集
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> suRootExecCommand(String command, String rootPassword, int timeout,
                                                  String inCharset, String outCharset) {
        if (StringUtils.isBlank(command) || StringUtils.isBlank(rootPassword)) {
            return new ResultDataTo<>(false, "command or rootPassword is blank!", null);
        }
        int execTimeout = (timeout > 0) ? timeout : EXEC_COMMAND_TIMEOUT;
        if (this.channelShell == null || !this.channelShell.isConnected()) {
            return new ResultDataTo<>(false, "Channel disconnect!", null);
        }
        try {
            if (this.inputStream == null) {
                this.inputStream = this.channelShell.getInputStream();
            }
            if (this.outputStream == null) {
                this.outputStream = this.channelShell.getOutputStream();
            }
            // 切换root用户
            this.outputStream.write("su root".getBytes(outCharset));
            this.outputStream.write(ENTER.getBytes(outCharset));
            this.outputStream.flush();
            waitCommandCompleted(EXEC_COMMAND_TIMEOUT);
            // 输入root密码
            this.outputStream.write(rootPassword.getBytes(outCharset));
            this.outputStream.write(ENTER.getBytes(outCharset));
            this.outputStream.flush();
            waitCommandCompleted(EXEC_COMMAND_TIMEOUT);
            // 输入业务命令
            this.outputStream.write(command.getBytes(outCharset));
            this.outputStream.write(ENTER.getBytes(outCharset));
            this.outputStream.flush();
            waitCommandCompleted(execTimeout);
            // 获取终端输出
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (this.inputStream.available() > 0) {
                byte[] buffers = new byte[this.inputStream.available()];
                int len = this.inputStream.read(buffers);
                if (len < 0) {
                    break;
                }
                byteArrayOutputStream.write(buffers);
            }
            byteArrayOutputStream.flush();
            String outputResult = byteArrayOutputStream.toString(inCharset);
            byteArrayOutputStream.close();
            return new ResultDataTo<>(true, "Execute completed.", outputResult);
        } catch (IOException e) {
            return new ResultDataTo<>(false, "Execute has exception: " + e.getMessage(), null);
        } finally {
            disconnect();
        }
    }

    /**
     * 执行多条shell命令，执行完成后会自动关闭通道（建议用于执行多条及时响应性的简单命令）
     *
     * @param commands 多条命令
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> execMultiCommand(final String[] commands) {
        return execMultiCommand(commands, StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name());
    }

    /**
     * 执行多条shell命令，执行完成后会自动关闭通道（推荐用于执行多条及时响应性的简单命令）
     *
     * @param commands 要执行的命令
     * @param inCharset 输入流字符集
     * @param outCharset 输出溜字符集
     * @return 执行结果，isSuccess表示是否执行完成，message表示错误信息，data表示终端输出
     */
    public ResultDataTo<String> execMultiCommand(final String[] commands, String inCharset, String outCharset) {
        if (commands == null || commands.length == 0) {
            return new ResultDataTo<>(false, "commands is blank!", null);
        }
        for (String command : commands) {
            if (StringUtils.isBlank(command)) {
                return new ResultDataTo<>(false, "Blank value in the commands array!", null);
            }
        }
        if (this.channelShell == null || !this.channelShell.isConnected()) {
            return new ResultDataTo<>(false, "Channel disconnect!", null);
        }
        try {
            if (this.inputStream == null) {
                this.inputStream = this.channelShell.getInputStream();
            }
            if (this.outputStream == null) {
                this.outputStream = this.channelShell.getOutputStream();
            }
            for (String command : commands) {
                // 逐条输入命令
                this.outputStream.write(command.getBytes(outCharset));
                this.outputStream.write(ENTER.getBytes(outCharset));
                this.outputStream.flush();
                // 等待当前命令执行完成
                waitCommandCompleted(EXEC_COMMAND_TIMEOUT);
            }
            // 获取终端输出
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (this.inputStream.available() > 0) {
                byte[] buffers = new byte[this.inputStream.available()];
                int len = this.inputStream.read(buffers);
                if (len < 0) {
                    break;
                }
                byteArrayOutputStream.write(buffers);
            }
            byteArrayOutputStream.flush();
            String outputResult = byteArrayOutputStream.toString(inCharset);
            byteArrayOutputStream.close();
            return new ResultDataTo<>(true, "Execute completed.", outputResult);
        } catch (IOException e) {
            return new ResultDataTo<>(false, "Execute has exception: " + e.getMessage(), null);
        } finally {
            disconnect();
        }
    }

    /**
     * 断开连接（shell通道使用完成后必选断开连接，节省系统资源）
     */
    public void disconnect() {
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                LOGGER.error("JschShellClient.disconnect outputStream.close has error: {}", e.getMessage());
            }
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                LOGGER.error("JschShellClient.disconnect inputStream.close has error: {}", e.getMessage());
            }
        }
        if (this.channelShell != null) {
            this.channelShell.disconnect();
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    /**
     * 等待执行完成
     *
     * @param execTimeout 执行超时时长
     */
    private void waitCommandCompleted(int execTimeout) {
        long endTime = System.currentTimeMillis() + execTimeout;
        while (true) {
            if (System.currentTimeMillis() >= endTime) {
                return;
            }
        }
    }
}
