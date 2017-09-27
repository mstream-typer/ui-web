Feature: Exercise View

@current
Scenario: user finishes exercise
  Given typer service responds with following courses
    | id | name          | description           |
    | 1  | Course Name 1 | Course description 1. |
  Given typer service responds with following course '1' exercises
    | id | name            | description             |
    | 1  | Exercise Name 1 | Exercise description 1. |
  And typer service responds with following exercise '1' details
    | time | text				     		|
    | 100  | line1 line1\nline2 line2\nline3 line3\nline4 line4 |
  When user enters the home page
  And user navigates to the course called 'Course Name 1'
  And user navigates to the exercise called 'Exercise Name 1'
  Then user should see a timer displaying '1:40'
  Then user should see following text hints
    | line         |
    | 		   |
    |              |
    | line1⎵line1↵ |
    | line2⎵line2↵ |
    | line3⎵line3↵ |
  And user types 'line1 xxx'
  And user types 'line1 line1\n'
  And user types 'line2 line2\n'
  And user types 'line3 line3\n'
  And user types 'line4 line4'
