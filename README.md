RunningWith
---

### 0. Description

- Online/Offline Study Recruitment and Management Web Application

### [1. TECH](./docs/tech/README.md)

- Backend: Java 17, SpringBoot 3, Junit5, Mockito, JPA with QueryDSL
- CI/CD: [Github Actions](https://docs.github.com/ko/actions)
- Infrastructure: AWS ECS with Fargate

### [2. Module-Structure](./docs/module/README.md)

<img src="./docs/images/module-architecture.png" width="500" height="400"/>

- Gradle project based on multi-modules.
- [Application Layer: module-application:runningwith-app-users-web](./module-application/runningwith-app-users-web/README.md)

### 3. How to build project and run

- `module-application:runningwith-app-users-web`: `./gradlew :module-application:runningwith-app-users-web:build`
- Local, dev profiles use DB as MariaDB docker image.
- If you have not installed Docker and MariaDB image, please install them before executing app from the
  sites. ([docker](https://docs.docker.com/desktop/install/mac-install/), [mariaDB](https://hub.docker.com/_/mariadb))
- After installing Docker and MariaDB Image, you should create database named 'runningwith' and
  execute [ddl script](./module-domain-rds/sql/ddl.sql)
- If the app does not run in the same way as above, please contact me by email.

### 4. ETC

-

### 5. Future Updates

- Log in to Naver, Google OAuth.
- Interlink place search at offline study meeting
- WebRTC-based video communication during online study meetings
