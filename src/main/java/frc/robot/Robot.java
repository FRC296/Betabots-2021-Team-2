// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.playingwithfusion.CANVenom;
import com.playingwithfusion.CANVenom.ControlMode;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;

enum AutonSteps {
  DriveForwardToTubeStand,
  LoadTubeFromStand,
  RotateTowardsPyramid,
  DriveToPyramid,
}

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  XboxController controller = new XboxController(0);
  CANVenom motorLeft = new CANVenom(2);
  CANVenom motorRight = new CANVenom(1);
  Timer autonomousTimer = new Timer();

  TalonSRX conveyer = new WPI_TalonSRX(15);
  Solenoid tilt = new Solenoid(0);

  com.ctre.phoenix.motorcontrol.ControlMode talonSpeed = com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput;

  AutonSteps currentAutonomousStep = AutonSteps.DriveForwardToTubeStand;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    motorRight.setInverted(true);
    conveyer.configNeutralDeadband(0.1);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {

    System.out.println("Autonomous Init");


    motorRight.resetPosition();
    motorLeft.resetPosition();
    motorLeft.setPID(1.25, 0.002, 0, 0, 0);
    motorRight.setPID(1.25, 0.002, 0, 0, 0);

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {




    switch(currentAutonomousStep) {
      case DriveForwardToTubeStand:
        // Drive forward 4 feet
        motorLeft.setCommand(ControlMode.PositionControl, 24.3);
        motorRight.setCommand(ControlMode.PositionControl, 24.3);

        if (motorLeft.getPosition() > 23) {
          currentAutonomousStep = AutonSteps.LoadTubeFromStand;
          autonomousTimer.reset();
          autonomousTimer.start();
        }
        break;
      case LoadTubeFromStand:
        conveyer.set(talonSpeed, -0.5);

        if (autonomousTimer.hasElapsed(1)) {
          currentAutonomousStep = AutonSteps.RotateTowardsPyramid;
          conveyer.set(talonSpeed, 0);
          autonomousTimer.stop();
          motorLeft.disable();
          motorRight.disable();
          motorRight.resetPosition();
          motorLeft.resetPosition();
          motorLeft.setCommand(ControlMode.PositionControl, -2.4);
          motorRight.setCommand(ControlMode.PositionControl, 2.4);
          motorLeft.enable();
          motorRight.enable();
        }

        break;
      case RotateTowardsPyramid:
        // Rotate 60-90 degrees either left/right 

        motorLeft.setPID(3, 0.003, 0, 0, 0);
        motorRight.setPID(3, 0.003, 0, 0, 0);
        motorLeft.setCommand(ControlMode.PositionControl, -17);
        motorRight.setCommand(ControlMode.PositionControl, 17);
        System.out.print("Hell");

        if (motorLeft.getPosition() < -15) {
          motorLeft.disable();
          motorRight.disable();
          motorRight.resetPosition();
          motorLeft.resetPosition();
          motorLeft.setPID(1.25, 0.002, 0, 0, 0);
          motorRight.setPID(1.25, 0.002, 0, 0, 0);
          motorLeft.setCommand(ControlMode.PositionControl, 5);
          motorRight.setCommand(ControlMode.PositionControl, 5);
          motorLeft.enable();
          motorRight.enable();
          currentAutonomousStep = AutonSteps.DriveToPyramid;
        }
        //currentAutonomousStep = AutonSteps.DriveToPyramid;
        break;
      case DriveToPyramid:
        // Drive 10-12 feet forward


        //currentAutonomousStep = null;
        break;
    }

    System.out.print(currentAutonomousStep);
    System.out.print("Right: ");
    System.out.print(motorRight.getPosition());
    System.out.print("    Left: ");
    System.out.println(motorLeft.getPosition());

    System.out.print(motorLeft.getKP());
    System.out.print("  ");
    System.out.print(motorLeft.getKI());
    System.out.print("  ");
    System.out.print(motorLeft.getKD());
    /*
      Steps:
        1. Drive forward
        2. Rotate 90 deg left/right (grab tube)
        3. Drive to tube stand
    */


  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    motorLeft.setControlMode(ControlMode.CurrentControl);

    motorRight.resetPosition();
    motorLeft.resetPosition();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    double left = controller.getY(Hand.kLeft);
    double right = controller.getY(Hand.kRight);
    double triggerLeft = controller.getTriggerAxis(Hand.kLeft);
    double triggerRight = controller.getTriggerAxis(Hand.kRight);

    motorLeft.set(left);
    motorRight.set(right);

    conveyer.set(talonSpeed, triggerRight - triggerLeft);

    if (controller.getBumperPressed(Hand.kRight)) {
      tilt.set(true);
    } else if (controller.getBumperPressed(Hand.kLeft)) {
      tilt.set(false);
    }

    System.out.println(motorLeft.getPosition());

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    motorLeft.setControlMode(ControlMode.PositionControl);
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
    System.out.print("Position: ");
    System.out.println(motorLeft.getPosition());

    System.out.print(motorLeft.getKP());
    System.out.print("  ");
    System.out.print(motorLeft.getKI());
    System.out.print("  ");
    System.out.println(motorLeft.getKD());
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}
}
