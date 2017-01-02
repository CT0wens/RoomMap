package de.tunetown.roommap.view.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import rainbowvis.Rainbow;
import de.tunetown.roommap.main.Main;
import de.tunetown.roommap.model.Measurement;;

/**
 * SPL distribution visualization panel
 * 
 * @author tweber
 *
 */
public class OutputGraphics extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Main main;
	
	// TODO constants
	private double resolution = 0.1;  // Resolution (model units, not pixels!)
	private int maxSize = 800;        // Initial max. Size of data panel (pixels)

	public OutputGraphics(Main main) {
		this.main = main;
		
		Dimension dim = getPaintDimension(maxSize, maxSize);
		this.setPreferredSize(dim);
		this.setMinimumSize(dim);
	}

	/**
	 * Returns the size for the drawing stage
	 *  
	 * @return
	 */
	private Dimension getPaintDimension() {
		return getPaintDimension(getWidth(), getHeight());
	}
	
	/**
	 * As getPaintDimension(), but has inputs to set the view width/height desired
	 * 
	 * @param viewWidth
	 * @param viewHeight
	 * @return
	 */
	private Dimension getPaintDimension(int viewWidth, int viewHeight) {
		double modelSizeX = main.getMeasurements().getMaxX() - main.getMeasurements().getMinX() + 2*main.getMargin();
		double modelSizeY = main.getMeasurements().getMaxY() - main.getMeasurements().getMinY() + 2*main.getMargin();
		double modelRatio = modelSizeX / modelSizeY;
		double panelRatio = (double)viewWidth / (double)viewHeight;

		int w;
		int h;
		if (modelRatio > panelRatio) {
			w = viewWidth;
			h = (int)(w / modelRatio);
		} else {
			h = viewHeight;
			w = (int)(h * modelRatio);
		}
		return new Dimension(w, h);
	}

	/**
	 * Paint method
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.WHITE);//TODO
		g.fillRect(0, 0, getWidth(), getHeight());
		
		paintData(g);
		paintPoints(g);
	}

	/**
	 * Paint data visualization
	 * 
	 * @param g
	 */
	private void paintData(Graphics g) {
		double modelZ = main.getViewZ();
		
		Dimension d = getPaintDimension();
		int resX = convertModelToViewX(resolution);
		int resY = convertModelToViewX(resolution);
		
		for(int x=0; x<d.getWidth()+resX/2; x+=resX) {
			for(int y=0; y<d.getHeight()+resY/2; y+=resY) {
				double rx = convertViewToModelX(x) - main.getMargin() + main.getMeasurements().getMinX(); 
				double ry = convertViewToModelY(y) - main.getMargin() + main.getMeasurements().getMinY();
				
				double spl = main.getMeasurements().getSpl(rx, ry, modelZ, main.getFrequency());
				
				g.setColor(getOutColor(spl));
				g.fillRect(x - resX/2, y - resY/2, resX, resY);
			}
		}
	}

	/**
	 * Paint points visualization
	 * 
	 * @param g
	 */
	private void paintPoints(Graphics g) {
		int diaX = convertModelToViewX(resolution);
		int diaY = convertModelToViewY(resolution);
		
		int minAplha = 10;
		int maxAlpha = 255;
		
		double minZ = main.getMeasurements().getMinZ();
		double maxZ = main.getMeasurements().getMaxZ();
		
		for(Measurement m : main.getMeasurements().getMeasurements()) {
			double z = main.getViewZ() - m.getZ(); 
			int x = getProjectionX(convertModelToViewX(m.getX() + main.getMargin() - main.getMeasurements().getMinX()), z);
			int y = getProjectionY(convertModelToViewY(m.getY() + main.getMargin() - main.getMeasurements().getMinY()), z);
			
			g.setColor(new Color(0, 0, 0, getAlpha(z)));
			g.fillOval(x - diaX/2, y - diaY/2, diaX, diaY);
		}
	}

	private int getProjectionX(int x, double z) {
		return x;
	}
	
	private int getProjectionY(int y, double z) {
		return y;
	}

	private int getAlpha(double z) {
		return 100;
	}

	/**
	 * Get visualization color for a given SPL level
	 * 
	 * @param spl
	 * @return
	 */
	public Color getOutColor(double spl) {
		if (spl == Double.NaN) return Color.BLACK;//TODO
		
		double minSpl;
		double maxSpl;
		
		if (main.getNormalizeByFrequency()){ 
			minSpl = main.getMeasurements().getMinSpl(main.getFrequency());
			maxSpl = main.getMeasurements().getMaxSpl(main.getFrequency());
		} else {
			minSpl = main.getMeasurements().getMinSpl();
			maxSpl = main.getMeasurements().getMaxSpl();
		}

		double val = (spl - minSpl) / (maxSpl - minSpl);

		Rainbow rainbow = new Rainbow();
		return rainbow.colourAt(100 - (int)(val * 100));
	}
	
	/**
	 * Coordinate conversion
	 * 
	 * @param x
	 * @return
	 */
	private double convertViewToModelX(int x) {
		return ((double)x / getPaintDimension().getWidth()) * (main.getMeasurements().getMaxX() - main.getMeasurements().getMinX() + 2*main.getMargin());
	}

	/**
	 * Coordinate conversion
	 * 
	 * @param y
	 * @return
	 */
	private double convertViewToModelY(int y) {
		return ((double)y / getPaintDimension().getHeight()) * (main.getMeasurements().getMaxY() - main.getMeasurements().getMinY() + 2*main.getMargin());
	}

	/**
	 * Coordinate conversion
	 * 
	 * @param x
	 * @return
	 */
	private int convertModelToViewX(double x) {
		return (int)((x / (main.getMeasurements().getMaxX() - main.getMeasurements().getMinX() + 2*main.getMargin())) * getPaintDimension().getWidth());
	}
	
	/**
	 * Coordinate conversion
	 * 
	 * @param y
	 * @return
	 */
	private int convertModelToViewY(double y) {
		return (int)((y / (main.getMeasurements().getMaxY() - main.getMeasurements().getMinY() + 2*main.getMargin())) * getPaintDimension().getHeight());
	}
}
