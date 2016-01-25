# spring-boot-mail-reader
Debug ssl connection when you connect to a mailbox from a web container like tomcat

http://localhost:8080/?username=user.abc&password=zzzzz&server=m.outlook.com&protocol=imaps&port=993


java.security.Security is shared across all instances in Tomcat and using an old version of bouncy castle (1.38) caused the SSL error:

Caused by: java.lang.RuntimeException: Could not generate DH keypair
        at sun.security.ssl.ECDHCrypt.<init>(ECDHCrypt.java:80)
