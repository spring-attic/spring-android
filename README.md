# Spring for Android

[Spring for Android] is an extension of the [Spring Framework] that aims to simplify the development of native [Android] applications.


## Downloading artifacts

The [Android Maven Plugin] makes it possible to build Android applications utilizing the power of Maven dependency management. See [downloading Spring artifacts] for Maven repository information. Unable to use Maven or other transitive dependency management tools? See [building a distribution with dependencies].


### Dependencies

Spring for Android consists of three modules: Core, Rest Template, and Auth.

```xml
<dependency>
    <groupId>org.springframework.android</groupId>
    <artifactId>spring-android-core</artifactId>
    <version>${org.springframework.android-version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.android</groupId>
    <artifactId>spring-android-rest-template</artifactId>
    <version>${org.springframework.android-version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.android</groupId>
    <artifactId>spring-android-auth</artifactId>
    <version>${org.springframework.android-version}</version>
</dependency>
```

### Repositories

```xml
<repository>
	<id>spring-repo</id>
	<name>Spring Repository</name>
	<url>http://repo.springsource.org/release</url>
</repository>	
	
<repository>
	<id>spring-milestone</id>
	<name>Spring Milestone Repository</name>
	<url>http://repo.springsource.org/milestone</url>
</repository>

<repository>
	<id>spring-snapshot</id>
	<name>Spring Snapshot Repository</name>
	<url>http://repo.springsource.org/snapshot</url>
</repository>
```

## Documentation

See the current [Javadoc] and [reference docs].


## Sample Applications

Several example projects are available in the [samples repository].


## Issue Tracking

Report issues via the [Spring Android JIRA]. While JIRA is preferred, [GitHub issues] are also welcome. Understand our issue management process by reading about [the lifecycle of an issue].


## Build from Source

1. Clone the repository from GitHub:

	```sh
	$ git clone git://github.com/SpringSource/spring-android.git
	```

2. Navigate into the cloned repository directory:

	```sh
	$ cd spring-android
	```

3. The project uses [Gradle] to build:

	```sh
	$ ./gradlew build
	```
		
4. Install jars into your local Maven cache (optional)

	```sh
	$ ./gradlew install
	```


## Import Source into your IDE

### Eclipse

1. To generate Eclipse metadata (.classpath and .project files):

	```sh
	$ ./gradlew eclipse
	```

2. Once complete, you may then import the projects into Eclipse as usual:

	```
	File -> Import -> Existing projects into workspace
	```

> **Note:** [Spring Tool Suite] has built in support for [Gradle], and you can simply import as Gradle projects.

### IDEA

Generate IDEA metadata (.iml and .ipr files):

```sh
$ ./gradlew idea
```


## Tests

There are three Android Test Projects located in the "test" folder of the repository that correspond to the three Spring for Android Modules (Core, Rest Template, and Auth). To run the suite of tests, perform the following steps. The parent POM located in the root of the "test" folder will execute each test project on all attached devices and emulators. The tests will fail if there is no device or emulator attached.

Run the Android tests:

```sh
$ ./gradlew testAndroid
```
		
> **Note:** To view the output, use the **--info** parameter when running Gradle

Gradle runs the tests using the [Android Maven Plugin]. Alternatively the test suite can be executed using the following Maven command:

```sh
$ mvn clean install -f ./test/pom.xml
```

Test results are available in the following directory for each test project:

```
/test/<test-project>/target/surefire-reports
```


## Contributing

[Pull requests] are welcome. See the [contributor guidelines] for details.


## License

Spring for Android is released under version 2.0 of the [Apache License].


[Spring for Android]: http://www.springsource.org/spring-android
[Spring Framework]: http://www.springsource.org/spring-framework
[Android]: http://developer.android.com/index.html
[Android Maven Plugin]: http://code.google.com/p/maven-android-plugin
[downloading Spring artifacts]: https://github.com/SpringSource/spring-framework/wiki/Downloading-Spring-artifacts
[building a distribution with dependencies]: https://github.com/SpringSource/spring-framework/wiki/Building-a-distribution-with-dependencies
[Javadoc]: http://static.springsource.org/spring-android/docs/1.0.x/api/
[reference docs]: http://static.springsource.org/spring-android/docs/1.0.x/reference/html/
[samples repository]: https://github.com/SpringSource/spring-android-samples
[Spring Android JIRA]: http://jira.springsource.org/browse/ANDROID
[GitHub issues]: https://github.com/SpringSource/spring-android/issues?direction=desc&sort=created&state=open
[the lifecycle of an issue]: https://github.com/cbeams/spring-framework/wiki/The-Lifecycle-of-an-Issue
[Gradle]: http://gradle.org
[Spring Tool Suite]: http://www.springsource.com/developer/sts
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: https://github.com/SpringSource/spring-android/wiki/Contributor-Guidelines
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0


