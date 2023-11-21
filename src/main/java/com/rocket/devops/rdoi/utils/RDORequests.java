package com.rocket.devops.rdoi.utils;

import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.common.def.MessageCodeConstants;
import com.rocket.devops.rdoi.common.RdoResult;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import com.rocket.devops.rdoi.connection.RDOiConnCfg;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
public class RDORequests {

//	public static RDOiConnection getCurrentConnection(Run<?, ?> run){
//		RDOiConnection currentConnection = null;
//		Optional<Instance> instance = PluginUtils.getInstance(run);
//		if (instance.isPresent()){
//			currentConnection = RDOiConnectionConfig.get().getSelectedConnectionByName(instance.get().getInstanceName());
//			currentConnection.setRun(run);
//		}else {
//			throw new RDORuntimeException(Messages.RDOi_rdop_base_url_null());
//		}
//		return currentConnection;
//	}
//	public static String getBaseUrl(Run<?, ?> run) {
//
//		String  baseUrl = null;
//		baseUrl = getCurrentConnection(run).getUrl();
//		if (baseUrl != null){
//			return baseUrl;
//		}else {
//			throw new RDORuntimeException(Messages.RDOi_rdop_base_url_null());
//		}
//	}

	public static String getBaseUrlWithoutRun() {

		String  baseUrl = null;
		baseUrl = RDOiConnCfg.get().getConn().get(0).getUrl();
		if (baseUrl != null){
			return baseUrl;
		}else {
			throw new RDORuntimeException(Messages.RDOi_rdop_base_url_null());
		}
	}
	public static String getRdopToken(boolean renew) {
		String rdopToken = RDOiConnCfg.get().getConn().get(0).getRDOpToken(renew);
		if (rdopToken == null){
			throw new RDORuntimeException(Messages.RDOi_apiToken_null());
		}
		return rdopToken;
	}
	public static RdoResult getValidToken(String url, Map<String, String> reqJson) {

		HttpRequest.Builder builder = HttpRequestUtils.newRequestBuilder(getBaseUrlWithoutRun() + url);
		HttpRequest httpRequest = builder
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(getParamsUrlEncoded(reqJson))
				.build();
		RdoResult result = HttpRequestUtils.sendToRdo(httpRequest);
		return result;
	}

	public static RdoResult testConnection(String url, Map<String, String> reqJson, String baseURL) {

		HttpRequest.Builder builder = HttpRequestUtils.newRequestBuilder(baseURL + url);
		HttpRequest httpRequest = builder
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(getParamsUrlEncoded(reqJson))
				.build();
		RdoResult result = HttpRequestUtils.sendToRdo(httpRequest);
		return result;
	}

	private static HttpRequest.BodyPublisher getParamsUrlEncoded(Map<String, String> parameters) {
		String urlEncoded = parameters.entrySet()
				.stream()
				.map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));
		return HttpRequest.BodyPublishers.ofString(urlEncoded);
	}

	private static RdoResult resendIfInvalidToken(RdoResult result, HttpRequest.Builder builder){
		if ((result.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS)) && result.getReturncode().equals(Constants.RDOP_INVALID_TOKEN)){
			HttpRequest httpRequest = builder.setHeader(Constants.RDOP_TOKEN, getRdopToken(true)).build();
			RdoResult resultResend = HttpRequestUtils.sendToRdo(httpRequest);
			return resultResend;
		}
		return result;
	}
	public static RdoResult actionRequest(String url, String jsonBody) {
		HttpRequest.Builder builder = HttpRequestUtils.newRequestBuilder(getBaseUrlWithoutRun() + url);
		HttpRequest httpRequest = builder
				.header("Content-Type", "application/json")
				.header(Constants.RDOP_TOKEN, getRdopToken(false))
				.header(Constants.JENKINS_API_KEY_HEADER, Constants.JENKINS_API_VALUE_HEADER)
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.build();
		RdoResult result = HttpRequestUtils.sendToRdo(httpRequest);
		RdoResult resultResend = resendIfInvalidToken(result, builder);
		return resultResend;
	}

	public static RdoResult getRequest(String url) {
		HttpRequest.Builder builder = HttpRequestUtils.newRequestBuilder(getBaseUrlWithoutRun() + url);
		HttpRequest httpRequest = builder
				.header("Content-Type", "application/json")
				.header(Constants.RDOP_TOKEN, getRdopToken(false))
				.header(Constants.JENKINS_API_KEY_HEADER, Constants.JENKINS_API_VALUE_HEADER)
				.GET()
				.build();
		RdoResult result = HttpRequestUtils.sendToRdo(httpRequest);

		RdoResult resultResend = resendIfInvalidToken(result, builder);
		return resultResend;
	}
}
