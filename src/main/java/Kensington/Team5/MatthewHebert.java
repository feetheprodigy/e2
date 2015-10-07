/*
 *  Based off Kimstron - Royal.
 */
package Kensington.Team5;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;

public class MatthewHebert extends AdvancedRobot
{
    // variables and constants
    static final double MAX_VELOCITY = 8;
    static final double WALL_MARGIN = 25;
    Point2D robotLocation;
    Point2D enemyLocation;
    double enemyDistance;
    double enemyAbsoluteBearing;
    double movementLateralAngle = 0.2;

    // for gun
    double BULLET_POWER = 2; // Our bulletpower.
    double BULLET_SPEED = 14; // Formula for bullet speed.
    double oldEnemyHeading = 0;

    public void run()
    {
        setAdjustRadarForGunTurn(true);

        do
        {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        } while (true);
    }

    public void onScannedRobot(ScannedRobotEvent e)
    {
        robotLocation = new Point2D.Double(getX(), getY());
        enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
        enemyDistance = e.getDistance();
        enemyLocation = vectorToLocation(enemyAbsoluteBearing, enemyDistance, robotLocation);

        // Change direction at random
        if (Math.random() < 0.015)
        {
            movementLateralAngle *= -1;
        }
        move();
        execute();

        // radar
        setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) * 2);

        /*
         * Circular Gun from wiki
         */
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        // Finding the heading and heading change.
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        oldEnemyHeading = enemyHeading;

        double deltaTime = 0;
        double predictedX = getX() + e.getDistance() * Math.sin(absBearing);
        double predictedY = getY() + e.getDistance() * Math.cos(absBearing);
        while ((++deltaTime) * BULLET_SPEED < Point2D.Double.distance(getX(), getY(), predictedX, predictedY))
        {

            // Add the movement we think our enemy will make to our enemy's current X and Y
            predictedX += Math.sin(enemyHeading) * (e.getVelocity());
            predictedY += Math.cos(enemyHeading) * (e.getVelocity());

            // Find our enemy's heading changes.
            enemyHeading += enemyHeadingChange;

            // If our predicted coordinates are outside the walls, put them 18
            // distance units away from the walls as we know
            // that that is the closest they can get to the wall (Bots are
            // non-rotating 36*36 squares).
            predictedX = Math.max(Math.min(predictedX, getBattleFieldWidth() - 18), 18);
            predictedY = Math.max(Math.min(predictedY, getBattleFieldHeight() - 18), 18);

        }
        // Find the bearing of our predicted coordinates from us.
        double aim = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));

        // Aim and fire.
        setTurnGunRightRadians(Utils.normalRelativeAngle(aim - getGunHeadingRadians()));
        setFire(BULLET_POWER);

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
    }

    private void move()
    {
        Point2D robotDestination = null;
        double tries = 0;
        do
        {
            robotDestination = vectorToLocation(absoluteBearing(enemyLocation, robotLocation) + movementLateralAngle, enemyDistance * (1.1 - tries / 100.0), enemyLocation);
            tries++;
        }
        while (tries < 100 && !fieldRectangle(WALL_MARGIN).contains(robotDestination));
        goTo(robotDestination);
    }

    private RoundRectangle2D fieldRectangle(double margin)
    {
        return new RoundRectangle2D.Double(margin, margin, getBattleFieldWidth() - margin * 2, getBattleFieldHeight() - margin * 2, 75, 75);
    }

    private void goTo(Point2D destination)
    {
        double angle = Utils.normalRelativeAngle(absoluteBearing(robotLocation, destination) - getHeadingRadians());
        double turnAngle = Math.atan(Math.tan(angle));
        setTurnRightRadians(turnAngle);
        setAhead(robotLocation.distance(destination) * (angle == turnAngle ? 1 : -1));
        // Hit the brake pedal hard if we need to turn sharply
        setMaxVelocity(Math.abs(getTurnRemaining()) > 33 ? 0 : MAX_VELOCITY);
    }

    private static Point2D vectorToLocation(double angle, double length, Point2D sourceLocation)
    {
        return vectorToLocation(angle, length, sourceLocation, new Point2D.Double());
    }

    private static Point2D vectorToLocation(double angle, double length, Point2D sourceLocation, Point2D targetLocation)
    {
        targetLocation.setLocation(sourceLocation.getX() + Math.sin(angle) * length, sourceLocation.getY() + Math.cos(angle) * length);
        return targetLocation;
    }

    static double absoluteBearing(Point2D source, Point2D target)
    {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }
}