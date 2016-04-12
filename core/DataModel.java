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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import core.Generator.CharSetType;

public class DataModel {

    public interface ChangeListener {
        enum Item {
            MIN_LENGTH, MAX_LENGTH, CHARACTER_SET
        };

        void dataChanged(DataModel d, Item whatChanged);
    }

    public int getMinLength()
    {
        return minLength;
    }

    public void setMinLength(int value)
    {
        if (value != minLength)
        {
            minLength = value;
            notifyListeners(ChangeListener.Item.MIN_LENGTH);
        }
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(int value)
    {
        if (value != maxLength)
        {
            maxLength = value;
            notifyListeners(ChangeListener.Item.MAX_LENGTH);
        }
    }

    public CharSetType[] getCharSet()
    {
        CharSetType[] result = new CharSetType[charSet.size()];
        result = charSet.toArray(result);
        return result;
    }

    public void setCharSet(CharSetType... value)
    {
        boolean changed = false;

        TreeSet<CharSetType> temp = new TreeSet<>();
        for (CharSetType c : value)
        {
            temp.add(c);
        }

        // Any objects in charSet that aren't in temp?
        for (Object c : charSet.toArray())
        {
            if (!temp.contains(c))
            {
                changed = true;
                break;
            }
        }

        // Any objects in temp that aren't in charSet?
        for (Object c : temp.toArray())
        {
            if (!charSet.contains(c))
            {
                changed = true;
                break;
            }
        }

        if (changed)
        {
            charSet = temp;
            notifyListeners(ChangeListener.Item.CHARACTER_SET);
        }
    }

    public void addCharSet(CharSetType cs)
    {
        if (charSet.add(cs))
        {
            notifyListeners(ChangeListener.Item.CHARACTER_SET);
        }
    }

    public void removeCharSet(CharSetType cs)
    {
        if (charSet.remove(cs))
        {
            notifyListeners(ChangeListener.Item.CHARACTER_SET);
        }
    }

    public void addListener(ChangeListener l)
    {
        _listeners.add(l);
    }

    public ChangeListener removeListener(ChangeListener l)
    {
        ChangeListener result = null;
        final int index = _listeners.indexOf(l);
        if (index >= 0)
        {
            result = _listeners.get(index);
            _listeners.remove(index);
        }
        return result;
    }

    private void notifyListeners(ChangeListener.Item what)
    {
        _listeners.forEach((l) -> l.dataChanged(this, what));
    }

    private int minLength;
    private int maxLength;
    private Set<CharSetType> charSet = new TreeSet<>();

    List<ChangeListener> _listeners = new ArrayList<>();
}