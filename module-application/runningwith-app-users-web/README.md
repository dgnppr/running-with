# module-application: runningwith-app-users-web

---

# 0. Description

- This module is SpringBoot 3.0.0 Web Application Server and provides Server Side Rendering
  with [thymeleaf](https://www.thymeleaf.org/index.html)

---

# 1. Module Rules

- View uses fragments if it can be reused.
- All transactions are made at the '@Service' layer.
- [Click here for additional rules.](./docs/rules.md)

---

# 2. [Added Modules](./build.gradle)

- Because the view is server-side rendered, the front-end library is built and managed in [build.gradle](./build.gradle)
- [Frontend libraries being managed in gradle](./src/main/resources/static/package.json)

---

# 3. ETC

- This app is designed to be domain-driven.
- User is being authenticated in a session.
- The authentication may be changed later for scalability.
