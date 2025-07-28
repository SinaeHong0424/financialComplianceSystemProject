#Required Software

Java JDK 11+ - Application runtime
Maven 3.9+ - Build tool
Oracle Database 23c Free - Database
Git 2.40+ - Version control
IDE Latest - Code editing

Optional: Oracle SQL Developer, Postman, Docker, VS Code

#Java Development Kit (JDK)
Install Oracle JDK or OpenJDK 11. Verify with java -version and javac -version. Set JAVA_HOME and PATH on Windows or macOS/Linux.

#Apache Maven
Install via package manager or download. Verify with mvn -version. Optional settings.xml for local repo and mirrors.

#Oracle Database 26c Free
Install natively or via Docker. For Docker, pull the image, run container, wait 5-10 min, connect using sqlplus. Create DFS user with necessary privileges and quota.

#Git Setup
Install Git via package manager or download. Configure username, email, default editor, and branch. Optional: generate SSH key for GitHub.

#IDE Setup
IntelliJ IDEA recommended: open project, set SDK to Java 11, configure Maven. Plugins: Lombok, SonarLint.
Alternatives: VS Code with Java/Maven extensions, Eclipse with JDK 11.

#Project Setup
Clone repo via HTTPS or SSH. Run mvn clean install, mvn test, mvn package. Connect to Oracle user and run schema scripts. Verify tables.

#Application Configuration
Edit application.properties for DB connection. Local overrides in application-local.properties for user, password, and debug logging.

#Running the Application
Maven: mvn tomcat7:run
IDE: run main controller or configure Tomcat server
Standalone JAR (future): java -jar target/dfs-compliance.war
Test endpoints via curl or Postman.

#Troubleshooting
Maven fails: clear cache or specify settings.xml.
Oracle connection: check listener and unlock account if needed.
Port conflicts: find and kill process or change port.
JDK mismatch: ensure JAVA_HOME points to JDK 11.

#Recommended Workflow
Update from remote, start DB, compile project, code, test frequently, commit, push daily.

#Additional Tools
Optional SonarQube for code quality and Swagger for API documentation.

#Useful Commands
Maven: clean, compile, test, package, install, dependency:tree
Git: status, log, diff, branch, remote
Oracle: sqlplus connection, lsnrctl status, run scripts