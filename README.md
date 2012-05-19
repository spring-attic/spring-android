# Spring for Android

[Spring for Android](http://www.springsource.org/spring-android) is an extension of the [Spring Framework](http://www.springsource.org/spring-framework) that aims to simplify the development of native [Android](http://developer.android.com/index.html) applications.

## Check Out and Build from Source

1. Clone the repository from GitHub:

		$ git clone git://github.com/SpringSource/spring-android.git

2. Navigate into the cloned repository directory:

		$ cd spring-android

3. The project uses [Gradle](http://gradle.org/) to build:

		$ ./gradlew build

## Eclipse

To generate Eclipse metadata (.classpath and .project files), use the following Gradle task:

	$ ./gradlew eclipse

Once complete, you may then import the projects into Eclipse as usual:

	File -> Import -> Existing projects into workspace

Alternatively, [SpringSource Tool Suite](http://www.springsource.com/developer/sts) has built in support for [Gradle](http://gradle.org/), and you can simply import as Gradle projects.

## IDEA

To generate IDEA metadata (.iml and .ipr files), use the following Gradle task:

	$ ./gradlew idea

## JavaDoc

Use the following Gradle task to build the JavaDoc

	$ ./gradlew :docs:api

_Note: The result will be available in 'docs/build/api'._

## Tests

There are three Android Test Projects located in the repository that correspond to the three Spring for Android Modules (Core, Rest Template, and Auth). These projects are executed separately from the Gradle build process. To run the suite of tests, perform the following steps. A parent POM located in the root of the repository will execute each test project on all attached devices and emulators.

1. Build Spring for Android JARs and install them to the local Maven repository:

		$ ./gradlew build install

2. The tests are run using the Android Maven Plugin:

		$ mvn clean install

	_Note: Each test project can also be executed individually, by running the previous command from within the respective test project's directory._
