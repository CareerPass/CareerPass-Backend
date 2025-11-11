plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "career-pass-backend"
val nettyVersion = "4.1.111.Final"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // ✅ macOS용 Netty DNS 네이티브 라이브러리
    val isMac = System.getProperty("os.name").lowercase().contains("mac")
    val isArm = System.getProperty("os.arch").lowercase().contains("aarch64") ||
            System.getProperty("os.arch").lowercase().contains("arm64")

    if (isMac) {
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:$nettyVersion:${if (isArm) "osx-aarch_64" else "osx-x86_64"}")
    }

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.1")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //구글
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    //mysql
    implementation("com.mysql:mysql-connector-j")
    //swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //voice ai - WebClient
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
