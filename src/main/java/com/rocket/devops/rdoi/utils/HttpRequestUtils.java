package com.rocket.devops.rdoi.utils;

import com.rocket.devops.rdoi.common.AppConfiguration;
import com.rocket.devops.rdoi.common.RdoResult;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * Mainly for communicate with RDO service
 */
public final class HttpRequestUtils {
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class);
	private static HttpClient httpClient;

	/**
	 * httpClient with default configurations
	 * @return
	 */
	public static HttpClient getHttpClient() {
		if (httpClient != null) return httpClient;
		HttpClient.Builder builder = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(AppConfiguration.HTTP_CLIENT_CONNECTION_TIMEOUT))
				.followRedirects(HttpClient.Redirect.ALWAYS)
				.version(HttpClient.Version.HTTP_2);
		ignoreCertVerify(builder);
		return builder.build();
	}

	/**
	 * Ignore cert verify for this version.
	 * @return
	 */
	private static void ignoreCertVerify(HttpClient.Builder builder) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			TrustManager[] trustManagers = new TrustManager[] {
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] chain, String authType) {}
						public void checkServerTrusted(X509Certificate[] chain, String authType) {}
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					}
			};
			sslContext.init(null, trustManagers, new SecureRandom());
			builder.sslContext(sslContext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * request builder with default configurations.
	 * @param url
	 * @return
	 */
	public static HttpRequest.Builder newRequestBuilder(String url) {
		try {
			URI uri = new URI(url);
			return HttpRequest.newBuilder(uri)
					.timeout(Duration.ofSeconds(AppConfiguration.HTTP_RESPONSE_TIMEOUT))
					.version(HttpClient.Version.HTTP_2);
		} catch (URISyntaxException e) {
			throw new RDORuntimeException(e);
		}
	}


	/**
	 * Adapt to RDO REST API response format
	 * @param request
	 * @return
	 */
	public static RdoResult sendToRdo(HttpRequest request) {
		return send(request, RdoResult.class);
	}

	/**
	 * Support various of response format
	 * @param request
	 * @param cls
	 * @param <T>
	 * @return
	 */
	public static <T> T send(HttpRequest request, Class<T> cls) {
		Assert.notNull(cls);
		try {
			HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			String str = response.body();
			return JacksonUtils.deserialize(str, cls);
		} catch (IOException e) {
			throw new RDORuntimeException(e);
		} catch (InterruptedException e) {
			throw new RDORuntimeException(e);
		}
	}

}
