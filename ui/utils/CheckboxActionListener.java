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

import javax.swing.JButton;

import core.DataModel;

/**
 * Update data model according to checkbox selections. *
 */
public class CheckboxActionListener implements ActionListener {
    DataModel _data;
    private JButton _applyButton;

    public CheckboxActionListener(DataModel data, JButton applyButton)
    {
        _data = data;
        _applyButton = applyButton;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof CharSetCheckBox)
        {
            CharSetCheckBox cb = (CharSetCheckBox) e.getSource();
            if (cb.isSelected())
            {
                _data.addCharSet(cb.getCharSet());
            }
            else
            {
                _data.removeCharSet(cb.getCharSet());
            }
            _applyButton.setEnabled(_data.getCharSet().length > 0);
        }
    }
}