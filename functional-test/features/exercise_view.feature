Feature: Exercise View

@current
Scenario: user finishes exercise
  Given typer service responds with following course exercises
    | id  | name  | description |
    | id1 | name1 | desc1       |
  And typer service responds with following exercise 'id1' details
    | time | text						|
    | 100  | line1 line1\nline2 line2\nline3 line3\nline4 line4 |
  When user enters the home page
  And user navigates to the course page
  And user navigates to the exercise called 'name1'
  And user types 'line1 xxx'
  And user types 'line1 line1\n'
  And user types 'line2 line2\n'
  And user types 'line3 line3\n'
  And user types 'line4 line4'
  And user navigates to the exercise called 'name1'