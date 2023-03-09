RunningWith
---

### 0. What is this project

- 온라인/오프라인 스터디 모집 및 관리 웹 어플리케이션

### 1. TECH

- Backend: Java 17, SpringBoot 3, Junit5, Mockito, JPA with QueryDSL
- CI/CD: [Github Actions](https://docs.github.com/ko/actions)
- Infrastructure: AWS ECS with Fargate

### 2. Structure

<img src="./docs/images/module-architecture.png" width="500" height="400"/>

- 멀티 모듈 기반 그래들 프로젝트
- `module-application:runningwith-app-users-web` is the Web Application Server for Users
- [자세한 모듈 내용](https://yoonyonghyun.notion.site/Multi-Module-0e90daefef574fdeac10c01c8bd3941e)

### 3. How to build project

- `module-application:runningwith-app-users-web`: `./gradlew :module-application:runningwith-app-users-web:build`

### 4. ETC
