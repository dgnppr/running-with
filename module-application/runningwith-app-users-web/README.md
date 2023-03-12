# module-application: runningwith-app-users-web

---

# 0. Description

- This module is SpringBoot 3.0.0 Web Application Server and provides Server Side Rendering
  with [thymeleaf](https://www.thymeleaf.org/index.html)

---

# 1. Module Rules

- 뷰는 가급적 재사용한다.
- 모든 트랜잭션은 `@Service` 계층에서 한다.

---

# 2. [Added Modules](./build.gradle)

---

# 3. ETC

- 뷰를 서버 사이드 렌더링하므로 프론트엔드 라이브러리를 [build.gradle](./build.gradle) 빌드하며 관리한다
- [프론트엔드 라이브러리](./src/main/resources/static/package.json)