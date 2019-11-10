/* asl file for Midfielder agent role */

/* Belief */
init(role).

/*Goal*/
!goal.

/* Plan */
+!goal: not time(over) & init(role) <- initialposition;-goal;-init(role);!has(score).

/* Subgoals*/
+!has(score): time(over) <- bye.
+!has(score): not time(over) & visible(defend) <- defend;!has(score).
+!has(score): not time(over) & visible(line) <- returnback;!has(score).
+!has(score): not time(over) & not visible(line) & not visible(defend) & visible(ball) & visible(balldirection) <- gonearball;!has(score).
+!has(score): not time(over) & not visible(line) & not visible(defend) & not visible(ball) <- findball;!has(score).
+!has(score): not time(over) & not visible(line) & not visible(defend) & visible(ball) & visible(setdirection) <- setdirection;!has(score).
+!has(score): not time(over) & not visible(line) & not visible(defend) & visible(ball) & visible(ballclose) & visible(goal) <- kick;!has(score).
+!has(score): not time(over) & not visible(line) & not visible(defend) & visible(ball) & visible(ballclose) & not visible(goal) & visible(player)  <- kick;!has(score).
+!has(score): not time(over) & not visible(line) & not visible(defend) & visible(ball) & visible(ballclose) & not visible(goal) & not visible(player) <- findgoalpost;!has(score).

















