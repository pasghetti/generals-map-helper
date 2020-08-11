package gmh;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatter;

import com.google.gson.Gson;

public class GUI {
	private GUI() { }
	private static Grid.MouseMode prevMouseMode;
	public static void init(JFrame parent) {
		JPanel con = new JPanel();
		con.setLayout(new BoxLayout(con, BoxLayout.X_AXIS));
		
		Grid display = new Grid(10, 10);
		
		prevMouseMode = Grid.MouseMode.PAINT;
		String curAction = "start_drag";
		con.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift SHIFT"), curAction);
		con.getActionMap().put(curAction, new AbstractAction() {
			private static final long serialVersionUID = -4792035707069403452L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(display.getMouseMode() != Grid.MouseMode.DRAG) {
					prevMouseMode = display.getMouseMode();
				}
				display.setMouseMode(Grid.MouseMode.DRAG);
			}
		});

		curAction = "stop_drag";
		con.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"), curAction);
		con.getActionMap().put(curAction, new AbstractAction() {
			private static final long serialVersionUID = 6225903856972125379L;
			@Override
			public void actionPerformed(ActionEvent e) {
				display.setMouseMode(prevMouseMode);
			}
		});
		
		Box opt = Box.createVerticalBox();
		
		Box terrainBox = Box.createHorizontalBox();
		String[] terrainChoices = { "Empty", "Mountain", "City", "Neutral Army", "Swamp", "General" };
		JLabel terrainLabel = new JLabel("Terrain Type: ");
		JComboBox<String> terrainField = new JComboBox<>(terrainChoices);
		terrainField.setMaximumSize(terrainField.getPreferredSize());
		terrainField.addActionListener((e) -> {
			display.setTerrain = terrainField.getSelectedIndex();
		});
		terrainBox.add(terrainLabel);
		terrainBox.add(terrainField);
		
		Box armyBox = Box.createHorizontalBox();
		JLabel armyLabel = new JLabel("Army Strength: ");
		JSpinner armyCount = new JSpinner(new SpinnerNumberModel(0, -9999, 9999, 1));
		armyCount.setMaximumSize(armyCount.getPreferredSize());
		((DefaultFormatter) ((JSpinner.DefaultEditor) armyCount.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);
		armyCount.addChangeListener((e) -> {
			display.setArmy = (Integer) armyCount.getValue();
		});
		armyBox.add(armyLabel);
		armyBox.add(armyCount);
		
		JButton clear = new JButton("Clear");
		clear.addActionListener((e) -> {
			display.clear();
		});
		
		Box mouseBox = Box.createHorizontalBox();
		JLabel mouseLabel = new JLabel("Mouse action: ");
		String[] mouseChoices = { "Paint", "Rectangle Fill", "Select" };
		JComboBox<String> mouseField = new JComboBox<>(mouseChoices);
		mouseField.setMaximumSize(mouseField.getPreferredSize());
		mouseField.addActionListener((e) -> {
			if(mouseField.getSelectedIndex() == 0) {
				display.setMouseMode(Grid.MouseMode.PAINT);
			} else if(mouseField.getSelectedIndex() == 1) {
				display.setMouseMode(Grid.MouseMode.RECT_FILL);
			} else if(mouseField.getSelectedIndex() == 2){
				display.setMouseMode(Grid.MouseMode.SELECT); 
			}
		});
		mouseBox.add(mouseLabel);
		mouseBox.add(mouseField);
		
		Box widthBox = Box.createHorizontalBox();
		JLabel widthLabel = new JLabel("Width: ");
		JSpinner widthField = new JSpinner(new SpinnerNumberModel(10, 0, 50, 1));
		widthField.setMaximumSize(widthField.getPreferredSize());
		((DefaultFormatter) ((JSpinner.DefaultEditor) widthField.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);
		widthField.addChangeListener((e) -> {
			display.setCols((Integer) widthField.getValue());
		});
		widthBox.add(widthLabel);
		widthBox.add(widthField);
		
		Box heightBox = Box.createHorizontalBox();
		JLabel heightLabel = new JLabel("Height: ");
		JSpinner heightField = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
		heightField.setMaximumSize(heightField.getPreferredSize());
		((DefaultFormatter) ((JSpinner.DefaultEditor) heightField.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);
		heightField.addChangeListener((e) -> {
			display.setRows((Integer) heightField.getValue());
		});
		heightBox.add(heightLabel);
		heightBox.add(heightField);
		
		// TODO: these three functions, then done
		JFileChooser mapFileDialog = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files (.txt)", "txt");
		mapFileDialog.setFileFilter(filter);
		JButton save = new JButton("Save");
		save.addActionListener((e) -> {
			if(mapFileDialog.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				try(FileWriter saveWriter = new FileWriter(mapFileDialog.getSelectedFile(), false)) {
					saveWriter.write(display.getRows() + "\n");
					saveWriter.write(display.getCols() + "\n");
					saveWriter.write(display.getMapRepresentation());
					JOptionPane.showMessageDialog(parent, "Successfully saved map.", "Yay", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent, "Error in trying to save file: " + e1.getMessage(), "Error in saving", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JButton load = new JButton("Load");
		load.addActionListener((e) -> {
			if(mapFileDialog.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				try(Scanner saveReader = new Scanner(mapFileDialog.getSelectedFile())) {
					int newRowCount = Integer.parseInt(saveReader.nextLine());
					int newColCount = Integer.parseInt(saveReader.nextLine());
					display.setRows(newRowCount);
					heightField.setValue(newRowCount);
					display.setCols(newColCount);
					widthField.setValue(newColCount);
					display.parseMapRepresentation(saveReader.nextLine());
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(parent, "Save file not found: " + e1.getMessage(), "Error in loading", JOptionPane.ERROR_MESSAGE);
				} catch(IllegalArgumentException e2) {
					JOptionPane.showMessageDialog(parent, "Save file format is unreadable: " + e2.getMessage(), "Error in loading", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JButton publish = new JButton("Publish");
		publish.addActionListener((e) -> {
			// TODO: only this function
			JPanel publishInfo = new JPanel();
			publishInfo.setLayout(new BoxLayout(publishInfo, BoxLayout.Y_AXIS));
			Box titleBox = Box.createHorizontalBox();
			JLabel titleLabel = new JLabel("Title: ");
			JTextField titleField = new JTextField(10);
			titleBox.add(titleLabel);
			titleBox.add(titleField);
			Box descBox = Box.createHorizontalBox();
			JLabel descLabel = new JLabel("Description: ");
			JTextArea descField = new JTextArea(3, 10);
			descBox.add(descLabel);
			descBox.add(descField);
			JLabel idLabel1 = new JLabel("NOTE: You must set an environment variable named");
			JLabel idLabel2 = new JLabel("GeneralsID to your user ID (NOT your username)."); 
			JLabel idLabel3 = new JLabel("Details on how you can find your user ID are");
			JLabel idLabel4	= new JLabel("in the file README.md");
			idLabel1.setFont(new Font("Arial", Font.BOLD, 25));
			idLabel2.setFont(new Font("Arial", Font.BOLD, 25));
			idLabel3.setFont(new Font("Arial", Font.BOLD, 25));
			idLabel4.setFont(new Font("Arial", Font.BOLD, 25));
			publishInfo.add(titleBox);
			publishInfo.add(smallArea());
			publishInfo.add(descBox);
			publishInfo.add(smallArea());
			publishInfo.add(idLabel1);
			idLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
			publishInfo.add(idLabel2);
			idLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
			publishInfo.add(idLabel3);
			idLabel3.setAlignmentX(Component.CENTER_ALIGNMENT);
			publishInfo.add(idLabel4);
			idLabel4.setAlignmentX(Component.CENTER_ALIGNMENT);
			if(JOptionPane.showConfirmDialog(parent, publishInfo, "Name/Describe Map", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
				String title = titleField.getText();
				String desc = descField.getText();
				try {
					URL serverUrl = new URL("http://generals.io/api/createCustomMap");
					HttpURLConnection connect = (HttpURLConnection) serverUrl.openConnection();
					connect.setRequestMethod("POST");
					connect.setDoOutput(true);
					connect.setRequestProperty("Pragma", "no-cache");
					connect.setRequestProperty("Cache-Control", "no-cache");
					connect.setRequestProperty("User-Agent", "");
					connect.setRequestProperty("Accept", "*/*");
					connect.setRequestProperty("Origin", "http://generals.io");
					connect.setRequestProperty("Referer", "http://generals.io/mapcreator");
					connect.setRequestProperty("Accept-Encoding", "gzip, deflate");
					connect.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
					connect.setRequestProperty("Content-Type", "application/json");
					Gson gson = new Gson();
					System.out.println(System.getenv().get("GeneralsID"));
					String mapJson = gson.toJson(new MapData(title, desc, display.getCols(), display.getRows(), display.getMapRepresentation(), System.getenv().get("GeneralsID")));
					System.out.println(mapJson);
					try(OutputStream os = connect.getOutputStream()) {
						os.write(mapJson.getBytes());
					}
					if(connect.getResponseCode() == HttpURLConnection.HTTP_OK) {
						JOptionPane.showMessageDialog(parent, "Map was published. Link is here: generals.io/maps/" + title, "Successfully published", JOptionPane.INFORMATION_MESSAGE);
					} else if (connect.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
						String responseMessage = "";
						try(Scanner response = new Scanner(connect.getErrorStream())) {
							while(response.hasNext()) {
								responseMessage += response.next() + " ";
							}
						}
						JOptionPane.showMessageDialog(parent, "Could not publish the map: " + responseMessage, "Error in publishing", JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(parent, "Could not publish the map.", "Error in publishing", JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent, "Failed to connect to server: " + e1.getMessage(), "Connection error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
		
		opt.add(smallArea());
		opt.add(terrainBox);
		terrainBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		opt.add(smallArea());
		opt.add(armyBox);
		armyBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		opt.add(smallArea());
		opt.add(clear);
		clear.setAlignmentX(Component.CENTER_ALIGNMENT);
		opt.add(smallArea());
		opt.add(mouseBox);
		opt.add(smallArea());
		opt.add(widthBox);
		opt.add(smallArea());
		opt.add(heightBox);
		opt.add(smallArea());
		opt.add(save);
		save.setAlignmentX(Component.CENTER_ALIGNMENT);
		opt.add(smallArea());
		opt.add(load);
		load.setAlignmentX(Component.CENTER_ALIGNMENT);
		opt.add(smallArea());
		opt.add(publish);
		publish.setAlignmentX(Component.CENTER_ALIGNMENT);
		opt.add(smallArea());
		
		Box displayBox = Box.createHorizontalBox();
		displayBox.add(display);
		con.add(displayBox);
		con.add(smallArea());
		con.add(opt);
		displayBox.setBorder(BorderFactory.createLineBorder(Color.black));
		opt.setBorder(BorderFactory.createLineBorder(Color.black));
		con.add(smallArea());
		parent.setContentPane(con);
		
		Timer paintTimer = new Timer(17, (e) -> {
			display.repaint();
			
		});
		paintTimer.start();
	}

	public static Component smallArea() {
		return Box.createRigidArea(new Dimension(10, 10));
	}
}
