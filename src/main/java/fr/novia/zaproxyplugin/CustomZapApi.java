package fr.novia.zaproxyplugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseFactory;
import org.zaproxy.clientapi.core.ClientApiException;

 



public class CustomZapApi {
	
	public  String zapProxyHost="";
	public  String zapProxyPort="";
	
	public CustomZapApi(String zapProxyHost, String zapProxyPort) {
		super();
		this.zapProxyHost = zapProxyHost;
		this.zapProxyPort = zapProxyPort;
	}
	
	
	 
	
	/**************************************** Methodes utilitaires ********************/
	
	
	/************************* Authentification ******************************/
	
	public ApiResponse getSupportedAuthenticationMethods() throws ClientApiException {
		Map<String, String> map = null;
		return callApi("authentication", "view", "getSupportedAuthenticationMethods", map);
	}
	
	public ApiResponse getAuthenticationMethodConfigParams(String authmethodname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("authMethodName", authmethodname);
		return callApi("authentication", "view", "getAuthenticationMethodConfigParams", map);
	}
	
	public ApiResponse getAuthenticationCredentialsConfigParams(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return callApi("users", "view", "getAuthenticationCredentialsConfigParams", map);
	}
	
	public ApiResponse setLoggedInIndicator(String apikey, String contextid, String loggedinindicatorregex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("loggedInIndicatorRegex", loggedinindicatorregex);
		return callApi("authentication", "action", "setLoggedInIndicator", map);
	}
	
	public ApiResponse getLoggedInIndicator(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return callApi("authentication", "view", "getLoggedInIndicator", map);
	}
	
	public ApiResponse setLoggedOutIndicator(String apikey, String contextid, String loggedoutindicatorregex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("loggedOutIndicatorRegex", loggedoutindicatorregex);
		return callApi("authentication", "action", "setLoggedOutIndicator", map);
	}
	public ApiResponse getLoggedOutIndicator(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return callApi("authentication", "view", "getLoggedOutIndicator", map);
	}
	
	public ApiResponse setAuthenticationMethod(String apikey, String contextid, String authmethodname, String authmethodconfigparams) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("authMethodName", authmethodname);
		map.put("authMethodConfigParams", authmethodconfigparams);
		return callApi("authentication", "action", "setAuthenticationMethod", map);
	}
	
	public ApiResponse getAuthenticationMethod(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return callApi("authentication", "view", "getAuthenticationMethod", map);
	}
	
	public ApiResponse newUser(String apikey, String contextid, String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("name", name);
		return callApi("users", "action", "newUser", map);
	}
	
	public ApiResponse setAuthenticationCredentials(String apikey, String contextid, String userid, String authcredentialsconfigparams) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		map.put("authCredentialsConfigParams", authcredentialsconfigparams);
		return callApi("users", "action", "setAuthenticationCredentials", map);
	}
	
	public ApiResponse getUserById(String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		map.put("userId", userid);
		return callApi("users", "view", "getUserById", map);
	}
	
	/******************************************* AjaxSpider.java ***********************************************************/
	
	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ajaxScan(String apikey, String url, String inscope) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		map.put("inScope", inscope);
		return callApi("ajaxSpider", "action", "scan", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ajaxStatus() throws ClientApiException {
		Map<String, String> map = null;
		return callApi("ajaxSpider", "view", "status", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ajaxResults(String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("start", start);
		map.put("count", count);
		return callApi("ajaxSpider", "view", "results", map);
	}
	
	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ajaxResults( ) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		 
		return callApi("ajaxSpider", "view", "results", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ajaxNumberOfResults() throws ClientApiException {
		Map<String, String> map = null;
		return callApi("ajaxSpider", "view", "numberOfResults", map);
	}
	
	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ajaxSpiderStop(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return callApi("ajaxSpider", "action", "stop", map);
	}
	
	/********************************************** ForcedUser.java *************************************************************/
	
	public ApiResponse isForcedUserModeEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return callApi("forcedUser", "view", "isForcedUserModeEnabled", map);
	}

	public ApiResponse getForcedUser(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return callApi("forcedUser", "view", "getForcedUser", map);
	}

	public ApiResponse setForcedUser(String apikey, String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		return callApi("forcedUser", "action", "setForcedUser", map);
	}

	public ApiResponse setForcedUserModeEnabled(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("boolean", Boolean.toString(bool));
		return callApi("forcedUser", "action", "setForcedUserModeEnabled", map);
	}
	
	
	/****************************************************************************************************************************/
	public ApiResponse scanAsUser(String apikey, String url, String contextid, String userid, String recurse, String ScanPolicyName) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		map.put("contextId", contextid);
		map.put("userId", userid);
		map.put("recurse", recurse);
		map.put("ScanPolicyName", ScanPolicyName);
		return callApi("ascan", "action", "scanAsUser", map);
	}
	public ApiResponse spiderAsUser(String apikey, String url, String contextid, String userid, String maxchildren) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		map.put("contextId", contextid);
		map.put("userId", userid);
		map.put("maxChildren", maxchildren);
		return callApi("spider", "action", "scanAsUser", map);
	}
	
	public ApiResponse spider(String apikey, String url,  String maxchildren) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		map.put("maxChildren", maxchildren);
		return callApi("spider", "action", "scan", map);
	}
	

	
	public ApiResponse scanPolicyNames() throws ClientApiException {
		Map<String, String> map = null;
		return callApi("ascan", "view", "scanPolicyNames", map);
	}

	/**
	 * Loads the session with the given name. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public ApiResponse loadSession(String apikey, String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("name", name);
		return callApi("core", "action", "loadSession", map);
	}
	
	/**
	 * Creates a new context in the current session
	 */
	public ApiResponse newContext(String apikey, String contextname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextName", contextname);
		return callApi("context", "action", "newContext", map);
	}
	
	/**
	 * List context names of current session
	 */
	public ApiResponse contextList() throws ClientApiException {
		Map<String, String> map = null;
		return callApi("context", "view", "contextList", map);
	}

	/**
	 * List the information about the named context
	 */
	public ApiResponse context(String contextname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextName", contextname);
		return callApi("context", "view", "context", map);
	}

	/**
	 * Add include regex to context
	 */
	public ApiResponse includeInContext(String apikey, String contextname, String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextName", contextname);
		map.put("regex", regex);
		return callApi("context", "action", "includeInContext", map);
	}
	
	/**
	 * Add exclude regex to context
	 */
	public ApiResponse excludeFromContext(String apikey, String contextname, String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextName", contextname);
		map.put("regex", regex);
		return callApi("context", "action", "excludeFromContext", map);
	}

	
	public ApiResponse setUserEnabled(String apikey, String contextid, String userid, String enabled) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		map.put("enabled", enabled);
		return callApi("users", "action", "setUserEnabled", map);
	}
	
	public ApiResponse results(String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("scanId", scanid);
		return callApi("spider", "view", "results", map);
	}
	
	public ApiResponse fullResults(String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("scanId", scanid);
		return callApi("spider", "view", "fullResults", map);
	}
	/***************************************************************************/
   
	public  int statusToInt(final ApiResponse response) {
		return Integer.parseInt(((ApiResponseElement) response).getValue());
	}

	public URL buildZapRequestUrl( String format, String component,
			String type, String method, Map<String, String> params) throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		sb.append("http://" + this.zapProxyHost + ":" + this.zapProxyPort + "/");
		sb.append(format);
		sb.append('/');
		sb.append(component);
		sb.append('/');
		sb.append(type);
		sb.append('/');
		sb.append(method);
		sb.append('/');
		if (params != null) {
			sb.append('?');
			for (Map.Entry<String, String> p : params.entrySet()) {
				sb.append(encodeQueryParam(p.getKey()));
				sb.append('=');
				if (p.getValue() != null) {
					sb.append(encodeQueryParam(p.getValue()));
				}
				sb.append('&');
			}
		}
		//debug=true
        System.out.println(sb.toString());
		return new URL(sb.toString());
	}

	private static String encodeQueryParam(String param) {
		try {
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
			// UTF-8 is a standard charset.
		}
		return param;
	}
/********************************************************************************************************************/
	public ApiResponse enableAllScanners(String apikey, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanPolicyName", scanpolicyname);
		return callApi("ascan", "action", "enableAllScanners", map);
	}
	
	//10014 : CSRF
	//http://10.107.2.102:8080/xml/ascan/action/setPolicyAttackStrength/?id=10014&attackStrength=HIGH&scanPolicyName=Default%20policy&apikey=2q0ap4er4dhnlauq165mv43cht
	public ApiResponse setPolicyAttackStrength(String apikey, String id, String attackstrength, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		map.put("scanPolicyName", scanpolicyname);
		return callApi("ascan", "action", "setPolicyAttackStrength", map);
	}

	public ApiResponse setPolicyAlertThreshold(String apikey, String id, String attackstrength, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("alertThreshold", attackstrength);
		map.put("scanPolicyName", scanpolicyname);
		return callApi("ascan", "action", "setPolicyAlertThreshold", map);
	}

	public ApiResponse setScannerAttackStrength(String apikey, String id, String attackstrength, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		map.put("scanPolicyName", scanpolicyname);
		return callApi("ascan", "action", "setScannerAttackStrength", map);
	}

	public ApiResponse setScannerAlertThreshold(String apikey, String id, String attackstrength, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		map.put("scanPolicyName", scanpolicyname);
		return callApi("ascan", "action", "setScannerAlertThreshold", map);
	}
	
	
	/************************************************************************************************************************/

	private Document callApiDom (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		try {
			URL url = buildZapRequestUrl("xml", component, type, method, params);
			//System.out.println(url.toString());
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			return db.parse(uc.getInputStream());
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
	}

	public ApiResponse callApi (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		Document dom;
		try {
			dom = callApiDom(component, type, method, params);
			//System.out.println(dom.getTextContent());
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
		return ApiResponseFactory.getResponse(dom.getFirstChild());
	}
	
	
	public ApiResponse numberOfAlerts(String baseurl) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		return callApi("core", "view", "numberOfAlerts", map);
	}
	
	public ApiResponse numberOfMessages(String baseurl) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		return callApi("core", "view", "numberOfMessages", map);
	}
	
	public byte[] xmlreport(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return callApiOther("core", "other", "xmlreport", map);
	}
	
	private  byte[] callApiOther (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		try {
			URL url = buildZapRequestUrl("other", component, type, method, params);
			//System.out.println(url);
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			InputStream in = uc.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8 * 1024];
			try {
				int bytesRead;
			    while ((bytesRead = in.read(buffer)) != -1) {
			    	out.write(buffer, 0, bytesRead);
			    }
			} catch (IOException e) {
				throw new ClientApiException(e);
			} finally {
				out.close();
				in.close();
			}
			return out.toByteArray();
			
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
	}
	/**
	 * Shuts down ZAP
	 */
	public ApiResponse shutdown(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return callApi("core", "action", "shutdown", map);
	}
	
	
/*********************************************************************************************************************/	
	
	public ApiResponse PsEnableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return callApi("pscan", "action", "enableAllScanners", map);
	}

	public ApiResponse PsDisableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return callApi("pscan", "action", "disableAllScanners", map);
	}
	public ApiResponse setOptionPostForm(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionPostForm", map);
	}

	public ApiResponse setOptionProcessForm(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionProcessForm", map);
	}
	
	public ApiResponse setOptionParseComments(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionParseComments", map);
	}

	public ApiResponse setOptionParseRobotsTxt(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionParseRobotsTxt", map);
	}

	public ApiResponse setOptionParseSitemapXml(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionParseSitemapXml", map);
	}
	
	public ApiResponse setOptionHandleODataParametersVisited(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionHandleODataParametersVisited", map);
	}
	
	public ApiResponse setOptionMaxDepth(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return callApi("spider", "action", "setOptionMaxDepth", map);
	}
	
	public ApiResponse setOptionShowAdvancedDialog(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return callApi("spider", "action", "setOptionShowAdvancedDialog", map);
	}


}