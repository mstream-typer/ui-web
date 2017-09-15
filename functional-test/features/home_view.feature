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