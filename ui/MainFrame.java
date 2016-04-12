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

package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;
import core.DataModel;
import core.DataModel.ChangeListener;
import core.Generator;
import core.Generator.CharSetType;
import java.awt.Color;

public class MainFrame extends JFrame implements ChangeListener {

    public MainFrame()
    {
        getContentPane().setBackground(Color.PINK);
        _data = new DataModel();
        _data.addListener(this);
        _pwGen = new Generator();
        initGUI();
    }

    @Override
    public void dataChanged(DataModel d, ChangeListener.Item whatChanged)
    {
        switch (whatChanged)
        {
        case CHARACTER_SET:
            break;
        case MAX_LENGTH:
            maxLengthTextField.setText(Integer.toString(d.getMaxLength()));
            // Ensure that minimum length is <= maximum length
            if (_data.getMinLength() > d.getMaxLength())
            {
                _data.setMinLength(d.getMaxLength());
            }
            break;
        case MIN_LENGTH:
            minLengthTextField.setText(Integer.toString(d.getMinLength()));
            // Ensure that minimum length is <= maximum length
            if (_data.getMaxLength() < d.getMinLength())
            {
                _data.setMaxLength(d.getMinLength());
            }
            break;
        default:
            break;
        }
    }

    private void initGUI()
    {
        setTitle("Password Generator");
        final JFrame mainFrame = this;
        getContentPane().setLayout(new BorderLayout(0, 0));

        checkBoxes.add(chckbxUpper);
        checkBoxes.add(chckbxLower);
        checkBoxes.add(chckbxDigit);
        checkBoxes.add(chckbxPunctuation);
        checkBoxes.add(chckbxSpecial);
        ActionListener l = new ActionListener() {
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
                }
            }
        };
        checkBoxes.forEach((cb) -> {
            cb.setSelected(true);
            cb.addActionListener(l);
            checkBoxPanel.add(cb);
        });

        _data.setCharSet(CharSetType.values());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FlowLayout flowLayout = (FlowLayout) outerPanel.getLayout();
        flowLayout.setVgap(8);
        flowLayout.setHgap(8);
        outerPanel.setBackground(Color.ORANGE);
        getContentPane().add(outerPanel, BorderLayout.CENTER);
        outerPanel.setLayout(new BorderLayout(0, 0));
        innerPanel.setBackground(Color.BLUE);
        outerPanel.add(innerPanel, BorderLayout.CENTER);
        innerPanel.setLayout(new BorderLayout(0, 0));
        textArea.setEditable(false);
        textArea.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
        textArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 14));
        textArea.setPreferredSize(new Dimension(320, 120));
        innerPanel.add(textArea, BorderLayout.CENTER);
        innerPanel.add(topPanel, BorderLayout.NORTH);
        lblMinimumLength.setLabelFor(minLengthTextField);
        maxLengthTextField.setColumns(10);
        maxLengthTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                _data.setMaxLength(Integer.valueOf(maxLengthTextField.getText()));
            }
        });
        minLengthTextField.setColumns(10);
        minLengthTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                _data.setMinLength(Integer.valueOf(minLengthTextField.getText()));
            }
        });
        topPanel.setLayout(new MigLayout("", "[77px][86px][81px][86px]", "[33px][33px][33px]"));
        topPanel.add(lblMinimumLength, "cell 0 0,alignx left,aligny center");
        topPanel.add(minLengthTextField, "cell 1 0,alignx center,aligny center");
        topPanel.add(lblMaximumLength, "cell 0 1,alignx left,aligny center");
        topPanel.add(maxLengthTextField, "cell 1 1,alignx center,aligny center");
        lblMaximumLength.setLabelFor(maxLengthTextField);

        topPanel.add(checkBoxPanel, "cell 0 2 4 1,alignx left,aligny center");
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));

        innerPanel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setPreferredSize(new Dimension(10, 32));
        buttonPanel.setMinimumSize(new Dimension(10, 32));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        btnCopy.setMnemonic('c');
        btnCopy.addActionListener((e) -> {
            System.out.println(textArea.getSelectedText());
            String password = textArea.getSelectedText();
            StringSelection selec = new StringSelection(password);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selec, selec);
        });
        
        btnNew.setMnemonic('n');
        btnNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                final String pw = getPassword();
                EventQueue.invokeLater(() -> {
                    textArea.setText(pw);
                    textArea.setCaretPosition(0);
                    textArea.moveCaretPosition(pw.length());
                    textArea.getCaret().setSelectionVisible(true);
                });
            }
        });
        
        btnQuit.setMnemonic('q');
        btnQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                mainFrame.dispose();
            }
        });
        
        buttonPanel.add(btnNew);
        buttonPanel.add(btnCopy);
        buttonPanel.add(btnQuit);

        pack();
    }

    private String getPassword()
    {
        final String characterSet = getCharacterSet();
        int minLength = valueOf(minLengthTextField);
        int maxLength = valueOf(maxLengthTextField);
        return _pwGen.createPassword(characterSet, minLength, maxLength);
    }

    private int valueOf(JTextComponent tf)
    {
        return Integer.valueOf(tf.getText());
    }

    private String getCharacterSet()
    {
        final CharSetType[] cs = _data.getCharSet();
        return _pwGen.getCharacterSet(cs);
    }

    private DataModel _data;
    private Generator _pwGen;
    private final JButton btnNew = new JButton("New");
    private final JButton btnCopy = new JButton("Copy");
    private final JButton btnQuit = new JButton("Quit");
    private final JTextArea textArea = new JTextArea();
    private final JPanel outerPanel = new JPanel();
    private final JPanel innerPanel = new JPanel();
    private final JPanel topPanel = new JPanel();
    private final JPanel buttonPanel = new JPanel();
    private final JTextField minLengthTextField = new JTextField("8");
    private final JTextField maxLengthTextField = new JTextField("8");
    private final JLabel lblMinimumLength = new JLabel("Minimum length:");
    private final JLabel lblMaximumLength = new JLabel("Maximum length:");
    private final CharSetCheckBox chckbxUpper = new CharSetCheckBox("upper", CharSetType.UPPER);
    private final CharSetCheckBox chckbxLower = new CharSetCheckBox("lower", CharSetType.LOWER);
    private final CharSetCheckBox chckbxDigit = new CharSetCheckBox("digit", CharSetType.DIGIT);
    private final CharSetCheckBox chckbxPunctuation = new CharSetCheckBox("punctuation", CharSetType.PUNCTUATION);
    private final CharSetCheckBox chckbxSpecial = new CharSetCheckBox("special", CharSetType.SPECIAL);
    private final List<CharSetCheckBox> checkBoxes = new ArrayList<>();
    private final JPanel checkBoxPanel = new JPanel();

    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> {
            JFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static final long serialVersionUID = 1L;

}
