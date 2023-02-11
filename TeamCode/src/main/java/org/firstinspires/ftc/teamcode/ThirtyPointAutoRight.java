package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;

import java.util.ArrayList;
@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "ThirtyPointAutoRight", group = "OpenCV Autos" )

public class ThirtyPointAutoRight extends LinearOpMode {

    private final ElapsedTime runtime = new ElapsedTime();
    public DcMotor middleslideDrive = null;
    private boolean hasRun = false;
    public DcMotor frontleftDrive = null;
    public DcMotor frontrightDrive = null;
    public DcMotor backleftDrive = null;
    public DcMotor backrightDrive = null;
    public Servo rightgripperDrive = null;
    public Servo leftgripperDrive = null;


    OpenCvCamera camera;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;

    static final double FEET_PER_METER = 3.28084;

    // Lens intrinsics
    // UNITS ARE PIXELS
    // NOTE: this calibration is for the C920 webcam at 800x448.
    // You will need to do your own calibration for other configurations!
    double fx = 578.272;
    double fy = 578.272;
    double cx = 402.145;
    double cy = 221.506;

    // UNITS ARE METERS
    double tagSize = 0.166;

    int tagPosition = 0;

    int tagOfInterest1 = 1; // Tag ID 1 from the 36h11 family
    int tagOfInterest2 = 2; // Tag ID 2 from the 36h11 family
    int tagOfInterest3 = 3; // Tag ID 3 from the 36h11 family
    public IMU imu;

    AprilTagDetection tagOfInterest = null;

    @Override
    public void runOpMode() {
        // Motors and Servos
        frontleftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        frontrightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        backleftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        backrightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        middleslideDrive = hardwareMap.get(DcMotor.class, "middle_slides_drive");

        rightgripperDrive = hardwareMap.get(Servo.class, "right_gripper_drive");
        leftgripperDrive = hardwareMap.get(Servo.class, "left_gripper_drive");

        frontleftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontrightDrive.setDirection(DcMotor.Direction.FORWARD);
        backleftDrive.setDirection(DcMotor.Direction.REVERSE);
        backrightDrive.setDirection(DcMotor.Direction.FORWARD);
        middleslideDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontleftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontrightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backleftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backrightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        middleslideDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontleftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontrightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backleftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backrightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        middleslideDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //imu
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters myIMUparameters;

        myIMUparameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );


        //Camera
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagSize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(800, 448, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                // does nothing if error
            }
        });

        telemetry.setMsTransmissionInterval(50);

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        Pose2d startPose = new Pose2d(36, -63, Math.toRadians(90));
        drive.setPoseEstimate(startPose);

        frontleftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        frontrightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        backleftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        backrightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        middleslideDrive = hardwareMap.get(DcMotor.class, "middle_slides_drive");

        TrajectorySequence GeneralMovement = drive.trajectorySequenceBuilder(startPose)
                .addDisplacementMarker(() -> {
                    setSlider(1,2);
                })
                .lineTo(new Vector2d(36,-12))
                .lineTo(new Vector2d(24,-12))
                .addDisplacementMarker(() -> {
                    setSlider(1,7);
                })
                .lineTo(new Vector2d(24,-10))
                .addDisplacementMarker(() -> {
                    setServo(0);
                })
                .waitSeconds(.2)
                .lineTo(new Vector2d(24,-12))
                .addDisplacementMarker(() -> {
                    setSlider(1,2);
                })
                .lineToLinearHeading(new Pose2d(60, -12, Math.toRadians(180)))
                .addDisplacementMarker(() -> {
                    setServo(1);
                })
                .lineToLinearHeading(new Pose2d(36, -12, Math.toRadians(-90)))
                .lineTo(new Vector2d(24,-12))
                .addDisplacementMarker(() -> {
                    setSlider(1,7);
                })
                .lineTo(new Vector2d(24,-10))
                .addDisplacementMarker(() -> {
                    setServo(0);
                })
                .waitSeconds(.2)
                .lineTo(new Vector2d(24,-12))
                .addDisplacementMarker(() -> {
                    setSlider(1,2);
                })
                .lineTo(new Vector2d(36,-12))
                .lineTo(new Vector2d(36,-36))
                .build();


        TrajectorySequence Tag1Ending = drive.trajectorySequenceBuilder(new Pose2d(36,-36,Math.toRadians(90)))
                .lineTo(new Vector2d(12,-36))
                .build();

        TrajectorySequence Tag3Ending = drive.trajectorySequenceBuilder(new Pose2d(36,-36,Math.toRadians(90)))
                .lineTo(new Vector2d(12,-36))
                .build();

        while (!isStarted() && !isStopRequested()) {
            ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

            if (currentDetections.size() != 0) {
                boolean tagFound = false;

                for (AprilTagDetection tag : currentDetections) {
                    if (tag.id == tagOfInterest1) {
                        tagOfInterest = tag;
                        tagFound = true;
                        tagPosition = 1;
                        break;
                    }

                    if (tag.id == tagOfInterest2) {
                        tagOfInterest = tag;
                        tagFound = true;
                        tagPosition = 2;
                        break;
                    }

                    if (tag.id == tagOfInterest3) {
                        tagOfInterest = tag;
                        tagFound = true;
                        tagPosition = 3;
                        break;
                    }
                }

                if (tagFound) {
                    telemetry.addLine("Tag of interest is in sight at Position " + tagPosition);
                    imu.resetYaw();
                } else {
                    telemetry.addLine("Don't see tag of interest :(");
                }
            }
            telemetry.update();
            sleep(20);  // is required or else system will break
        }

        if (tagOfInterest != null) {
            telemetry.addLine("Tag was seen at position " + tagPosition);
            telemetry.addLine("Executing plan " + tagPosition);
            telemetry.update();

            if (tagPosition == 1) {
                frontleftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
                frontrightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
                backleftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
                backrightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
                middleslideDrive = hardwareMap.get(DcMotor.class, "middle_slides_drive");

                drive.followTrajectorySequence(GeneralMovement);

                drive.followTrajectorySequence(Tag1Ending);

            }

            if (tagPosition == 2) {
                frontleftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
                frontrightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
                backleftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
                backrightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
                middleslideDrive = hardwareMap.get(DcMotor.class, "middle_slides_drive");

                drive.followTrajectorySequence(GeneralMovement);

            }

            if (tagPosition == 3) {
                frontleftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
                frontrightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
                backleftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
                backrightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
                middleslideDrive = hardwareMap.get(DcMotor.class, "middle_slides_drive");

                drive.followTrajectorySequence(GeneralMovement);
                drive.followTrajectorySequence(Tag3Ending);

            }

        } else {
            telemetry.addLine("No tag available, it was never seen during the init loop :(");
            telemetry.addLine("Backup plan INITIATED :)");
            while (!hasRun) {
                setServo(0);
            }
            telemetry.update();
        }
    }

    public void setSlider(double speed, double level) {
        frontleftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        frontrightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        backleftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        backrightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        middleslideDrive = hardwareMap.get(DcMotor.class, "middle_slides_drive");
        double position = 0;
        if (level == 0) {position = 0;}
        if (level == 1) {position = 200;}
        if (level == 2) {position = 500;}
        if (level == 3) {position = 700;}
        if (level == 4) {position = 1000;}
        if (level == 5) {position = 1700;}
        if (level == 6) {position = 2600;}
        if (level == 7) {position = 4340;}


        if (middleslideDrive.getCurrentPosition() < position) {
            while (middleslideDrive.getCurrentPosition()< position) {
                middleslideDrive.setPower(-speed);}
            telemetry();

        }
        if (middleslideDrive.getCurrentPosition() > position) {
            while (middleslideDrive.getCurrentPosition()> position) {
                middleslideDrive.setPower(speed);}
            telemetry();
        }
        middleslideDrive.setPower(0);
    }
    public void setServo(double position) {
        position = position * 1;
        if (position == 1) {
            while (leftgripperDrive.getPosition() != .505) {
                leftgripperDrive.setPosition(.505);
                rightgripperDrive.setPosition(.35);
            }

        }
        if (position == 0) {
            while (leftgripperDrive.getPosition() != .77) {
                leftgripperDrive.setPosition(.77);
                rightgripperDrive.setPosition(.12);
            }

        }

    }


    public void telemetry() {
        telemetry.addData("Run Time", runtime.toString());
        telemetry.addData("Front Right Encoder", frontrightDrive.getCurrentPosition());
        telemetry.addData("Front Left Encoder", frontleftDrive.getCurrentPosition());
        telemetry.addData("Back Right Encoder", backrightDrive.getCurrentPosition());
        telemetry.addData("Back Left Encoder", backleftDrive.getCurrentPosition());
        telemetry.addData("IMU Z Angle", imu.getRobotOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle);
        telemetry.update();
    }

}