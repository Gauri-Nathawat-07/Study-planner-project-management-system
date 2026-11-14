# StudyPlanner Pro

> **Study Planner & Project Management System**  
> Built with **Core Java, Java AWT, Event Handling, and MySQL Database** using the standalone **MySQL Connector/J** library (No Maven/Gradle required).

---

## Project Overview

**StudyPlanner Pro** is a desktop productivity dashboard for students and researchers. It uses Java AWT with custom `Graphics2D` painting for a modern UI look and is backed by a **MySQL Database** (`studyplanner_db`).

---

## How to Run

### In IntelliJ IDEA
1. Open this project folder in IntelliJ IDEA.
2. IntelliJ will detect the `.iml` file and configure everything automatically.
3. Click **Run > Run 'Main'** (or press Shift+F10).

### In VS Code / Terminal
```
javac -d out -cp "lib/mysql-connector-j-26.10.0.jar" -sourcepath src src/com/studyplanner/Main.java
java -cp "out;lib/mysql-connector-j-26.10.0.jar" com.studyplanner.Main
```

---

## MySQL Database Setup

### Step 1: Import the Schema
Import `schema.sql` into MySQL using any of these methods:

**MySQL Command Line:**
```
mysql -u root -p < schema.sql
```

**MySQL Workbench:**
1. Open MySQL Workbench and connect to your local instance.
2. Go to File > Open SQL Script and select `schema.sql`.
3. Click the lightning bolt icon to execute.

**phpMyAdmin (XAMPP):**
1. Open `http://localhost/phpmyadmin`.
2. Click the Import tab.
3. Choose `schema.sql` and click Go.

### Step 2: Configure Connection
Edit `db.properties` if your MySQL settings are different:

```properties
db.host=localhost
db.port=3306
db.database=studyplanner_db
db.user=root
db.password=
```

> **Note:** If MySQL is not running, the app automatically works in offline/cache mode so it never crashes.

---

## Database Tables

1. **subjects** — Academic courses (Math, Physics, CS, etc.)
2. **projects** — Long-term projects with deadlines
3. **tasks** — Individual assignments linked to subjects and projects
4. **study_sessions** — Focus/Pomodoro study session logs
5. **user_settings** — App preferences (timer durations, dark mode, etc.)

---

## Project Structure

```
StudyPlannerProject/
├── .idea/                  (IntelliJ IDEA config)
├── lib/                    (MySQL Connector JAR)
├── src/
│   └── com/studyplanner/
│       ├── Main.java
│       ├── app/            (Application startup & config)
│       ├── model/          (Data models: Task, Project, Subject, etc.)
│       ├── service/        (Business logic & database operations)
│       ├── ui/             (All GUI screens & components)
│       └── util/           (Colors, fonts, dates, validation)
├── out/                    (Compiled .class files)
├── db.properties           (MySQL connection settings)
├── schema.sql              (Database schema & sample data)
└── .iml                    (IntelliJ module file)
```

---

## Tech Stack

- **Language:** Java (Core JDK)
- **GUI:** Java AWT + Graphics2D custom painting
- **Database:** MySQL 8.0 with MySQL Connector/J
- **No Maven, No Gradle, No Swing, No JavaFX**

---

## Features

- Dashboard with task stats and study charts
- Task management (create, edit, delete, prioritize)
- Project tracking with deadlines
- Subject management with color coding
- Calendar view
- Focus/Pomodoro timer
- Analytics and study insights
- User settings and preferences
