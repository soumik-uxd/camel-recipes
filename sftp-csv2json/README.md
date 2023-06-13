# SFTP CSV to JSON
An [Apache Camel](https://camel.apache.org/) route to map CSV rows from an SFTP source directory to JSON rows to a target SFTP directory.

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
#### 3. Run the application
The application requires an SFTP service (an SFTP service based on [atmoz/sftp](https://hub.docker.com/r/atmoz/sftp) is present in the `docker-compose.yml`), there are two spring profiles `demo` and `docker`. The `docker` profile is configured to use the services run via `docker-compose`. If we wish to use the `docker` profile (the easier approach), we can refer the `docker-compose.yml`. 

**Note**: Before starting the `docker-compose` we need to check that the env variable `CSV_DELIMITER` reflects the correct delimeter (i.e. comma, semicolon etc.). It is set to comma by default.

For using the docker profile we can simply run:
```bash
docker-compose up -d
```
#### 3a. Run the application locally
For the `demo` spring profile we need to start our own SFTP service. The SFTP service can be run by:
```bash
docker run --name sftp1 -v $PWD/<src_dir>:/home/<user_name1>/<src_dir> -p 2221:22 -d atmoz/sftp <user_name1>:<password1>:::<src_dir>
docker run --name sftp2 -v $PWD/<target_dir>:/home/<user_name2>/<target_dir> -p 2222:22 -d atmoz/sftp <user_name2>:<password2>:::<target_dir>
```
Then we need to adjust the source and target directories in the `application-demo.properties` file by adjusting the following properties. 
```properties
app.sftp.input.host=localhost
app.sftp.input.port=2221
app.sftp.input.remote-dir=<src_dir>
app.sftp.input.username=<user_name1>
app.sftp.input.password=<password1>
...

app.sftp.output.host=localhost
app.sftp.output.port=2222
app.sftp.output.remote-dir=<target_dir>
app.sftp.output.username=<user_name2>
app.sftp.output.password=<password2>
```
Then we rebuild our package:
```bash
./mvnw -V -B -DskipTests clean package verify
```
**Note**: We can also run a single SFTP service that can double up as a source and the destination. But we need to ensure ensure then that the `<target_dir>` is a subdirectory under the `<src_dir>`, else there might be IO/permission issues . E.g.
```bash
docker run --name sftpserv -v $PWD/<src_dir>:/home/<user_name>/<src_dir> -p 2222:22 -d atmoz/sftp <user_name>:<password>:::<src_dir>
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
To avoid permission issues we also need to give the docker user (in this case 1001) the ownership to our local `<src_dir>` and `<target_dir>`.
```bash
sudo chown 1001:1001 <src_dir>
sudo chown 1001:1001 <target_dir>
```
Then we can start the application using
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```
#### 4. Upload the files
Once application is started the endpoint available:
- Either at `sftp:\\localhost:2221` and `sftp:\\localhost:2222` (in case 2 SFTP services are started).
- Or at `sftp:\\localhost:2222` (in case 1 SFTP service is started locally or via `docker-compose`).

We can now upload our files at this endpoint:
- with an SFTP client or
- via the `scp` command
- copying files directly to the local `<src_dir>`

Once files are present at the source destination the application should automatically transform the files to the destination.

#### 4. Stop the application
- If the applications was started via `docker-compose` then it can be stopped using.
```bash
docker-compose down
```
In case the volumes need to be removed we can use:
```bash
docker-compose down --volumes
```
- If the application was started locally please ensure that the SFTP service(s) are stopped after stopping the application.