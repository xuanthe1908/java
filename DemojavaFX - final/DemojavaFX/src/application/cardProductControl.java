package application;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class cardProductControl implements Initializable {

	@FXML
	private TextField prod_amounts;

	@FXML
	private Label prod_price;

	@FXML
	private Button prod_addBtn;

	@FXML
	private AnchorPane card_form;

	@FXML
	private Label prod_name;

	@FXML
	private ImageView prod_imageView;

	//private productData prodData;

	private Image image;

	private String prodID;

	private String type;
	private String prod_date;
	private String prod_image;

	private Connection connect;
	private PreparedStatement prepare;
	private ResultSet result;
	private Alert alert;

	public void setData(productData prodData) {
		//this.prodData = prodData;

		prod_image = prodData.getImage();

		prod_date = String.valueOf(prodData.getDate());

		type = prodData.getType();

		prodID = prodData.getProductId();

		prod_name.setText(prodData.getProductName());
		prod_price.setText(String.valueOf(prodData.getPrice()) + " đ");
		String path = "File:" + prodData.getImage();
		image = new Image(path, 100, 100, false, true);
		prod_imageView.setImage(image);
		pr = prodData.getPrice();
	}

	private int qty;

//	public void setQuantity() {
//
//	}
	private float totalP;
	private float pr;

	public void addBtn() {

		mainFormFoodControl mForm = new mainFormFoodControl();
		mForm.customerID();

		String inputText = prod_amounts.getText();

		try {
			qty = (inputText != null && !inputText.isEmpty()) ? Integer.parseInt(inputText) : 0;
		} catch (NumberFormatException e) {
			// Show an error message or handle the invalid input as needed
			alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Message");
			alert.setHeaderText(null);
			alert.setContentText("Please enter a valid quantity (numeric value).");
			alert.showAndWait();
			return; // Exit the method if the input is invalid
		}

		qty = Integer.parseInt(inputText);

		String check = "";

		String checkAvailable = "SELECT status FROM product WHERE pro_id= '" + prodID + "'";

		connect = database.connectDB();

		try {

			int checkStck = 0;
			String checkStock = "SELECT stock FROM product WHERE pro_id = '" + prodID + "'";

			prepare = connect.prepareStatement(checkStock);
			result = prepare.executeQuery();

			if (result.next()) {
				checkStck = result.getInt("stock");
			}

			if (checkStck == 0) {

				String updateStock = "UPDATE product SET pro_name = '" + prod_name.getText() + "', type = '" + type
						+ "', stock = 0, price = '" + pr + "', status = 'Unavailable'" + "', image ='" + prod_image
						+ "', date = '" + prod_date + "' WHERE pro_id = '" + prodID + "'";
				prepare = connect.prepareStatement(updateStock);
				prepare.executeUpdate();
			}

			prepare = connect.prepareStatement(checkAvailable);
			result = prepare.executeQuery();

			if (result.next()) {
				check = result.getString("status");
			}

			if (!check.equals("Available") || qty == 0) {
				alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Message");
				alert.setHeaderText(null);
				alert.setContentText("Something Wrong!");
				alert.showAndWait();

			} else {
				
				prod_image = prod_image.replace("\\", "\\\\");

				if (checkStck < qty) {
					alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Message");
					alert.setHeaderText(null);
					alert.setContentText("Invalid. This product is Out of Stock");
					alert.showAndWait();
				} else {
					String insertData = "INSERT INTO customer "
							+ "(customer_id, prod_id, prod_name, type, quantity, price, date, image, em_username)"
							+ "VALUES(?,?,?,?,?,?,?,?,?)";

					prepare = connect.prepareStatement(insertData);
					prepare.setString(1, String.valueOf(data.cID));
					prepare.setString(2, prodID);
					prepare.setString(3, prod_name.getText());
					prepare.setString(4, type);
					prepare.setString(5, String.valueOf(qty));

					totalP = (qty * pr);
					prepare.setString(6, String.valueOf(totalP));

					Date date = new Date(System.currentTimeMillis());
					java.sql.Date sqlDate = new java.sql.Date(date.getTime());
					prepare.setString(7, String.valueOf(sqlDate));
					
					prepare.setString(8, prod_image);

					prepare.setString(9, data.username);

					prepare.executeUpdate();

					int upStock = checkStck - qty;

					

					String updateStock = "UPDATE product SET pro_name = '" + prod_name.getText() + "', type = '" + type
							+ "', stock = '" + upStock + "', price = '" + pr + "', status = '" + check + "', image ='"
							+ prod_image + "', date = '" + prod_date + "' WHERE pro_id = '" + prodID + "'";

					prepare = connect.prepareStatement(updateStock);
					prepare.executeUpdate();

					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Message");
					alert.setHeaderText(null);
					alert.setContentText("Successfully Added!");
					alert.showAndWait();
					
					mForm.menuGetTotal();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// setQuantity();
	}

}
