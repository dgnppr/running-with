RunningWith
---

### 0. Description

- Online/Offline Study Recruitment and Management Web Application
- [Troubleshoot]()

### [1. TECH](./docs/tech/README.md)

- Backend: Java 17, SpringBoot 3, Junit5, Mockito, JPA with QueryDSL
- CI/CD: [Github Actions](https://docs.github.com/ko/actions)
- Infrastructure: AWS ECS with Fargate

### [2. Module-Structure](./docs/module/README.md)

<img src="./docs/images/module-architecture.png" width="500" height="400"/>

- Gradle project based on multi-modules
- `module-application:runningwith-app-users-web` is the Web Application Server for Users

### 3. How to build project

- `module-application:runningwith-app-users-web`: `./gradlew :module-application:runningwith-app-users-web:build`

### 4. ETC

-

### 5. Future Updates

- Log in to Naver, Google OAuth.
- Interlink place search at offline study meeting
- WebRTC-based video communication during online study meetings
