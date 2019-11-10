# RoboCup
Implementation of robocup using BDI (krislet)

Integrated RoboCup environment with Jason-based BDI Framework

Team of 5 players
1 Defender Role
2 Midfielders Role
2 Forwarders Role

Features:
Positioning Strategy
Placing the players at their desired positions based on their roles
Restricting the players to cross boundaries
Play Mode: Midfielders exhibit different behaviours based on different play modes.
Player Co-operation: Passing the ball to near by team player whenever necessary

General Player Behaviors:
Defender: Guard its  own  goal  post.
Forwarder: Chase the ball only in the opponent’s area.
Midfielder: Chase the ball.

Defender Behavior:
Stop the opponent agents’ attack to goal by kicking the ball away.

Forwarder Behavior:
Maintaining its position in opponent’s area by not crossing the center line.

Play Mode:
Other team’s chance to kick off : Midfielders run towards its own goal post to defend it. 

Player Co-operation:
Forwarders and Midfielders pass ball to each other when goal not visible.
