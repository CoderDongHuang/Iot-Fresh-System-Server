# IoT Fresh 2022 启动脚本

# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者打包后运行
# mvn clean package
# java -jar target/Iot-fresh-2022-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# 生产环境启动
# java -jar target/Iot-fresh-2022-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod