package com.appchina.rpc.thrift.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.appchina.rpc.base.client.Client;
import com.appchina.rpc.base.client.ClientIOException;
import com.appchina.rpc.base.client.RemoteException;
import com.appchina.rpc.base.utils.CloseUtils;
import com.appchina.rpc.base.utils.ExceptionUtils;
import com.appchina.rpc.thrift.server.AbstractThriftServer;
import com.appchina.rpc.thrift.server.AbstractThriftServer.ThriftServceImpl;
import com.appchina.rpc.thrift.server.ServerHelper;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;
import com.appchina.rpc.thrift.support.ThriftServce;

/**
 * thrift实现的Client
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftClient implements Client<Request, Response> {
	private static Log log = LogFactory.getLog(ThriftClient.class);
	
	protected Socket socket;
	protected TTransport transport;
	protected ThriftServce.Iface iface;
	
	protected boolean closed = true;
	protected long sendCount = 0;
	protected long openTimestamp = -1;
	
	protected ThriftClientConfig config = new ThriftClientConfig();
	
	public ThriftClient() { }
	
	public ThriftClient(ThriftClientConfig config) {
		this.config = config;
	}
	
	@Override
	public void open() throws ClientIOException {
		try {
			socket = new Socket();
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
			socket.setSoLinger(false, 0);
			socket.setSoTimeout(config.timeout);
			socket.connect(new InetSocketAddress(config.host, config.port), config.connectionTimeout);
			//使用自己的socket对象，是为了在ping和send的时候设置不同的超时时间
			transport = new TSocket(socket);
			if (config.framed) {
				transport = new TFramedTransport(transport);
			}
			//iface是Thrift生成的接口，在Thrift的包装下，是个远程调用的接口
			iface = new ThriftServce.Client(config.protocolFactory.getProtocol(transport));
			
			closed = false;
			sendCount = 0;
			openTimestamp = System.currentTimeMillis();
			if(log.isInfoEnabled()){
				log.info("Open client: " + config.toString());
			}
		} catch (TTransportException | IOException e) {
			throw new ClientIOException("Can not open client " + toString(), e);
		}
	}

	@Override
	public void close() {
		closed = true;
		CloseUtils.close(transport);
		if(log.isInfoEnabled()){
			log.info("Close client: " + this);
		}
	}

	@Override
	public long openTimestamp() {
		return openTimestamp;
	}

	@Override
	public long sendCount() {
		return sendCount;
	}

	@Override
	public boolean closed() {
		return closed;
	}
	
	/**
	 * 这会向服务端发送一个请求，判断服务端是否可用
	 * 
	 * @see {@link AbstractThriftServer#setStopTimeoutVal(int)}
	 * @see {@link AbstractThriftServer#stop()}
	 * @see {@link ThriftServceImpl#ping()}
	 * 
	 * @return 连接不可用，或者服务已停止，或者服务正在停止，都会返回false
	 * */
	@Override
	public boolean ping() {
		try {
			//这里需要使用connectionTimeout
			socket.setSoTimeout(config.connectionTimeout);
			//ping的返回值由服务器决定
			return !closed() && iface.ping();
		} catch (TTransportException | IOException e) {
			//iface.ping的服务端并没有抛出任何异常，这里出现这两种异常表示网络不可用，因而返回false
			return false;
		} catch (Exception e) {
			//这种异常根本不会出现，如果出现应该抛出来，这是框架的BUG
			throw ExceptionUtils.getRuntimeException(e);
		}
	}
	
	/**
	 * 向服务端发送一个请求，除{@link ClientIOException}之外的其它异常被视为RemoteException<br>
	 * <br>
	 * 服务端出现了任何异常都会使用{@link ServerHelper#putError(Response, String)}放入header<br>
	 * 这里需要使用{@link ClientHelper#validateError(Response, String)}验证服务端是否放入了异常，如果放入了异常，就需要抛出RemoteException<br>
	 * 所以，服务端不可以抛出任何异常 {@link ThriftServceImpl#execute(Request)} <br>
	 * <br>
	 * 这么做是因为thrift异常体系不好用，程序是想将网络级错误、框架级错误，应用级错误进行分离（应用级错误包含在返回结果，由更高层处理）。<br>
	 * <br>
	 * @throws ClientIOException 网络错误
	 * @throws RemoteException 服务端未知错误
	 * */
	@Override
	public Response send(Request request) throws ClientIOException, RemoteException {
		try {
			sendCount++;
			ClientHelper.putCredential(request, config.from, config.token);
			socket.setSoTimeout(config.timeout);
			Response response = iface.execute(request);
			ClientHelper.validateError(response);
			return response;
		} catch (RemoteException e) {
			//服务端发生了异常
			e.addSuppressed(new SuppressedException(config.host + ":" + config.port));
			throw e;
		} catch (TTransportException | IOException e) {
			//这里出现这两种异常表示网络不可用
			throw new ClientIOException("can not send or receive data at " + config.host + ":" + config.port, e);
		} catch (Exception e) {
			//这种异常根本不会出现，如果出现应该抛出来，这是框架的BUG
			throw ExceptionUtils.getRuntimeException(e);
		}
	}

	public String getHost() {
		return config.getHost();
	}

	public void setHost(String host) {
		config.setHost(host);
	}

	public int getPort() {
		return config.getPort();
	}

	public void setPort(int port) {
		config.setPort(port);
	}

	public int getTimeout() {
		return config.getTimeout();
	}

	public void setTimeout(int timeout) {
		config.setTimeout(timeout);
	}

	public int getConnectionTimeout() {
		return config.getConnectionTimeout();
	}

	public void setConnectionTimeout(int connectionTimeout) {
		config.setConnectionTimeout(connectionTimeout);
	}

	public boolean isFramed() {
		return config.isFramed();
	}

	public void setFramed(boolean framed) {
		config.setFramed(framed);
	}

	public TProtocolFactory getProtocolFactory() {
		return config.getProtocolFactory();
	}

	public void setProtocolFactory(TProtocolFactory protocolFactory) {
		config.setProtocolFactory(protocolFactory);
	}

	public String getFrom() {
		return config.getFrom();
	}

	public void setFrom(String from) {
		config.setFrom(from);
	}

	public String getToken() {
		return config.getToken();
	}

	public void setToken(String token) {
		config.setToken(token);
	}

	public String toString() {
		return config.toString();
	}
	
}
