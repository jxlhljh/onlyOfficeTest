package cn.ljhua.onlyoffice.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * httpclient Sock5支持参考：https://blog.csdn.net/weixin_34075268/article/details/92040047
 * @author liujh
 *
 */
public class HttpsKitWithProxyAuth {

	private static Logger logger = LoggerFactory.getLogger(HttpsKitWithProxyAuth.class);
	private static final int CONNECT_TIMEOUT = 10000;// 设置连接建立的超时时间为10000ms
	private static final int SOCKET_TIMEOUT = 30000; // 多少时间没有数据传输
	private static final int HttpIdelTimeout = 30000;//空闲时间
	private static final int HttpMonitorInterval = 10000;//多久检查一次
	private static final int MAX_CONN = 200; // 最大连接数
	private static final int Max_PRE_ROUTE = 200; //设置到路由的最大连接数,
	private static CloseableHttpClient httpClient; // 发送请求的客户端单例
	private static PoolingHttpClientConnectionManager manager; // 连接池管理类
	private static ScheduledExecutorService monitorExecutor;
	
	private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String APPLICATION_JSON = "application/json";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";
	private static final Object syncLock = new Object(); // 相当于线程锁,用于线程安全
	
	/**
	 * 代理相关的变量,
	 */
	public static final String HTTP = "http";//proxyType的取值之一http
	public static final String SOCKS = "socks";//proxyType的取值之一socks
	private static boolean needProxy = false; //是否需要代理连接
	private static boolean needLogin = false;//代理连接是否需要账号和密码，为true时填上proxyUsername和proxyPassword
	private static String proxyType = HTTP; //代理类型,http,socks分别为http代理和sock5代理
	private static String proxyHost = "127.0.0.1"; //代理IP
	private static int proxyPort = 1080; //代理端口
	private static String proxyUsername = "sendi";//代理账号，needLogin为true时不能为空
	private static String proxyPassword = "123456";//代理密码，needLogin为true时不能为空
	
	private static RequestConfig requestConfig = RequestConfig.custom()
			.setConnectionRequestTimeout(CONNECT_TIMEOUT)
			.setConnectTimeout(CONNECT_TIMEOUT)
			//.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
			.setSocketTimeout(SOCKET_TIMEOUT).build();
	
	static {
		/**
		 * Sock5代理账号和密码设置
		 * 如果账号和密码都不为空表示需要账号密码认证,因为这个是全局生效，因此在这里直接设置
		 * 可通过Authenticator.setDefault(null)取消全局配置
		 * Authenticator.setDefault(Authenticator a)关于a参数的说明如下：
		 * (The authenticator to be set. If a is {@code null} then any previously set authenticator is removed.)
		 */
		if(needProxy && SOCKS.equals(proxyType) && needLogin){
			//用户名和密码验证
	    	Authenticator.setDefault(new Authenticator(){
	            protected  PasswordAuthentication  getPasswordAuthentication(){
	                PasswordAuthentication p = new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
	                return p;
	            }
	        });
		}
	}
	
	/**
	 * 设置代理信息，可以在发请求前进行调用，用于替换此类中的代理相关的变量,全局设置一次就可
	 * needProxy 是否需要代理连接
	 * needLogin 代理连接是否需要账号和密码，为true时填上proxyUsername和proxyPassword
	 * proxyType 代理类型,http,socks分别为http代理和sock5代理
	 * proxyHost 代理IP
	 * proxyPort 代理端口
	 * proxyUsername 代理账号，needLogin为true时不能为空
	 * proxyPassword 代理密码，needLogin为true时不能为空
	 */
	public static void setProxy(boolean needProxy,boolean needLogin,String proxyType,String proxyHost,int proxyPort,String proxyUserName,String proxyPassword){
		
		HttpsKitWithProxyAuth.needProxy = needProxy;
		HttpsKitWithProxyAuth.needLogin = needLogin;
		HttpsKitWithProxyAuth.proxyType = proxyType;
		HttpsKitWithProxyAuth.proxyHost = proxyHost;
		HttpsKitWithProxyAuth.proxyPort = proxyPort;
		HttpsKitWithProxyAuth.proxyUsername = proxyUserName;
		HttpsKitWithProxyAuth.proxyPassword = proxyPassword;
		
	}

	private static CloseableHttpClient getHttpClient() {

		if (httpClient == null) {
			// 多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
			synchronized (syncLock) {
				if (httpClient == null) {
					
					try {
						httpClient = createHttpClient();
					} catch (KeyManagementException e) {
						logger.error("error",e);
					} catch (NoSuchAlgorithmException e) {
						logger.error("error",e);
					} catch (KeyStoreException e) {
						logger.error("error",e);
					}
					
					// 开启监控线程,对异常和空闲线程进行关闭
					monitorExecutor = Executors.newScheduledThreadPool(1);
					monitorExecutor.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							
							// 关闭异常连接
							manager.closeExpiredConnections();
							
							// 关闭5s空闲的连接
							manager.closeIdleConnections(HttpIdelTimeout,TimeUnit.MILLISECONDS);
							
							//logger.info(manager.getTotalStats().toString());
							//logger.info("close expired and idle for over "+HttpIdelTimeout+"ms connection");
						}
						
					}, HttpMonitorInterval, HttpMonitorInterval, TimeUnit.MILLISECONDS);
				}
			}
		}
		return httpClient;
	}

	/**
	 * 构建httpclient实例
	 * @return
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	private static CloseableHttpClient createHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		
		SSLContextBuilder builder = new SSLContextBuilder();
        // 全部信任 不做身份鉴定
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        });

        
        ConnectionSocketFactory plainSocketFactory = null;
        LayeredConnectionSocketFactory sslSocketFactory = null;
        
        /**
		 * 如果需要进行Sock5代理访问开放如下代码
		 * */
		if(needProxy && SOCKS.endsWith(proxyType)){
			
	        plainSocketFactory = new MyConnectionSocketFactory();
			sslSocketFactory = new MySSLConnectionSocketFactory(builder.build());
			
	    	
		}else {
			
			plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
			sslSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
			
		}
		
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("http", plainSocketFactory)
				.register("https", sslSocketFactory).build();

		manager = new PoolingHttpClientConnectionManager(registry);
		// 设置连接参数
		manager.setMaxTotal(MAX_CONN); // 最大连接数
		manager.setDefaultMaxPerRoute(Max_PRE_ROUTE); // 路由最大连接数

		// 请求失败时,进行请求重试
		HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
			
			@Override
			public boolean retryRequest(IOException e, int i,	HttpContext httpContext) {
				
				if (i > 3) {
					// 重试超过3次,放弃请求
					logger.error("retry has more than 3 time, give up request");
					return false;
				}
				if (e instanceof NoHttpResponseException) {
					// 服务器没有响应,可能是服务器断开了连接,应该重试
					logger.error("receive no response from server, retry");
					return true;
				}
				if (e instanceof SSLHandshakeException) {
					// SSL握手异常
					logger.error("SSL hand shake exception");
					return false;
				}
				if (e instanceof InterruptedIOException) {
					// 超时
					logger.error("InterruptedIOException");
					return false;
				}
				if (e instanceof UnknownHostException) {
					// 服务器不可达
					logger.error("server host unknown");
					return false;
				}
				if (e instanceof ConnectTimeoutException) {
					// 连接超时
					logger.error("Connection Time out");
					return false;
				}
				if (e instanceof SSLException) {
					logger.error("SSLException");
					return false;
				}

				HttpClientContext context = HttpClientContext.adapt(httpContext);
				HttpRequest request = context.getRequest();
				
				if (!(request instanceof HttpEntityEnclosingRequest)) {
					// 如果请求不是关闭连接的请求
					return true;
				}
				return false;
			}
		};

		
		CloseableHttpClient client = null;
		/**
		 * 如果需要进行HTTPS代理访问开放如下代码
		 * */
		if(needProxy && HTTP.endsWith(proxyType)){
			
			client = HttpClients.custom()
					.setConnectionManager(manager)
					.setProxy(new HttpHost(proxyHost, proxyPort))
					.setRetryHandler(handler).build();
			
		}else {
			
			client = HttpClients.custom()
					.setConnectionManager(manager)
					.setRetryHandler(handler).build();
			
		}
		
		return client;
	}
	
    public static String get(String url) {
    	return get(url, null);
    }

	public static String get(String url,Map<String,Object> headerParams) {
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",USER_AGENT);
		httpGet.setConfig(requestConfig);
		
		if(headerParams != null && headerParams.size()>0){
	    	for(String headerName : headerParams.keySet()) {
	    		httpGet.setHeader(headerName,headerParams.get(headerName)+"");
	        }
	    }
		
		CloseableHttpResponse response = null;
		InputStream in = null;

		String result = null;
		
		try {
			
			HttpClientContext ctx  = createContext();
			response = getHttpClient().execute(httpGet,ctx);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent();
				result = IOUtils.toString(in, "utf-8");
			}
			
		} catch (Exception e) {
			logger.error("error",e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
			
			try {
				if (response != null) response.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
		}
		
		return result;
	}
	
	public static String postJson(String url,Map<String,Object> requestParams) {
		return postJson(url, JsonUtil.toJSONString(requestParams));
	}
	
	public static String postJson(String url,Map<String,Object> requestParams,Map<String,String> headerParams) {
		return postJson(url, JsonUtil.toJSONString(requestParams),headerParams);
	}
	
	public static String postJson(String url,String requestParamStr) {
		return postJson(url, requestParamStr, null);
	}
	
	/**
	 * PUT方式调用http请求方法
	 * @param url
	 * @param requestParamStr
	 * @param headerParams
	 * @return
	 */
	public static String put(String url,String requestParamStr,Map<String,String> headerParams) {
		
		HttpPut httpput = new HttpPut(url);
		httpput.setHeader("Content-Type", APPLICATION_JSON+";charset=" + CharEncoding.UTF_8);
		httpput.setHeader("Accept",APPLICATION_JSON+";charset=" +CharEncoding.UTF_8);
		httpput.setHeader("User-Agent",USER_AGENT);
		
		if(headerParams != null && headerParams.size()>0){
        	for(String headerName : headerParams.keySet()) {
        		httpput.setHeader(headerName,headerParams.get(headerName)+"");
            }
        }
		
		StringEntity se = new StringEntity(requestParamStr,CharEncoding.UTF_8);
        se.setContentType(APPLICATION_JSON+";charset=" +CharEncoding.UTF_8);
        httpput.setEntity(se);
        httpput.setConfig(requestConfig);
		
		CloseableHttpResponse response = null;
		InputStream in = null;

		String result = null;
		
		try {
			
			HttpClientContext ctx  = createContext();
			response = getHttpClient().execute(httpput,ctx);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent();
				result = IOUtils.toString(in, "utf-8");
			}
			
		} catch (Exception e) {
			logger.error("error",e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
			
			try {
				if (response != null) response.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
		}
		
		return result;
		
	}
	
	/**
	 * 创建一个HttpClientContext
	 * @return
	 * @throws MalformedChallengeException
	 */
	public static HttpClientContext createContext() throws MalformedChallengeException{
		
		HttpClientContext ctx  = HttpClientContext.create();
		
		/**
		 * 如果需要进行Sock5代理访问
		 */
		if(needProxy && SOCKS.endsWith(proxyType)){
			InetSocketAddress socksaddr = new InetSocketAddress(proxyHost,proxyPort);
            ctx.setAttribute("socks.address", socksaddr);
		}else{
			
			/**
			 * 如果需要进行HTTPS代理访问开放如下代码
			 */
			if(needProxy && HTTP.endsWith(proxyType)){
				
				/**
				 * 代理连接认证如果需要认证账号和密码时处理
				 */
				if(needLogin){
					
					AuthState authState = new AuthState();
		            BasicScheme basicScheme = new BasicScheme();
		            basicScheme.processChallenge(new BasicHeader(AUTH.PROXY_AUTH, "BASIC realm=default"));  
		            authState.update(basicScheme, new UsernamePasswordCredentials(proxyUsername, proxyPassword));
		            ctx.setAttribute(HttpClientContext.PROXY_AUTH_STATE, authState);
					
				}
				
			}
			
		}
		return ctx;
	}
	
	public static String postJson(String url,String requestParamStr,Map<String,String> headerParams) {
		
		HttpPost httppost = new HttpPost(url);
		
		httppost.setHeader("Content-Type", APPLICATION_JSON+";charset=" + CharEncoding.UTF_8);
        httppost.setHeader("Accept",APPLICATION_JSON+";charset=" +CharEncoding.UTF_8);
        httppost.setHeader("User-Agent",USER_AGENT);
        
        if(headerParams != null && headerParams.size()>0){
        	for(String headerName : headerParams.keySet()) {
        		httppost.setHeader(headerName,headerParams.get(headerName)+"");
            }
        }
        
        StringEntity se = new StringEntity(requestParamStr,CharEncoding.UTF_8);
        se.setContentType(APPLICATION_JSON+";charset=" +CharEncoding.UTF_8);
        httppost.setEntity(se);
        
        httppost.setConfig(requestConfig);
		
		CloseableHttpResponse response = null;
		InputStream in = null;

		String result = null;
		
		try {
			
			HttpClientContext ctx  = createContext();
			response = getHttpClient().execute(httppost,ctx);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent();
				result = IOUtils.toString(in, "utf-8");
			}
			
		} catch (Exception e) {
			logger.error("error",e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
			
			try {
				if (response != null) response.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
		}
		
		return result;
	}
	
	//requestParamStr---------->>> name=test&age=12
	public static String postFormUrlencoded(String url,String requestParamStr) {
		return postFormUrlencoded(url, requestParamStr ,null);
		
	}
	
	public static String postFormUrlencoded(String url,String requestParamStr,Map<String,Object> headerParams) {
		Map<String,String> requestParams = new HashMap<String,String>();
		
		String[] strs = requestParamStr.split("&");
		for(String str : strs) {
			String[] keyValues = str.split("=");
			if(keyValues.length == 2) {
				requestParams.put(keyValues[0], keyValues[1]);
			}
		}
		
		return postFormUrlencoded(url, requestParams,headerParams);
		
	}
	
	public static String postFormUrlencoded(String url,Map<String,String> requestParams) {
		return postFormUrlencoded(url,requestParams,null);
	}
	
	public static String postFormUrlencoded(String url,Map<String,String> requestParams,Map<String,Object> headerParams) {
		
		HttpPost httppost = new HttpPost(url);
		
		//application/json
		httppost.setHeader("Content-Type", APPLICATION_FORM_URLENCODED+";charset=" + CharEncoding.UTF_8);
        httppost.setHeader("Accept",APPLICATION_JSON+";charset=" +CharEncoding.UTF_8);
        httppost.setHeader("User-Agent",USER_AGENT);
        
        if(headerParams != null && headerParams.size()>0){
        	for(String headerName : headerParams.keySet()) {
        		httppost.setHeader(headerName,headerParams.get(headerName)+"");
            }
        }
        
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        
        for(String keyStr : requestParams.keySet()) {
            formparams.add(new BasicNameValuePair(keyStr, requestParams.get(keyStr)));
        }
        
        UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        httppost.setEntity(uefe);
        
        httppost.setConfig(requestConfig);
		
		CloseableHttpResponse response = null;
		InputStream in = null;

		String result = null;
		
		try {
			
			HttpClientContext ctx  = createContext();
			response = getHttpClient().execute(httppost,ctx);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent();
				result = IOUtils.toString(in, "utf-8");
			}
			
		} catch (Exception e) {
			logger.error("error",e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
			
			try {
				if (response != null) response.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
		}
		
		return result;
		 
	}
	
	//文件上传的通用方法例子测试, 除了file部分参数外，写死了格外的字段参数如scene,output，后台将接收到file,scene,output三个参数，可以根据需求修改
	public static String postFormMultipart(String url,InputStream fin,String originalFilename) {
		
		HttpPost httppost = new HttpPost(url);
        
        httppost.setConfig(requestConfig);
        InputStreamBody bin = new InputStreamBody(fin, originalFilename);
        
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addPart("file",bin);
        multipartEntityBuilder.addPart("fileName",new StringBody(originalFilename,ContentType.TEXT_PLAIN));
        multipartEntityBuilder.addPart("fileSize",new StringBody("1024",ContentType.TEXT_PLAIN));
        multipartEntityBuilder.addPart("scene", new StringBody("default",ContentType.TEXT_PLAIN));
        multipartEntityBuilder.addPart("output", new StringBody("json2",ContentType.TEXT_PLAIN));
        HttpEntity reqEntity = multipartEntityBuilder.build();
        
        httppost.setEntity(reqEntity);
		
		CloseableHttpResponse response = null;
		InputStream in = null;
		String result = null;
		try {
			
			HttpClientContext ctx  = createContext();
			response = getHttpClient().execute(httppost,ctx);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent();
				result = IOUtils.toString(in, "utf-8");
			}
			
		} catch (Exception e) {
			logger.error("error",e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
			
			try {
				if (response != null) response.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
		}
		
		return result;
		
	}
	
	/**
	 * 下载文件到本地
	 * @param downloadUrl
	 * @param fullFilePath
	 */
	public static void downloadFile(String downloadUrl,String savePathAndName){
		HttpGet httpGet = new HttpGet(downloadUrl);
		httpGet.setHeader("User-Agent",USER_AGENT);
		httpGet.setConfig(requestConfig);
		
		CloseableHttpResponse response = null;
		InputStream in = null;

		try {
			
			response = getHttpClient().execute(httpGet,HttpClientContext.create());
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				in = entity.getContent();
				
				//如果path传进来是/结束的话处理一下，先去掉。
				FileOutputStream out = new FileOutputStream(new File(savePathAndName));
				IOUtils.copy(in, out);
				out.close();
				
			}
			
		} catch (IOException e) {
			logger.error("error",e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
			
			try {
				if (response != null) response.close();
			} catch (IOException e) {
				logger.error("error",e);
			}
		}
	}
	
	/**
	 * 下载文件到本地
	 * @param downloadUrl
	 * @param savefileName
	 * @param savePath
	 */
	public static void downloadFile(String downloadUrl,String saveFileName,String savePath){
		
		//如果path传进来是/结束的话处理一下，先去掉。
		String savePathAndName = savePath.endsWith("/") ? savePath.substring(0,savePath.lastIndexOf("/")) : savePath;
		downloadFile(downloadUrl, savePathAndName);
		
	}
	

	/**
	 * 关闭连接池
	 */
	public static void closeConnectionPool() {
		
		if(manager != null) manager.close();
		if(monitorExecutor != null) monitorExecutor.shutdown();
		try {if(httpClient != null) httpClient.close();} catch (IOException e) {logger.error("error",e);}
		
		manager = null;
		monitorExecutor = null;
		httpClient = null;
		
	}
	
	private static class MyConnectionSocketFactory extends PlainConnectionSocketFactory {
		
	    @Override
	    public Socket createSocket(final HttpContext context) throws IOException {
	        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
	        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
	        return new Socket(proxy);
	    }

	    @Override
	    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
	                                InetSocketAddress localAddress, HttpContext context) throws IOException {
	        // Convert address to unresolved
	        InetSocketAddress unresolvedRemote = InetSocketAddress
	                .createUnresolved(host.getHostName(), remoteAddress.getPort());
	        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
	    }
	    
	}
	
	private static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {
		 
	    public MySSLConnectionSocketFactory(final SSLContext sslContext) {
	        // You may need this verifier if target site's certificate is not secure
	        super(sslContext, NoopHostnameVerifier.INSTANCE);
	        
	    }

	    @Override
	    public Socket createSocket(final HttpContext context) throws IOException {
	        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
	        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
	        return new Socket(proxy);
	    }

	    @Override
	    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
	                                InetSocketAddress localAddress, HttpContext context) throws IOException {
	        // Convert address to unresolved
	        InetSocketAddress unresolvedRemote = InetSocketAddress
	                .createUnresolved(host.getHostName(), remoteAddress.getPort());
	        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
	    }
	    
	}

	public static void main(String[] args) throws InterruptedException, MalformedURLException {
		
		String url = "https://api.openai.com/v1/chat/completions";
		url = "https://www.baidu.com";
		System.out.println(HttpsKitWithProxyAuth.get(url));
		
		//关闭连接池，正式环境中这个不要关闭
		HttpsKitWithProxyAuth.closeConnectionPool();
		
	}
	
}
