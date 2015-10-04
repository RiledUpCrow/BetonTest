# BetonTest

BetonTest is a Bukkit quiz plugin which allows you to create randomized quizes,
test or trials for your players. You can have multiple tests, each containing
multiple categories. Player has to pass all categories to pass the test. Each
category can have multiple questions displayed at the same time, so the player
can choose which one he want to answer.

The plugin displays the questions on boards made with wall signs. There are two
rows, upper and lower. The first one displays the question, and the second one
displays all possible answers. The player has to click on the sign to choose the
answer for the question. Once all categories have been passed the player will
pass the whole test. If he makes too many mistakes, he will fail. There
is also an option to pause the test on wrong answer so you can punish the player
in your own way for mistake and then resume it.

All questions are choosen randomly from the category pool. Answers are also
choosen randomly from all defined answers. There can be multiple correct
answers, so the question looks more dynamic when the player has to repeat it.
This way you can be sure that players won't help each other by posting correct
answers somewhere.

You can define what commands will be executed once the player passes or fails
the test and customize all messages displayed to the player while taking the
test. After choosing each answer the player can see a comment to this particular
answer, so you can tell him why it was wrong.

BetonTest is integrated with BetonQuest plugin, which means you can start tests
for players with events and check the status of those tests with conditions. You
can use that to improve test implementation on your server or to make them
more immersive.

BetonTest can also connect to MySQL database and save all the information about
players' activity there. It's completely optional. You can use it to display
test results on a website etc.

## Configuration

Writing the tests must be done in the configuration files, there is no built-in
in-game editor right now. It will be added in the future.

Sample test is shown as a comment in the _config.yml_ file. You can use it for
reference or as a template. Now let's look at different part of the
configuration file. First there is `mysql` setting. It is responsible for
connecting to the database. Leave it blank if you don't want to use MySQL.
(`base` value is the name of the database). The plugin will inform you if the
connection is made with "MySQL connected" info while enabling.

### Tests

Each test has several settings:

* `messages` - list of messages displayed to the player when he starts,
  resumes, pauses, passes or fails the test. You can use color codes here.
* `commands` - list of commands issued by the console once the test is
  paused, passed or failed. You can use `%player%` to represent player's name.
* `blocked_cmds` - list of commands that cannot be used while in this test.
* `max_mistakes` - number of mistakes allowed on the test. If it's `3`,
  then player will fail the test after 3 incorrect answers.
* `teleport_back` - if set to `true` it will teleport the player back
  to the place where he was once the test has been passed or failed. Pausing
  the test will not teleport him anywhere, so you need to handle it separately.
* `on_mistake` - action after the player makes a mistake. Possible values are
  `next`, `nothing`, `reset` and `pause`. They are described below.
* `categories` - this is the list of the categories. They are described below.

### Categories

Each category can contain multiple boards which will display multiple questions.
To pass a category player has to answer a question on one of the boards in this
category. If you make more than one boards for each category the player will be
able to choose which question he wants to answer. Remember that there has to be
at least the same amount of questions as boards, never less!

* `choices` - this setting is responsible for length of the board and amount
  of answers. If it's set to 3 then the board will be 3 signs wide and there
  will be only 3 answers to choose. If it's 5, there will be 5 signs in a row
  and 5 possible answers etc. All boards in this category must have that width
  and all questions must have at least that many answers defined.
* `player_loc` - this is a location where the player will be teleported when
  starting this category. It follows this syntax: `x;y;z;world;yaw;pitch`,
  where x, y and z are coordinates (may have floating point), world is name of
  the world and yaw and pitch are head rotation parameters.
* `boards` - a list of boards. They are described below.
* `questions` - a list of questions. They are described below.

### Boards

Each board has location and direction. Location is the string formatted in the
same way as `player_loc` but without yaw/pitch. It should point to upper left
sign on the board (the place where the question starts).

Direction is responsible for rotation of the board. It can be `north`,
`south`, `west` and `east`. It describes "facing" direction of the board,
so if youre looking at the board and you're facing south, the board is facing
the opposite direction, north.

### Questions

Each question has the text, correct answers and incorrect answers. Text is just
the text displayed as the question. Each answer, correct or incorrect, must have
defined its text and the comment. Text is what will be displayed on the sign and
the comment will be shown to the player once he chooses this answer.

There can be multiple correct answers, BetonTest will always choose one of them.
This may be useful when creating more open questions, like "Which mob cannot
swim" - Iron Golem and Slime cannot, they can both be correct answers.

The amount of incorrect answers must be equal or greater than the amount of 
choices in the category minus one (because there always will be one correct
answer)

While defining question and answers keep in mind that they need to fit on the
signs - question has as many signs as the `choices` setting of the category
and all questions have only 3 lines on the sign. 

## Actions on mistake

There are 4 different actions you can make when the player makes a mistake on
the test.

`next` moves the player to the next category. It still does count the mistake,
so if the player will make too many of them before he finishes the test, he
will fail.

`nothing` leaves the player in current category. He made a mistake, so he
must repeat the question from this category. It will of course draw other
question and shuffle all answers, so the player cannot just shoot until he
hits the right one.

`reset` moves the player to the beginning of the test, resetting all his
progress. He must answer all questions correctly in a single try to pass the
test.

`pause` ends the test when the player makes a mistake and issues the command
defined as "commands.pause". This way you can teleport the player to a lava pool
or cobweb path as a punishment for incorrect answer. The player will have to
start the test again to continue it. It's called "resuming", because it still
counts mistakes. Once the player pauses the test for nth time he will fail
instead of just pausing the test.

## Commands and permissions

For players who want to start the test there is **/starttest <test>** command.
There is also alias for this command, **/startquiz <test>**.
They need to have _betontest.start_ and _betontest.test.<test>_ permissions to
use it. The first permission is default for all players and it will allow using
this command, the second permission must be given to the player with a
permission plugin, like PEX. 

There is one administrative command, "/test" (or "/quiz", which is an alias).
With it you can start tests for players, purge their data and reload the plugin.

**/test start <test> <player>** command does exactly that - it starts the test
for the player. It does not matter if the test is actually started or just
resumed, the command will make the test start.

**/test purge <player>** removes the player from any test taken right now and
clears his data in the _data.yml_ file. If the player is taking the test right
now and the test has `teleport_back` set to true, it will also teleport
the player back. If not, you will be informed if the player should be manually
removed from the test.

**/test reload** reloads the configuration file. It does not save anything to
_config.yml_, so all your comments will be preserved.

## Starting the test

You can give the appropriate permission to the player so he can use the command
to start the test for himself. Keep in mind that he will be able to resume the
test right after pausing it with that command!

You can also make other plugins start the test for the player by issuing the
console command **/test start <test> <player>**, for example CommandSigns (when
the player clicks on the sign), Skript (in a scripted way) or BetonQuest (in a
conversation).

## Compatibility with BetonQuest

BetonTest adds 4 conditions to BetonQuest and 1 event. Here is the description:

* `testactive` - condition which checks if the player has an active test.
  There is one required argument, name of the test. It can be set to `any`, so
  any test counts.
* `testpaused` - condition which checks if the player has a paused test.
  There is one required argument, name of the test. It can be set to `any`, so
  any test counts.
* `testpassed` - condition which checks if the player has passed a test.
  There is one required argument, name of the test. It can be set to `any`, so
  any test counts.
* `testfailed` - condition which checks if the player has failed a test.
  There is one required argument, name of the test. It can be set to `any`, so
  any test counts.

* `teststart` - event which starts or resumes the test. The only required
  argument is the name of the test. It works exactly as the **/test start**
  command.

## Test server

There is a test server where you can pass an example quiz about general Minecraft
knowledge. It's the same quiz as the example in the default _config.yml_ file.
Connect with `betonquest.betoncraft.pl` server and talk with
Co0sh. Ask him to show you other plugins. You will be teleported to an ugly inn.
Talk with ScrollGiver and accept his scrolls. Now use **Trial Portal** to
teleport to the quiz. There you can start the quiz by talking with the Guard.
The quiz has 4 categories, 4 choices each and you have 5 chances before you fail.
It is set to `pause` and you will be teleported to a short cobweb path after
each mistake. To resume the test, talk with the Guard again. Don't use scrolls
while in the test (TownPortal will have an update with WorldGuard flag denying
scroll use in a region, so soon it won't be a problem).

## Metrics

BetonTest uses the metrics system, which can be disabled by setting `opt-out`
to true in _config.yml_ file in "plugins/PluginMetrics" directory.

## Licensing, source code and compiling

BetonTest is licensed under GPLv3, which means it's a free software (as in "free
speech", not "free beer"). It's distributed from SpigotMC.org for 7€.
The source code can be found on [GitHub](https://github.com/Co0sh/BetonTest). To
compile it you need JDK 1.7 and Maven 3 installed on your system. Issue
`mvn install` command inside the root directory. The JAR file should appear
in _target_ folder.
