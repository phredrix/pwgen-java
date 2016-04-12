/*******************************************************************************
 * Copyright (c) 2016 Don Fredricks.
 *
 * This file is part of the pwgen project (https://github.com/phredrix/pwgen-java).
 *
 * pwgen is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * pwgen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * pwgen.  If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/

package core;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Generator {

    public static void main(String[] args)
    {
        System.out.println("RNG algorithm is " + _rng.getAlgorithm());
    
        Generator gen = new Generator();
        final String characterSet = gen.getCharacterSet(CharSetType.UPPER, CharSetType.LOWER,
                CharSetType.DIGIT, CharSetType.PUNCTUATION);
        for (int ii = 0; ii < 20; ++ii)
        {
            final String pw = gen.createPassword(characterSet, 80, 80);
            System.out.println(pw);
        }
    }

    public enum CharSetType {
        UPPER,
        LOWER,
        DIGIT,
        PUNCTUATION,
        SPECIAL
    }

    public Generator()
    {
        _charSets.put(CharSetType.UPPER, UPPER);
        _charSets.put(CharSetType.LOWER, LOWER);
        _charSets.put(CharSetType.DIGIT, DIGIT);
        _charSets.put(CharSetType.PUNCTUATION, PUNCTUATION);
        _charSets.put(CharSetType.SPECIAL, SPECIAL);
    }

    /**
     * @param characterSet
     * @param minLength
     * @param maxLength
     * @return
     */
    public String createPassword(String characterSet, int minLength, int maxLength)
    {
        if (characterSet.isEmpty())
        {
            throw new IllegalArgumentException("Empty character set");
        }
        StringBuilder result = new StringBuilder();
        int length = getRandomInt(minLength, 1 + maxLength);
        for (int ii = 0; ii < length; ++ii)
        {
            result.append(getRandomChar(characterSet));
        }
        return result.toString();
    }

    public String getCharacterSet(CharSetType... set)
    {
        StringBuffer sb = new StringBuffer();
        for (CharSetType t : set)
        {
            sb.append(_charSets.get(t));
        }
        return sb.toString();
    }

    private static char getRandomChar(String characterSet)
    {
        int pos = getRandomInt(0, characterSet.length());
        return characterSet.charAt(pos);
    }

    /**
     * @param min
     *            Result will be greater than or equal to this value
     * @param max
     *            Result will be less than this value
     * @return
     */
    private static int getRandomInt(int min, int max)
    {
        return min + (int) Math.floor((max - min) * getRandomDouble());
    }

    private static double getRandomDouble()
    {
        return _rng.nextDouble();
    }

    private Map<CharSetType, String> _charSets = new HashMap<>();
    private static final SecureRandom _rng = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String PUNCTUATION = "!();:'\",.?/";
    private static final String SPECIAL = "@#$%^&_|{}[]<>+-*=";
}
