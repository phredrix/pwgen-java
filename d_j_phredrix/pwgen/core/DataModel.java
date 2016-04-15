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

package d_j_phredrix.pwgen.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import d_j_phredrix.pwgen.core.Generator.CharSetType;

public class DataModel {

    public interface ChangeListener {
        enum Item {
            MIN_LENGTH, MAX_LENGTH, CHARACTER_SET
        };

        void dataChanged(DataModel d, Item whatChanged, Object source);

        void exceptionOccurred(Exception ex, Object source);
    }

    private DataModel()
    {
    }

    public static DataModel create()
    {
        return new DataModel();
    }

    public static DataModel loadFromPrefs()
    {
        DataModel result = create();
        Persistence p = new Persistence(DataModel.class);
        Preferences prefs = p.prefs();
        if (prefs.getLong(Messages.getString("DataModel.version"), -1) == serialVersionUID) //$NON-NLS-1$
        {
            result.minLength = prefs.getInt(Messages.getString("DataModel.minLength"), 8); //$NON-NLS-1$
            result.maxLength = prefs.getInt(Messages.getString("DataModel.maxLength"), 8); //$NON-NLS-1$
            String charSetList = prefs.get(Messages.getString("DataModel.charSets"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (!charSetList.isEmpty())
            {
                String[] charSets = charSetList.split(Messages.getString("DataModel.charSetSeparator")); //$NON-NLS-1$
                for (String charSet : charSets)
                {
                    result.charSet.add(CharSetType.valueOf(charSet));
                }
            }
        }
        return result;
    }

    public void saveToPrefs()
    {
        Persistence p = new Persistence(DataModel.class);
        Preferences prefs = p.prefs();
        prefs.putLong(Messages.getString("DataModel.version"), serialVersionUID); //$NON-NLS-1$
        prefs.putInt(Messages.getString("DataModel.minLength"), minLength); //$NON-NLS-1$
        prefs.putInt(Messages.getString("DataModel.maxLength"), maxLength); //$NON-NLS-1$
        CharSetType[] charSets = charSet.toArray(new CharSetType[charSet.size()]);
        List<String> charSetList = new ArrayList<>();
        for (CharSetType c : charSets) {
            charSetList.add(c.toString());
        }
        String[] charSetNames = charSetList.toArray(new String[charSetList.size()]);
        prefs.put(Messages.getString("DataModel.charSets"), String.join(Messages.getString("DataModel.charSetSeparator"), charSetNames)); //$NON-NLS-1$ //$NON-NLS-2$
        try
        {
            prefs.flush();
        }
        catch (BackingStoreException e)
        {
            e.printStackTrace();
        }
    }

    public int getMinLength()
    {
        return minLength;
    }

    public void setMinLength(int value, Object source)
    {
        try
        {
            checkValue(value);
            if (value != minLength)
            {
                minLength = value;
                notifyListeners(ChangeListener.Item.MIN_LENGTH, source);
            }
        }
        catch (Exception ex)
        {
            notifyException(ex, source);
        }
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(int value, Object source)
    {
        try
        {
            checkValue(value);
            if (value != maxLength)
            {
                maxLength = value;
                notifyListeners(ChangeListener.Item.MAX_LENGTH, source);
            }
        }
        catch (Exception ex)
        {
            notifyException(ex, source);
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
            notifyListeners(ChangeListener.Item.CHARACTER_SET, null);
        }
    }

    public void addCharSet(CharSetType cs)
    {
        if (charSet.add(cs))
        {
            notifyListeners(ChangeListener.Item.CHARACTER_SET, null);
        }
    }

    public void removeCharSet(CharSetType cs)
    {
        if (charSet.remove(cs))
        {
            notifyListeners(ChangeListener.Item.CHARACTER_SET, null);
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

    private void notifyListeners(ChangeListener.Item what, Object source)
    {
        _listeners.forEach(l -> l.dataChanged(this, what, source));
    }

    private void notifyException(Exception ex, Object source)
    {
        _listeners.forEach(l -> l.exceptionOccurred(ex, source));
    }

    private void checkValue(int value) throws Exception
    {
        if (value < 0)
        {
            throw new Exception(Messages.getString("DataModel.positiveValueRequired")); //$NON-NLS-1$
        }
    }

    private int minLength = 8;
    private int maxLength = 8;
    private Set<CharSetType> charSet = new TreeSet<>();
    private List<ChangeListener> _listeners = new ArrayList<>();
    
    private static final long serialVersionUID = -3967729926712058588L;
}