Feature: Home View

Scenario: user enters successfuly loaded home page
  Given typer service responds with course exercises 
  When users enter the home page
  Then user should see following exercises
  