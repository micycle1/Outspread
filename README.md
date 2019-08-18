# Outspread
Outspread - a game where players must conquer the available space using randomly sized rectangles. 

Supports 2-4 players.

Inspired by [this](https://9gag.com/gag/aGZRpDG/) *9GAG* post.

## Gameplay
1. Each turn, a rectangle of random dimensions is given to the player to place. The orientation of the rectangle can be changed, but the dimensions cannot be changed.
2. The player must place the rectangle such that it adjoins their existing territory. If it is their first turn, they must place it in their assigned starting corner.
3. When placed, the player's score is increased equal to the area of the rectangle (i.e. a 5x4 rectangle gives a score of 20 when placed).
3. If the player cannot place the rectangle (for example it may be too large to fit in any remaining space), they can skip their turn.
4. When all space is filled the game ends and the player with the most territory wins (other possible modes could be: first player to the opposing edge wins or can't place = lose).

## Strategy
Any unclaimed territory cut off from your opponents territory will eventually be yours. Therefore, try to cut of an area as large as possible from your opponent to secure this area for yourself later.

## Parameters
*The following parameters are adjustable.*
- Player count (2-4)
- Game grid divisions (both x and y axis independently)
- Maximum rectangle dimension

## Screenshot

<h1 align="center">
<img src="/assets/screenshot1.PNG"/>
</h1>