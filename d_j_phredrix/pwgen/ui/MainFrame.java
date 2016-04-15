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

package d_j_phredrix.pwgen.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import net.miginfocom.swing.MigLayout;
import d_j_phredrix.pwgen.core.DataModel;
import d_j_phredrix.pwgen.core.DataModel.ChangeListener;
import d_j_phredrix.pwgen.core.Generator;
import d_j_phredrix.pwgen.core.Generator.CharSetType;
import d_j_phredrix.pwgen.core.Persistence;
import d_j_phredrix.pwgen.ui.utils.CharSetCheckBox;
import d_j_phredrix.pwgen.ui.utils.CheckboxActionListener;
import d_j_phredrix.pwgen.ui.utils.DecimalDigitsOnly;
import d_j_phredrix.pwgen.ui.utils.FocusLost;
import d_j_phredrix.pwgen.ui.utils.NewActionListener;
import d_j_phredrix.pwgen.ui.utils.QuitActionListener;
import d_j_phredrix.pwgen.ui.utils.SelectAllOnFocus;

public class MainFrame extends JFrame implements ChangeListener {

    public static void main(String[] args)
    {
        EventQueue.invokeLater(MainFrame::startApplication);
    }
    
    public static void startApplication() {
        JFrame frame = new MainFrame();
        frame.setVisible(true);
    }

    public MainFrame()
    {
//        try
//        {
//            _data = DataModel.loadDataModel(dataPersistenceFile());
//        }
//        catch (ClassNotFoundException | IOException e)
//        {
//            e.printStackTrace();
//        }
        _data = DataModel.loadFromPrefs();

        if (_data == null)
        {
            _data = DataModel.create();
        }

        for (Item t : Item.values())
        {
            dataChanged(_data, t, null);
        }

        _data.addListener(this);
        new Generator();

        initGUI();

        //loadUiData(uiPersistenceFile());
        loadFromPrefs();

        final WindowAdapter l = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                saveState();
            }
        };
        addWindowStateListener(l);
        addWindowListener(l);
    }

    /**
     * @param d
     * @param whatChanged
     */
    @Override
    public void dataChanged(DataModel d, ChangeListener.Item whatChanged, Object source)
    {
        if (source == this) return;

        switch (whatChanged)
        {
        case CHARACTER_SET:
            break;
        case MAX_LENGTH:
            _maxLengthTextField.setText(Integer.toString(d.getMaxLength()));
            // Ensure that minimum length is <= maximum length
            if (_data.getMinLength() > d.getMaxLength())
            {
                _data.setMinLength(d.getMaxLength(), source);
            }
            setDefaultColor(source);
            setMessage(null);
            break;
        case MIN_LENGTH:
            _minLengthTextField.setText(Integer.toString(d.getMinLength()));
            // Ensure that minimum length is <= maximum length
            if (_data.getMaxLength() < d.getMinLength())
            {
                _data.setMaxLength(d.getMinLength(), source);
            }
            setDefaultColor(source);
            setMessage(null);
            break;
        default:
            break;
        }
    }

    @Override
    public void exceptionOccurred(Exception ex, Object source)
    {
        setMessage(ex.getMessage());
        setErrorColor(source);
    }

    public void setText(String text)
    {
        _textArea.setText(text);
        _textArea.setCaretPosition(0);
        _textArea.moveCaretPosition(text.length());
        _textArea.getCaret().setSelectionVisible(true);
    }

    /**
     * NB: _data must be initialized before calling initGUI()
     */
    private void initGUI()
    {
        // TODO: Create a real icon for the app.
        final String icon = Messages.getString("MainFrame.iconResource"); //$NON-NLS-1$
        setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource(icon)));
        setTitle(Messages.getString("MainFrame.passwordGenerator")); //$NON-NLS-1$
        final JFrame mainFrame = this;
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        JButton btnNew = new JButton(Messages.getString("MainFrame.new")); //$NON-NLS-1$
        ActionListener l = new CheckboxActionListener(_data, btnNew);

        final CharSetType[] charSets = _data.getCharSet();
        HashMap<CharSetType, Boolean> charSetMap = new HashMap<>();
        for (CharSetType t : charSets)
        {
            charSetMap.put(t, true);
        }

        for (CharSetType v : CharSetType.values())
        {
            final String label = v.toString().toLowerCase();
            final CharSetCheckBox cb = new CharSetCheckBox(label, v);
            final boolean sel = charSetMap.get(v) != null;
            cb.setSelected(sel);
            _checkBoxes.add(cb);
        }

        _checkBoxes.forEach((cb) -> {
            cb.addActionListener(l);
            l.actionPerformed(new ActionEvent(cb, ActionEvent.ACTION_PERFORMED, "")); //$NON-NLS-1$
            checkBoxPanel.add(cb);
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel outerPanel = new JPanel();
        outerPanel.setPreferredSize(new Dimension(800, 400));
        getContentPane().add(outerPanel, BorderLayout.CENTER);
        SpringLayout sl_outerPanel = new SpringLayout();
        outerPanel.setLayout(sl_outerPanel);

        JPanel innerPanel = new JPanel();
        sl_outerPanel.putConstraint(SpringLayout.NORTH, innerPanel, 8, SpringLayout.NORTH, outerPanel);
        sl_outerPanel.putConstraint(SpringLayout.WEST, innerPanel, 8, SpringLayout.WEST, outerPanel);
        sl_outerPanel.putConstraint(SpringLayout.SOUTH, innerPanel, -8, SpringLayout.SOUTH, outerPanel);
        sl_outerPanel.putConstraint(SpringLayout.EAST, innerPanel, -8, SpringLayout.EAST, outerPanel);
        outerPanel.add(innerPanel);
        innerPanel.setLayout(new BorderLayout(0, 0));

        JLabel lblMinimumLength = new JLabel(Messages.getString("MainFrame.minimumLength")); //$NON-NLS-1$
        lblMinimumLength.setLabelFor(_minLengthTextField);

        JLabel lblMaximumLength = new JLabel(Messages.getString("MainFrame.maximumLength")); //$NON-NLS-1$
        lblMaximumLength.setLabelFor(_maxLengthTextField);

        final KeyListener kl = new DecimalDigitsOnly();

        _maxLengthTextField.setName("Maximum length"); //$NON-NLS-1$
        _maxLengthTextField.setColumns(10);
        _maxLengthTextField.addKeyListener(kl);
        _maxLengthTextField.addFocusListener(new SelectAllOnFocus());
        _maxLengthTextField.addFocusListener(
                new FocusLost(tf -> _data.setMaxLength(Integer.valueOf(tf.getText()), tf)));

        _minLengthTextField.setName("Minimum length"); //$NON-NLS-1$
        _minLengthTextField.setColumns(10);
        _minLengthTextField.addKeyListener(kl);
        _minLengthTextField.addFocusListener(new SelectAllOnFocus());
        _minLengthTextField.addFocusListener(
                new FocusLost(tf -> _data.setMinLength(Integer.valueOf(tf.getText()), tf)));

        JPanel topPanel = new JPanel();
        topPanel.setName("Top panel"); //$NON-NLS-1$
        topPanel.setLayout(new MigLayout("", "[77px][86px][81px][86px]", "[33px][33px][33px]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        topPanel.add(lblMinimumLength, "cell 0 0,alignx left,aligny center"); //$NON-NLS-1$
        topPanel.add(_minLengthTextField, "cell 1 0,alignx center,aligny center"); //$NON-NLS-1$
        topPanel.add(lblMaximumLength, "cell 0 1,alignx left,aligny center"); //$NON-NLS-1$
        topPanel.add(_maxLengthTextField, "cell 1 1,alignx center,aligny center"); //$NON-NLS-1$

        topPanel.add(checkBoxPanel, "cell 0 2 4 1,alignx left,aligny center"); //$NON-NLS-1$
        innerPanel.add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(10, 32));
        buttonPanel.setMinimumSize(new Dimension(10, 32));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        innerPanel.add(buttonPanel, BorderLayout.SOUTH);

        btnNew.setMnemonic('n');
        btnNew.addActionListener(new NewActionListener(this, _data));

        JButton btnCopy = new JButton(Messages.getString("MainFrame.copy")); //$NON-NLS-1$
        btnCopy.setMnemonic('c');
        btnCopy.addActionListener((e) -> {
            String password = _textArea.getSelectedText();
            StringSelection selec = new StringSelection(password);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selec, selec);
        });

        JButton btnQuit = new JButton(Messages.getString("MainFrame.quit")); //$NON-NLS-1$
        btnQuit.setMnemonic('q');
        btnQuit.addActionListener(new QuitActionListener(mainFrame));

        buttonPanel.setName("Button panel"); //$NON-NLS-1$
        buttonPanel.add(btnNew);
        buttonPanel.add(btnCopy);
        buttonPanel.add(btnQuit);
        
        JPanel textPanel = new JPanel();
        textPanel.setName("Text panel"); //$NON-NLS-1$
        textPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        innerPanel.add(textPanel, BorderLayout.CENTER);
        textPanel.setLayout(new BorderLayout(8, 8));

        _textArea.setName("Text area"); //$NON-NLS-1$
        _textArea.setLineWrap(true);
        _textArea.setEditable(false);
        _textArea.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
        _textArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 14)); //$NON-NLS-1$
        _textArea.setPreferredSize(new Dimension(320, 120));
        textPanel.add(_textArea, BorderLayout.CENTER);

        _messageArea.setName("Message area"); //$NON-NLS-1$
        _messageArea.setBackground(SystemColor.control);
        _messageArea.setBorder(null);
        textPanel.add(_messageArea, BorderLayout.SOUTH);

        pack();
    }

    private void setMessage(String message)
    {
        _messageArea.setText(message);
    }

    private void setDefaultColor(Object source)
    {
        if (source instanceof JTextField)
        {
            ((JTextField) source).setBackground(_defaultColor);
        }
    }

    private void setErrorColor(Object source)
    {
        if (source instanceof Container)
        {
            ((Container) source).setBackground(Color.RED);
        }
    }

    private void saveStateToPrefs()
    {
        _data.saveToPrefs();
        saveToPrefs();
    }
    
    private void saveState()
    {
        saveStateToPrefs();
    }
    
    private void loadFromPrefs()
    {
        Persistence p = new Persistence(getClass());
        Preferences prefs = p.prefs();
        if (prefs.getLong(Messages.getString("MainFrame.version"), -1) == serialVersionUID) { //$NON-NLS-1$
            Dimension size = getSize();
            size.width = prefs.getInt(Messages.getString("MainFrame.width"), size.width); //$NON-NLS-1$
            size.height = prefs.getInt(Messages.getString("MainFrame.height"), size.height); //$NON-NLS-1$
            setSize(size);
            Point loc = getLocation();
            loc.x = prefs.getInt(Messages.getString("MainFrame.x"), loc.x); //$NON-NLS-1$
            loc.y = prefs.getInt(Messages.getString("MainFrame.y"), loc.y); //$NON-NLS-1$
            setLocation(loc);
        }
    }

    private void saveToPrefs()
    {
        Persistence p = new Persistence(getClass());
        Preferences prefs = p.prefs();
        prefs.putLong(Messages.getString("MainFrame.version"), serialVersionUID); //$NON-NLS-1$
        Dimension size = getSize();
        prefs.putInt(Messages.getString("MainFrame.width"), size.width); //$NON-NLS-1$
        prefs.putInt(Messages.getString("MainFrame.height"), size.height); //$NON-NLS-1$
        Point loc = getLocation();
        prefs.putInt(Messages.getString("MainFrame.x"), loc.x); //$NON-NLS-1$
        prefs.putInt(Messages.getString("MainFrame.y"), loc.y); //$NON-NLS-1$
    }

    private DataModel _data;
    private final List<CharSetCheckBox> _checkBoxes = new ArrayList<>();
    private final JTextArea _textArea = new JTextArea();
    private final JTextArea _messageArea = new JTextArea();
    private final JTextField _minLengthTextField = new JTextField();
    private final JTextField _maxLengthTextField = new JTextField();
    private final Color _defaultColor = _minLengthTextField.getBackground();

    private static final long serialVersionUID = 1L;
}
