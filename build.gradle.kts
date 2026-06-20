plugins {
    `java-library`
    `maven-publish`
}

group = "org.luaj"
version = "3.0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.apache.bcel:bcel:5.2")
}

// Set JAR name to match expected artifact name
tasks.named<Jar>("jar") {
    archiveBaseName.set("luaj-jse")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("luajc*.class")
    exclude("org/luaj/vm2/luajc/**")
    exclude("org/luaj/vm2/script/**")
    exclude("org/luaj/vm2/server/**")
    exclude("org/luaj/vm2/lib/jse/JseProcess*.class")

    // Include META-INF/services from src/jse
    from("src/jse") {
        include("META-INF/services/**")
    }
}

// Source sets configuration
sourceSets {
    main {
        java {
            srcDirs("src/core", "src/jse")
        }
        resources {
            srcDir("src/jse")
            include("META-INF/services/**")
        }
    }
}

// Generate parser from JavaCC (optional - pre-generated parser included)
tasks.register<JavaExec>("generateParser") {
    group = "build"
    description = "Generate Lua parser using JavaCC"
    classpath = files("lib/javacc.jar")
    mainClass.set("javacc")
    args("grammar/LuaParser.jj")
    workingDir = projectDir
}

// Compile options
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    options.release.set(8)
}

// Publishing to mavenLocal
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "luaj-jse"
            version = project.version.toString()
        }
    }
    repositories {
        mavenLocal()
    }
}