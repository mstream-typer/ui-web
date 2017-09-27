Feature: Home View


Scenario: user enters successfuly loaded home page
  Given typer service responds with following courses
    | id | name          | description           |
    | 1  | Course Name 1 | Course description 1. |
    | 2  | Course Name 2 | Course description 2. |
    | 3  | Course Name 3 | Course description 3. |
  When user enters the home page
  Then user should see following courses
    | name          | description           |
    | Course Name 1 | Course description 1. |
    | Course Name 2 | Course description 2. |
    | Course Name 3 | Course description 3. |