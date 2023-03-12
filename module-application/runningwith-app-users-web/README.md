# module-application: runningwith-app-users-web

---

# 0. Description

- This module is SpringBoot 3.0.0 Web Application Server and provides Server Side Rendering
  with [thymeleaf](https://www.thymeleaf.org/index.html)

---

# 1. Module Rules

- View uses a fragment if it can be reused.
- All transactions are made at the '@Service' layer.
- [URL Description]()
- [View Description]()

---

# 2. [Added Modules](./build.gradle)

---

# 3. ETC

- Because the view is server-side rendered, the front-end library is built and managed in [build.gradle](./build.gradle)
- [FrontEnd Library](./src/main/resources/static/package.json)