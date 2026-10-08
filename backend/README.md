# Earthquakes API

## Requirements

- JDK 25 or newer
- MongoDB running at `localhost:27017`

## Start the backend on macOS

From the repository root:

```sh
./backend/run.sh
```

The launcher selects an installed JDK 25 automatically on macOS, even if the terminal's default Java is older. On other systems, set `JAVA_HOME` to a JDK 25 or newer before running the script.

When Spring Boot reports that Tomcat started on port 8080, open the repository's `index.html` through a local web server such as VS Code Live Server. The API health endpoint is `http://localhost:8080/actuator/health`.
