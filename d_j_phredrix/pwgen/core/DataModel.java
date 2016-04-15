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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import d_j_phredrix.pwgen.core.Generator.CharSetType;

public class DataModel implements Serializable {

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

    public static DataModel loadDataModel(String fileName) throws ClassNotFoundException, IOException
    {
        DataModel result = null;
        if (Files.exists(Paths.get(fileName)))
        {
            try (InputStream is = new FileInputStream(fileName);
                    ObjectInputStream ois = new ObjectInputStream(is))
            {
                Object o = ois.readObject();
                if (o instanceof DataModel)
                {
                    result = (DataModel) o;
                }
            }
        }

        return result;
    }

    public static DataModel loadFromPrefs()
    {
        DataModel result = create();
        Persistence p = new Persistence(DataModel.class);
        Preferences prefs = p.prefs();
        if (prefs.getLong("version", -1) == serialVersionUID)
        {
            result.minLength = prefs.getInt("minLength", 8);
            result.maxLength = prefs.getInt("maxLength", 8);
            String charSetList = prefs.get("charSets", "");
            if (!charSetList.isEmpty())
            {
                String[] charSets = charSetList.split(";");
                for (String charSet : charSets)
                {
                    result.charSet.add(CharSetType.valueOf(charSet));
                }
            }
        }
        return result;
    }

    public void saveDataModel(String fileName) throws IOException
    {
        try (OutputStream os = new FileOutputStream(fileName);
                ObjectOutputStream oos = new ObjectOutputStream(os))
        {
            oos.writeObject(this);
        }
    }

    public void saveToPrefs()
    {
        Persistence p = new Persistence(DataModel.class);
        Preferences prefs = p.prefs();
        prefs.putLong("version", serialVersionUID);
        prefs.putInt("minLength", minLength);
        prefs.putInt("maxLength", maxLength);
        CharSetType[] charSets = charSet.toArray(new CharSetType[charSet.size()]);
        List<String> charSetList = new ArrayList<>();
        for (CharSetType c : charSets) {
            charSetList.add(c.toString());
        }
        String[] charSetNames = charSetList.toArray(new String[charSetList.size()]);
        prefs.put("charSets", String.join(";", charSetNames));
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
            throw new Exception("Value must be positive");
        }
    }

    /**
     * The writeObject method is responsible for writing the state of the object
     * for its particular class so that the corresponding readObject method can
     * restore it. The default mechanism for saving the Object's fields can be
     * invoked by calling out.defaultWriteObject. The method does not need to
     * concern itself with the state belonging to its superclasses or
     * subclasses. State is saved by writing the individual fields to the
     * ObjectOutputStream using the writeObject method or by using the methods
     * for primitive data types supported by DataOutput.
     * 
     * @param out
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException
    {
        out.writeInt(minLength);
        out.writeInt(maxLength);
        out.writeObject(charSet);
        out.writeInt(charSet.size());
    }

    /**
     * The readObject method is responsible for reading from the stream and
     * restoring the classes fields. It may call in.defaultReadObject to invoke
     * the default mechanism for restoring the object's non-static and
     * non-transient fields. The defaultReadObject method uses information in
     * the stream to assign the fields of the object saved in the stream with
     * the correspondingly named fields in the current object. This handles the
     * case when the class has evolved to add new fields. The method does not
     * need to concern itself with the state belonging to its superclasses or
     * subclasses. State is saved by writing the individual fields to the
     * ObjectOutputStream using the writeObject method or by using the methods
     * for primitive data types supported by DataOutput.
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        _listeners = new ArrayList<>();

        minLength = in.readInt();
        maxLength = in.readInt();
        final Object o = in.readObject();
        charSet = (Set<CharSetType>) o;
        int charSetSize = in.readInt();
        if (charSet.size() != charSetSize)
        {
            throw new IOException("Incorrect collection size: charSet");
        }
    }

    /**
     * The readObjectNoData method is responsible for initializing the state of
     * the object for its particular class in the event that the serialization
     * stream does not list the given class as a superclass of the object being
     * deserialized. This may occur in cases where the receiving party uses a
     * different version of the deserialized instance's class than the sending
     * party, and the receiver's version extends classes that are not extended
     * by the sender's version. This may also occur if the serialization stream
     * has been tampered; hence, readObjectNoData is useful for initializing
     * deserialized objects properly despite a "hostile" or incomplete source
     * stream.
     * 
     * @throws ObjectStreamException
     */
    @SuppressWarnings("unused")
    private void readObjectNoData()
            throws ObjectStreamException
    {
    }

    private int minLength = 8;
    private int maxLength = 8;
    private Set<CharSetType> charSet = new TreeSet<>();

    List<ChangeListener> _listeners = new ArrayList<>();
    private static final long serialVersionUID = -3967729926712058588L;
}