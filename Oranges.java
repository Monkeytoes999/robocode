package GarmBox;
import robocode.*;
import java.awt.Color;
import java.lang.Math;
import robocode.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Oranges extends TeamRobot
{
	int deaths = 0;
	boolean last = false;
	double lastEnergy = 100;

	public void run() {
		if (getRoundNum() > 0) { 
			try {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(getDataFile("data.dat")));
					deaths = Integer.parseInt(reader.readLine());
					last = (Integer.parseInt(reader.readLine()) == getRoundNum());
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
			} catch (IOException e) {} catch (NumberFormatException e) {}
		}
		System.out.println(deaths);
		setColors(new Color(184, 8, 132), new Color(81, 16, 206), new Color(150, 4, 0)); // body,gun,radar
		setBulletColor(new Color(42, 255, 0));
		setScanColor(new Color(156, 255, 0));
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		while(true) {
			scan();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (isTeammate(e.getName())){
			return;
		}

		double radarTurn =
        getHeadingRadians() + e.getBearingRadians()
        - getRadarHeadingRadians();
   		setTurnRadarRightRadians(1.9*Utils.normalRelativeAngle(radarTurn));
		if (e.getEnergy() != lastEnergy) {
			setAhead(60);
			setTurnLeft(40);
		} else {
			double gunTurn = 
			(getHeadingRadians() + e.getBearingRadians()
			- getGunHeadingRadians());
			setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
			int firePower = (getEnergy() > 30 ? 3 : 2);
			setFire(firePower);
			if (Math.random() > .6) {
				setAhead(40);
			}
			if (Math.random() > .6) {
				if (Math.random() > .8) {
					setTurnLeft(50);
				} else {
					setTurnRight(50);
				}
			}
		}
		lastEnergy = e.getEnergy();
		execute();
	}

	public void onHitWall(HitWallEvent e) {
		setTurnRight(65);
		setAhead(90);
	}
	
	public void onDeath(DeathEvent e) {
		PrintStream w = null;
		try {
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("data.dat")));
			w.println(deaths + 1);
			w.println(getRoundNum());
		} catch (IOException ex) {} finally {
			if (w != null) {
				w.close();
			}
		}
	}
}