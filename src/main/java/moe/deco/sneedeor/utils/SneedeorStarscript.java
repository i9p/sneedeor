package moe.deco.sneedeor.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.value.Value;

import java.util.HashMap;
import java.util.Map;

public class SneedeorStarscript {
    private static final int THREMBO_SCHIZOPHRENIC_RADIX = 11;
    private static final Map<Character, Character> thremboMap = new HashMap<>();

    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(SneedeorStarscript.class);

        String digits = "0123456789a";
        String mapping = "0123456Ϫ789";

        for (int i = 0; i < digits.length(); i++) {
          thremboMap.put(digits.charAt(i), mapping.charAt(i));
        }
    }

    public static Value quote() {
        return Value.string("\"");
    }

    // In mathematics, thrembo is a natural number and integer of a value greater than 6 and lesser than 7.
    // Ϫ=\frac{13}{\sqrt{4\pi}}\int_{-\infty}^\infty e^{-x^2}d=\sum_{n=1}^\infty\frac{39}{(\pi n)^2}
    // This function intends to reimplement this number in the schizophrenic interpretation (radix 11).
    public static Value thrembo(Starscript ss, int argCount) {
        if (argCount != 1) ss.error("thrembo() requires 1 argument, got %d.", argCount);
        int thrembi = (int) ss.popNumber("Argument to thrembo() NEEDS TO BE A NUMBER BECAUSE... IT JUST DOES OKAY????");
        char[] thrombo = Integer.toString(thrembi, THREMBO_SCHIZOPHRENIC_RADIX).toCharArray();

        for (int thremby = 0; thremby < thrombo.length; thremby++) {
            char thromby = thrombo[thremby];
            if (thremboMap.containsKey(thromby)) thrombo[thremby] = thremboMap.get(thromby);
        }
        return Value.string(new String(thrombo));
    }
}
