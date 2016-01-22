package com.mihaita.mail.reader;

import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.naming.AuthenticationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@WebAppConfiguration
public class SpringBootMailReaderApplication extends SpringBootServletInitializer {
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private static final Logger log = Logger.getLogger(SpringBootMailReaderApplication.class.getSimpleName());
	static {

		System.setProperty("javax.net.debug", "ssl");
	}
	@Bean
	public CommandLineRunner runner() {
		return args -> {
			PropertySource<?> propertySource = new SimpleCommandLinePropertySource(args);
			
			SSLContext context = SSLContext.getDefault();
			SSLSocketFactory sf = context.getSocketFactory();
			log.info("================================================");
			log.info("getSupportedCipherSuites: " + Arrays.toString(sf.getSupportedCipherSuites()));
			log.info("getDefaultCipherSuites: " + Arrays.toString(sf.getDefaultCipherSuites()));
			log.info("getProviders: " + Arrays.toString(Security.getProviders()));
			log.info("================================================");
			String username = (String) propertySource.getProperty("username");
			if (username == null) {
				log.warning("no username provided. stop!");
				return;
			}
			String password = (String) propertySource.getProperty("password");
			String server = (String) propertySource.getProperty("server");
			String protocol = (String) propertySource.getProperty("protocol");
			int port = Integer.parseInt((String) propertySource.getProperty("port"));
			getFolders(username, password, server, protocol, port).stream().map( f -> f.toString()).forEach(log::info);
		};
	}
	
	public static List<Folder> getFolders(String username, String password, String server, String protocol, int port) throws AuthenticationException, MessagingException {
		Properties props = new Properties();
		if ("imaps".equalsIgnoreCase(protocol)) {
			props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.imap.socketFactory.fallback", "false");
		} else if ("pop3s".equalsIgnoreCase(protocol)) {
			props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
		}

		Session session = Session.getInstance(props);
		Store store = null;
    	try {
			store = session.getStore(protocol);
			if (port >= 0) {
				store.connect(server, port, username, password);
			} else {
				store.connect(server, username, password);
			}
			
			return getRoots(store.getDefaultFolder());
			
		} catch (MessagingException e) {
			log.severe("Error when connecting to mailbox: " + e);
			throw new AuthenticationException("Authentication failed. Error:" + e);
		} catch (Exception e) {
			throw new RuntimeException("Could not retrieve the list of folders from the email box.",  e);
		} finally {
			if (store != null)
				store.close();
		}
	}
	
	/**
	 * @return list of all the root folders.
	 */
	public static List<Folder> getRoots(Folder root) {
		List<Folder> roots = new ArrayList<Folder>();
		Folder[] list = null;
		try {
			list = root.list();
			if (list != null) {
				for (Folder folder: list) {

					if (folder != null && folder.exists()) {
						roots.add(folder);
					}
				}
			}
		} catch (MessagingException e) {
			throw new IllegalStateException("Could not retrieve the list with the folders",  e);
		}
		return roots;
	}
	

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringBootMailReaderApplication.class);
    }
    
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMailReaderApplication.class, args);
	}
	
}
