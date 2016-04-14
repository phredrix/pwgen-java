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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ui.MainFrame;
import core.DataModel;
import core.Generator;

public final class NewActionListener implements ActionListener {

    public NewActionListener(MainFrame mf, DataModel data)
    {
        _mf = mf;
        _data = data;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        _mf.setText(getPassword());
    }

    private String getPassword()
    {
        final String characterSet = _gen.getCharacterSet(_data.getCharSet());
        final int minLength = _data.getMinLength();
        final int maxLength = _data.getMaxLength();
        return _gen.createPassword(characterSet, minLength, maxLength);
    }

    private MainFrame _mf;
    private DataModel _data;
    private Generator _gen = new Generator();
}