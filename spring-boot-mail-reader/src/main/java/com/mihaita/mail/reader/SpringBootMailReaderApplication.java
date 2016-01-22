package com.mihaita.mail.reader;

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
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

@SpringBootApplication
public class SpringBootMailReaderApplication {
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private static final Logger log = Logger.getLogger(SpringBootMailReaderApplication.class.getSimpleName());
	
	@Bean
	public CommandLineRunner runner() {
		return args -> {
			PropertySource<?> propertySource = new SimpleCommandLinePropertySource(args);
			
			SSLContext context = SSLContext.getDefault();
			SSLSocketFactory sf = context.getSocketFactory();
			System.out.println("================================================");
			System.out.println("getSupportedCipherSuites: " + Arrays.toString(sf.getSupportedCipherSuites()));
			System.out.println("getDefaultCipherSuites: " + Arrays.toString(sf.getDefaultCipherSuites()));
			System.out.println("================================================");
			String username = (String) propertySource.getProperty("username");
			String password = (String) propertySource.getProperty("password");
			String serverAddress = (String) propertySource.getProperty("server");
			String protocol = (String) propertySource.getProperty("protocol");
			int port = Integer.parseInt((String) propertySource.getProperty("port"));
			
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
					store.connect(serverAddress, port, username, password);
				} else {
					store.connect(serverAddress, username, password);
				}
				
				getRoots(store.getDefaultFolder()).forEach(System.out::println);
				
			} catch (MessagingException e) {
				log.severe("Error when connecting to mailbox: " + e);
				throw new AuthenticationException("Authentication failed. Check username/password.");
			} catch (Exception e) {
				throw new RuntimeException("Could not retrieve the list of folders from the email box.",  e);
			} finally {
				if (store != null)
					store.close();
			}
		};
	}
	
	/**
	 * @return list of all the root folders.
	 */
	public List<Folder> getRoots(Folder root) {
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
	
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMailReaderApplication.class, args);
	}
}
