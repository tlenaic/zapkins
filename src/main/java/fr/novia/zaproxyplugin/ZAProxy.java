

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 ludovicRoucoux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.novia.zaproxyplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.EnvironmentSpecific;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.slaves.SlaveComputer;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import main.java.fr.novia.zaproxyplugin.CustomZapClientApi;
import main.java.fr.novia.zaproxyplugin.ZAPcmdLine;
import main.java.fr.novia.zaproxyplugin.report.ZAPreport;
import main.java.fr.novia.zaproxyplugin.report.ZAPreportCollection;

/**
 * Contains methods to start and execute ZAProxy.
 * Members variables are bind to the config.jelly placed to fr/novia/zaproxyplugin/ZAProxy
 * 
 * @author ludovic.roucoux
 *
 */
public class ZAProxy extends AbstractDescribableImpl<ZAProxy> implements Serializable  {

	private static final long serialVersionUID = 3381268691497579059L;

//	private static final String zapProxyKey = "ZAPROXY-PLUGIN";
	
	private static final String user = "ZAPR USER";
	
	private static final int MILLISECONDS_IN_SECOND = 1000;
	public static final String FILE_POLICY_EXTENSION = ".policy";
	public static final String FILE_SESSION_EXTENSION = ".session";
	public static final String NAME_POLICIES_DIR_ZAP = "policies";
	
	public static final String CMD_LINE_DIR = "-dir";
	public static final String CMD_LINE_HOST = "-host";
	public static final String CMD_LINE_PORT = "-port";
	public static final String CMD_LINE_DAEMON = "-daemon";
	private static final String CMD_LINE_CONFIG = "-config";
	private static final String CMD_LINE_API_KEY = "api.key";
	
	// TODO Do import when zap-2.4.0.jar will contain the correct API version
//	public static final String CMD_LINE_DIR = CommandLine.DIR;
//	public static final String CMD_LINE_CONFIG = CommandLine.CONFIG;
//	public static final String CMD_LINE_HOST = CommandLine.HOST;
//	public static final String CMD_LINE_PORT = CommandLine.PORT;
//	public static final String CMD_LINE_DAEMON = CommandLine.DAEMON;
	
	private static final String ZAP_PROG_NAME_BAT = "zap.bat";
	private static final String ZAP_PROG_NAME_SH = "zap.sh";
	
	/** Host configured when ZAProxy is used as proxy */
	private String zapProxyHost;
	
	/** Port configured when ZAProxy is used as proxy */
	private int zapProxyPort;
	
	/** the secret API key when ZAProxy is used */
	private String zapProxyKey ;
	
	/** Path to the ZAProxy program */
	private String zapProgram;
	
	/** Indicate if ZAProxy is automatically installed by Jenkins or if it is already install on the machine */
	private final boolean autoInstall;
	
	/** The ZAproxy tool to use */
	private final String toolUsed;
	
	/** Environment variable about ZAProxy path */
	private final String zapHome;
	
	/** Time total to wait for zap initialization. After this time, the program is stopped */
	private final int timeoutInSec;

	/** Filename to load ZAProxy session. Contains the absolute path to the session */
	private final String filenameLoadSession;
	
	/** URL to attack by ZAProxy */
	private final String targetURL;
	
	/** Realize a url spider or not by ZAProxy */
	private final boolean spiderURL;

	/** Realize a url AjaxSpider or not by ZAProxy */
	private final boolean ajaxSpiderURL;
	
	/** Realize a url scan or not by ZAProxy */
	private final boolean scanURL;
	
	/** Realize a url spider as user or not by ZAProxy */
	private final boolean spiderAsUser;	
	
	/** Authentication script name*/	
	private final String scriptName;	

	/** loggin url**/
	private final String loginUrl;
	
	/** context Name**/
	private final String contextName;
	
	
	/** Included url in scan **/
	private final String includedUrl;
	
	/** Exclude url from scan **/
	private final String excludedUrl;
	
	
	

	/** logged in indication*/
	private final String loggedInIndicator;
	
	/** logged out indicator */	
	private final String loggedOutIndicator;
	
	/** post data used to request the login URL without credentials */
	private final String postData;
	
	/** cookie requiered to request the login URL*/
	private final String cookie;
	
	/** username post data parameter*/
	private final String usernameParameter;
	
	/** password post data parameter*/
	private final String passwordParameter;
	
	/** Authentication information for conduct spider as a user*/
	/** user name for authentication*/
	private final String username;

	/** Password for the defined user */
	private final String password;
	

	/** Realize a url AjaxSpider or not by ZAProxy using credentials*/
	private final boolean ajaxSpiderURLAsUser;

	/** Realize a url scan or not by ZAProxy using credentials */
	private final boolean scanURLAsUser;
	
	/** Save reports or not */
	private final boolean saveReports;

	/** List of chosen format for reports.
	 * ArrayList because it needs to be Serializable (whereas List is not Serializable)
	 */
	private final ArrayList<String> chosenFormats;
	
	/** Filename for ZAProxy reports. It can contain a relative path. */
	private final String filenameReports;
	
	/** Save session or not */
	private final boolean saveSession;
	
	/** Filename to save ZAProxy session. It can contain a relative path. */
	private final String filenameSaveSession;
	
	/** The default directory that ZAP uses */
	private final String zapDefaultDir;
	
	/** The file policy to use for the scan. It contains only the policy name (without extension) */
	private final String chosenPolicy;
	
	/** List of all ZAP command lines specified by the user 
	 * ArrayList because it needs to be Serializable (whereas List is not Serializable)
	 */
	private final ArrayList<ZAPcmdLine> cmdLinesZAP;
	
	/** The jdk to use to start ZAProxy */
	private final String jdk;
	
	
	/** Id of the newly created context*/
	private String contextId;

	/** Id of the newly created user*/
	private String userId;
	
	/** Id of the newly created scan*/
	private String scanId;

	/**
	 * @return the scanId
	 */
	public String getScanId() {
		return scanId;
	}

	/**
	 * @param scanId the scanId to set
	 */
	public void setScanId(String scanId) {
		this.scanId = scanId;
	}

	/**
     * @deprecated
     * Old constructor 
     */
//	@Deprecated
//	public ZAProxy(boolean autoInstall, String toolUsed, String zapHome, int timeoutInSec,
//			String filenameLoadSession, String targetURL, boolean spiderURL, boolean scanURL,
//			boolean saveReports, List<String> chosenFormats, String filenameReports,
//			boolean saveSession, String filenameSaveSession,
//			String zapDefaultDir, String chosenPolicy,
//			List<ZAPcmdLine> cmdLinesZAP, String jdk) {
//		
//		this.autoInstall = autoInstall;
//		this.toolUsed = toolUsed;
//		this.zapHome = zapHome;
//		this.timeoutInSec = timeoutInSec;
//		this.filenameLoadSession = filenameLoadSession;
//		this.targetURL = targetURL;
//		this.spiderURL = spiderURL;
//		this.scanURL = scanURL;
//		this.saveReports = saveReports;
//		this.chosenFormats = chosenFormats != null ? new ArrayList<String>(chosenFormats) : new ArrayList<String>();
//		this.filenameReports = filenameReports;
//		this.saveSession = saveSession;
//		this.filenameSaveSession = filenameSaveSession;
//		this.zapDefaultDir = zapDefaultDir;
//		this.chosenPolicy = chosenPolicy;
//		this.cmdLinesZAP = cmdLinesZAP != null ? new ArrayList<ZAPcmdLine>(cmdLinesZAP) : new ArrayList<ZAPcmdLine>();
//		this.ajaxSpiderURL=false;
//		this.jdk = jdk;
//		
//		this.spiderAsUser=false;
//		this.username="";
//		this.password="";
//		this.usernameParameter="";
//		this.passwordParameter="";
//		this.loginUrl="";
//		this.loggedInIndicator="";
//
//		System.out.println(this.toString());
//	}
	
	//ce constructeur est ajoute par moi meme
	@DataBoundConstructor
	public ZAProxy(String zapDefaultDir, int zapProxyPort, String zapProxyKey, boolean ajaxSpiderURL, String loggedInIndicator,
			String loggedOutIndicator, ArrayList<ZAPcmdLine> cmdLinesZAP, boolean spiderAsUser, String password,
			String userId, boolean scanURLAsUser, String filenameLoadSession, String filenameSaveSession,
			String loginUrl, int timeoutInSec, String postData, boolean saveSession, String zapProgram,
			boolean ajaxSpiderURLAsUser, String username, String contextId, String usernameParameter,
			boolean autoInstall, String chosenPolicy, boolean spiderURL, String zapProxyHost, String filenameReports,
			String targetURL, ArrayList<String> chosenFormats, String cookie, String jdk, boolean saveReports,
			String toolUsed, String passwordParameter, String scriptName, String zapHome, boolean scanURL) {
		super();
		this.zapDefaultDir = zapDefaultDir;
		this.zapProxyPort = zapProxyPort;
		this.ajaxSpiderURL = ajaxSpiderURL;
		this.loggedInIndicator = loggedInIndicator;
		this.loggedOutIndicator = loggedOutIndicator;
		this.cmdLinesZAP = cmdLinesZAP;
		this.spiderAsUser = spiderAsUser;
		this.password = password;
		this.userId = userId;
		this.scanURLAsUser = scanURLAsUser;
		this.filenameLoadSession = filenameLoadSession;
		this.filenameSaveSession = filenameSaveSession;
		this.loginUrl = loginUrl;
		this.timeoutInSec = timeoutInSec;
		this.postData = postData;
		this.saveSession = saveSession;
		this.zapProgram = zapProgram;
		this.ajaxSpiderURLAsUser = ajaxSpiderURLAsUser;
		this.username = username;
		this.contextId = contextId;
		this.usernameParameter = usernameParameter;
		this.autoInstall = autoInstall;
		this.chosenPolicy = chosenPolicy;
		this.spiderURL = spiderURL;
		this.zapProxyHost = zapProxyHost;
		this.zapProxyKey = zapProxyKey;
		this.filenameReports = filenameReports;
		this.targetURL = targetURL;
		this.chosenFormats = chosenFormats;
		this.cookie = cookie;
		this.jdk = jdk;
		this.saveReports = saveReports;
		this.toolUsed = toolUsed;
		this.passwordParameter = passwordParameter;
		this.scriptName = scriptName;
		this.zapHome = zapHome;
		this.scanURL = scanURL;
		
		System.out.println(this.toString());
	}

/**
	 * @return the scriptName
	 */
	public String getScriptName() {
		return scriptName;
	}

	/**
	 * @return the loggedOutIndicator
	 */
	public String getLoggedOutIndicator() {
		return loggedOutIndicator;
	}

	/**
	 * @return the postData
	 */
	public String getPostData() {
		return postData;
	}

	/**
	 * @return the cookie
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * @return the ajaxSpiderURLAsUser
	 */
	public boolean isAjaxSpiderURLAsUser() {
		return ajaxSpiderURLAsUser;
	}

	/**
	 * @return the scanURLAsUser
	 */
	public boolean isScanURLAsUser() {
		return scanURLAsUser;
	}

	//	@DataBoundConstructor
//	public ZAProxy(boolean autoInstall, String toolUsed, String zapHome, int timeoutInSec,
//			String filenameLoadSession, String targetURL, boolean spiderURL, boolean spiderAsUser, boolean ajaxSpiderURL, 
//			boolean scanURL, boolean saveReports, List<String> chosenFormats, String filenameReports,
//			boolean saveSession, String filenameSaveSession, String zapDefaultDir, String chosenPolicy,
//			List<ZAPcmdLine> cmdLinesZAP, String jdk, String username, String password, String usernameParameter, 
//			String passwordParameter, String loginUrl, String loggedInIndicator) {
//		
//		this.autoInstall = autoInstall;
//		this.toolUsed = toolUsed;
//		this.zapHome = zapHome;
//		this.timeoutInSec = timeoutInSec;
//		this.filenameLoadSession = filenameLoadSession;
//		this.targetURL = targetURL;
//		this.spiderURL = spiderURL;
//		this.ajaxSpiderURL=ajaxSpiderURL;
//		this.scanURL = scanURL;
//		this.saveReports = saveReports;
//		this.chosenFormats = chosenFormats != null ? new ArrayList<String>(chosenFormats) : new ArrayList<String>();
//		this.filenameReports = filenameReports;
//		this.saveSession = saveSession;
//		this.filenameSaveSession = filenameSaveSession;
//		this.zapDefaultDir = zapDefaultDir;
//		this.chosenPolicy = chosenPolicy;
//		this.cmdLinesZAP = cmdLinesZAP != null ? new ArrayList<ZAPcmdLine>(cmdLinesZAP) : new ArrayList<ZAPcmdLine>();
//		
//		this.spiderAsUser=spiderAsUser;
//		this.username=username;
//		this.password=password;
//		this.usernameParameter=usernameParameter;
//		this.passwordParameter=passwordParameter;
//		this.loginUrl=loginUrl;
//		this.loggedInIndicator=loggedInIndicator;
//
//		this.jdk = jdk;
//		System.out.println(this.toString());
//	}
//	
	@Override
	public String toString() {
		String s = "";
		
		s += "--------------------------------------------------";
		s+= "jdk ["+jdk+"]";
		s += "zapHome ["+zapHome+"]\n";
		s += "zapDefaultDir ["+zapDefaultDir+"]\n";
		s += "toolUsed ["+toolUsed+"]\n";
		s += "autoInstall ["+autoInstall+"]\n";
		s += "zapProxyHost ["+zapProxyHost+"]\n";
		s += "zapProxyPort ["+zapProxyPort+"]\n";	
		s += "saveReports ["+saveReports+"]\n";
		s += "chosenFormats ["+chosenFormats+"]\n";
		s += "filenameReports ["+filenameReports+"]\n";
		s += "saveSession ["+saveSession+"]\n";
		s += "filenameSaveSession ["+filenameSaveSession+"]\n";		 
		s += "--------------------------------------------------";		
		s += "timeoutInSec ["+timeoutInSec+"]\n";
		s += "filenameLoadSession ["+filenameLoadSession+"]\n";
		s += "--------------------------------------------------";
		s += "targetURL ["+targetURL+"]\n";
		s += "chosenPolicy ["+chosenPolicy+"]\n";
		s += "Authentication Script Name ["+scriptName+"]\n";
		s += "loginUrl ["+loginUrl+"]\n";
		s += "post Data ["+postData+"]\n";
		s += "cookie ["+cookie+"]\n";
		s += "loggedInIndicator ["+loggedInIndicator+"]\n";
		s += "loggedInIndicator ["+loggedOutIndicator+"]\n";
		s += "--------------------------------------------------";
		s += "spiderURL ["+spiderURL+"]\n";
		s += "ajaxSpiderURL ["+ajaxSpiderURL+"]\n";
		s += "scanURL ["+scanURL+"]\n";
		s += "--------------------------------------------------";		
		s += "spider as user ["+spiderAsUser+"]\n";
		s += "ajaxSpiderURLAsUser ["+ajaxSpiderURLAsUser+"]\n";
		s += "scanURLAsUser ["+scanURLAsUser+"]\n";
		s += "--------------------------------------------------";		
		s += "usernameParameter ["+usernameParameter+"]\n";
		s += "passwordParameter ["+passwordParameter+"]\n";
		s += "username ["+username+"]\n";
		s += "--------------------------------------------------";		
		
		return s;
	}
	
	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public ZAProxyDescriptorImpl getDescriptor() {
		return (ZAProxyDescriptorImpl)super.getDescriptor();
	}
	
	/*
	 * Getters allows to load members variables into UI.
	 */
	public boolean getAutoInstall() {
		return autoInstall;
	}

	public String getToolUsed() {
		return toolUsed;
	}

	public String getZapHome() {
		return zapHome;
	}

	public int getTimeoutInSec() {
		return timeoutInSec;
	}

	public String getFilenameLoadSession() {
		return filenameLoadSession;
	}

	public String getTargetURL() {
		return targetURL;
	}

	public boolean getSpiderURL() {
		return spiderURL;
	}

	public boolean getAjaxSpiderURL() {
		return ajaxSpiderURL;
	}

	public boolean getScanURL() {
		return scanURL;
	}

	public boolean getSaveReports() {
		return saveReports;
	}

	public List<String> getChosenFormats() {
		return chosenFormats;
	}

	public String getFilenameReports() {
		return filenameReports;
	}

	public boolean getSaveSession() {
		return saveSession;
	}

	public String getFilenameSaveSession() {
		return filenameSaveSession;
	}

	public String getZapDefaultDir() {
		return zapDefaultDir;
	}
	
	public String getChosenPolicy() {
		return chosenPolicy;
	}

	public void setZapProxyHost(String zapProxyHost) {
		this.zapProxyHost = zapProxyHost;
	}

	public void setZapProxyPort(int zapProxyPort) {
		this.zapProxyPort = zapProxyPort;
	}
	
	public List<ZAPcmdLine> getCmdLinesZAP() {
		return cmdLinesZAP;
	}

	public boolean getSpiderAsUser() {
		return spiderAsUser;
	}

	public String  getUsernameParameter() {
		return usernameParameter;
	}

	public String  getpasswordParameter() {
		return passwordParameter;
	}
	
	public String  getusername() {
		return username;
	}

	public String getpassword() {
		return password;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public String getLoggedInIndicator() {
		return loggedInIndicator;
	}
	/**
	 * Gets the JDK that this Sonar builder is configured with, or null.
	 */
	public JDK getJDK() {
		return Jenkins.getInstance().getJDK(jdk);
	}

	public String getJdk() {
		return jdk;
	}

	/**
	 * Get the ZAP_HOME setup by Custom Tools Plugin or already present on the build's machine. 
	 * 
	 * @param build
	 * @param listener the listener to display log during the job execution in jenkins
	 * @return the installed tool location, without zap.bat or zap.sh at the end
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @see <a href="https://groups.google.com/forum/#!topic/jenkinsci-dev/RludxaYjtDk">
	 * 	https://groups.google.com/forum/#!topic/jenkinsci-dev/RludxaYjtDk</a>
	 */
	private String retrieveZapHomeWithToolInstall(AbstractBuild<?, ?> build, BuildListener listener) 
			throws IOException, InterruptedException {	
		
		EnvVars env = null;
		Node node = null;
		String installPath = null;
			
		if(autoInstall) {
			env = build.getEnvironment(listener);
			node = build.getBuiltOn();
			for (ToolDescriptor<?> desc : ToolInstallation.all()) {
				for (ToolInstallation tool : desc.getInstallations()) {
					if (tool.getName().equals(toolUsed)) {
						if (tool instanceof NodeSpecific) {
							tool = (ToolInstallation) ((NodeSpecific<?>) tool).forNode(node, listener);
						}
						if (tool instanceof EnvironmentSpecific) {
							tool = (ToolInstallation) ((EnvironmentSpecific<?>) tool).forEnvironment(env);
						}
						installPath = tool.getHome();
						
						return installPath;
					}
				}
			}
		} else {
			installPath = build.getEnvironment(listener).get(zapHome);
		}
		return installPath;
	}
	
	/**
	 * Return the ZAProxy program name (zap.bat or zap.sh) depending of the build node and the OS.
	 * 
	 * @param build
	 * @return the ZAProxy program name (zap.bat or zap.sh)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private String getZAPProgramName(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		Node node = build.getBuiltOn();
		String zapProgramName = "";
		
		// Append zap program following Master/Slave and Windows/Unix
		if( "".equals(node.getNodeName())) { // Master
			if( File.pathSeparatorChar == ':' ) { // UNIX
				zapProgramName = ZAP_PROG_NAME_SH;
			} else { // Windows (pathSeparatorChar == ';')
				zapProgramName = ZAP_PROG_NAME_BAT;
			}
		} 
		else { // Slave
			if( "Unix".equals(((SlaveComputer)node.toComputer()).getOSDescription()) ) {
				zapProgramName = ZAP_PROG_NAME_SH;
			} else {
				zapProgramName = ZAP_PROG_NAME_BAT;
			}
		}
		return zapProgramName;
	}
	
	/**
	 * Return the ZAProxy program name with separator prefix (\zap.bat or /zap.sh) depending of the build node and the OS.
	 * 
	 * @param build
	 * @return the ZAProxy program name with separator prefix (\zap.bat or /zap.sh)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private String getZAPProgramNameWithSeparator(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		Node node = build.getBuiltOn();
		String zapProgramName = "";
		
		// Append zap program following Master/Slave and Windows/Unix
		if( "".equals(node.getNodeName())) { // Master
			if( File.pathSeparatorChar == ':' ) { // UNIX
				zapProgramName = "/" + ZAP_PROG_NAME_SH;
			} else { // Windows (pathSeparatorChar == ';')
				zapProgramName = "\\" + ZAP_PROG_NAME_BAT;
			}
		} 
		else { // Slave
			if( "Unix".equals(((SlaveComputer)node.toComputer()).getOSDescription()) ) {
				zapProgramName = "/" + ZAP_PROG_NAME_SH;
			} else {
				zapProgramName = "\\" + ZAP_PROG_NAME_BAT;
			}
		}
		return zapProgramName;
	}
	
	/**
	 * Verify parameters of the build setup are correct (null, empty, negative ...)
	 * 
	 * @param build
	 * @param listener the listener to display log during the job execution in jenkins
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception throw an exception if a parameter is invalid.
	 */
	private void checkParams(AbstractBuild<?, ?> build, BuildListener listener) 
			throws IllegalArgumentException, IOException, InterruptedException {
		zapProgram = retrieveZapHomeWithToolInstall(build, listener);
		
		if(zapProgram == null || zapProgram.isEmpty()) {
			throw new IllegalArgumentException("zapProgram is missing");
		} else
			listener.getLogger().println("zapProgram = " + zapProgram);
		
		if(targetURL == null || targetURL.isEmpty()) {
			throw new IllegalArgumentException("targetURL is missing");
		} else
			listener.getLogger().println("targetURL = " + targetURL);

		if(zapProxyHost == null || zapProxyHost.isEmpty()) {
			throw new IllegalArgumentException("zapProxy Host is missing");
		} else
			listener.getLogger().println("zapProxyHost = " + zapProxyHost);

		if(zapProxyPort < 0) {
			throw new IllegalArgumentException("zapProxy Port is less than 0");
		} else
			listener.getLogger().println("zapProxyPort = " + zapProxyPort);
		
	}
		
	/**
	 * Start ZAProxy using command line. It uses host and port configured in Jenkins admin mode and
	 * ZAProxy program is launched in daemon mode (i.e without UI).
	 * ZAProxy is started on the build's machine (so master machine ou slave machine) thanks to 
	 * {@link FilePath} object and {@link Launcher} object.
	 * 
	 * @param build
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param launcher the object to launch a process locally or remotely
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public void startZAP(AbstractBuild<?, ?> build, BuildListener listener, Launcher launcher) 
			throws IllegalArgumentException, IOException, InterruptedException {
		checkParams(build, listener);
		
		FilePath ws = build.getWorkspace();
		if (ws == null) {
			Node node = build.getBuiltOn();
			if (node == null) {
				throw new NullPointerException("no such build node: " + build.getBuiltOnStr());
			}
			throw new NullPointerException("no workspace from node " + node + " which is computer " + node.toComputer() + " and has channel " + node.getChannel());
		}
		
		// Contains the absolute path to ZAP program
		FilePath zapPathWithProgName = new FilePath(ws.getChannel(), zapProgram + getZAPProgramNameWithSeparator(build));
		listener.getLogger().println("Start ZAProxy [" + zapPathWithProgName.getRemote() + "]");
		
		// Command to start ZAProxy with parameters
		List<String> cmd = new ArrayList<String>();
		cmd.add(zapPathWithProgName.getRemote());
		cmd.add(CMD_LINE_DAEMON);
		cmd.add(CMD_LINE_HOST);
		cmd.add(zapProxyHost);
		cmd.add(CMD_LINE_PORT);
		cmd.add(String.valueOf(zapProxyPort));
		cmd.add(CMD_LINE_CONFIG);
		cmd.add(CMD_LINE_API_KEY + "=" + zapProxyKey);
		
		// Set the default directory used by ZAP if it's defined and if a scan is provided
		if(scanURL && zapDefaultDir != null && !zapDefaultDir.isEmpty()) {
			cmd.add(CMD_LINE_DIR);
			cmd.add(zapDefaultDir);
		}
		
		// Adds command line arguments if it's provided
		if(!cmdLinesZAP.isEmpty()) {
			addZapCmdLine(cmd);
		}
			
		EnvVars envVars = build.getEnvironment(listener);
		// on Windows environment variables are converted to all upper case,
		// but no such conversions are done on Unix, so to make this cross-platform,
		// convert variables to all upper cases.
		for(Map.Entry<String,String> e : build.getBuildVariables().entrySet())
			envVars.put(e.getKey(),e.getValue());
		
		FilePath workDir = new FilePath(ws.getChannel(), zapProgram);
		
		// JDK choice
		computeJdkToUse(build, listener, envVars);
		
		// Launch ZAP process on remote machine (on master if no remote machine)
		launcher.launch().cmds(cmd).envs(envVars).stdout(listener).pwd(workDir).start();
		
		// Call waitForSuccessfulConnectionToZap(int, BuildListener) remotely
		build.getWorkspace().act(new WaitZAProxyInitCallable(this, listener));
	}
	
	/**
	 * Set the JDK to use to start ZAP.
	 * 
	 * @param build
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param env list of environment variables. Used to set the path to the JDK
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void computeJdkToUse(AbstractBuild<?, ?> build,
			BuildListener listener, EnvVars env) throws IOException, InterruptedException {
		JDK jdkToUse = getJdkToUse(build.getProject());
		if (jdkToUse != null) {
			Computer computer = Computer.currentComputer();
			// just in case we are not in a build
			if (computer != null) {
				jdkToUse = jdkToUse.forNode(computer.getNode(), listener);
			}
			jdkToUse.buildEnvVars(env);
		}
	}

	/**
	 * @return JDK to be used with this project.
	 */
	private JDK getJdkToUse(AbstractProject<?, ?> project) {
		JDK jdkToUse = getJDK();
		if (jdkToUse == null) {
			jdkToUse = project.getJDK();
		}
		return jdkToUse;
	}
	
	/**
	 * Add list of command line in the list in param
	 * @param l the list to attach ZAP command line
	 */
	private void addZapCmdLine(List<String> l) {
		for(ZAPcmdLine zapCmd : cmdLinesZAP) {
			if(zapCmd.getCmdLineOption() != null && !zapCmd.getCmdLineOption().isEmpty()) {
				l.add(zapCmd.getCmdLineOption());
			}
			if(zapCmd.getCmdLineValue() != null && !zapCmd.getCmdLineValue().isEmpty()) {
				l.add(zapCmd.getCmdLineValue());
			}
		}
	}
	
	/**
	 * Wait for ZAProxy initialization, so it's ready to use at the end of this method
	 * (otherwise, catch exception). This method is launched on the remote machine (if there is one)
	 *   
	 * @param timeout the time in sec to try to connect at zap proxy. 
	 * @param listener the listener to display log during the job execution in jenkins
	 * @see <a href="https://groups.google.com/forum/#!topic/zaproxy-develop/gZxYp8Og960">
	 * 		https://groups.google.com/forum/#!topic/zaproxy-develop/gZxYp8Og960</a>
	 */
	private void waitForSuccessfulConnectionToZap(int timeout, BuildListener listener) {
		int timeoutInMs = getMilliseconds(timeout);
		int connectionTimeoutInMs = timeoutInMs;
		int pollingIntervalInMs = getMilliseconds(1);
		boolean connectionSuccessful = false;
		long startTime = System.currentTimeMillis();
		Socket socket = null;
		do {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(zapProxyHost, zapProxyPort), connectionTimeoutInMs);
				connectionSuccessful = true;
			} catch (SocketTimeoutException ignore) {
				listener.error(ExceptionUtils.getStackTrace(ignore));
				throw new BuildException("Unable to connect to ZAP's proxy after " + timeout + " seconds.");
				
			} catch (IOException ignore) {
				// and keep trying but wait some time first...
				try {
					Thread.sleep(pollingIntervalInMs);
				} catch (InterruptedException e) {
					listener.error(ExceptionUtils.getStackTrace(ignore));
					throw new BuildException("The task was interrupted while sleeping between connection polling.", e);
				}

				long ellapsedTime = System.currentTimeMillis() - startTime;
				if (ellapsedTime >= timeoutInMs) {
					listener.error(ExceptionUtils.getStackTrace(ignore));
					throw new BuildException("Unable to connect to ZAP's proxy after " + timeout + " seconds.");
				}
				connectionTimeoutInMs = (int) (timeoutInMs - ellapsedTime);
			} finally {
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						listener.error(ExceptionUtils.getStackTrace(e));
					}
				}
			}
		} while (!connectionSuccessful);
	}
	
	/**
	 * Converts seconds in milliseconds.
	 * @param seconds the time in second to convert
	 * @return the time in milliseconds
	 */
	private static int getMilliseconds(int seconds) {
		return seconds * MILLISECONDS_IN_SECOND;
	}
	
	/**
	 * Get all security alerts raised by ZAProxy
	 *
	 * @param format the report format file
	 * @param listener the listener to display log during the job execution in jenkins
	 * @return all alerts from ZAProxy in a string
	 * @throws IOException 
	 * @throws Exception
	 */
	private String getAllAlerts(final String format, BuildListener listener) throws IOException {
		URL url;
		String result = "";
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(zapProxyHost, zapProxyPort));
		
		url = new URL("http://zap/" + format + "/core/view/alerts");

		listener.getLogger().println("Open URL: " + url.toString());

		final HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
		uc.connect();

		final BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			result = result + inputLine;
		}

		in.close();
		return result;
	}
	
	/**
	 * Generates security report for one format. Reports are saved into build's workspace.
	 * 
	 * @param reportFormat the format of the report
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param workspace a {@link FilePath} representing the build's workspace
	 * @param clientApi the ZAP client API to call method
	 * @throws ClientApiException 
	 * @throws IOException
	 */
	private void saveReport(ZAPreport reportFormat, BuildListener listener, FilePath workspace, 
			CustomZapClientApi clientApi)   {
		final String fullFileName = filenameReports + "." + reportFormat.getFormat();
		File reportsFile = new File(workspace.getRemote(), fullFileName);
		FileUtils.writeByteArrayToFile(reportsFile, reportFormat.generateReport(clientApi, zapProxyKey));
		listener.getLogger().println("File ["+ reportsFile.getAbsolutePath() +"] saved");
	}

	/**
	 * Execute ZAProxy method following build's setup and stop ZAP at the end.
	 * 
	 * @param workspace a {@link FilePath} representing the build's workspace
	 * @param listener the listener to display log during the job execution in jenkins
	 * @return true is no exception is caught, false otherwise.
	 */
//	public boolean executeZAP(FilePath workspace, BuildListener listener) {
//		ClientApi zapClientAPI = new ClientApi(zapProxyHost, zapProxyPort);
//		boolean buildSuccess = true;
//		
//		
//		
//		// Try/catch here because I need to stopZAP in finally block and for that,
//		// I need the zapClientAPI created in this method
//		try {
//			/* ======================================================= 
//			 * |                  LOAD SESSION                        |
//			 * ======================================================= 
//			 */
//			if(filenameLoadSession != null && filenameLoadSession.length() != 0) {
//				File sessionFile = new File(filenameLoadSession);
//				listener.getLogger().println("Load session at ["+ sessionFile.getAbsolutePath() +"]");
//				zapClientAPI.core.loadSession(API_KEY, sessionFile.getAbsolutePath());
//			} else {
//				listener.getLogger().println("Skip loadSession");
//			}
//			
//			/* ======================================================= 
//			 * |                  SPIDER URL                          |
//			 * ======================================================= 
//			 */
//			if (spiderURL) {
//				listener.getLogger().println("Spider the site [" + targetURL + "]");
//				spiderURL(targetURL, listener, zapClientAPI);
//			} else {
//				listener.getLogger().println("Skip spidering the site [" + targetURL + "]");
//			}
//
//			/* ======================================================= 
//			 * |                AJAX SPIDER URL                       |
//			 * ======================================================= 
//			 */
//			if (ajaxSpiderURL) {
//				listener.getLogger().println("Ajax Spider the site [" + targetURL + "]");
//				ajaxSpiderURL(targetURL, listener, zapClientAPI);
//			} else {
//				listener.getLogger().println("Skip Ajax spidering the site [" + targetURL + "]");
//			}
//
//			/* ======================================================= 
//			 * |                  SPIDER AS USER                      |
//			 * ======================================================= 
//			 */
//			if (spiderAsUser) {
//				listener.getLogger().println("Setting up Authentication");
//				setUpAuthentication(targetURL,listener,zapClientAPI, 
//					username,password,usernameParameter,passwordParameter,loginUrl,loggedInIndicator);
//
//				listener.getLogger().println("Spider the site [" + targetURL + "] as user ["+username+"]");				
//				spiderURLAsUser(targetURL, listener, zapClientAPI, contextId, userId);
//			} else {
//				listener.getLogger().println("Skip spidering the site [" + targetURL + "] as user ["+username+"]");
//			}
//
//			/* ======================================================= 
//			 * |                  SCAN URL                            |
//			 * ======================================================= 
//			 */
//			if (scanURL) {				
//				listener.getLogger().println("Scan the site [" + targetURL + "]");
//				scanURL(targetURL, listener, zapClientAPI);
//			} else {
//				listener.getLogger().println("Skip scanning the site [" + targetURL + "]");
//			}
//			
//			/* ======================================================= 
//			 * |                  SAVE REPORTS                        |
//			 * ======================================================= 
//			 */
//			if (saveReports) {			
//				// Generates reports for all formats selected
//				for(String format : chosenFormats) {
//					ZAPreport report = ZAPreportCollection.getInstance().getMapFormatReport().get(format);
//					saveReport(report, listener, workspace, zapClientAPI);
//				}
//			}
//			
//			/* ======================================================= 
//			 * |                  SAVE SESSION                        |
//			 * ======================================================= 
//			 */
//			if(saveSession) {
//				if(filenameSaveSession != null && !filenameSaveSession.isEmpty()) {
//					File sessionFile = new File(workspace.getRemote(), filenameSaveSession);
//					listener.getLogger().println("Save session to ["+ sessionFile.getAbsolutePath() +"]");
//					
//					// Path creation if it doesn't exist
//					if(!sessionFile.getParentFile().exists()) {
//						sessionFile.getParentFile().mkdirs();
//					}
//					
//					// Method signature : saveSession(String apikey, String name, String overwrite)
//					zapClientAPI.core.saveSession(API_KEY, sessionFile.getAbsolutePath(), "true");
//				} 
//			} else {
//				listener.getLogger().println("Skip saveSession");
//			}
//			
//			listener.getLogger().println("Total alerts = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			listener.getLogger().println("Total messages = " + zapClientAPI.core.numberOfMessages("").toString(2));
//			
//		} catch (Exception e) {
//			listener.error(ExceptionUtils.getStackTrace(e));
//			buildSuccess = false;
//		} finally {
//			try {
//				stopZAP(zapClientAPI, listener);
//			} catch (ClientApiException e) {
//				listener.error(ExceptionUtils.getStackTrace(e));
//				buildSuccess = false;
//			}
//		}
//		return buildSuccess;
//	}
	
	public boolean executeZAP(FilePath workspace, BuildListener listener) {
		//ClientApi zapClientAPI = new ClientApi(zapProxyHost, zapProxyPort);
		CustomZapClientApi zapClientAPI =new CustomZapClientApi(zapProxyHost,zapProxyPort, zapProxyKey);
		boolean buildSuccess = true;
		
		
		
		// Try/catch here because I need to stopZAP in finally block and for that,
		// I need the zapClientAPI created in this method
		try {
			/* ======================================================= 
			 * |                  LOAD SESSION                        |
			 * ======================================================= 
			 */
			if(filenameLoadSession != null && filenameLoadSession.length() != 0) {
				File sessionFile = new File(filenameLoadSession);
				listener.getLogger().println("Load session at ["+ sessionFile.getAbsolutePath() +"]");
				//zapClientAPI.core.loadSession(zapProxyKey, sessionFile.getAbsolutePath());
				zapClientAPI.loadSession(sessionFile.getAbsolutePath());
			} else {
				listener.getLogger().println("Skip loadSession");
			}
			
			
			
			/* ========================== PREPARE THE SCANNER ============================== */ 
			
			
		/* ============================================================================================= */				
			
			/* ======================================================= 
			 * |                  SPIDER URL                          |
			 * ======================================================= 
			 */
			if (spiderURL) {
				listener.getLogger().println("Spider the site [" + targetURL + "] without credentials");
				spiderURL(targetURL, listener, zapClientAPI);
			} else {
				listener.getLogger().println("Skip spidering the site [" + targetURL + "]");
			}

			/* ======================================================= 
			 * |                AJAX SPIDER URL                       |
			 * ======================================================= 
			 */
			if (ajaxSpiderURL) {
				listener.getLogger().println("Ajax Spider the site [" + targetURL + "] without credentials");
				ajaxSpiderURL(targetURL, listener, zapClientAPI);
			} else {
				listener.getLogger().println("Skip Ajax spidering the site [" + targetURL + "]");
			}
			
			
			/* ======================================================= 
			 * |                  SCAN URL                            |
			 * ======================================================= 
			 */
			if (scanURL) {				
				listener.getLogger().println("Scan the site [" + targetURL + "]");
				scanURL(targetURL, listener, zapClientAPI);
			} else {
				listener.getLogger().println("Skip scanning the site [" + targetURL + "]");
			}
				
			
			/* ============================================================================================= */	
			
			
			/* ======================================================= 
			 * |                  SPIDER URL AS USER                      |
			 * ======================================================= 
			 */
			if (spiderAsUser) {
				listener.getLogger().println("Setting up Authentication");
				
				
				//setUpAuthentication(targetURL,listener,zapClientAPI,username,password,usernameParameter,passwordParameter,loginUrl,loggedInIndicator);
				setUpAuthenticationConf(zapClientAPI, listener);
				listener.getLogger().println("Spider the site [" + targetURL + "] as user ["+username+"]");					
				
				spiderURLAsUser(targetURL, listener, zapClientAPI, this.getContextId(), this.getUserId());			
				
				
			} else {
				listener.getLogger().println("Skip spidering the site [" + targetURL + "] as user ["+username+"]");
			}
			
			
			/* ======================================================= 
			 * |                AJAX SPIDER URL AS USER                       |
			 * ======================================================= 
			 */
			if (ajaxSpiderURLAsUser) {
				listener.getLogger().println("Ajax Spider the site [" + targetURL + "] As User");
				ajaxSpiderURL(targetURL, listener, zapClientAPI);
			} else {
				listener.getLogger().println("Skip Ajax spidering the site [" + targetURL + "] As User");
			}
			
			
			/* ======================================================= 
			 * |                  SCAN URL  As USER                          |
			 * ======================================================= 
			 */
			if (scanURLAsUser) {				
				listener.getLogger().println("Scan the site [" + targetURL + "] As user");
				scanURLAsUser(targetURL, listener, zapClientAPI);
				//zapClientAPI.scanURLAsUser(url, scanid, this.getContextid(), this.userid(), "true", ScanPolicyName, listener);
			} else {
				listener.getLogger().println("Skip scanning the site [" + targetURL + "]");
			}
			
			
			/* ============================================================================================= */
			
			
			
			/* ======================================================= 
			 * |                  SAVE REPORTS                        |
			 * ======================================================= 
			 */
			if (saveReports) {			
				// Generates reports for all formats selected
				for(String format : chosenFormats) {
					ZAPreport report = ZAPreportCollection.getInstance().getMapFormatReport().get(format);
					saveReport(report, listener, workspace, zapClientAPI);
				}
			}
			
			/* ======================================================= 
			 * |                  SAVE SESSION                        |
			 * ======================================================= 
			 */
			if(saveSession) {
				if(filenameSaveSession != null && !filenameSaveSession.isEmpty()) {
					File sessionFile = new File(workspace.getRemote(), filenameSaveSession);
					listener.getLogger().println("Save session to ["+ sessionFile.getAbsolutePath() +"]");
					
					// Path creation if it doesn't exist
					if(!sessionFile.getParentFile().exists()) {
						sessionFile.getParentFile().mkdirs();
					}
					
					// Method signature : saveSession(String apikey, String name, String overwrite)
					zapClientAPI.core.saveSession(zapProxyKey, sessionFile.getAbsolutePath(), "true");
				} 
			} else {
				listener.getLogger().println("Skip saveSession");
			}
			
//			listener.getLogger().println("Total alerts = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			listener.getLogger().println("Total messages = " + zapClientAPI.core.numberOfMessages("").toString(2));
//			
		} catch (Exception e) {
			listener.error(ExceptionUtils.getStackTrace(e));
			buildSuccess = false;
		} 
//		finally {
//			try {
//				stopZAP(zapClientAPI, listener);
//			} catch (ClientApiException e) {
//				listener.error(ExceptionUtils.getStackTrace(e));
//				buildSuccess = false;
//			}
//		}
		return buildSuccess;
	}
	
//	/**
//	 * Converts the ZAP API status response to an integer
//	 *
//	 * @param response the ZAP API response code
//	 * @return the integer status of the ApiResponse
//	 */
//	private int statusToInt(final ApiResponse response) {
//		return Integer.parseInt(((ApiResponseElement)response).getValue());
//	}

//	/**
//	 * Converts the ZAP API status response to an String
//	 *
//	 * @param response the ZAP API response code
//	 * @return the String status of the ApiResponse
//	 */
//	@SuppressWarnings("unchecked")
//	private String statusToString(final ApiResponse response) {
//		return ((ApiResponseElement)response).getValue();
//	}

//	/**
//	 *get user id
//	 * @param response the ZAP API response code
//	 * @return the user ID of the  user
//	 */
//	@SuppressWarnings("unchecked")
//	private String extractUserId(ApiResponse response) {
//		return ((ApiResponseElement) response).getValue();
//	}

//	/**
//	 *get context id
//	 * @param response the ZAP API response code
//	 * @return the context ID of the context
//	 */
//	@SuppressWarnings("unchecked")
//	private String extractContextId(ApiResponse response) {
//		return ((ApiResponseElement) response).getValue();
//	}

	/**
	 * set up a context and add url to it
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param URL the URL to be added to context
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @return the context ID of the context
	 * @throws ClientApiException
	 */
	private String setUpContext(BuildListener listener,final String url, CustomZapClientApi zapClientAPI) {

		String contextName="context1";//name of the Context to be create
		String contextURL="\\Q"+url+"\\E.*";//url to added to context same url user give to scan
		String contextIdTemp;

		//Create new context
		//method signature : newContext(String apikey,String contextname) throws ClientApiException
		contextIdTemp=extractContextId(zapClientAPI.context.newContext(zapProxyKey,contextName));

		//add url to the context
		//method signature : includeInContext(String apikey, String contextname, String regex) 
		//					 throws ClientApiException
		zapClientAPI.context.includeInContext(zapProxyKey,contextName,contextURL);

		listener.getLogger().println("URL "+url+" added to Context ["+contextIdTemp+"]");
		
		return contextIdTemp;
	}
	
	private void setUpScanner(CustomZapClientApi zapClientAPI){
/************************ PREPARATION DU SCANNER **********************/
		
		zapClientAPI.includeInContext(INCLUDEURL,contextName);	
		//test.includeInContext(api, "https://webmail.orange.fr/");
		
		if(!EXCLUDEURL.equals("")){
			zapClientAPI.excludeFromContext(EXCLUDEURL,contextName);
		}
		
		zapClientAPI.enableAllScanner(scanPolicyName );
		
		
		/*********************************************************************/
		zapClientAPI.setPolicyAttackStrength("0", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAttackStrength( "1", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAttackStrength("2", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAttackStrength("3", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAttackStrength( "4", "HIGH", scanPolicyName);
		
		/*********************************************************************/
		zapClientAPI.setPolicyAlertThreshold( "0", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAlertThreshold( "1", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAlertThreshold( "2", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAlertThreshold( "3", "HIGH", scanPolicyName);
		zapClientAPI.setPolicyAlertThreshold( "4", "HIGH", scanPolicyName);
		
		/*********************************************************************/
		zapClientAPI.setOptionPostForm( true);
		zapClientAPI.setOptionProcessForm( true);	
		zapClientAPI.setOptionHandleODataParametersVisited(true);
		zapClientAPI.setOptionShowAdvancedDialog(true);
		
		zapClientAPI.setOptionParseComments(true);
		zapClientAPI.setOptionParseRobotsTxt(true);
		zapClientAPI.setOptionParseSitemapXml(true);
		
		/*********************************************************************/
		
		
		//test.PassiveScanDisableAllScanner();
		zapClientAPI.PassiveScanEnableAllScanner();
	}

	/**
	 * set up authentication method for the created context
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @param loggedInIdicator indication for know its logged in
	 * @param usernameParameter parameter define in passing username
	 * @param passwordParameter parameter that define in passing password for the user
	 * @param contextId id of the creted context
	 * @param loginUrl login page url
	 * @throws ClientApiException
	 * @throws UnsupportedEncodingException
	 */
	private void setUpAuthenticationMethod(BuildListener listener, CustomZapClientApi zapClientAPI, 
				String loggedInIndicator, String usernameParameter, String passwordParameter,
				String contextId, String loginUrl) 
				throws UnsupportedEncodingException{

		String loginRequestData = usernameParameter+"={%username%}&"+passwordParameter+"={%password%}";

		// set authentication method 		
		// Prepare the configuration in a format similar to how URL parameters are formed. This
		// means that any value we add for the configuration values has to be URL encoded.
		StringBuilder formBasedConfig = new StringBuilder();
		formBasedConfig.append("loginUrl=").append(URLEncoder.encode(loginUrl, "UTF-8"));
		formBasedConfig.append("&loginRequestData=").append(URLEncoder.encode(loginRequestData, "UTF-8"));

		zapClientAPI.authentication.setAuthenticationMethod(zapProxyKey, contextId, "formBasedAuthentication",
				formBasedConfig.toString());
		
		//end set auth method
		listener.getLogger().println("Form Based Authentication added to context");

		//add logged in idicator
		zapClientAPI.authentication.setLoggedInIndicator(zapProxyKey, contextId, loggedInIndicator);
		listener.getLogger().println("Logged in indicator "+loggedInIndicator+" added to context ");

	}

	/**
	 * set up user for the context and enable user
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @param username user name to be used in authentication
	 * @param password password for the authentication user
	 * @param contextId id of the created context
	 * @return userId id of the newly setup user
	 * @throws ClientApiException
	 * @throws UnsupportedEncodingException 
	 */
	private String setUpUser(BuildListener listener, CustomZapClientApi zapClientAPI, String username,
						String password, String contextId) 
						throws  UnsupportedEncodingException {

		String userIdTemp;
		// add new user and authentication details
		// Make sure we have at least one user
		// extract user id 
		userIdTemp = extractUserId(zapClientAPI.users.newUser(zapProxyKey, contextId, "admin"));

		// Prepare the configuration in a format similar to how URL parameters are formed. This
		// means that any value we add for the configuration values has to be URL encoded.
		StringBuilder userAuthConfig = new StringBuilder();
		userAuthConfig.append("username=").append(URLEncoder.encode(username, "UTF-8"));
		userAuthConfig.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
		String authCon=userAuthConfig.toString();
		
		zapClientAPI.users.setAuthenticationCredentials(zapProxyKey, contextId, userIdTemp, authCon);

		listener.getLogger().println("New user added. username :" +username);
		
		zapClientAPI.users.setUserEnabled(zapProxyKey, contextId,userIdTemp,"true");
		listener.getLogger().println("User : "+username+" is now Enabled");

		return userIdTemp;
	}
	
	/**
	 * Set up all authentication details
	 * @author thilina27
	 * @param username user name to be used in authentication
	 * @param password password for the authentication user
	 * @param usernameParameter parameter define in passing username
	 * @param passwordParameter parameter that define in passing password for the user
	 * @param loginUrl login page url
	 * @param loggedInIdicator indication for know its logged in
	 * @throws ClientApiException
	 * @throws InterruptedException 
	 * @throws UnsupportedEncodingException
	 */
	private void setUpAuthentication(final String url, BuildListener listener, CustomZapClientApi zapClientAPI, 
				String username, String password, String usernameParameter, 
				String passwordParameter, String loginUrl, String loggedInIndicator)
				throws  UnsupportedEncodingException {

		//setup context
		this.contextId=setUpContext(listener,url,zapClientAPI);
				
		//set up authentication method
		setUpAuthenticationMethod(listener,zapClientAPI,loggedInIndicator,usernameParameter,
									passwordParameter,contextId,loginUrl);

		//set up user
		this.userId=setUpUser(listener,zapClientAPI,username,password,contextId);
	}
	
	
	
	/**
	 * Set up all authentication details
	 * @author thilina27
	 * @param username user name to be used in authentication
	 * @param password password for the authentication user
	 * @param usernameParameter parameter define in passing username
	 * @param passwordParameter parameter that define in passing password for the user
	 * @param loginUrl login page url
	 * @param loggedInIdicator indication for know its logged in
	 * @throws ClientApiException
	 * @throws InterruptedException 
	 * @throws UnsupportedEncodingException
	 */
	private void setUpAuthenticationConf(CustomZapClientApi zapClientAPI, BuildListener listener){

		listener.getLogger().println(zapClientAPI.getContextList());
		
		
		 
		//r�cup�re l'id du contexte si celui l� est cr�e sinon elle le cr�e et retourne son id 
		String contextId=zapClientAPI.getContextId(contextName,listener);
		this.setContextId(contextId);
		
		listener.getLogger().println("---------------------------------------");
		listener.getLogger().println("ContextId : "+contextId);	
		
		 
		/***************** AUTHENTIFICATION ********************/
		listener.getLogger().println("---------------------------------------");
		//test.setFormBasedAuthentication(api,contextId );
		//{"error":"false","engine":"Rhino","description":"","name":"b.espaceclientv3.orange.fr.js","type":"authentication"}
		//String LoginUrl,String postData, String Cookie, String scriptName 
		zapClientAPI.setScriptBasedAuthentication(contextId,LoginUrl,postData, cookie, scriptName, listener);
		
		listener.getLogger().println("---------------------------------------");
		zapClientAPI.setLoggedInIndicator(contextId,loggedInIndicator,listener);
		
		listener.getLogger().println("---------------------------------------");
		zapClientAPI.setLoggedOutIndicator(contextId,loggedOutIndicator,listener) ;
		
		listener.getLogger().println("---------------------------------------");
		zapClientAPI.listUserConfigInformation(contextId,listener);
		
		listener.getLogger().println("---------------------------------------");
		//String user, String username, String password
		String userid=zapClientAPI.setUserAuthConfig(contextId,user, username, password,listener);
		this.setUserId(userid);
		
		zapClientAPI.enableUser( contextId, userid,listener);
		
		/*********************** Forced User **********************************/
		//https://groups.google.com/forum/#!topic/zaproxy-users/GRtzMJ4WJzk
		//pour que la partie ajaxSpider se fasse d'une mani�re authentifi�e il faut activer et renseigner le ForcedUser 
		zapClientAPI.isForcedUserModeEnabled(listener);
		zapClientAPI.setForcedUser( contextId, userid,listener);
		zapClientAPI.getForcedUser(contextId,listener);
		zapClientAPI.setForcedUserModeEnabled( true,listener);
		zapClientAPI.isForcedUserModeEnabled(listener);
		
		
		
		/*********************************************************************/
		
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Search for all links and pages on the URL and raised passives alerts
	 *
	 * @param url the url to investigate
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @throws ClientApiException
	 * @throws InterruptedException 
	 */
//	private void spiderURL(final String url, BuildListener listener, ClientApi zapClientAPI) 
//			throws ClientApiException, InterruptedException {
//		// Method signature : scan(String key, String url, String maxChildren, String recurse)
//		zapClientAPI.spider.scan(zapProxyKey, url, "", "");
//
//		// Wait for complete spidering (equal to 100)
//		// Method signature : status(String scanId)
//		while (statusToInt(zapClientAPI.spider.status("")) < 100) {
//			listener.getLogger().println("Status spider = " + statusToInt(zapClientAPI.spider.status("")) + "%");
//			listener.getLogger().println("Alerts number = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			Thread.sleep(1000);
//		}
//	}
	private void spiderURL(final String url, CustomZapClientApi zapClientAPI,BuildListener listener) 
			throws  InterruptedException {
		// Method signature : scan(String key, String url, String maxChildren, String recurse)
		//zapClientAPI.spider.spider.scan(zapProxyKey, url, "", "");
		String scanId=zapClientAPI.spiderURL(url, "",listener);
		this.setScanId(scanId);
		

//		// Wait for complete spidering (equal to 100)
//		// Method signature : status(String scanId)
//		while (statusToInt(zapClientAPI.spider.status("")) < 100) {
//			listener.getLogger().println("Status spider = " + statusToInt(zapClientAPI.spider.status("")) + "%");
//			listener.getLogger().println("Alerts number = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			Thread.sleep(1000);
//		}
	}
	/**
	 * Search for all links and pages on the URL and raised passives alerts
	 * @author thilina27
	 * @param url the url to investigate
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @throws ClientApiException
	 * @throws InterruptedException
	 */
	 
	private void spiderURLAsUser(final String url, BuildListener listener, CustomZapClientApi zapClientAPI, 
				String contextId, String userId)
				throws InterruptedException {
		
		
		
		
		
		String scanId=zapClientAPI.spiderAsUserURL(url, this.getContextid(), this.getUserid(), "0", listener);
		this.setScanId(scanId);
		
		
		
		
		
		
		
		
		// Start spider as user
		//zapClientAPI.spider.scanAsUser(zapProxyKey, url, contextId, userId, "0", "");
		
//		// Wait for complete spidering (equal to 100)
//		// Method signature : status(String scanId)
//		while (statusToInt(zapClientAPI.spider.status("")) < 100) {
//			listener.getLogger().println("Status spider = " + statusToInt(zapClientAPI.spider.status("")) + "%");
//			listener.getLogger().println("Alerts number = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			Thread.sleep(1000);
//		}
	}

	/**
	 * @return the contextId
	 */
	public String getContextId() {
		return contextId;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param contextId the contextId to set
	 */
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Search for all links and pages on the URL and raised passives alerts
	 * @author thilina27
	 * @param url the url to investigate
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @throws ClientApiException
	 * @throws InterruptedException 
	 */
	private void ajaxSpiderURL(final String url, BuildListener listener, CustomZapClientApi zapClientAPI) 
			throws InterruptedException{


		//Method signature : scan(String apikey,String url,String inscope)
		//zapClientAPI.ajaxSpider.scan(zapProxyKey, url, "false");
		zapClientAPI.ajaxSpiderURL(url,listener, "false");
		
 		
 		// Wait for complete spidering (equal to status complete)
		// Method signature : status(String scanId)
//		while ("running".equalsIgnoreCase(statusToString(zapClientAPI.ajaxSpider.status()))) { 
//		    listener.getLogger().println("Status spider = " + statusToString(zapClientAPI.ajaxSpider.status()));
//			listener.getLogger().println("Alerts number = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			Thread.sleep(2500);
//		} 
	}
	
	/**
	 * Scan all pages found at url and raised actives alerts
	 *
	 * @param url the url to scan
	 * @param listener the listener to display log during the job execution in jenkins
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @throws ClientApiException
	 * @throws InterruptedException 
	 */
	private void scanURL(final String url, BuildListener listener, CustomZapClientApi zapClientAPI) 
			 throws InterruptedException {
		if(chosenPolicy == null || chosenPolicy.isEmpty()) {
			listener.getLogger().println("Scan url [" + url + "] with the policy by default");		
		} else {
			listener.getLogger().println("Scan url [" + url + "] with the following policy ["
							+ chosenPolicy + "]");
		}
		
		// Method signature : scan(String apikey, String url, String recurse, String inscopeonly, String scanpolicyname, String method, String postdata)
		// Use a default policy if chosenPolicy is null or empty
		//zapClientAPI.ascan.scan(zapProxyKey, url, "true", "false", chosenPolicy, null, null);
		
		zapClientAPI.scanURL(url, this.getScanId(), scanPolicyName, listener);
//		// Wait for complete scanning (equal to 100)
//		// Method signature : status(String scanId)
//		while (statusToInt(zapClientAPI.ascan.status("")) < 100) {
//			listener.getLogger().println("Status scan = " + statusToInt(zapClientAPI.ascan.status("")) + "%");
//			listener.getLogger().println("Alerts number = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			listener.getLogger().println("Messages number = " + zapClientAPI.core.numberOfMessages("").toString(2));
//			Thread.sleep(5000);
//		}
	}
	
	private void scanURLAsUser(final String url, BuildListener listener, CustomZapClientApi zapClientAPI) 
			 throws InterruptedException {
		if(chosenPolicy == null || chosenPolicy.isEmpty()) {
			listener.getLogger().println("Scan url [" + url + "] with the policy by default As User");		
		} else {
			listener.getLogger().println("Scan url [" + url + "] As User with the following policy ["
							+ chosenPolicy + "]");
		}
		
		// Method signature : scan(String apikey, String url, String recurse, String inscopeonly, String scanpolicyname, String method, String postdata)
		// Use a default policy if chosenPolicy is null or empty
		//zapClientAPI.ascan.scan(zapProxyKey, url, "true", "false", chosenPolicy, null, null);
		zapClientAPI.scanURLAsUser(url, this.getScanId(), this.getContextid(), this.getUserid(), "true", ScanPolicyName, listener);
		//zapClientAPI.scanURL(url, this.getScanId(), scanPolicyName, listener);
//		// Wait for complete scanning (equal to 100)
//		// Method signature : status(String scanId)
//		while (statusToInt(zapClientAPI.ascan.status("")) < 100) {
//			listener.getLogger().println("Status scan = " + statusToInt(zapClientAPI.ascan.status("")) + "%");
//			listener.getLogger().println("Alerts number = " + zapClientAPI.core.numberOfAlerts("").toString(2));
//			listener.getLogger().println("Messages number = " + zapClientAPI.core.numberOfMessages("").toString(2));
//			Thread.sleep(5000);
//		}
	}
	
	/**
	 * Stop ZAproxy if it has been previously started.
	 * 
	 * @param zapClientAPI the client API to use ZAP API methods
	 * @param listener the listener to display log during the job execution in jenkins
	 * @throws ClientApiException 
	 */
	private void stopZAP(CustomZapClientApi zapClientAPI, BuildListener listener)  {
		if (zapClientAPI != null) {
			listener.getLogger().println("Shutdown ZAProxy");
			//throw new ClientApiException("Exception lancee dans stopZAP");
			zapClientAPI.core.shutdown(zapProxyKey);
		} else {
			listener.getLogger().println("No shutdown of ZAP (zapClientAPI==null)");
		}
	}
	
	
	/**
	 * Descriptor for {@link ZAProxy}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See <tt>src/main/resources/fr/novia/zaproxyplugin/ZAProxy/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	public static class ZAProxyDescriptorImpl extends Descriptor<ZAProxy> implements Serializable {
		
		private static final long serialVersionUID = 4028279269334325901L;
		
		/**
		 * To persist global configuration information,
		 * simply store it in a field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		
		/** Map where key is the report format represented by a String
		 *  and value is a ZAPreport object allowing to generate a report with the corresponding format.
		 */
		private Map<String, ZAPreport> mapFormatReport;
		
		/** Represents the build's workspace */
		private FilePath workspace;
		
		/**
		 * In order to load the persisted global configuration, you have to
		 * call load() in the constructor.
		 */
		public ZAProxyDescriptorImpl() {
			mapFormatReport = ZAPreportCollection.getInstance().getMapFormatReport();
			load();
		}
		
		@Override
		public String getDisplayName() { 
			return null; 
		}

		public Map<String, ZAPreport> getMapFormatReport() {
			return mapFormatReport;
		}
		
		public List<String> getAllFormats() {
			return new ArrayList<String>(mapFormatReport.keySet());
		}
		
		public void setWorkspace(FilePath ws) {
			this.workspace = ws;
		}
		
		/**
		 * Performs on-the-fly validation of the form field 'filenameReports'.
		 *
		 * @param filenameReports
		 *      This parameter receives the value that the user has typed.
		 * @return
		 *      Indicates the outcome of the validation. This is sent to the browser.
		 *      <p>
		 *      Note that returning {@link FormValidation#error(String)} does not
		 *      prevent the form from being saved. It just means that a message
		 *      will be displayed to the user.
		 */
		public FormValidation doCheckFilenameReports(@QueryParameter("filenameReports") final String filenameReports) {
			if(filenameReports.isEmpty())
				return FormValidation.error("Field is required");
			if(!FilenameUtils.getExtension(filenameReports).isEmpty())
				return FormValidation.warning("A file extension is not necessary.");
			return FormValidation.ok();
		}
		
		/**
		 * Performs on-the-fly validation of the form field 'filenameSaveSession'.
		 * <p>
		 * If the user wants to save session whereas a session is already loaded, 
		 * the relative path to the saved session must be different from the relative path to the loaded session.
		 *
		 * @param filenameLoadSession
		 *      Parameter to compare with filenameSaveSession.
		 * @param filenameSaveSession
		 *      This parameter receives the value that the user has typed.
		 * @return
		 *      Indicates the outcome of the validation. This is sent to the browser.
		 *      <p>
		 *      Note that returning {@link FormValidation#error(String)} does not
		 *      prevent the form from being saved. It just means that a message
		 *      will be displayed to the user.
		 */
		public FormValidation doCheckFilenameSaveSession(
				@QueryParameter("filenameLoadSession") final String filenameLoadSession,
				@QueryParameter("filenameSaveSession") final String filenameSaveSession) {
			// Contains just the name of the session (without workspace path and extension)
			String cleanFilenameLoadSession = null;
			if(workspace != null) {
				cleanFilenameLoadSession = filenameLoadSession
						.replace(workspace.getRemote(), "") // Remove workspace path
						.replaceFirst("\\\\", "") // Remove separator after workspace path if windows
						.replaceFirst("/", ""); // Remove separator after workspace path if Unix
					
				if(!cleanFilenameLoadSession.isEmpty() && 
						(filenameSaveSession.equals(cleanFilenameLoadSession) 
								|| filenameSaveSession.equals(cleanFilenameLoadSession.replace(FILE_SESSION_EXTENSION, ""))) )
					return FormValidation.error("The saved session filename is the same of the loaded session filename.");
			}
			
			if(!filenameLoadSession.isEmpty())
				return FormValidation.warning("A session is loaded, so it's not necessary to save session");
			
			if(!FilenameUtils.getExtension(filenameSaveSession).isEmpty())
				return FormValidation.warning("A file extension is not necessary. A default file extension will be added (.session)");
			return FormValidation.ok();
		}
		
		/**
		 * List model to choose the alert report format
		 * 
		 * @return a {@link ListBoxModel}
		 */
		public ListBoxModel doFillChosenFormatsItems() {
			ListBoxModel items = new ListBoxModel();
			for(String format: mapFormatReport.keySet()) {
				items.add(format);
			}
			return items;
		}
		
		/**
		 * List model to choose the tool used (normally, it should be the ZAProxy tool).
		 * 
		 * @return a {@link ListBoxModel}
		 */
		public ListBoxModel doFillToolUsedItems() {
			ListBoxModel items = new ListBoxModel();
			for(ToolDescriptor<?> desc : ToolInstallation.all()) {
				for (ToolInstallation tool : desc.getInstallations()) {
					items.add(tool.getName());
				}
			}
			return items;
		}
		
		/**
		 * List model to choose the policy file to use by ZAProxy scan. It's called on the remote machine (if present)
		 * to load all policy files in the ZAP default dir of the build's machine.
		 * 
		 * @param zapDefaultDir A string that represents an absolute path to the directory that ZAP uses.
		 * @return a {@link ListBoxModel}. It can be empty if zapDefaultDir doesn't contain any policy file.
		 */		
		public ListBoxModel doFillChosenPolicyItems(@QueryParameter String zapDefaultDir) {			
			ListBoxModel items = new ListBoxModel();
			
			// No workspace before the first build, so workspace is null
			if(workspace != null) {
				File[] listFiles = {};
					try {
						listFiles = workspace.act(new PolicyFileCallable(zapDefaultDir));
					} catch (IOException e) {
						// No listener because it's not during a build but it's on the job config page
						e.printStackTrace();
					} catch (InterruptedException e) {
						// No listener because it's not during a build but it's on the job config page
						e.printStackTrace();
					}
					
				items.add(""); // To not load a policy file, add a blank choice
				
				// Add policy files to the list, without their extension
				for(int i = 0; i < listFiles.length; i++) {
					items.add(FilenameUtils.getBaseName(listFiles[i].getName()));
				}
			}
		
			return items;
		}
		
		/**
		 * List model to choose the ZAP session to use. It's called on the remote machine (if present)
		 * to load all session files in the build's workspace.
		 * 
		 * @return a {@link ListBoxModel}. It can be empty if the workspace doesn't contain any ZAP sessions.
		 * @throws InterruptedException 
		 * @throws IOException 
		 */
		public ListBoxModel doFillFilenameLoadSessionItems() throws IOException, InterruptedException {
			ListBoxModel items = new ListBoxModel();
			
			// No workspace before the first build, so workspace is null
			if(workspace != null) {
				Collection<String> sessionsInString = workspace.act(new FileCallable<Collection<String>>() {
					private static final long serialVersionUID = 1328740269013881941L;
	
					public Collection<String> invoke(File f, VirtualChannel channel) {
						
						// List all files with FILE_SESSION_EXTENSION on the machine where the workspace is located
						Collection<File> colFiles = FileUtils.listFiles(f,
								FileFilterUtils.suffixFileFilter(FILE_SESSION_EXTENSION),
								TrueFileFilter.INSTANCE);
						
						Collection<String> colString = new ArrayList<String>();
						
						// "Transform" File into String
						for (File file : colFiles) {
							colString.add(file.getAbsolutePath());
							// The following line is to remove the full path to the workspace,
							// keep just the relative path to the session
							//colString.add(file.getAbsolutePath().replace(workspace.getRemote() + File.separatorChar, ""));
						}
						return colString;
					}
	
					@Override
					public void checkRoles(RoleChecker checker) throws SecurityException {
						// Nothing to do
					}
				});
			
				items.add(""); // To not load a session, add a blank choice
				
				for (String s : sessionsInString) {
					items.add(s);
				}
			}
			
			return items;
		}
	}
	
	/**
	 * This class allows to search all ZAP policy files in the ZAP default dir of the remote machine
	 * (or local machine if there is no remote machine). It's used in the plugin configuration page
	 * to fill the list of policy files and choose one of them.  
	 * 
	 * @author ludovic.roucoux
	 *
	 */
	private static class PolicyFileCallable implements FileCallable<File[]> {
		private static final long serialVersionUID = 1328740269013881941L;
		
		private String zapDefaultDir;
		
		public PolicyFileCallable(String zapDefaultDir) {
			this.zapDefaultDir = zapDefaultDir;
		}

		public File[] invoke(File f, VirtualChannel channel) {
			File[] listFiles = {};
			
			Path pathPolicyDir = Paths.get(zapDefaultDir, NAME_POLICIES_DIR_ZAP);
			
			if(Files.isDirectory(pathPolicyDir)) {
				File zapPolicyDir = new File(zapDefaultDir, NAME_POLICIES_DIR_ZAP);
				// create new filename filter (get only file with FILE_POLICY_EXTENSION extension)
				FilenameFilter policyFilter = new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						if (name.lastIndexOf('.') > 0) {
							// get last index for '.' char
							int lastIndex = name.lastIndexOf('.');

							// get extension
							String str = name.substring(lastIndex);

							// match path name extension
							if (str.equals(FILE_POLICY_EXTENSION)) {
								return true;
							}
						}
						return false;
					}
				};
				
				// returns pathnames for files and directory
				listFiles = zapPolicyDir.listFiles(policyFilter);
			}
			return listFiles;
		}
	
		@Override
		public void checkRoles(RoleChecker checker) throws SecurityException {
			// Nothing to do
		}
	}
	
	/**
	 * This class allows to launch a method on a remote machine (if there is, otherwise, on a local machine).
	 * The method launched is to wait the complete initialization of ZAProxy.
	 * 
	 * @author ludovic.roucoux
	 *
	 */
	private static class WaitZAProxyInitCallable implements FileCallable<Void> {

		private static final long serialVersionUID = -313398999885177679L;
		
		private ZAProxy zaproxy; 
		private BuildListener listener;
		
		public WaitZAProxyInitCallable(ZAProxy zaproxy, BuildListener listener) {
			this.zaproxy = zaproxy;
			this.listener = listener;
		}

		@Override
		public Void invoke(File f, VirtualChannel channel) {
			zaproxy.waitForSuccessfulConnectionToZap(zaproxy.timeoutInSec, listener);
			return null;
		}
		
		@Override
		public void checkRoles(RoleChecker checker) throws SecurityException {
			// Nothing to do
		}
	}
}