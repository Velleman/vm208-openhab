package org.openhab.binding.vm208.internal.i2c;

import java.util.EnumSet;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.impl.PinImpl;

public class TCA6424APin {

    public static final Pin GPIO_00 = createDigitalPin(0, "GPIO 0");
    public static final Pin GPIO_01 = createDigitalPin(1, "GPIO 1");
    public static final Pin GPIO_02 = createDigitalPin(2, "GPIO 2");
    public static final Pin GPIO_03 = createDigitalPin(3, "GPIO 3");
    public static final Pin GPIO_04 = createDigitalPin(4, "GPIO 4");
    public static final Pin GPIO_05 = createDigitalPin(5, "GPIO 5");
    public static final Pin GPIO_06 = createDigitalPin(6, "GPIO 6");
    public static final Pin GPIO_07 = createDigitalPin(7, "GPIO 7");
    public static final Pin GPIO_10 = createDigitalPin(8, "GPIO 8");
    public static final Pin GPIO_11 = createDigitalPin(9, "GPIO 9");
    public static final Pin GPIO_12 = createDigitalPin(10, "GPIO 10");
    public static final Pin GPIO_13 = createDigitalPin(11, "GPIO 11");
    public static final Pin GPIO_14 = createDigitalPin(12, "GPIO 12");
    public static final Pin GPIO_15 = createDigitalPin(13, "GPIO 13");
    public static final Pin GPIO_16 = createDigitalPin(14, "GPIO 14");
    public static final Pin GPIO_17 = createDigitalPin(15, "GPIO 15");
    public static final Pin GPIO_20 = createDigitalPin(16, "GPIO 16");
    public static final Pin GPIO_21 = createDigitalPin(17, "GPIO 17");
    public static final Pin GPIO_22 = createDigitalPin(18, "GPIO 18");
    public static final Pin GPIO_23 = createDigitalPin(19, "GPIO 19");
    public static final Pin GPIO_24 = createDigitalPin(20, "GPIO 20");
    public static final Pin GPIO_25 = createDigitalPin(21, "GPIO 21");
    public static final Pin GPIO_26 = createDigitalPin(22, "GPIO 22");
    public static final Pin GPIO_27 = createDigitalPin(23, "GPIO 23");

    public static final Pin GPIO_INT = createDigitalPin(35, "GPIO INT");

    public static Pin[] ALL = { TCA6424APin.GPIO_00, TCA6424APin.GPIO_01, TCA6424APin.GPIO_02, TCA6424APin.GPIO_03,
            TCA6424APin.GPIO_04, TCA6424APin.GPIO_05, TCA6424APin.GPIO_06, TCA6424APin.GPIO_07, TCA6424APin.GPIO_10,
            TCA6424APin.GPIO_11, TCA6424APin.GPIO_12, TCA6424APin.GPIO_13, TCA6424APin.GPIO_14, TCA6424APin.GPIO_15,
            TCA6424APin.GPIO_16, TCA6424APin.GPIO_17, TCA6424APin.GPIO_20, TCA6424APin.GPIO_21, TCA6424APin.GPIO_22,
            TCA6424APin.GPIO_23, TCA6424APin.GPIO_24, TCA6424APin.GPIO_25, TCA6424APin.GPIO_26, TCA6424APin.GPIO_27 };

    private static Pin createDigitalPin(int address, String name) {
        return new PinImpl(TCA6424AGpioProvider.NAME, address, name,
                EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                EnumSet.of(PinPullResistance.PULL_UP, PinPullResistance.OFF));
    }

}
