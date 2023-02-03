package org.hockey.hockeyware.loader;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;


public class LoaderGui
{

    private final JLabel usernameLabel;
    public final JTextField userText;
    private final JLabel passLabel;
    public final JPasswordField passText;
    public final JButton button;
    private final JLabel success;

    public static LoaderGui instance;

    public JFrame frame;


    public LoaderGui() {
        instance = this;


        try {
            FlatLaf.registerCustomDefaultsSource( "guiThemes" );
            UIManager.setLookAndFeel( new WindowsLookAndFeel() );

        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }

        frame = new JFrame( "HockeyWare Login" );
        JPanel panel = new JPanel();

        frame.setSize( 350, 200 );
        frame.setLocationRelativeTo( null );
        frame.add( panel );

        panel.setLayout( null );

        boolean cached = Cache.cached();
        Pair< String, String > data = null;
        if ( cached )
            data = Cache.read();

        usernameLabel = new JLabel( "Username" );
        usernameLabel.setBounds( 10, 20, 80, 25 );
        panel.add( usernameLabel );

        userText = new JTextField( 20 );
        userText.setBounds( 100, 20, 165, 25 );
        if ( cached )
        {
            assert data != null;
            userText.setText( data.getKey() );
        }
        panel.add( userText );

        passLabel = new JLabel( "Password" );
        passLabel.setBounds( 10, 50, 80, 25 );
        panel.add( passLabel );

        passText = new JPasswordField();
        passText.setBounds( 100, 50, 165, 25 );
        panel.add( passText );

        button = new JButton( "Login" );
        button.setBounds( 100, 80, 80, 25 );
        button.addActionListener( ( event ) ->
        {
            String user = userText.getText();
            String password = String.valueOf( passText.getPassword() );

            if ( !user.isEmpty() && !password.isEmpty() )
            {
                button.setEnabled( false );
                Loader.onLogin( user, password );
            }
        } );
        panel.add( button );

        frame.setIconImage(new ImageIcon(ClassLoader.getSystemResource("guiImages/icon.png")).getImage());

        success = new JLabel( "" );
        success.setBounds( 10, 110, 300, 25 );
        panel.add( success );

        if ( cached )
        {
            LogManager.getLogger( "HockeyWare" ).info( "Found Saved Auth Files For The Username {}, Attempting To Login", data.getKey() );
            Loader.onLogin( data.getKey(), data.getValue() );
        } else frame.setVisible( true );
    }

}