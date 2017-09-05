Feature: Home View

Scenario: user enters successfuly loaded home page
  Given typer service responds with following course exercises
    | id  | name  | description |
    | id1 | name1 | desc1       |
    | id2 | name2 | desc2       |
    | id3 | name3 | desc3       |
  When user enters the home page
  And user navigates to the course page
  Then user should see following exercises
    | name  | description | 
    | name1 | desc1       | 
    | name2 | desc2       | 
    | name3 | desc3       |


Scenario: user starts an exercise
  Given typer service responds with following course exercises
    | id  | name  | description |
    | id1 | name1 | desc1       |
  And typer service responds with following exercise 'id1' details
    | time | text						 |
    | 100  | line1 line1\nline2 line2\nline3 line3\nline4 \line4 |
  When user enters the home page
  And user navigates to the course page
  And user navigates to the exercise called 'name1'
  Then user should see a timer displaying '1:40'
  Then user should see following text hints
    | line         |
    | 		   |
    |              |
    | line1⎵line1↵ |
    | line2⎵line2↵ |
    | line3⎵line3↵ |
  

    
