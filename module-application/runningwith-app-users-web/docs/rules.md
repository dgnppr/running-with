# URL

### Naming

- Make it restful.
- All URLs are converted into constants.
    - eg: ```public static final String URL_USERS_PROFILE = "/profile";```

- The path url including the path variable is as follows.
    - eg: ```public static final String URL_STUDY_SETTINGS_PATH = "/settings/study/path";```

---

# View

### Naming

- Each controller returns a view name as shown below.
    - ```return PAGE_SIGN_UP;```
    - ```public static final String PAGE_SIGN_UP = "users/sign-up";```

### Use fragments

- Recycle the fragments of the timeleaf as much as possible to prevent overlapping codes in View.