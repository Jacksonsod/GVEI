import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Year;

class RegisterVehicle extends Frame
{
    RegisterVehicle()
    {
      String  vehicle_id= "", owner_id ="", plate_no ="", fuel_type="", year ="", mileage = "";
      // Add labels
      Label ownerId = new Label("Owner Id: ");
      Label plateNo = new Label("PlateNo");
      Label fuelType = new Label("Fuel type: ");
      Label year1 = new Label("Year: ");
      Label mileage1 = new Label("Mileage: ");

      // Set bounds
      ownerId.setBounds(40,70,150,25);
      plateNo.setBounds(40,100,150,25);
      fuelType.setBounds(40,130,150,25);
      year1.setBounds(40,160,150,25);
      mileage1.setBounds(40,190,150,25);

      setLayout(null);

      // Add Labels to frame
      add(ownerId);
      add(plateNo);
      add(fuelType);
      add(year1);
      add(mileage1);

      // add input fields
      TextField inputOwnerId = new TextField();
      TextField inputPlateNo = new TextField();
      Choice inputFuelType = new Choice();
      inputFuelType.add("Diesel");
      inputFuelType.add("Petrol");
      TextField inputYear = new TextField();
      TextField inputMileage = new TextField();

      // Set fields bounds
      inputOwnerId.setBounds(190,70,200,25);
      inputPlateNo.setBounds(190,100,200,25);
      inputFuelType.setBounds(190,130,200,25);
      inputYear.setBounds(190,160,200,25);
      inputMileage.setBounds(190,190,200,25);


      //Add fields to frame
      add(inputOwnerId);
      add(inputPlateNo);
      add(inputFuelType);
      add(inputYear);
      add(inputMileage);

        // Add register button
        Button reg = new Button();
        reg.setLabel("Register");
        reg.setBackground(Color.DARK_GRAY);
        reg.setForeground(Color.white);
        reg.setFont(new Font("serif", 18,20));
        reg.setBounds(240,210,100,30);

        // Add button to frame
        add(reg);
            reg.addActionListener(ev ->{
                int owner = Integer.parseInt(inputOwnerId.getText());
                String plate = inputPlateNo.getText();
                String fuel = inputFuelType.getSelectedItem();
                int year2 = Integer.parseInt(inputYear.getText());
                String mile = inputMileage.getText();
                String url = "jdbc:mysql://localhost/gvei";
                String user = "root";
                String pass = "";
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection connection = DriverManager.getConnection(url,user,pass);
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO vehicles(owner_id,plate_no,fuel_type,year,mileage) VALUES (?,?,?,?,?)"
                    );
                    ps.setInt(1,owner);
                    ps.setString(2,plate);
                    ps.setString(3,fuel);
                    ps.setInt(4,year2);
                    ps.setString(5,mile);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null,"Vehicle registered successfully");
                    dispose();
                    new RegisterVehicle();


                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "❌ Error: " + ex.getMessage());
                }
            });



        // Set frame
      setSize(500,350);
      setTitle("Register Vehicle");
      setVisible(true);
      addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
              dispose();
          }
      });


    }
}
