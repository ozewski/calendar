# Calendar

A text-based calendar application allowing for the storage and modification of events through a simple interface.

Data is automatically stored in an automatic database (has been explicitly tested and confirmed to work with
PostgreSQL). This application requires a database in order to function properly. The credentials for this database
should be set through a `.env` file including the following variables:

* `URL` (like `postgresql://localhost:5432/db_name`)
* `USERNAME`
* `PASSWORD`

In order to run this application:

1. Set up a relational database.
2. Run the `schema.sql` file, which will populate your database with the necessary tables.
2. Clone this repository to your local machine.
3. Add the `/lib` folder to your class path.
4. Compile and run the program.

This repository comes with the [dotenv](https://github.com/cdimascio/dotenv-java) and JDBC Postgres library pre-bundled 
under `/lib`. Additional JDBC connectors may need to be downloaded for other relational databases.