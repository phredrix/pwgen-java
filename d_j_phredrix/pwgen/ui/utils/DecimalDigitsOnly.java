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

package d_j_phredrix.pwgen.ui.utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DecimalDigitsOnly extends KeyAdapter {
    @Override
    public void keyTyped(KeyEvent e)
    {
        char c = e.getKeyChar();
        boolean isDigit = (c >= '0' && c <= '9');
        if (!isDigit)
        {
            e.consume();
        }
    }

}