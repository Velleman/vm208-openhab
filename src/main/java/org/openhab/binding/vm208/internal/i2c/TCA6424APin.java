package org.openhab.binding.vm208.internal.i2c;

import java.util.EnumSet;

import org.mapdb.Atomic.String;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.impl.PinImpl;

public class TCA6424APin {

    public static final Pin TCA6424A_P00 = createDigitalPin(0, "TCA6424A 0");
    public static final Pin TCA6424A_P01 = createDigitalPin(1, "TCA6424A 1");
    public static final Pin TCA6424A_P02 = createDigitalPin(2, "TCA6424A 2");
    public static final Pin TCA6424A_P03 = createDigitalPin(3, "TCA6424A 3");
    public static final Pin TCA6424A_P04 = createDigitalPin(4, "TCA6424A 4");
    public static final Pin TCA6424A_P05 = createDigitalPin(5, "TCA6424A 5");
    public static final Pin TCA6424A_P06 = createDigitalPin(6, "TCA6424A 6");
    public static final Pin TCA6424A_P07 = createDigitalPin(7, "TCA6424A 7");
    public static final Pin TCA6424A_P10 = createDigitalPin(8, "TCA6424A 8");
    public static final Pin TCA6424A_P11 = createDigitalPin(9, "TCA6424A 9");
    public static final Pin TCA6424A_P12 = createDigitalPin(10, "TCA6424A 10");
    public static final Pin TCA6424A_P13 = createDigitalPin(11, "TCA6424A 11");
    public static final Pin TCA6424A_P14 = createDigitalPin(12, "TCA6424A 12");
    public static final Pin TCA6424A_P15 = createDigitalPin(13, "TCA6424A 13");
    public static final Pin TCA6424A_P16 = createDigitalPin(14, "TCA6424A 14");
    public static final Pin TCA6424A_P17 = createDigitalPin(15, "TCA6424A 15");
    public static final Pin TCA6424A_P20 = createDigitalPin(16, "TCA6424A 16");
    public static final Pin TCA6424A_P21 = createDigitalPin(17, "TCA6424A 17");
    public static final Pin TCA6424A_P22 = createDigitalPin(18, "TCA6424A 18");
    public static final Pin TCA6424A_P23 = createDigitalPin(19, "TCA6424A 19");
    public static final Pin TCA6424A_P24 = createDigitalPin(20, "TCA6424A 20");
    public static final Pin TCA6424A_P25 = createDigitalPin(21, "TCA6424A 21");
    public static final Pin TCA6424A_P26 = createDigitalPin(22, "TCA6424A 22");
    public static final Pin TCA6424A_P27 = createDigitalPin(23, "TCA6424A 23");

    // We don't need this?
    public static final Pin TCA6424A__PINT = createDigitalPin(32, "TCA INT");

    public static Pin[] ALL = { TCA6424APin.TCA6424A_P00, TCA6424APin.TCA6424A_P01, TCA6424APin.TCA6424A_P02,
            TCA6424APin.TCA6424A_P03, TCA6424APin.TCA6424A_P04, TCA6424APin.TCA6424A_P05, TCA6424APin.TCA6424A_P06,
            TCA6424APin.TCA6424A_P07, TCA6424APin.TCA6424A_P10, TCA6424APin.TCA6424A_P11, TCA6424APin.TCA6424A_P12,
            TCA6424APin.TCA6424A_P13, TCA6424APin.TCA6424A_P14, TCA6424APin.TCA6424A_P15, TCA6424APin.TCA6424A_P16,
            TCA6424APin.TCA6424A_P17, TCA6424APin.TCA6424A_P20, TCA6424APin.TCA6424A_P21, TCA6424APin.TCA6424A_P22,
            TCA6424APin.TCA6424A_P23, TCA6424APin.TCA6424A_P24, TCA6424APin.TCA6424A_P25, TCA6424APin.TCA6424A_P26,
            TCA6424APin.TCA6424A_P27 };

    private static Pin createDigitalPin(int address, String name) {
        return new PinImpl(TCA6424AProvider.NAME, address, name,
                EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                EnumSet.of(PinPullResistance.PULL_UP, PinPullResistance.OFF));
    }

}
