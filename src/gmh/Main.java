package gmh;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
				e.printStackTrace(System.out);
			}            
			JFrame win = new JFrame();
			
			GUI.init(win);
			
			win.pack();
			win.setVisible(true);
			win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}

