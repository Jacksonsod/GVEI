import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

class RegisterUser extends Frame
{
    RegisterUser()
    {
        String name = "";
        String email = "";
        String userName ="";
        String password = "";
        String role = "";

        //set Labels
        Label n = new Label("Names: ");
        Label e = new Label("Email: ");
        Label u = new Label("Email: ");
        Label p = new Label("Password: ");
        Label r = new Label("Role: ");

         //Setting labels bounds
        n.setBounds(40,70,150,25);
        e.setBounds(40,100,150,25);
        u.setBounds(40,130,150,25);
        p.setBounds(40,160,150,25);
        r.setBounds(40,190,150,25);
        setLayout(null);
        // Add labels to a frame
        add(n);
        add(e);
        add(u);
        add(p);
        add(r);

        // Input fields
        TextField inputName = new TextField();
        TextField inputEmail = new TextField();
        TextField inputUser = new TextField();
        TextField inputPassword = new TextField();
        Choice inputRole = new Choice();
        inputRole.add("Admin");
        inputRole.add("User");
        // Set bounds to input fields
        inputName.setBounds(190,70,200,25);
        inputEmail.setBounds(190,100,200,25);
        inputUser.setBounds(190,130,200,25);
        inputPassword.setBounds(190,160,200,25);
        inputRole.setBounds(190,190,200,25);

        // Add input fields to frame
        add(inputName);
        add(inputEmail);
        add(inputUser);
        add(inputPassword);
        add(inputRole);

        // Add register button
        Button reg = new Button();
        reg.setLabel("Register");
        reg.setBackground(Color.DARK_GRAY);
        reg.setForeground(Color.white);
        reg.setFont(new Font("serif", Font.BOLD,20));
        reg.setBounds(240,230,100,30);

        reg.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // Add button to frame
        add(reg);

        // Add link to Log in
        Label loginLink = new Label("Have account? Login");
        loginLink.setForeground(Color.blue);

        //Set link bounds
        loginLink.setBounds(150,280,200,25);

        // Add link to frame
        add(loginLink);

        // Add event listener
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new Login();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                loginLink.setForeground(Color.green);
            }
        });

        reg.addActionListener(ev ->{
            String names = inputName.getText();
            String userEmail = inputEmail.getText();
            String userpassword = inputPassword.getText();
            String userrole = inputRole.getSelectedItem();

            String url = "jdbc:mysql://localhost/gvei";
            String user = "root";
            String pass = "";
           try {
              Class.forName("com.mysql.cj.jdbc.Driver");
              Connection connection = DriverManager.getConnection(url,user,pass);
               PreparedStatement ps = connection.prepareStatement("INSERT INTO users(name,email,password,role) VALUES (?,?,?,?)"
               );
               ps.setString(1,names);
               ps.setString(2,userEmail);
               ps.setString(3,userpassword);
               ps.setString(4,userrole);
               ps.executeUpdate();
               JOptionPane.showMessageDialog(null,"Inserted successfully ✅");
               new Login();


           } catch (Exception ex) {
               ex.printStackTrace();
               JOptionPane.showMessageDialog(null, "❌ Error: " + ex.getMessage());
           }
        });



        setSize(500,350);
        setTitle("Register User");
        setVisible(true);
        setResizable(false);
        // Add close listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });



    }
}
