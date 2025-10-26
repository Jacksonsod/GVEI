import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


class Login extends Frame
{
    Login()
    {
        final String userName;
        final String password ;
        Label username = new Label("Enter Username: ");
        Label pass = new Label("Enter Password: ");

        // Setting frame
        setSize(500,350);
        setTitle("Login Page");
        setBackground(Color.white);
        setResizable(true);
        setForeground(Color.DARK_GRAY);
        //setFont(new Font("serif",Font.PLAIN,15));
        username.setBounds(40,70,150,20);
        pass.setBounds(40,100,150,20);
        setLayout(null);


        TextField inputEmail = new TextField();
        TextField inputpass = new TextField();
        inputEmail.setBounds(190,70,200,25);
        inputpass.setBounds(190,100,200,25);

        //Add buttons
        Button login = new Button("Login");
        login.setBackground(Color.DARK_GRAY);
        login.setForeground(Color.white);
        login.setBounds(240,150,100,25);

        add(username);
        add(pass);
        add(inputEmail);
        add(inputpass);
        add(login);

        // Add link to register
        Label registerLink = new Label( "Don't have account Register");
        registerLink.setForeground(Color.BLUE);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //Set link bounds
        registerLink.setBounds(130,200,200,25);


        // Add link to frame
        add(registerLink);

        // Add event listener
        registerLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new RegisterUser();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                registerLink.setForeground(Color.red);
            }
        });

        login.addActionListener(ev ->{
            String userEmail = inputEmail.getText();
            String userpassword = inputpass.getText();

            String url = "jdbc:mysql://localhost/gvei";
            String user = "root";
            String passw = "";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection connection = DriverManager.getConnection(url,user,passw);
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE email=? AND password=?");
                ps.setString(1,userEmail);
                ps.setString(2,userpassword);
                ResultSet res = ps.executeQuery();
                if (res.next()) {
                    String role = res.getString("role");
                    String name = res.getString("name");
                    JOptionPane.showMessageDialog(null, "Login successful!\nWelcome " + name + " (" + role + ")");
                    dispose();
                    if (role.equalsIgnoreCase("Admin")) {
                        new AdminDashboard();
                    } else {
                        new CitizenDashboard();
                    }

                } else {
                    JOptionPane.showMessageDialog(null,
                            "Invalid email or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });


        setVisible(true);
        // Close window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

    }
}
