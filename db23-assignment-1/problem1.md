# Problem 1 - ER Models, Relational Models, Normal Forms

Please consider the following scenario and present your model for the requirements.

## Scenario

Let's say we want to design a database to manage the relationships between projects, professors, and students.

- Each professor has an id, a name, and a rank.
- Each project has a project number, a sponsor name (e.g. MOST), a starting date, an ending date, and a budget.
- Each student has a student id, a name, and a degree program (Bachelor, Master or PhD).
- Each project is managed by a professor. It is possible that a professor manages multiple projects.
- Each student is supervised by a professor. It is possible that a professor supervises multiple students.
- A student is able to work on a project. It is possible that a student participates in multiple projects.
- It should record when a student starts working on a project, when he/she stops, and how much he/she is paid per month.

## Requirements

1. Please design an ER model and draw an ER diagram for this scenario. (30 points)
2. Please design a relational model for this scenario with the following constraints. (30 points)
  - You should use SQL `CREATE TABLE` to present your schema.
  - You should `identify primary keys and foreign keys relationships` in your SQLs.
  - You can only use [data types in PostgreSQL][1].
  - The relations should at least follow `the 3rd normal form`.

[1]: https://www.postgresql.org/docs/14/datatype.html
