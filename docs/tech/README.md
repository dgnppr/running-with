# Technology Selection Reason
---

### Backend

#### Java 17, Springboot 3.0.0

I decided to use the springboot 3 version to reduce the technology debt. Since the Spring Boot 3 version is based on
Java 17, it was decided to use Java 17.

#### JPA with QueryDSL

- I decided to use ORM to focus on backend development and chose JPA.
- I also use query DSL, which can conveniently create JPA dynamic queries.

---

### CI/CD

- CI/CD is handled by GitHub Action without distinction.
- I decided to use GitHub action to minimize the quick CI and management.
- For the sake of management issue, CI/CD is not strictly separated yet.
- Ideally this should be separated in the future.

---

### Infrastructure

- Even though I use the container, I decided to use ec2 instead of the fargate due to the cost.
- For the sake of management issue, decided EC2 With elastic beanstalk