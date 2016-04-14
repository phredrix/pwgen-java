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

package ui.utils;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

import javax.swing.JTextField;

/**
 * Perform an action when a JTextField loses focus. *
 */
public class FocusLost extends FocusAdapter {
    public FocusLost(Consumer<JTextField> c)
    {
        _c = c;
    }

    @Override
    public void focusLost(FocusEvent e)
    {
        if (e.getID() == FocusEvent.FOCUS_LOST)
        {
            Component component = e.getComponent();
            if (component instanceof JTextField)
            {
                try
                {
                    _c.accept((JTextField) component);
                }
                catch (Exception ex)
                {
                    ;
                }
            }
        }
    }

    private Consumer<JTextField> _c;
}