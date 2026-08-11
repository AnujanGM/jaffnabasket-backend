FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Render's free tier caps the container at 512MB. Left untuned, the JVM's
# defaults (uncapped Metaspace, ~240MB reserved JIT code cache, G1GC's
# native bookkeeping) can exceed that during Spring/Hibernate startup,
# which gets the process OOM-killed (exit 137) before it ever finishes
# booting. These flags keep total JVM memory comfortably under the limit.
ENTRYPOINT ["java", \
  "-Xms128m", "-Xmx256m", \
  "-XX:MaxMetaspaceSize=128m", \
  "-XX:MaxDirectMemorySize=32m", \
  "-XX:ReservedCodeCacheSize=48m", \
  "-XX:+UseSerialGC", \
  "-Xss512k", \
  "-jar", "app.jar"]
