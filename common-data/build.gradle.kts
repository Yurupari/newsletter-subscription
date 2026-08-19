plugins {
	id("java-library")
}

var mapstructVersion = "1.6.3"
var springdocVersion = "3.1.0"
var aopVersion = "4.0.0-M2"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	api("org.mapstruct:mapstruct:${mapstructVersion}")
	api("org.springframework.boot:spring-boot-starter-webmvc")
	api("org.springframework.boot:spring-boot-starter-json")
	api("org.springframework.boot:spring-boot-starter-data-jpa")
	api("org.springframework.boot:spring-boot-starter-aop:${aopVersion}")
	api("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.bootJar {
	enabled = false
}

tasks.jar {
	enabled = true
}

tasks.test {
	filter {
		isFailOnNoMatchingTests = false
	}
}
