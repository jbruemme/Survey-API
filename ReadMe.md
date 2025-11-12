# ☕ Spring Boot Survey API

[![Java](https://img.shields.io/badge/Java-17+-orange)](https://www.oracle.com/java/)  
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)  
[![License](https://img.shields.io/badge/License-Educational-blue)](LICENSE)

---

## 🌟 Overview

This project is a **Spring Boot REST API** built with **Gradle** and an **in-memory data store**.  
It provides endpoints for managing surveys, survey items, and survey instances — all bootstrapped automatically on application startup.

When launched, the application preloads:
- **5 Survey Items**
- **2 Surveys**
- **2 Survey Instances**

API documentation is included under the root project folder in the **`/apidoc`** directory.  
The provided `index.html` file contains full documentation for all available endpoints.

---

## ⚡ Features

- **9 REST API Endpoints** for surveys, survey items, and survey instances.
- **In-memory data store** (no database required).
- **Auto-seeded sample data** on startup for easy testing.
- **Postman collection included** — import `SER 421 Assignment 6.postman_collection` directly into Postman.
- Each endpoint includes **success and failure** test cases.
- **Cross-platform Gradle wrapper** for simplified builds.

---

## 🛠️ Technologies Used

This project leverages the following technologies:

- **Java 17+** – Primary programming language.
- **Spring Boot 3.x** – Framework for RESTful service development.
- **Gradle** – Build automation and dependency management.
- **In-memory data store** – Lightweight runtime persistence.
- **Postman** – For API endpoint testing and validation.

---

## ⚙️ Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/jbruemme/Survey-API.git
   cd Survey-API
    ```
   
---
   
## Usage
1. Import the provided Postman collection:
   ```ngix
   SER 421 Assignment 6.postman_collection
   ```
2. Test teh API endpoints directly through Postman, including both success and failure
   test cases.
3. API documentation:
   ```bash
   /apidoc/index.html
   ```

---

## Future Implementation
1. Future implementation of this project will include a frontend web interface to:
 - Create and manage surveys visually.
 - Submit and view survey responses.
 - Display analytics and summary results dynamically.

