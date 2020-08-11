package gmh;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Grid extends JPanel {
	private static final long serialVersionUID = -2130886389459737695L;
	public static final int TILE_EMPTY = 0;
	public static final int TILE_MOUNTAIN = 1;
	public static final int TILE_CITY = 2;
	public static final int TILE_NEUTRAL = 3;
	public static final int TILE_SWAMP = 4;
	public static final int TILE_GENERAL = 5;
	public static String[] tileCodes = { " ", "m", "", "n", "s", "g" };
	
	private final int tileSize = 40;
	private final int borderWidth = 1;
	private final Font armyFont = new Font("Arial", Font.BOLD, 12);
	
	public static enum MouseMode {
		DRAG,
		PAINT,
		RECT_FILL,
		SELECT
	}
	private MouseMode curMode;
	private int prevMouseX;
	private int prevMouseY;
	private int prevMousePressCol;
	private int prevMousePressRow;
	private int curMouseCol;
	private int curMouseRow;
	private int offsetX;
	private int offsetY;
	private double zoom;
	private int rows;
	private int cols;
	private int terrain[][];
	private int army[][];
	private int savedTerrain[][];
	private int savedArmy[][];
	int setTerrain;
	int setArmy;
	public Grid(int w, int h) {
		super();
		curMode = MouseMode.PAINT;
		prevMouseX = prevMouseY = -1;
		curMouseCol = curMouseRow = -1;
		prevMousePressCol = prevMousePressRow = -1;
		offsetX = offsetY = 0;
		zoom = 1.0;
		cols = w;
		rows = h;
		terrain = new int[rows][];
		army = new int[rows][];
		for(int a = 0; a < rows; a++) {
			terrain[a] = new int[cols];
			army[a] = new int[cols];
		}
		savedTerrain = savedArmy = null;
		setTerrain = 0;
		setArmy = 0;
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int col = (int) Math.floor((x - offsetX) / ((borderWidth + tileSize) * zoom));
				int row = (int) Math.floor((y - offsetY) / ((borderWidth + tileSize) * zoom));
				if(inbounds(row, col)) {
					curMouseCol = col;
					curMouseRow = row;
				}
				if(curMode == MouseMode.DRAG) {
					if(!(0 <= x && x < getWidth() && 0 <= y && y < getHeight())) {
						prevMouseX = prevMouseY = -1;
						return;
					}
					if(prevMouseX == -1) {
						prevMouseX = x;
						prevMouseY = y;
						return;
					}
					int dx = x - prevMouseX;
					int dy = y - prevMouseY;
					offsetX += dx;
					offsetY += dy;
					prevMouseX = x;
					prevMouseY = y;
				} else if(curMode == MouseMode.PAINT) {
					if(inbounds(row, col)) {
						terrain[row][col] = setTerrain;
						if(setTerrain == TILE_NEUTRAL || setTerrain == TILE_CITY) {
							army[row][col] = setArmy;
						} else {
							army[row][col] = 0;
						}
					}
				}
			}
			@Override
			public void mouseMoved(MouseEvent e) { }
		});
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) { }
			
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow();
				int x = e.getX();
				int y = e.getY();
				int col = (int) Math.floor((x - offsetX) / ((borderWidth + tileSize) * zoom));
				int row = (int) Math.floor((y - offsetY) / ((borderWidth + tileSize) * zoom));
				if(inbounds(row, col)) {
					prevMousePressCol = col;
					prevMousePressRow = row;
					curMouseCol = col;
					curMouseRow = row;
				}
				if(curMode == MouseMode.PAINT) {
					if(inbounds(row, col)) {
						terrain[row][col] = setTerrain;
						if(setTerrain == TILE_NEUTRAL || setTerrain == TILE_CITY) {
							army[row][col] = setArmy;
						} else {
							army[row][col] = 0;
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				prevMouseX = prevMouseY = -1;
				if(prevMousePressCol != -1 && curMouseCol != -1) {
					if(curMode == MouseMode.RECT_FILL) {
						for(int a = Math.min(prevMousePressRow, curMouseRow); a <= Math.max(prevMousePressRow, curMouseRow); a++) {
							for(int b = Math.min(prevMousePressCol, curMouseCol); b <= Math.max(prevMousePressCol, curMouseCol); b++) {
								terrain[a][b] = setTerrain;
								if(setTerrain == TILE_NEUTRAL || setTerrain == TILE_CITY) {
									army[a][b] = setArmy;
								} else {
									army[a][b] = 0;
								}
							}
						}
						prevMousePressCol = prevMousePressRow = -1;
						curMouseCol = curMouseRow = -1;
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) { }

			@Override
			public void mouseExited(MouseEvent e) { }
			
		});
		this.addMouseWheelListener((e) -> {
			zoom *= Math.pow(2, -e.getPreciseWheelRotation() / 10.0);
        });
		
		String curAction = "copy";
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control C"), curAction);
		this.getActionMap().put(curAction, new AbstractAction() {
			private static final long serialVersionUID = -1164395815260395812L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(prevMousePressCol != -1 && curMouseCol != -1 && curMode == MouseMode.SELECT) {
					savedTerrain = new int[Math.abs(prevMousePressRow - curMouseRow) + 1][];
					savedArmy = new int[Math.abs(prevMousePressRow - curMouseRow) + 1][];
					for(int a = Math.min(prevMousePressRow, curMouseRow), c = 0; a <= Math.max(prevMousePressRow, curMouseRow); a++, c++) {
						savedTerrain[c] = new int[Math.abs(prevMousePressCol - curMouseCol) + 1];
						savedArmy[c] = new int[Math.abs(prevMousePressCol - curMouseCol) + 1];
						for(int b = Math.min(prevMousePressCol, curMouseCol), d = 0; b <= Math.max(prevMousePressCol, curMouseCol); b++, d++) {
							savedTerrain[c][d] = terrain[a][b];
							savedArmy[c][d] = army[a][b];
						}
					}
				}
			}
		});
		
		curAction = "cut";
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control X"), curAction);
		this.getActionMap().put(curAction, new AbstractAction() {
			private static final long serialVersionUID = 5831718701082320118L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(prevMousePressCol != -1 && curMouseCol != -1 && curMode == MouseMode.SELECT) {
					savedTerrain = new int[Math.abs(prevMousePressRow - curMouseRow) + 1][];
					savedArmy = new int[Math.abs(prevMousePressRow - curMouseRow) + 1][];
					for(int a = Math.min(prevMousePressRow, curMouseRow), c = 0; a <= Math.max(prevMousePressRow, curMouseRow); a++, c++) {
						savedTerrain[c] = new int[Math.abs(prevMousePressCol - curMouseCol) + 1];
						savedArmy[c] = new int[Math.abs(prevMousePressCol - curMouseCol) + 1];
						for(int b = Math.min(prevMousePressCol, curMouseCol), d = 0; b <= Math.max(prevMousePressCol, curMouseCol); b++, d++) {
							savedTerrain[c][d] = terrain[a][b];
							savedArmy[c][d] = army[a][b];
							terrain[a][b] = army[a][b] = 0;
						}
					}
				}
			}
		});
		
		curAction = "paste";
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control V"), curAction);
		this.getActionMap().put(curAction, new AbstractAction() {
			private static final long serialVersionUID = -6071156817968293887L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(savedTerrain == null || savedArmy == null) return;
				if(prevMousePressCol != -1 && curMode == MouseMode.SELECT) {
					for(int a = 0; a < savedTerrain.length; a++) {
						for(int b = 0; b < savedTerrain[a].length; b++) {
							if(inbounds(prevMousePressRow + a, prevMousePressCol + b)) {
								terrain[prevMousePressRow + a][prevMousePressCol + b] = savedTerrain[a][b];
								army[prevMousePressRow + a][prevMousePressCol + b] = savedArmy[a][b];
							}
						}
					}
					curMouseRow = Math.min(rows - 1, prevMousePressRow + savedTerrain.length - 1);
					curMouseCol = Math.min(cols - 1, prevMousePressCol + savedTerrain[0].length - 1);
				}
			}
		});
	}
	private boolean inbounds(int row, int col) {
		return 0 <= row && row < rows && 0 <= col && col < cols;
	}
	public void setCols(int w) {
		cols = w;
		for(int a = 0; a < rows; a++) {
			int[] nRowTerrain = new int[w];
			int[] nRowArmy = new int[w];
			for(int b = 0; b < Math.min(w, terrain[a].length); b++) {
				nRowTerrain[b] = terrain[a][b];
				nRowArmy[b] = army[a][b];
			}
			terrain[a] = nRowTerrain;
			army[a] = nRowArmy;
		}
	}
	public void setRows(int h) {
		rows = h;
		int[][] nTerrain = new int[h][];
		int[][] nArmy = new int[h][];
		for(int a = 0; a < h; a++) {
			if(a < terrain.length) {
				nTerrain[a] = terrain[a];
				nArmy[a] = army[a];
			} else {
				nTerrain[a] = new int[cols];
				nArmy[a] = new int[cols];
			}
		}
		terrain = nTerrain;
		army = nArmy;
	}
	public int getRows() {
		return rows;
	}
	public int getCols() {
		return cols;
	}
	public void clear() {
		for(int a = 0; a < rows; a++) {
			for(int b = 0; b < cols; b++) {
				army[a][b] = terrain[a][b] = 0;
			}
		}
	}
	public void setMouseMode(MouseMode nMode) {
		curMode = nMode;
		prevMousePressRow = prevMousePressCol = -1;
		curMouseRow = curMouseCol = -1;
	}
	public MouseMode getMouseMode() {
		return curMode;
	}
	public String getMapRepresentation() {
		String res = "";
		for(int a = 0; a < rows; a++) {
			for(int b = 0; b < cols; b++) {
				if(terrain[a][b] == TILE_NEUTRAL || terrain[a][b] == TILE_CITY) {
					res += tileCodes[terrain[a][b]];
					res += army[a][b];
				} else {
					res += tileCodes[terrain[a][b]];
				}
				if(a != rows - 1 || b != cols - 1) {
					res += ",";
				}
			}
		}
		return res;
	}
	public void parseMapRepresentation(String map) throws IllegalArgumentException {
		int curPos = 0;
		for(int a = 0; a < map.length(); a++, curPos++) {
			char c = map.charAt(a);
			int tileTerrain = -1;
			if('0' <= c && c <= '9') { // it is city
				tileTerrain = TILE_CITY;
			} else {
				for(int b = 0; b < tileCodes.length; b++) {
					if(b == TILE_CITY) continue;
					else if(tileCodes[b].charAt(0) == c) {
						tileTerrain = b;
						break;
					}
				}
				a++; // must consume the character before moving on
			}
			if(!inbounds(curPos / cols, curPos % cols) || tileTerrain == -1) {
				throw new IllegalArgumentException("Malformed save");
			}
			int tileArmy = 0;
			if(tileTerrain == TILE_CITY || tileTerrain == TILE_NEUTRAL) {
				int start = a;
				while(a < map.length() && map.charAt(a) != ',') {
					a++;
				}
				try {
					tileArmy = Integer.parseInt(map.substring(start, a));
				} catch(NumberFormatException nfe) {
					throw new IllegalArgumentException("Malformed save");
				}
			}
			terrain[curPos / cols][curPos % cols] = tileTerrain;
			army[curPos / cols][curPos % cols] = tileArmy;
		}
	}
	private void drawCenteredString(Graphics2D g2, String text, double x, double y, Font f) {
		FontMetrics fm = g2.getFontMetrics(f);
		int cornerX = (int) (x - fm.stringWidth(text) / 2);
		int cornerY = (int) (y + fm.getAscent() / 2);
		g2.setFont(f);
		g2.drawString(text, cornerX, cornerY);
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);
        for(int a = 0; a < rows; a++) {
        	for(int b = 0; b < cols; b++) {
        		int curTerrain = terrain[a][b];
        		int curArmy = army[a][b];
        		Rectangle2D.Double tileRect = new Rectangle2D.Double(b * (tileSize + borderWidth),
        				a * (tileSize + borderWidth),
        				tileSize, tileSize);
        		Ellipse2D.Double tileCircle = new Ellipse2D.Double(b * (tileSize + borderWidth),
        				a * (tileSize + borderWidth),
        				tileSize, tileSize);
        		switch(curTerrain) {
        		case TILE_EMPTY:
        			g2.setPaint(Color.WHITE);
        			g2.fill(tileRect);
        			break;
        		case TILE_MOUNTAIN:
        			g2.setPaint(Color.BLACK);
        			g2.fill(tileRect);
        			 break;
        		case TILE_CITY:
        			g2.setPaint(Color.WHITE);
        			g2.fill(tileRect);
        			g2.setPaint(Color.GRAY);
        			g2.fill(tileCircle);
        			break;
        		case TILE_NEUTRAL:
        			g2.setPaint(Color.GRAY);
        			g2.fill(tileRect);
        			break;
        		case TILE_SWAMP:
        			g2.setPaint(new Color(0x006400));
        			g2.fill(tileRect);
        			break;
        		case TILE_GENERAL:
        			g2.setPaint(Color.BLACK);
        			g2.fill(tileRect);
        			g2.setPaint(Color.RED);
        			g2.fill(tileCircle);
        			break;
        		}
        		if(curTerrain == TILE_NEUTRAL || curTerrain == TILE_CITY) {
        			g2.setPaint(Color.WHITE);
        			drawCenteredString(g2, "" + curArmy, tileRect.getCenterX(), tileRect.getCenterY(), armyFont);
        		}
        	}
        }
        if((curMode == MouseMode.RECT_FILL || curMode == MouseMode.SELECT) && curMouseRow != -1 && prevMousePressRow != -1) {
			Rectangle2D.Double highlightedRect = new Rectangle2D.Double(
					Math.min(curMouseCol, prevMousePressCol) * (tileSize + borderWidth),
					Math.min(curMouseRow, prevMousePressRow) * (tileSize + borderWidth),
					(Math.abs(curMouseCol - prevMousePressCol) + 1) * (tileSize + borderWidth) - borderWidth,
					(Math.abs(curMouseRow - prevMousePressRow) + 1) * (tileSize + borderWidth) - borderWidth);
			g2.setPaint(new Color(0.0f, 1.0f, 0.0f, 0.1f));
			g2.fill(highlightedRect);
		}
	}
}
