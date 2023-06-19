### GraalVM Native Image Support

#### Install & Setting

- [Installation (GraalVM for JDK) on macOS Platforms](https://www.graalvm.org/22.0/docs/getting-started/macos/)

``` remark
sudo xattr -r -d com.apple.quarantine /path/to/graalvm
which java
echo $JAVA_HOME
java --version
```

- [Install Native Image](https://www.graalvm.org/22.0/reference-manual/native-image/#install-native-image)

``` install gu
gu install native-image
```

- [Configure and Build Project with Spring Boot](https://www.baeldung.com/spring-native-intro#configure-and-build-project-with-spring-boot)

``` cmd
./gradlew nativeTest
./gradlew nativeCompile
using here: ./build/native/nativeCompile/cashcard
```

- [Building a Native Image Using Buildpacks](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.developing-your-first-application.buildpacks.gradle)

``` 
./gradlew bootBuildImage
```