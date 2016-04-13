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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;
import core.DataModel;
import core.DataModel.ChangeListener;
import core.Generator;
import core.Generator.CharSetType;

public class MainFrame extends JFrame implements ChangeListener {

    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> {
            JFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    public MainFrame()
    {
        try
        {
            _data = DataModel.loadDataModel(dataPersistenceFile());
        }
        catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }

        if (_data == null)
        {
            _data = DataModel.create();
        }

        for (Item t : Item.values())
        {
            dataChanged(_data, t, null);
        }

        _data.addListener(this);
        _pwGen = new Generator();

        initGUI();

        loadUiData(uiPersistenceFile());

        final WindowAdapter l = new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e)
            {
                System.out.println("windowIconified()");
                System.out.println(e);
            }

            @Override
            public void windowDeiconified(WindowEvent e)
            {
                System.out.println("windowDeiconified()");
                System.out.println(e);
                System.out.println(e.getWindow().getSize());
            }

            @Override
            public void windowStateChanged(WindowEvent e)
            {
                System.out.println("windowStateChanged()");
                System.out.println(e);
                if (e.getNewState() == Frame.NORMAL) {
                    System.out.println(e.getWindow().getSize());
                }
            }

            @Override
            public void windowOpened(WindowEvent e)
            {
                System.out.println("windowOpened()");
                System.out.println(e);
                System.out.println(e.getWindow().getSize());
            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("windowClosing()");
                System.out.println(e);
                saveState();
            }

            @Override
            public void windowClosed(WindowEvent e)
            {
                System.out.println("windowClosed()");
                System.out.println(e);
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

    /**
     * NB: _data must be initialized before calling initGUI()
     */
    private void initGUI()
    {
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                MainFrame.class.getResource("/com/sun/javafx/scene/web/skin/Undo_16x16_JFX.png")));
        setTitle("Password Generator");
        final JFrame mainFrame = this;
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        JButton btnNew = new JButton("New");
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
            l.actionPerformed(new ActionEvent(cb, ActionEvent.ACTION_PERFORMED, ""));
            checkBoxPanel.add(cb);
        });

        updateControlSensitivities();

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

        JLabel lblMinimumLength = new JLabel("Minimum length:");
        lblMinimumLength.setLabelFor(_minLengthTextField);

        JLabel lblMaximumLength = new JLabel("Maximum length:");
        lblMaximumLength.setLabelFor(_maxLengthTextField);

        final KeyListener kl = new DecimalDigitsOnly();

        _maxLengthTextField.setColumns(10);
        _maxLengthTextField.addKeyListener(kl);
        _maxLengthTextField.addFocusListener(new SelectAllOnFocus());
        _maxLengthTextField.addFocusListener(
                new FocusLost(tf -> _data.setMaxLength(Integer.valueOf(tf.getText()), tf)));

        _minLengthTextField.setColumns(10);
        _minLengthTextField.addKeyListener(kl);
        _minLengthTextField.addFocusListener(new SelectAllOnFocus());
        _minLengthTextField.addFocusListener(
                new FocusLost(tf -> _data.setMinLength(Integer.valueOf(tf.getText()), tf)));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new MigLayout("", "[77px][86px][81px][86px]", "[33px][33px][33px]"));
        topPanel.add(lblMinimumLength, "cell 0 0,alignx left,aligny center");
        topPanel.add(_minLengthTextField, "cell 1 0,alignx center,aligny center");
        topPanel.add(lblMaximumLength, "cell 0 1,alignx left,aligny center");
        topPanel.add(_maxLengthTextField, "cell 1 1,alignx center,aligny center");

        topPanel.add(checkBoxPanel, "cell 0 2 4 1,alignx left,aligny center");
        innerPanel.add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(10, 32));
        buttonPanel.setMinimumSize(new Dimension(10, 32));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        innerPanel.add(buttonPanel, BorderLayout.SOUTH);

        btnNew.setMnemonic('n');
        btnNew.addActionListener(new NewActionListener(this));

        JButton btnCopy = new JButton("Copy");
        btnCopy.setMnemonic('c');
        btnCopy.addActionListener((e) -> {
            String password = _textArea.getSelectedText();
            StringSelection selec = new StringSelection(password);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selec, selec);
        });

        JButton btnQuit = new JButton("Quit");
        btnQuit.setMnemonic('q');
        btnQuit.addActionListener(new QuitActionListener(mainFrame));

        buttonPanel.add(btnNew);
        buttonPanel.add(btnCopy);
        buttonPanel.add(btnQuit);
        JPanel textPanel = new JPanel();
        textPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        innerPanel.add(textPanel, BorderLayout.CENTER);
        textPanel.setLayout(new BorderLayout(8, 8));
        textPanel.add(_textArea, BorderLayout.CENTER);

        _textArea.setLineWrap(true);
        _textArea.setEditable(false);
        _textArea.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
        _textArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 14));
        _textArea.setPreferredSize(new Dimension(320, 120));

        _messageArea.setBackground(SystemColor.control);
        _messageArea.setBorder(null);
        textPanel.add(_messageArea, BorderLayout.SOUTH);

        pack();
    }

    private void updateControlSensitivities()
    {
        // (_data.getCharSet().length > 0);
    }

    private String getPassword()
    {
        final String characterSet = getCharacterSet();
        int minLength = valueOf(_minLengthTextField);
        int maxLength = valueOf(_maxLengthTextField);
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

    private void setText(String text)
    {
        _textArea.setText(text);
        _textArea.setCaretPosition(0);
        _textArea.moveCaretPosition(text.length());
        _textArea.getCaret().setSelectionVisible(true);
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

    private void saveState()
    {
        try
        {
            _data.saveDataModel(dataPersistenceFile());
            saveUiData(uiPersistenceFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void saveUiData(String uiPersistenceFile)
    {
        int frameState = getExtendedState();
        if ((frameState | Frame.MAXIMIZED_BOTH) != 0)
        {
            this.setExtendedState(Frame.NORMAL);
        }
        try (OutputStream out = new FileOutputStream(uiPersistenceFile);
                ObjectOutputStream oos = new ObjectOutputStream(out))
        {
            oos.writeLong(serialVersionUID);
            oos.writeObject(getSize());
            oos.writeObject(getLocation());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void loadUiData(String uiPersistenceFile)
    {
        if (Files.exists(Paths.get(uiPersistenceFile)))
        {
            try (InputStream is = new FileInputStream(uiPersistenceFile);
                    ObjectInputStream ois = new ObjectInputStream(is))
            {
                if (ois.readLong() == serialVersionUID)
                {
                    Dimension size = (Dimension) ois.readObject();
                    setSize(size);
                    Point position = (Point) ois.readObject();
                    setLocation(position);
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static String dataPersistenceFile()
    {
        return persistenceFile(DataModel.class.getCanonicalName());
    }

    private static String uiPersistenceFile()
    {
        return persistenceFile(MainFrame.class.getCanonicalName());
    }

    private static String persistenceFile(String className)
    {
        if (!_dataPersistenceFile.containsKey(className))
        {
            final String tmpdir = System.getProperty("java.io.tmpdir");
            String fileName = className;

            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(fileName.getBytes("UTF-8"));
                byte[] digest = md.digest();
                fileName = String.format("%032x", new java.math.BigInteger(1, digest));
            }
            catch (NoSuchAlgorithmException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Path p = Paths.get(tmpdir, "pwgen-java", fileName + ".data");
            try
            {
                Files.createDirectories(p.getParent());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            _dataPersistenceFile.put(className, p.toString());
        }

        return _dataPersistenceFile.get(className);
    }

    private DataModel _data;
    private Generator _pwGen;
    private final List<CharSetCheckBox> _checkBoxes = new ArrayList<>();
    private final JTextArea _textArea = new JTextArea();
    private final JTextArea _messageArea = new JTextArea();
    private final JTextField _minLengthTextField = new JTextField();
    private final JTextField _maxLengthTextField = new JTextField();
    private final Color _defaultColor = _minLengthTextField.getBackground();

    private static Map<String, String> _dataPersistenceFile = new HashMap<>();

    private static final long serialVersionUID = 1L;

    private static final class DecimalDigitsOnly extends KeyAdapter {
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

    private static final class SelectAllOnFocus extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e)
        {
            if (e.getID() == FocusEvent.FOCUS_GAINED)
            {
                Component component = e.getComponent();
                if (component instanceof JTextField)
                {
                    ((JTextField) component).selectAll();
                }
            }
        }
    }

    /**
     * Perform an action when a JTextField loses focus. *
     */
    private static final class FocusLost extends FocusAdapter {
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

    /**
     * Update data model according to checkbox selections. *
     */
    private static final class CheckboxActionListener implements ActionListener {
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

    private static final class QuitActionListener implements ActionListener {
        private final JFrame _mf;

        private QuitActionListener(JFrame mainFrame)
        {
            _mf = mainFrame;
        }

        public void actionPerformed(ActionEvent e)
        {
            _mf.dispatchEvent(new WindowEvent(_mf, WindowEvent.WINDOW_CLOSING));
//            _mf.setVisible(false);
//            _mf.dispose();
        }
    }

    private static final class NewActionListener implements ActionListener {
        private MainFrame _mf;

        NewActionListener(MainFrame mf)
        {
            _mf = mf;
        }

        public void actionPerformed(ActionEvent e)
        {
            _mf.setText(_mf.getPassword());
        }
    }
}
