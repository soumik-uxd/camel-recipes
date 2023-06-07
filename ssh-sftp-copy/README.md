# SFTP Copy
An Apache Camel route to copy from one sftp source to another sftp target using SSH key authentication.

## How to
### 1. Clone the application
Clone the repository
```bash
git clone <repo>
```
#### 2. Build the application (optional)
The application will be automatically built if `docker-compose` is used (see below).
But if we wish to do a local build, we need to first install the dependencies
```bash
./mvnw clean install
```
Then we build the packages
```bash
./mvnw -V -B -DskipTests clean package verify
```
#### 3. Generate keys
The application requires an sftp service (an sftp service based on [atmoz/sftp](https://hub.docker.com/r/atmoz/sftp) is present in the `docker-compose.yml`), and we will SSH key based authentication. For this we need to generate keypairs.
- Create the directory `keys`. 
- Create the keypair as below:
```bash
ssh-keygen -t ed25519 -f ./keys/ssh_host_ed25519_key < /dev/null
ssh-keygen -t rsa -b 4096 -f ./keys/ssh_host_rsa_key < /dev/null
```
- In case we wish to use multiple SFTP services (one for source and the other for target), then we would need another pair of keypairs.
#### 4. Run the application
There are two spring profiles local and docker. The docker profile is configured to use the services run via `docker-compose`. If we wish to use the docker profile (the easier approach), we refer the `docker-compose.yml`.
Once the keypair is generated, to use the docker profile we run:
```bash
docker-compose up -d
```
- If we refer to the `docker-compose.yml`, it is configured to use a single SFTP service as both the source and the target. In case we wish to use multiple SFTP services (one for source and the other for target), you would need to adjust the `docker-compose.yml` and the `application-local.properties` to ensure that the correct path and credentials are passed. Then we can run:
```bash
docker-compose up -d --build
```
#### 3a. Run the application locally
For the local spring profile you need to start your own sftp service. The SFTP service can be run by:
```bash
```bash
docker run -d --rm --name sshftp1 \
    -v $PWD/keys/ssh_host_ed25519_key:/etc/ssh/ssh_host_ed25519_key \
    -v $PWD/keys/ssh_host_rsa_key:/etc/ssh/ssh_host_rsa_key \
    -v $PWD/keys/ssh_host_ed25519_key.pub:/home/<user_name1>/.ssh/keys/ssh_host_ed25519_key.pub:ro \
    -v $PWD/keys/ssh_host_rsa_key.pub:/home/<user_name1>/.ssh/keys/ssh_host_rsa_key.pub:ro \
    -v $PWD/<src_dir>:/home/<user_name1>/<src_dir> \
    -p 2221:22 atmoz/sftp:latest <user_name1>::1001
docker run -d --rm --name sshftp2 \
    -v $PWD/keys/ssh_host_ed25519_key:/etc/ssh/ssh_host_ed25519_key \
    -v $PWD/keys/ssh_host_rsa_key:/etc/ssh/ssh_host_rsa_key \
    -v $PWD/keys/ssh_host_ed25519_key.pub:/home/<user_name2>.ssh/keys/ssh_host_ed25519_key.pub:ro \
    -v $PWD/keys/ssh_host_rsa_key.pub:/home/<user_name2>/.ssh/keys/ssh_host_rsa_key.pub:ro \
    -v $PWD/<target_dir>:/home/<user_name2>/<target_dir> \
    -p 2222:22 atmoz/sftp:latest <user_name2>::1001    
```
Then we need to adjust the source and target directories in the `application-local.properties` file by adjusting the following properties.
```properties
app.sftp.input.host=localhost
app.sftp.input.port=2221
app.sftp.input.remote-dir=<src_dir>
app.sftp.input.username=<user_name1>
...

app.sftp.output.host=localhost
app.sftp.output.port=2222
app.sftp.output.remote-dir=<target_dir>
app.sftp.output.username=<user_name2>
```
Then we rebuild our package:
```bash
./mvnw -V -B -DskipTests clean package verify
```
**Note**: You can also run a single SFTP service that can double up as a source and the destination. Please ensure then that the `<target_dir>` is a subdirectory under the `<src_dir>`, else there might be IO/permission issues . E.g.
```bash
docker run -d --rm --name sshftp \
    -v $PWD/keys/ssh_host_ed25519_key:/etc/ssh/ssh_host_ed25519_key \
    -v $PWD/keys/ssh_host_rsa_key:/etc/ssh/ssh_host_rsa_key \
    -v $PWD/keys/ssh_host_ed25519_key.pub:/home/<user_name>.ssh/keys/ssh_host_ed25519_key.pub:ro \
    -v $PWD/keys/ssh_host_rsa_key.pub:/home/<user_name>/.ssh/keys/ssh_host_rsa_key.pub:ro \
    -v $PWD/<src_dir>:/home/<user_name>/<src_dir> \
    -p 2222:22 atmoz/sftp:latest <user_name>::1001
```
```properties
app.sftp.input.host=localhost
app.sftp.input.port=2222
app.sftp.input.remote-dir=<src_dir>
app.sftp.input.username=<user_name>
app.sftp.input.password=<password>
...
app.sftp.output.host=localhost
app.sftp.output.port=2222
app.sftp.output.remote-dir=<src_dir>/<target_dir>
app.sftp.output.username=<user_name>
app.sftp.output.password=<password>
```
To avoid permission issues we also need to give the docker user (in this case ) the ownership top our local `<src_dir>` and `<target_dir>`.
```bash
sudo chown 1001:1001 <src_dir>
sudo chown 1001:1001 <target_dir>
```
Then we can start the application 
```bash
export SSH_KEY=$(cat keys/ssh_host_ed25519_key)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dapp.sftp.input.sshkey="$SSH_KEY" -Dapp.sftp.output.sshkey="$SSH_KEY"
```
- Please note that if the SSH keys are different for the source and the target they need to be passed as separate env variables. E.g.
```bash
export SSH_KEY_IN=$(cat keys/in_ssh_host_ed25519_key)
export SSH_KEY_OUT=$(cat keys/out_ssh_host_ed25519_key)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dapp.sftp.input.sshkey="$SSH_KEY_IN" -Dapp.sftp.output.sshkey="$SSH_KEY_OUT"
```
#### 4. Upload the files
Once application is started the endpoint available at 
- Either at `sftp:\\localhost:2221` and `sftp:\\localhost:2222` (in case 2 SFTP services are started).
- Or at `sftp:\\localhost:2222` (in case 1 SFTP service is started locally or via `docker-compose`). 

We can now upload our files at this endpoint: 
- with an SFTP client or 
- via the `scp` command
- copying files directly to the local `<src_dir>`

Once files are present at the source destination the application automatically should copy the files to the destination.

#### 4. Stop the application
- If the applications was started via `docker-compose` then it can be stopped using.
```bash
docker-compose down
```
In case the volumes need to be removed we can use:
```bash
docker-compose down --volumes
```
- If the application was started locally please ensure the SFTP service(s) are stopped after stopping the application.



