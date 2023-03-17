# Distributed-File-System

## Here is how to run the Document Server: 

Option 1: (Locally) 
1. Build the Project which exports a JAR file under PROJECT_ROOT/out/artifacts
2. Run by using command: 'java -jar DistributedSystemsProject.jar --server.port=8083'. This will start a Leader server.
3. Run multiple workers as desired by using command: java -jar DistributedSystemsProject.jar --server.port=8084/8085/8086...

Option 2: (On AWS)
1. Launch AWS EC2 instances (1 for leader & minimum 3 for workers)
2. Build the project and export the JAR file
3. Copy JAR file to EC2 instances
4. Install JDK and Tmux on the EC2 instances
5. Start tmux
6. Start leader server using 'sudo java -jar DistributedSystemsProject.jar'
7. Start worker server using 'sudo java -jar DistributedSystemsProject.jar --nodeId 'http://<ip_of_machine' '
8. Press Control + B, then release and press D
9. This will ensure that the process is not killed when we kill the SSH connection. 
