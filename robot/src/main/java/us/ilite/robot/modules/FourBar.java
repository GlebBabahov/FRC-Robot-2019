package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.team254.lib.util.Util;

import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.EFourBarData;


public class FourBar extends Module {

    private ILog mLog = Logger.createLog( FourBar.class );
    private Data mData;

    private CANSparkMax mNeos;
    private CANSparkMax mNeo2;
    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;

    private double mAngularPosition;
    private double mPreviousNeo1Rotations;
    private double mPreviousNeo2Rotations;
    private final double kMinOutput = -1;
    private final double kMaxOutput = 1;

    private double mOutput;

    /**
     * Construct a FourBar with a Data object
     * @param pData a Data object used to log to codex
     */
    public FourBar( Data pData ) {
        // Later: SystemSettings address
        mNeos = new CANSparkMax( 9, CANSparkMaxLowLevel.MotorType.kBrushless );
        mNeo2 = new CANSparkMax( 10, CANSparkMaxLowLevel.MotorType.kBrushless );
        mNeo2.follow( mNeos, true );
    
        // Connect the NEO's to the encoders
        mNeo1Encoder = mNeos.getEncoder();
        mNeo2Encoder = mNeo2.getEncoder();

        mAngularPosition = ( ( mNeo1Encoder.getPosition() / 300 ) + ( mNeo2Encoder.getPosition() / 300 ) ) / 2;
        mData = pData;
    }


    @Override
    public void modeInit( double pNow ) {
        mLog.error( "FourBar Initialized..." );
        mOutput = 0;
        mPreviousNeo1Rotations = mNeo1Encoder.getPosition();
        mPreviousNeo2Rotations = mNeo2Encoder.getPosition();

        mNeos.setSmartCurrentLimit( 20 );
    }

    @Override
    public void periodicInput( double pNow ) {
        updateCodex();
    }

    @Override
    public void update( double pNow ) {
        mNeos.set( mOutput );
    }

    @Override
    public void shutdown( double pNow ) {
        mNeos.disable();
    }

    /**
     * Sets output to desired output
     * @param desiredOutput the desired percent output
     * @param isIdle whether it is idle or not ( don't add gravity comp if just b is being held )
     */
    public void setDesiredOutput( double desiredOutput, boolean isIdle ) {
        if ( isIdle ) {
            mOutput = 0;
        } else {
            mOutput = Util.limit(desiredOutput + gravityCompAtPosition(), kMinOutput, kMaxOutput);
        }
    }

    /**
     * Calculates necessary output to counter gravity
     * @return the percent output to counter gravity
     */
    public double gravityCompAtPosition() {
        return SystemSettings.kTFourBar * Math.cos( mAngularPosition );
    }

    /**
     * Update angular position based on current rotations
     */
    public void updateAngularPosition() {
        mAngularPosition = ( ( mNeo1Encoder.getPosition() - mPreviousNeo1Rotations / 300 ) + ( mNeo2Encoder.getPosition() - mPreviousNeo2Rotations / 300 ) ) / 2;
    }
    
    /**
     * Handle stop type based on location
     * Hold if not at 0
     */
    public void handleStopType() {
        if ( mAngularPosition != 0 ) {
            hold();
        } else {
            stop();
        }
    }

    /**
     * holds arm at current location using gravity compensation
     */
    public void hold() {
        mOutput = gravityCompAtPosition();
    }

    /**
     * Cut power to the motor
     */
    public void stop() {
        setDesiredOutput( 0, true );
        mNeos.stopMotor();
    }

    /**
     * Update tracked variables in the codex
     */
    public void updateCodex() {
        updateAngularPosition();
        mData.fourbar.set( EFourBarData.A_OUTPUT, mNeo1.get() );
        mData.fourbar.set( EFourBarData.A_VOLTAGE, mNeo1.getBusVoltage() );
        mData.fourbar.set( EFourBarData.A_CURRENT, mNeo1.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.B_OUTPUT, mNeo2.get() );
        mData.fourbar.set( EFourBarData.B_VOLTAGE, mNeo2.getBusVoltage() );
        mData.fourbar.set( EFourBarData.B_CURRENT, mNeo2.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.ANGLE, mAngularPosition );
    }
}
