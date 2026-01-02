@echo off
REM IoT Fresh 2022 启动脚本

echo Starting IoT Fresh 2022 Application...

REM 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

REM 或者打包后运行
REM mvn clean package
REM java -jar target\Iot-fresh-2022-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

REM 生产环境启动
REM java -jar target\Iot-fresh-2022-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

pause