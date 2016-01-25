package com.mihaita.mail.reader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Security;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.naming.AuthenticationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConnectController {
	private static final Logger log = Logger.getLogger(ConnectController.class.getSimpleName());
	
	@RequestMapping("/")
	public @ResponseBody String connect(
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam("server") String server,
			@RequestParam("protocol") String protocol,
			@RequestParam("port") int port) throws AuthenticationException, MessagingException {
		log.info("Connect: username = " + username);
		StringBuffer sb = new StringBuffer();
		PrintStream stdout = System.out;

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    
		System.setOut(ps);
		String logs = "";
		
		try {
			String debug = System.getProperty("javax.net.debug");
			log.info("Starting to log from here: javax.net.debug = " + debug);
			SSLContext context = SSLContext.getDefault();
			SSLSocketFactory sf = context.getSocketFactory();
			log.info("getSupportedCipherSuites: " + Arrays.toString(sf.getSupportedCipherSuites()));
			log.info("getDefaultCipherSuites: " + Arrays.toString(sf.getDefaultCipherSuites()));
			log.info("getProviders: " + Arrays.toString(Security.getProviders()));
			SpringBootMailReaderApplication.getFolders(username, password, server, protocol, port).stream().map( f -> f.toString() + "<br/>").forEach(sb::append);
		} catch(Exception e) {
		    System.out.flush();
			logs =  baos.toString();
			
			throw new IllegalStateException("Oops: " + e + " Logs: " + logs, e);
		} finally {
		    System.out.flush();
			System.setOut(stdout);	
		}     
		
		log.info("Connect: Done");
		logs =  baos.toString();
		logs.replace("\n", "<br/>");
		
		return "Folders: " + sb + " \nSSL: " + logs;
	}

	@RequestMapping("/provider")
	public @ResponseBody String demo(@RequestParam(value="addProvider", defaultValue="false") boolean add) {
		log.info("Demo");
		if (add)
			Security.addProvider(new BouncyCastleProvider());
		else 
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		return "Hello from controller backed by so many providers: " +  Arrays.toString(Security.getProviders());
	}
	
	
	@ExceptionHandler
	public @ResponseBody String onError(Exception e) {
		
		return "error: " + e;
	}
}