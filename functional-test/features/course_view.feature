Feature: Course View


Scenario: user starts an exercise
  Given typer service responds with following courses
    | id | name          | description           |
    | 1  | Course Name 1 | Course description 1. |
  Given typer service responds with following course '1' exercises
    | id | name            | description             |
    | 1  | Exercise Name 1 | Exercise description 1. |
    | 2  | Exercise Name 2 | Exercise description 2. |
    | 3  | Exercise Name 3 | Exercise description 3. |
  When user enters the home page
  And user navigates to the course called 'Course Name 1'
  Then user should see following exercises
    | name            | description             |
    | Exercise Name 1 | Exercise description 1. |
    | Exercise Name 2 | Exercise description 2. |
    | Exercise Name 3 | Exercise description 3. |