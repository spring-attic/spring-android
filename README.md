# Spring for Android

[Spring for Android] is a library that is designed to provide components of the [Spring Framework] family of projects for use in native [Android] applications.


## Features

* A REST client for Android
* Auth support for accessing secure APIs


## Download Artifacts

The new [Android Build System] provides a Gradle plugin for building Android apps, and [Gradle] itself supports external dependency resolution via Maven repositories. Additionally, the [Android Maven Plugin] makes it possible to build Android applications utilizing the power of [Maven] dependency management. See [downloading Spring artifacts] for [Maven] repository information. 

### Rest Template

The Rest Template library can be included in your project using Gradle or Maven.

Gradle:

```groovy
dependencies {
    compile("org.springframework.android:spring-android-rest-template:${version}")
}
```

Maven:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.android</groupId>
        <artifactId>spring-android-rest-template</artifactId>
        <version>${org.springframework.android-version}</version>
    </dependency>    
</dependencies>
```


### Spring Repositories

The following Maven repositories are available from Spring. You do not need to include all three repositories, rather select the one that corresponds to the release type of the dependency. GA releases are also available through Maven Central.

Gradle:

```groovy
repositories {
    maven { url "https://repo.spring.io/release" }
    maven { url "https://repo.spring.io/milestone" }
    maven { url "https://repo.spring.io/snapshot" }
}
```

Maven:

```xml
<repositories>
    <repository>
        <id>spring-repo</id>
        <name>Spring Repository</name>
        <url>https://repo.spring.io/release</url>
    </repository>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
    </repository>
    <repository>
        <id>spring-snapshots</id>
        <name>Spring Snapshots</name>
        <url>https://repo.spring.io/snapshot</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### Example Build Configuration

The following is an example `build.gradle` that illustrates how to include the `spring-android-rest-template` module along with the `jackson-databind` library for marshaling JSON data. Note the `packagingOptions` section which filters specific files that can cause APK packaging to fail.

```groovy
apply plugin: 'com.android.application'

android {
    compileSdkVersion 20
    buildToolsVersion '20.0.0'

    defaultConfig {
        applicationId 'org.springframework.demo'
        minSdkVersion 15
        targetSdkVersion 20
        versionCode 1
        versionName '1.0'
    }
    buildTypes {
        release {
            runProguard false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    packagingOptions {
        exclude 'META-INF/ASL2.0'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/notice.txt'
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:support-v4:20.+'
    compile 'org.springframework.android:spring-android-rest-template:2.0.0.M1'
    compile 'com.fasterxml.jackson.core:jackson-databind:2.4.1.3'
}

```

## Documentation

See the current [Javadoc] and [reference docs].


## Sample Applications

Several example projects are available in the [samples repository].


## Getting Started Guides

The [spring.io] web site contains many [getting started guides][guides] that cover a broad range of topics. 

- [Consuming a RESTful Web Service with Spring for Android](https://spring.io/guides/gs/consuming-rest-android/)
- [Consuming XML from a RESTful Web Service with Spring for Android](https://spring.io/guides/gs/consuming-rest-xml-android/)
- [Building Android Projects with Gradle](https://spring.io/guides/gs/gradle-android/)
- [Building Android Projects with Maven](https://spring.io/guides/gs/maven-android/)
- [Installing the Android Development Environment](https://spring.io/guides/gs/android/)


## Support

Check out the [spring-android][spring-android tag] tag on [Stack Overflow]. [Commercial support] is also available.


## Issue Tracking

Report issues via the [Spring Android JIRA]. While JIRA is preferred, [GitHub issues] are also welcome. Understand our issue management process by reading about [the lifecycle of an issue].


## Build from Source

1. Clone the repository from GitHub:

    ```sh
    $ git clone git://github.com/spring-projects/spring-android.git
    ```
    > *Note:* If you are unfamiliar with [Git], you may want to try [GitHub for Windows] or [GitHub for Mac].

2. Navigate into the cloned repository directory:

    ```sh
    $ cd spring-android
    ```

3. The project uses [Gradle] to build:

    ```sh
    $ ./gradlew build
    ```

4. Install jars into your local [Maven] cache (optional)

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

    > **Note**: [Spring Tool Suite][sts] includes support for [Gradle], and you can simply import as Gradle projects.

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

> **Note**: To view the output, use the **--info** parameter when running Gradle

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


## Stay in Touch

Follow [@SpringCentral] as well as [@SpringAndroid] on Twitter. In-depth articles can be found at [The Spring Blog], and releases are announced via our [news feed].


## License

[Spring for Android] is released under version 2.0 of the [Apache License].


[Spring for Android]: http://www.spring.io/projects/spring-android
[Spring Framework]: http://www.spring.io/projects/spring-framework
[Android Build System]: http://tools.android.com/tech-docs/new-build-system/user-guide
[Android]: http://developer.android.com
[Gradle]: http://www.gradle.org
[Android Maven Plugin]: http://code.google.com/p/maven-android-plugin
[Maven]: http://maven.apache.org
[downloading Spring artifacts]: https://github.com/spring-projects/spring-framework/wiki/Downloading-Spring-artifacts
[building a distribution with dependencies]: https://github.com/spring-projects/spring-framework/wiki/Building-a-distribution-with-dependencies
[Javadoc]: http://docs.spring.io/spring-android/docs/current/api/
[reference docs]: http://docs.spring.io/spring-android/docs/current/reference/html/
[samples repository]: https://github.com/spring-projects/spring-android-samples
[spring.io]: http://spring.io
[guides]: http://spring.io/guides
[spring-android tag]: http://stackoverflow.com/questions/tagged/spring-android
[Stack Overflow]: http://stackoverflow.com/faq
[Commercial support]: http://spring.io/services
[Spring Android JIRA]: http://jira.spring.io/browse/ANDROID
[Git]: http://git-scm.com
[GitHub for Windows]: http://windows.github.com
[GitHub for Mac]: http://mac.github.com
[GitHub issues]: https://github.com/spring-projects/spring-android/issues?direction=desc&sort=created&state=open
[the lifecycle of an issue]: https://github.com/spring-projects/spring-framework/wiki/The-Lifecycle-of-an-Issue
[sts]: http://www.spring.io/sts
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: CONTRIBUTING.md
[@SpringCentral]: https://twitter.com/springcentral
[@SpringAndroid]: https://twitter.com/springandroid
[The Spring Blog]: http://spring.io/blog/
[news feed]: http://spring.io/blog/category/news
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
